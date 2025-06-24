package com.netgrif.application.engine.auth.provider;

import com.netgrif.application.engine.objects.auth.provider.AuthMethod;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;

public interface AuthMethodProvider<T extends AbstractAuthConfig> {

    String getProviderType();

    AuthMethod<T> createAuthMethod(AuthMethodConfig<?> authMethodConfig);

    Class<T> getConfigClass();

    Class<? extends AuthMethod<T>> getAuthMethodClass();
}
