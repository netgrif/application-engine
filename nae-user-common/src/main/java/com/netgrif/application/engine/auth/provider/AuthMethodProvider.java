package com.netgrif.application.engine.auth.provider;

import com.netgrif.application.engine.objects.auth.provider.AuthMethod;

public interface AuthMethodProvider<T extends AbstractAuthConfig> {

    String getProviderType();

    Class<T> getConfigClass();

    Class<? extends AuthMethod<T>> getAuthMethodClass();
}
