package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.*;
import com.netgrif.application.engine.authentication.domain.repositories.UserRepository;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.startup.SystemUserRunner.*;

public abstract class AbstractUserService implements IUserService {

    @Autowired
    protected IAuthorityService authorityService;

    @Autowired
    protected IRoleService roleService;

    @Autowired
    protected INextGroupService groupService;

    @Autowired
    protected UserRepository repository;

    /**
     * todo javadoc
     * */
    @Override
    public void addDefaultRole(IUser user) {
        Role defaultRole = roleService.findDefaultRole();
        roleService.assignRolesToUser(user.getStringId(), Set.of(defaultRole.getStringId()));
    }

    @Override
    public void addDefaultAuthorities(IUser user) {
        if (user.getAuthorities().isEmpty()) {
            HashSet<Authority> authorities = new HashSet<>();
            authorities.add(authorityService.getOrCreate(Authority.user));
            user.setAuthorities(authorities);
        }
    }

    @Override
    public IUser assignAuthority(String userId, String authorityId) {
        IUser user = resolveById(userId);
        Authority authority = authorityService.getOne(authorityId);
        user.addAuthority(authority);
        authority.addUser(user);

        return save(user);
    }

    @Override
    public Identity getAnonymousLogged() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(UserProperties.ANONYMOUS_AUTH_KEY)) {
            getLoggedUser().transformToLoggedUser();
        }
        return (Identity) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public IUser createSystemUser() {
        User system = repository.findByEmail(SYSTEM_USER_EMAIL);
        if (system == null) {
            system = new User(SYSTEM_USER_EMAIL, "n/a", SYSTEM_USER_NAME, SYSTEM_USER_SURNAME);
            system.setState(IdentityState.ACTIVE);
            repository.save(system);
        }
        return system;
    }

    public <T> Page<IUser> changeType(Page<T> users, Pageable pageable) {
        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
    }

    public <T> List<IUser> changeType(List<T> users) {
        return users.stream().map(IUser.class::cast).collect(Collectors.toList());
    }

    @Override
    public IUser getUserFromLoggedUser(Identity identity) {
        IUser user = resolveById(identity.getId());
        IUser fromLogged = identity.transformToUser();
        user.setImpersonated(fromLogged.getImpersonated());
        return user;
    }
}