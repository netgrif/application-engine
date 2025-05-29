package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.authorization.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.AnonymousIdentityRunner;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * todo javadoc
 */
@Slf4j
public class PublicBasicAuthenticationFilter extends PublicJwtAuthenticationFilter  {

    private final IUserService userService;

    public PublicBasicAuthenticationFilter(IIdentityService identityService, ProviderManager authenticationManager,
                                           AnonymousAuthenticationProvider provider, String[] urls, String[] exceptions,
                                           IJwtService jwtService, IUserService userService, IRoleService roleService) {
        super(identityService, authenticationManager, provider, urls, exceptions, jwtService, roleService);
        this.userService = userService;
    }

    /**
     * todo javadoc
     */
    @Override
    protected Identity getAnonymousIdentityWithUser() {
        String hash = new ObjectId().toString();

        Optional<Identity> anonymIdentityOpt = identityService.findByUsername(AnonymIdentityConstants.usernameOf(hash));
        if (anonymIdentityOpt.isPresent()) {
            return anonymIdentityOpt.get();
        }

        Optional<User> anonymUserOpt = userService.findByEmail(AnonymIdentityConstants.defaultUsername());
        User anonymUser = anonymUserOpt.orElseThrow(() -> new IllegalStateException(String.format(
                "Default anonymous user with email [%s] doesn't exist", AnonymIdentityConstants.defaultUsername())));

        return identityService.encodePasswordAndCreate(IdentityParams.with()
                .username(new TextField(AnonymIdentityConstants.usernameOf(hash)))
                .firstname(new TextField(AnonymIdentityConstants.FIRSTNAME))
                .lastname(new TextField(AnonymIdentityConstants.LASTNAME))
                .password(new TextField("n/a"))
                .mainActor(CaseField.withValue(List.of(anonymUser.getStringId())))
                .properties(Map.of(AnonymousIdentityRunner.getAnonymousFlag(), "true"))
                .build());
    }
}
