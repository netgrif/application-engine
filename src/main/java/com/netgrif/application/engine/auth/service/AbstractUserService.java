//package com.netgrif.application.engine.auth.service;
//
//import com.netgrif.core.auth.domain.Authority;.*;
//import com.netgrif.core.auth.domain.Authority;.repositories.UserRepository;
//import com.netgrif.adapter.auth.service.AuthorityService;
//import com.netgrif.adapter.auth.service.UserService;
//import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
//import com.netgrif.core.petrinet.domain.PetriNet;
//import com.netgrif.core.petrinet.domain.roles.ProcessRole;
//import com.netgrif.adapter.petrinet.service.ProcessRoleService;
//import com.netgrif.application.engine.security.service.ISecurityContextService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageImpl;
//import org.springframework.data.domain.Pageable;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.stream.Collectors;
//
//import static com.netgrif.application.engine.startup.runner.SystemUserRunner.*;
//
//public abstract class AbstractUserService implements IUserService {
//
//    @Autowired
//    protected AuthorityService authorityService;
//
//    @Autowired
//    protected ProcessRoleService processRoleService;
//
//
//    @Autowired
//    protected INextGroupService groupService;
//
//    @Autowired
//    protected UserRepository repository;
//
//    @Autowired
//    private ISecurityContextService securityContextService;
//
//    @Override
//    public void addDefaultRole(IUser user) {
//        user.addProcessRole(processRoleService.defaultRole());
//    }
//
//    @Override
//    public void addAnonymousRole(IUser user) {
//        user.addProcessRole(processRoleService.defaultRole());
//    }
//
//    @Override
//    public void addDefaultAuthorities(IUser user) {
//        if (user.getAuthorities().isEmpty()) {
//            HashSet<Authority> authorities = new HashSet<>();
//            authorities.add(authorityService.getOrCreate(Authority.user));
//            user.setAuthorities(authorities);
//        }
//    }
//
//    @Override
//    public void addAnonymousAuthorities(IUser user) {
//        if (user.getAuthorities().isEmpty()) {
//            HashSet<Authority> authorities = new HashSet<>();
//            authorities.add(authorityService.getOrCreate(Authority.anonymous));
//            user.setAuthorities(authorities);
//        }
//    }
//
//    @Override
//    public IUser assignAuthority(String userId, String authorityId) {
//        IUser user = resolveById(userId, true);
//        Authority authority = authorityService.getOne(authorityId);
//        user.addAuthority(authority);
//        authority.addUser(user);
//
//        return save(user);
//    }
//
//    @Override
//    public LoggedUser getAnonymousLogged() {
//        if (SecurityContextHolder.getContext().getAuthentication().getPrincipal().equals(UserProperties.ANONYMOUS_AUTH_KEY)) {
//            getLoggedUser().transformToLoggedUser();
//        }
//        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//    }
//
//    @Override
//    public IUser addRole(IUser user, String roleStringId) {
//        ProcessRole role = processRoleService.findById(roleStringId);
//        user.addProcessRole(role);
//        securityContextService.saveToken(user.getStringId());
//        securityContextService.reloadSecurityContext(userService.transformToLoggedUser(user));
//        return save(user);
//    }
//
//    /**
//     * @param user
//     * @param roleStringId
//     * @return
//     * @deprecated use {@link AbstractUserService#removeRole(IUser, ProcessRole)} instead
//     */
//    @Override
//    @Deprecated(since = "6.2.0")
//    public IUser removeRole(IUser user, String roleStringId) {
//        return removeRole(user, processRoleService.findByImportId(roleStringId));
//    }
//
//    @Override
//    public IUser removeRole(IUser user, ProcessRole role) {
//        user.removeProcessRole(role);
//        securityContextService.saveToken(user.getStringId());
//        securityContextService.reloadSecurityContext(userService.transformToLoggedUser(user));
//        return save(user);
//    }
//
//    @Override
//    public void removeRoleOfDeletedPetriNet(PetriNet net) {
//        List<IUser> users = findAllByProcessRoles(net.getRoles().keySet(), false);
//        users.forEach(u -> {
//            net.getRoles().forEach((k, role) -> removeRole(u, role));
//        });
//    }
//
//    @Override
//    public IUser createSystemUser() {
//        User system = repository.findByEmail(SYSTEM_USER_EMAIL);
//        if (system == null) {
//            system = new User(SYSTEM_USER_EMAIL, "n/a", SYSTEM_USER_NAME, SYSTEM_USER_SURNAME);
//            system.setState(UserState.ACTIVE);
//            repository.save(system);
//        }
//        return system;
//    }
//
//    public <T> Page<IUser> changeType(Page<T> users, Pageable pageable) {
//        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
//    }
//
//    public <T> List<IUser> changeType(List<T> users) {
//        return users.stream().map(IUser.class::cast).collect(Collectors.toList());
//    }
//
//}
