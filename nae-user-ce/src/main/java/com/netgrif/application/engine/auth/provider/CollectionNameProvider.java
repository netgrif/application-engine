package com.netgrif.application.engine.auth.provider;

import com.netgrif.application.engine.adapter.spring.utils.PageableUtils;
import com.netgrif.application.engine.auth.service.RealmService;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CollectionNameProvider {

    private RealmService realmService;

    @Autowired
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    public String getCollectionNameForRealm(String realmId) {
        if (realmId == null || realmId.isEmpty()) {
            return getDefaultRealmCollection();
        }
        return "users_" + realmId;
    }

    public String getAdminRealmCollection() {
        Optional<Realm> defaultRealmOptional = realmService.getAdminRealm();
        if (defaultRealmOptional.isEmpty()) {
            throw new MissingResourceException("Admin realm is not specified.", Realm.class.getName(), "defaultRealm");
        }
        return "users_" + defaultRealmOptional.get().getId();
    }

    public Set<String> getCollectionNamesForRealms(Collection<String> realmIds) {
        if (realmIds == null || realmIds.isEmpty()) {
            return getCollectionNamesForAllRealm();
        }
        return realmIds.stream().map(realmId -> "users_" + realmId).collect(Collectors.toSet());
    }

    public Set<String> getCollectionNamesForAllRealm() {
        return realmService.getAllRealm(PageableUtils.fullPageRequest()).getContent().stream().map(realm -> "user_" + realm.getId()).collect(Collectors.toSet());
    }

    public String getDefaultRealmCollection() {
        Optional<Realm> defaultRealmOptional = realmService.getDefaultRealm();
        if (defaultRealmOptional.isEmpty()) {
            throw new MissingResourceException("Default realm is not specified.", Realm.class.getName(), "defaultRealm");
        }
        return "users_" + defaultRealmOptional.get().getId();
    }
}
