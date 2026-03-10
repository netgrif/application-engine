package com.netgrif.application.engine.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.application.engine.adapter.spring.configuration.AbstractMongoCollectionConfigurator;
import com.netgrif.application.engine.auth.provider.AbstractAuthConfig;
import com.netgrif.application.engine.auth.provider.AuthMethodProvider;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.provider.ProviderRegistry;
import com.netgrif.application.engine.auth.realm.request.RealmSearch;
import com.netgrif.application.engine.auth.repository.RealmRepository;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import com.netgrif.application.engine.objects.auth.provider.RealmUpdate;
import com.netgrif.application.engine.objects.tenant.TenantConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;


@Slf4j
public class RealmServiceImpl implements RealmService {

    @Autowired
    private RealmRepository realmRepository;

    @Autowired
    private ProviderRegistry providerRegistry;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnonymousUserRefService anonymousUserRefService;

    @Autowired
    private TenantService tenantService;

    private AbstractMongoCollectionConfigurator mongoCollectionConfigurator;

    private CollectionNameProvider collectionNameProvider;

    @Autowired
    public void setMongoCollectionConfigurator(AbstractMongoCollectionConfigurator mongoCollectionConfigurator) {
        this.mongoCollectionConfigurator = mongoCollectionConfigurator;
    }

    @Lazy
    @Autowired
    public void setCollectionNameProvider(CollectionNameProvider collectionNameProvider) {
        this.collectionNameProvider = collectionNameProvider;
    }

    @Override
    public Realm createRealm(Realm createRequest) {
        if (realmRepository.existsById(createRequest.getName())) {
            throw new IllegalArgumentException("Realm with name " + createRequest.getName() + " already exists");
        }
        com.netgrif.application.engine.adapter.spring.auth.domain.Realm realm = new com.netgrif.application.engine.adapter.spring.auth.domain.Realm(createRequest.getName());
        realm.setDescription(createRequest.getDescription());
        realm.setAdminRealm(createRequest.isAdminRealm());

        boolean tenantExists = tenantService.exists(createRequest.getTenantId());

        if (tenantExists) {
            throw new IllegalArgumentException("Tenant with id [%s] does not exist.".formatted(createRequest.getTenantId()));
        }

        realm.setTenantId(createRequest.getTenantId());

        if (createRequest.isDefaultRealm() && getDefaultRealm(createRequest.getTenantId()).isEmpty()) {
            realm.setDefaultRealm(true);
        }

        realm = realmRepository.save(realm);

        tenantService.addRealm(realm.getTenantId(), realm);

        String collectionName = collectionNameProvider.getCollectionNameForRealm(realm.getName());

        if (!mongoTemplate.collectionExists(collectionName)) {
            try {
                mongoTemplate.createCollection(collectionName);
                mongoCollectionConfigurator.resolveIndexes(collectionName, User.class);
            } catch (Exception e) {
                log.error("Error occurred while creating collection for realm {}", realm.getName(), e);
                realmRepository.delete(realm);
                tenantService.removeRealm(realm.getTenantId(), realm.getName());
                throw new RuntimeException("Error occurred while creating collection for realm " + realm.getName(), e);
            }
        }

        return realmRepository.save(realm);
    }

    @Override
    public Page<Realm> search(RealmSearch nodeProbe, Pageable pageable) {
        return realmRepository.searchRealms(nodeProbe, pageable, mongoTemplate).map(Realm.class::cast);
    }

