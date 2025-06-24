package com.netgrif.application.engine.auth.provider;

import com.netgrif.application.engine.adapter.spring.utils.PageableUtils;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CollectionNameProvider {

    private RealmService realmService;

    private static final String USER_MONGO_COLLECTION_PREFIX = "users_";
    private static final String NULL = "null";

    @Autowired
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public String getCollectionNameForRealm(String realmId) {
        if (realmId == null || realmId.isEmpty() || realmId.equals(NULL)) {
            return getDefaultRealmCollection();
        }
        return USER_MONGO_COLLECTION_PREFIX + realmId;
    }

    public String getAdminRealmCollection() {
        Optional<Realm> defaultRealmOptional = realmService.getAdminRealm();
        if (defaultRealmOptional.isEmpty()) {
            throw new MissingResourceException("Admin realm is not specified.", Realm.class.getName(), "defaultRealm");
        }
        return USER_MONGO_COLLECTION_PREFIX + defaultRealmOptional.get().getId();
    }

    public Set<String> getCollectionNamesForRealms(Collection<String> realmIds) {
        if (realmIds == null || realmIds.isEmpty()) {
            return getCollectionNamesForAllRealm();
        }
        return realmIds.stream().map(realmId -> USER_MONGO_COLLECTION_PREFIX + realmId).collect(Collectors.toSet());
    }

    public Set<String> getCollectionNamesForAllRealm() {
        return realmService.getAllRealm(PageableUtils.fullPageRequest()).getContent().stream().map(realm -> USER_MONGO_COLLECTION_PREFIX + realm.getId()).collect(Collectors.toSet());
    }

    public String getDefaultRealmCollection() {
        Optional<Realm> defaultRealmOptional = realmService.getDefaultRealm();
        if (defaultRealmOptional.isEmpty()) {
            throw new MissingResourceException("Default realm is not specified.", Realm.class.getName(), "defaultRealm");
        }
        return USER_MONGO_COLLECTION_PREFIX + defaultRealmOptional.get().getId();
    }
}
