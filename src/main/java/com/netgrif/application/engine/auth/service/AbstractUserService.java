package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.*;
import com.netgrif.application.engine.auth.domain.repositories.UserRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.querydsl.core.types.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.startup.SystemUserRunner.*;

public abstract class AbstractUserService implements IUserService {

    @Autowired
    protected IAuthorityService authorityService;

    @Autowired
    protected IProcessRoleService processRoleService;


    @Autowired
    protected INextGroupService groupService;

    @Autowired
    protected UserRepository repository;

    @Autowired
    private ISecurityContextService securityContextService;

    @Override
    public void addDefaultRole(IUser user) {
        user.addProcessRole(processRoleService.defaultRole());
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
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public IUser assignAuthority(String userId, String authorityId) {
        IUser user = resolveById(userId, true);
        Authority authority = authorityService.getOne(authorityId);
        user.addAuthority(authority);
        authority.addUser(user);

        return save(user);
    }

    @Override
    public LoggedUser getAnonymousLogged() {
        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(UserProperties.ANONYMOUS_AUTH_KEY)) {
            getLoggedUser().transformToLoggedUser();
        }
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public IUser addRole(IUser user, String roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        user.addProcessRole(role);
        securityContextService.saveToken(user.getStringId());
        securityContextService.reloadSecurityContext(user.transformToLoggedUser());
        return save(user);
    }

    /**
     * @param user
     * @param roleStringId
     * @return
     * @deprecated use {@link AbstractUserService#removeRole(IUser, ProcessRole)} instead
     */
    @Override
    @Deprecated(since = "6.2.0")
    public IUser removeRole(IUser user, String roleStringId) {
        return removeRole(user, processRoleService.findById(roleStringId));
    }

    protected IUser removeRole(IUser user, ProcessRole role) {
        user.removeProcessRole(role);
        securityContextService.saveToken(user.getStringId());
        securityContextService.reloadSecurityContext(user.transformToLoggedUser());
        return save(user);
    }

    @Override
    public void removeRoleOfDeletedPetriNet(PetriNet net) {
        List<IUser> users = findAllByProcessRoles(net.getRoles().keySet(), false);
        users.forEach(u -> {
            net.getRoles().forEach((k, role) -> removeRole(u, role));
        });
    }

    @Override
    public IUser createSystemUser() {
        User system = repository.findByEmail(SYSTEM_USER_EMAIL);
        if (system == null) {
            system = new User(SYSTEM_USER_EMAIL, "n/a", SYSTEM_USER_NAME, SYSTEM_USER_SURNAME);
            system.setState(UserState.ACTIVE);
            repository.save(system);
        }
        return system;
    }

    @Override
    public Page<IUser> search(Predicate predicate, Pageable pageable) {
        if (predicate == null) {
            return Page.empty();
        }
        if (pageable == null) {
            pageable = Pageable.unpaged();
        }
        return repository.findAll(predicate, pageable).map(IUser.class::cast);
    }

    @Override
    public IUser searchOne(Predicate predicate) {
        if (predicate == null) {
            return null;
        }
        Page<User> userAsPage = repository.findAll(predicate, PageRequest.of(0, 1));
        if (userAsPage.getTotalElements() > 0) {
            return userAsPage.getContent().get(0);
        }
        return null;
    }

    @Override
    public long count(Predicate predicate) {
        if (predicate == null) {
            return 0;
        }
        return repository.count(predicate);
    }

    @Override
    public boolean exists(Predicate predicate) {
        if (predicate == null) {
            return false;
        }
        return repository.exists(predicate);
    }

    public <T> Page<IUser> changeType(Page<T> users, Pageable pageable) {
        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
    }

    public <T> List<IUser> changeType(List<T> users) {
        return users.stream().map(IUser.class::cast).collect(Collectors.toList());
    }

}