    @Override
    public void enableAnonymUser(Realm realm) {
        anonymousUserRefService.getOrCreateRef(realm.getName());
        realm.setPublicAccess(true);
        realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);
    }

    @Override
    public void disableAnonymUser(Realm realm) {
        anonymousUserRefService.deleteRef(realm.getName());
        realm.setPublicAccess(false);
        realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);
    }

    @Override
    public Optional<Realm> getDefaultRealm() {
        return realmRepository.findByDefaultRealmTrueAndTenantId(TenantConstants.AdminTenant.ID).map(Realm.class::cast);
    }

    @Override
    public Optional<Realm> getDefaultRealm(String tenantId) {
        return realmRepository.findByDefaultRealmTrueAndTenantId(tenantId).map(Realm.class::cast);
    }

    @Override
    public Optional<Realm> getAdminRealm() {
        return realmRepository.findAdminRealm().map(Realm.class::cast);
    }

    @Override
    public Page<Realm> getAllRealm(Pageable pageable) {
        return realmRepository.findAll(pageable == null ? PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()) : pageable).map(Realm.class::cast);
    }

    @Override
    public Page<Realm> getSmallRealm(Pageable pageable) {
        return realmRepository.findAllSmall(pageable == null ? PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()) : pageable).map(Realm.class::cast);
    }

    @Override
    public Optional<Realm> getRealmById(String id) {
        return (id == null || id.isEmpty()) ? Optional.empty() : realmRepository.findById(id).map(Realm.class::cast);
    }

    @Override
    public Optional<Realm> getRealmByName(String name) {
        return (name == null || name.isEmpty()) ? Optional.empty() : realmRepository.findByName(name).map(Realm.class::cast);
    }

    @Override
    public <C extends AbstractAuthConfig> Realm addProvider(String realmId, AuthMethodConfig<C> config) {
        AuthMethodProvider<C> provider = (AuthMethodProvider<C>) providerRegistry.getProvider(config.getType());
        if (provider == null) {
            throw new IllegalArgumentException("Provider type " + config.getType() + " not found");
        }

        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        realm.addAuthMethod(config);
        return realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);
    }

    @Override
    public void removeProvider(String realmId, String providerId) {
        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        Optional<AuthMethodConfig<?>> configToRemove = realm.getAuthMethods().stream()
                .filter(config -> config.getId().equals(providerId))
                .findFirst();

        if (configToRemove.isPresent()) {
            realm.removeAuthMethod(configToRemove.get());
            realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);
        } else {
            throw new IllegalArgumentException("Provider with id " + providerId + " not found in realm " + realmId);
        }
    }

    @Override
    public Realm updateRealm(String realmId, Realm update) {
        Realm realm = getRealmById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));

        realm.setDescription(update.getDescription());
        realm.setEnableBlocking(update.isEnableBlocking());
        realm.setMaxFailedAttempts(update.getMaxFailedAttempts());
        realm.setBlockDurationMinutes(update.getBlockDurationMinutes());
        realm.setPublicAccess(update.isPublicAccess());
        realm.setSessionTimeout(update.getSessionTimeout());
        realm.setPublicSessionTimeout(update.getPublicSessionTimeout());
        realm.setEnableLimitSessions(update.isEnableLimitSessions());
        realm.setMaxSessionsAllowed(update.getMaxSessionsAllowed());

        if (update.isDefaultRealm()) {
            if (!realm.isDefaultRealm() && getDefaultRealm(realm.getTenantId()).isEmpty()) {
                realm.setDefaultRealm(true);
            }
        } else {
            realm.setDefaultRealm(false);
        }

        return realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);
    }

    @Override
    public AuthMethodConfig<?> updateConfigInRealm(String realmId, AuthMethodConfig<?> config) {
        if (config == null) {
            throw new IllegalArgumentException("Authentication config not provided");
        }

        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        Optional<AuthMethodConfig<?>> configToUpdateOpt = realm.getAuthMethods().stream()
                .filter(realmConfig -> realmConfig.getId().equals(config.getId()))
                .findFirst();

        if (configToUpdateOpt.isEmpty()) {
            throw new IllegalArgumentException("Authentication config with id " + config.getId() + " not found in realm " + realmId);
        }

        AuthMethodConfig configToUpdate = configToUpdateOpt.get();
        configToUpdate.setName(config.getName());
        configToUpdate.setDescription(config.getDescription());
        configToUpdate.setEnabled(config.isEnabled());
        configToUpdate.setOrder(config.getOrder());
        configToUpdate.setConfiguration(config.getConfiguration());

        realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);

        return configToUpdate;
    }

    @Override
    public AuthMethodConfig<?> partialUpdateConfigInRealm(String realmId, String providerId, RealmUpdate updates) {
        if (updates == null || (updates.getConfiguration() == null &&
                updates.getEnabled() == null &&
                updates.getName() == null &&
                updates.getDescription() == null &&
                updates.getOrder() == null)) {
            throw new IllegalArgumentException("No update data provided");
        }

        Realm realm = getRealmById(realmId)
                .orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));

        Optional<AuthMethodConfig<?>> configToUpdateOpt = realm.getAuthMethods().stream()
                .filter(existing -> existing.getId().equals(providerId))
                .findFirst();

        if (configToUpdateOpt.isEmpty()) {
            throw new IllegalArgumentException("Authentication config with id " + providerId + " not found in realm " + realmId);
        }

        AuthMethodConfig<?> existingConfig = configToUpdateOpt.get();

        if (updates.getName() != null) {
            existingConfig.setName(updates.getName());
        }

        if (updates.getDescription() != null) {
            existingConfig.setDescription(updates.getDescription());
        }

        if (updates.getEnabled() != null) {
            existingConfig.setEnabled(updates.getEnabled());
        }

        if (updates.getOrder() != null) {
            existingConfig.setOrder(updates.getOrder());
        }

        Map<String, Object> configUpdates = updates.getConfiguration();
        Object targetConfig = existingConfig.getConfiguration();

        if (targetConfig != null && configUpdates != null && !configUpdates.isEmpty()) {
            try {
                JsonNode patchNode = objectMapper.valueToTree(configUpdates);
                objectMapper.readerForUpdating(targetConfig).readValue(patchNode);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to apply configuration patch", e);
            }
        }
        realmRepository.save((com.netgrif.application.engine.adapter.spring.auth.domain.Realm) realm);
        return existingConfig;
    }

    @Override
    public void deleteRealm(String realmId) {
        if (!realmRepository.existsById(realmId)) {
            throw new IllegalArgumentException("Realm with id " + realmId + " not found");
        }
        tenantService.getByRealm(realmId).ifPresent(t -> tenantService.removeRealm(t.getId(), realmId));
        realmRepository.deleteById(realmId);
    }
}
