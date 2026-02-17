package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.provider.AbstractAuthConfig;
import com.netgrif.application.engine.auth.realm.request.RealmSearch;
import com.netgrif.application.engine.objects.auth.domain.Realm;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import com.netgrif.application.engine.objects.auth.provider.RealmUpdate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Map;
import java.util.Optional;

public interface RealmService {

    Realm createRealm(Realm realm);

    Page<Realm> search(RealmSearch nodeProbe, Pageable pageable);

    void enableAnonymUser(Realm realm);

    void disableAnonymUser(Realm realm);

    @Deprecated(forRemoval = true)
    Optional<Realm> getDefaultRealm();

    Optional<Realm> getDefaultRealm(String tenantId);

    Optional<Realm> getAdminRealm();

    Page<Realm> getAllRealm(Pageable pageable);

    Page<Realm> getSmallRealm(Pageable pageable);

    Optional<Realm> getRealmById(String id);

    Optional<Realm> getRealmByName(String name);

    <C extends AbstractAuthConfig> Realm addProvider(String realmId, AuthMethodConfig<C> config);

    void removeProvider(String realmId, String providerId);

    Realm updateRealm(String realmId, Realm update);

    AuthMethodConfig<?> updateConfigInRealm(String realmId, AuthMethodConfig<?> config);

    public AuthMethodConfig<?> partialUpdateConfigInRealm(String realmId, String providerId, RealmUpdate updates);

    void deleteRealm(String realmId);
}
