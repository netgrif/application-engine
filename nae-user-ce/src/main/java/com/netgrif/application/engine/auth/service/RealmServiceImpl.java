package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.provider.AbstractAuthConfig;
import com.netgrif.application.engine.auth.provider.AuthMethodProvider;
import com.netgrif.application.engine.auth.provider.ProviderRegistry;
import com.netgrif.application.engine.auth.realm.request.RealmSearch;
import com.netgrif.application.engine.auth.repository.RealmRepository;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.auth.provider.AuthMethod;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;

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
    private AnonymousUserRefService anonymousUserRefService;


    @Override
    public Realm createRealm(Realm createRequest) {
        Realm realm = new com.netgrif.application.engine.adapter.spring.auth.domain.Realm(createRequest.getId());
        realm.setDescription(createRequest.getDescription());

        realm.setAdminRealm(createRequest.isAdminRealm());

        if (createRequest.isDefaultRealm() && getDefaultRealm().isEmpty()) {
            realm.setDefaultRealm(true);
        }

        return realmRepository.save(realm);
    }

    @Override
    public Page<Realm> search(RealmSearch nodeProbe, Pageable pageable) {
        return realmRepository.searchRealms(nodeProbe, pageable, mongoTemplate);
    }

    @Override
    public void enableAnonymUser(Realm realm) {
        anonymousUserRefService.getOrCreateRef(realm.getId());
        realm.setPublicAccess(true);
        realmRepository.save(realm);
    }

    @Override
    public void disableAnonymUser(Realm realm) {
        anonymousUserRefService.deleteRef(realm.getId());
        realm.setPublicAccess(false);
        realmRepository.save(realm);
    }

    @Override
    public Optional<Realm> getDefaultRealm() {
        return realmRepository.findByDefaultRealmTrue();
    }

    @Override
    public Optional<Realm> getAdminRealm() {
        return realmRepository.findAdminRealm();
    }

    @Override
    public Page<Realm> getAllRealm(Pageable pageable) {
        return realmRepository.findAll(pageable == null ? PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()) : pageable);
    }

    @Override
    public Page<Realm> getSmallRealm(Pageable pageable) {
        return realmRepository.findAllSmall(pageable == null ? PageRequest.of(0, Integer.MAX_VALUE, Sort.unsorted()) : pageable);
    }

    @Override
    public Optional<Realm> getRealmById(String id) {
        return (id == null || id.isEmpty()) ? Optional.empty() : realmRepository.findById(id);
    }

    @Override
    public Optional<Realm> getRealmByName(String name) {
        return (name == null || name.isEmpty()) ? Optional.empty() : realmRepository.findByName(name);
    }

    @Override
    public <C extends AbstractAuthConfig, T extends AuthMethod<C>> T addProvider(String realmId, AuthMethodConfig<C> config) {
        AuthMethodProvider<C> provider = (AuthMethodProvider<C>) providerRegistry.getProvider(config.getType());
        if (provider == null) {
            throw new IllegalArgumentException("Provider type " + config.getType() + " not found");
        }

        AuthMethod<C> authMethod = provider.createAuthMethod(config);
        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        realm.addAuthMethod(config);
        realmRepository.save(realm);

        return (T) authMethod;
    }

    @Override
    public void removeProvider(String realmId, String providerId) {
        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        Optional<AuthMethodConfig<?>> configToRemove = realm.getAuthMethods().stream()
                .filter(config -> config.getId().equals(providerId))
                .findFirst();

        if (configToRemove.isPresent()) {
            realm.removeAuthMethod(configToRemove.get());
            realmRepository.save(realm);
        } else {
            throw new IllegalArgumentException("Provider with id " + providerId + " not found in realm " + realmId);
        }
    }

    @Override
    public Realm updateRealm(String realmId, Realm update) {
        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        realm.setName(update.getId());
        realm.setDescription(update.getDescription());
        realm.setAdminRealm(update.isAdminRealm());
        if (update.isDefaultRealm() && getDefaultRealm().isEmpty()) {
            realm.setDefaultRealm(true);
        }
        return realmRepository.save(realm);
    }

    @Override
    public void deleteRealm(String realmId) {
        if (!realmRepository.existsById(realmId)) {
            throw new IllegalArgumentException("Realm with id " + realmId + " not found");
        }
        realmRepository.deleteById(realmId);
    }

    @Override
    public void addUserToRealm(String realmId, String userId) {
        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        realm.getUserIds().add(userId);
        realmRepository.save(realm);
    }

    @Override
    public void removeUserFromRealm(String realmId, String userId) {
        Realm realm = getRealmById(realmId).orElseThrow(() -> new IllegalArgumentException("Realm with id " + realmId + " not found"));
        realm.getUserIds().remove(userId);
        realmRepository.save(realm);
    }
}
