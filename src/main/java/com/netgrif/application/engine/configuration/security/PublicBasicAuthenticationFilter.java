package com.netgrif.application.engine.configuration.security;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.IdentityProperties;
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.params.ActorParams;
import com.netgrif.application.engine.authorization.service.interfaces.IActorService;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.configuration.security.jwt.IJwtService;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.security.authentication.AnonymousAuthenticationProvider;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * todo javadoc
 */
@Slf4j
public class PublicBasicAuthenticationFilter extends PublicJwtAuthenticationFilter  {
    private static final String USERNAME = "anonymous";

    private final IActorService actorService;

    public PublicBasicAuthenticationFilter(IIdentityService identityService, IRoleService roleService, ProviderManager authenticationManager,
                                           AnonymousAuthenticationProvider provider, Authority anonymousAuthority,
                                           String[] urls, String[] exceptions, IJwtService jwtService, IActorService actorService) {
        super(identityService, roleService, authenticationManager, provider, anonymousAuthority, urls, exceptions, jwtService);
        this.actorService = actorService;
    }

    /**
     * todo javadoc
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (isPublicApi(request.getRequestURI())) {
            String jwtToken = resolveValidToken(request);
            authenticate(request, jwtToken);
            response.setHeader(JWT_HEADER_NAME, BEARER + jwtToken);
            log.info("Anonymous identity was authenticated.");
        }
        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String jwtToken) {
        AnonymousAuthenticationToken authRequest = new AnonymousAuthenticationToken(
                IdentityProperties.ANONYMOUS_AUTH_KEY,
                jwtService.getLoggedIdentity(jwtToken, this.anonymousAuthority),
                Collections.singleton(this.anonymousAuthority)
        );
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        Authentication authResult = this.authenticationManager.authenticate(authRequest);
        SecurityContextHolder.getContext().setAuthentication(authResult);
    }

    /**
     * todo javadoc
     * @return
     */
    @Override
    protected Identity createAnonymousIdentityWithActor() {
        String hash = new ObjectId().toString();

        Optional<Identity> anonymIdentityOpt = identityService.findByUsername(AnonymIdentityConstants.usernameOf(hash));
        if (anonymIdentityOpt.isPresent()) {
            return anonymIdentityOpt.get();
        }

        Optional<Actor> anonymActorOpt = actorService.findByEmail(AnonymIdentityConstants.usernameOf(USERNAME));
        Actor anonymActor = anonymActorOpt.orElseGet(() -> actorService.create(ActorParams.with()
                .email(new TextField(AnonymIdentityConstants.usernameOf(USERNAME)))
                .firstname(new TextField(AnonymIdentityConstants.FIRSTNAME))
                .lastname(new TextField(AnonymIdentityConstants.LASTNAME))
                .build()));

        roleService.assignRolesToActor(anonymActor.getStringId(), Set.of(roleService.findAnonymousRole().getStringId()));
        // todo 2058 app role

        return identityService.encodePasswordAndCreate(IdentityParams.with()
                .username(new TextField())
                .firstname(new TextField())
                .lastname(new TextField())
                .password(new TextField("n/a"))
                .mainActor(CaseField.withValue(List.of(anonymActor.getStringId())))
                .build());
    }
}
