package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.LoggedUserImpl;
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.workflow.service.FilterImportExportService;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.UserRepository;
import com.netgrif.application.engine.objects.auth.constants.UserConstants;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.petrinet.domain.workspace.DefaultWorkspaceService;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.petrinet.service.workspace.WorkspaceContextHolder;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;

    private CollectionNameProvider collectionNameProvider;

    private MongoTemplate mongoTemplate;

    private AuthorityService authorityService;

    private PasswordEncoder passwordEncoder;

    private ProcessRoleService processRoleService;

    private FilterImportExportService filterImportExportService;

    private GroupService groupService;

    private GroupConfigurationProperties groupConfigurationProperties;

    private IUser systemUser;

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setCollectionNameProvider(CollectionNameProvider collectionNameProvider) {
        this.collectionNameProvider = collectionNameProvider;
    }

    @Autowired
    public void setMongoTemplate(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Autowired
    public void setAuthorityService(AuthorityService authorityService) {
        this.authorityService = authorityService;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Lazy
    @Autowired
    public void setProcessRoleService(ProcessRoleService processRoleService) {
        this.processRoleService = processRoleService;
    }

    @Autowired
    public void setGroupConfigurationProperties(GroupConfigurationProperties groupConfigurationProperties) {
        this.groupConfigurationProperties = groupConfigurationProperties;
    }

    @Autowired(required = false)
    public void setFilterImportExportService(FilterImportExportService filterImportExportService) {
        this.filterImportExportService = filterImportExportService;
    }

    @Lazy
    @Autowired
    public void setGroupService(GroupService groupService) {
        this.groupService = groupService;
    }

    @Override
    public IUser saveUser(IUser user, String realmId) {
        log.debug("Saving user [{}] in DEFAULT realm", user.getUsername());
        user.setWorkspaceId(null);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        user = userRepository.saveUser((User) user, mongoTemplate, collectionName);
        log.trace("User [{}] saved in collection [{}]", user.getUsername(), collectionName);
        return user;
    }

    @Override
    public List<User> saveUsers(List<IUser> users) {
        return users.stream().map(u -> (User) this.saveUser(u, u.getRealmId())).toList();
    }

    @Override
    public Optional<IUser> findUserByUsername(String username, String realmId) {
        log.debug("Finding user by username [{}] in realm [{}]", username, realmId);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        Optional<IUser> userOpt = userRepository.findByUsername(username, mongoTemplate, collectionName).map(user -> (IUser) user);
        if (userOpt.isPresent()) {
            log.debug("User [{}] found in realm [{}]", username, collectionName);
        } else {
            log.warn("User [{}] not found in realm [{}]", username, collectionName);
        }
        return userOpt;
    }

    @Override
    public List<IUser> findAllUsers(String realmId) {
        log.trace("Retrieving all users in realm [{}]", realmId);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        List<IUser> users = mongoTemplate.findAll(IUser.class, collectionName);
        log.debug("Found [{}] users in realm [{}]", users.size(), realmId);
        return users;
    }

    @Override
    public IUser createUser(String username, String email, String firstName, String lastName, String password, String realmId) {
        User user = initializeNewUser(username, email, firstName, lastName, realmId);
        return createUser(user, realmId);
    }

    @Override
    public IUser createUser(IUser user, String realmId) {
        log.info("Creating user [{}] in realm [{}]", user.getUsername(), realmId);
        addDefaultAuthorities(user);
        addDefaultRole(user);
        ((User) user).addAuthMethod("basic");
        setPassword(((User) user), ((User) user).getPassword());

        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        user = userRepository.saveUser(((User) user), mongoTemplate, collectionName);

        filterImportExportService.createFilterImport(user);
        filterImportExportService.createFilterExport(user);

        if (groupConfigurationProperties.isDefaultEnabled())
            groupService.create(user);

        if (groupConfigurationProperties.isSystemEnabled())
            groupService.addUserToDefaultSystemGroup(user);

        user = userRepository.saveUser(((User) user), mongoTemplate, collectionName);
        log.info("User [{}] successfully created in realm [{}]", user.getUsername(), realmId);
        return user;
    }

    @Override
    public User createUserFromThirdParty(String username, String email, String firstName, String lastName, String realmId, String authMethod) {
        log.info("Creating user [{}] from third-party auth [{}] in realm [{}] without password", username, authMethod, realmId);
        User user = initializeNewUser(username, email, firstName, lastName, realmId);
        addDefaultAuthorities(user);
        addDefaultRole(user);
        setDisablePassword(user);
        user.addAuthMethod(authMethod);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        userRepository.saveUser(user, mongoTemplate, collectionName);
        log.info("User [{}] from third-party [{}] successfully created in realm [{}]", username, authMethod, realmId);
        return user;
    }

    @Override
    public void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, Collection<String> realmIds) {
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForRealms(realmIds);
        userRepository.removeAllByStateAndExpirationDateBefore(state, expirationDate, mongoTemplate, collectionNames);
    }

    @Override
    public List<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, Collection<String> realmIds) {
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForRealms(realmIds);
        return userRepository.findAllByStateAndExpirationDateBefore(state, expirationDate, mongoTemplate, collectionNames);
    }

    @Override
    public void addDefaultAuthorities(IUser user) {
        log.trace("Assigning default authorities to user [{}]", user.getUsername());
        if (user.getAuthorities().isEmpty()) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(authorityService.getOrCreate(Authority.user));
            user.setAuthorities(authorities);
            log.debug("Default authority [user] assigned to user [{}]", user.getUsername());
        } else {
            log.debug("User [{}] already has authorities, skipping default assignment", user.getUsername());
        }
    }

    @Override
    public void addDefaultRole(IUser user) {
        log.trace("Assigning default role to user [{}]", user.getUsername());
        user.addProcessRole(processRoleService.defaultRole());
    }

    @Override
    public void addAnonymousAuthorities(IUser user) {
        log.trace("Assigning anonymous authorities to user [{}]", user.getUsername());
        if (user.getAuthorities().isEmpty()) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(authorityService.getOrCreate(Authority.anonymous));
            user.setAuthorities(authorities);
            log.debug("Anonymous authority assigned to user [{}]", user.getUsername());
        } else {
            log.debug("User [{}] already has authorities, skipping anonymous assignment", user.getUsername());
        }
    }

    @Override
    public void addAllRolesToAdminByUsername(String username) {
        String collectionName = collectionNameProvider.getAdminRealmCollection();
        Optional<IUser> userOptional = userRepository.findByUsername(username, mongoTemplate, collectionName).map(user -> user);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Admin user with username [%s] cannot be found.".formatted(username));
        }
        IUser user = userOptional.get();
        user.setProcessRoles(new HashSet<>(processRoleService.findAll()));
        saveUser(user, user.getRealmId());
    }

    @Override
    public void addAnonymousRole(IUser user) {
        log.trace("Assigning anonymous role to user [{}]", user.getUsername());
        user.addProcessRole(processRoleService.anonymousRole());
    }

    @Override
    public IUser findById(String id, String realmId) {
        log.debug("Finding user by ID [{}]", id);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        Optional<User> userOpt = userRepository.findById(new ObjectId(id), mongoTemplate, collectionName);
        return userOpt.orElse(null);
//        return userOpt.orElseThrow(() -> new IllegalArgumentException("User with ID [" + id + "] not found"));  //TODO why?
    }

    @Override
    public void deleteUser(IUser user) {
        log.warn("Deleting user [{}]", user.getUsername());
        String collectionName = collectionNameProvider.getCollectionNameForRealm(user.getRealmId());
        mongoTemplate.remove(user, collectionName);
        log.info("User [{}] deleted from realm [{}]", user.getUsername(), user.getRealmId());
    }

    @Override
    public void deleteAllUsers(Collection<String> realmIds) {
        log.debug("Deleting all users");
        userRepository.deleteAll(mongoTemplate, collectionNameProvider.getCollectionNamesForRealms(realmIds));
    }

    @Override
    public IUser findByAuth(Authentication auth, String realmId) {
        return findByEmail(auth.getName(), realmId);
    }

    @Override
    public IUser update(IUser user, IUser updatedUser) {
        log.debug("Updating user with ID [{}]", user.getStringId());
        UserMapper userMapper = new UserMapperImpl();
        userMapper.update((User) user, (User) updatedUser);
        return saveUser(user, user.getRealmId());
    }

    @Override
    public IUser findByEmail(String email, String realmId) {
        log.debug("Finding user by email [{}]", email);
        Optional<User> userOpt = userRepository.findByEmail(email, mongoTemplate, collectionNameProvider.getCollectionNameForRealm(realmId));
        return userOpt.orElse(null);
    }

    @Override
    public Page<IUser> findAllCoMembers(com.netgrif.application.engine.objects.auth.domain.LoggedUser loggedUser, Pageable pageable) {
        Set<String> members = groupService.getAllCoMembers(loggedUser.getSelfOrImpersonated().transformToUser());
        members.add(loggedUser.getSelfOrImpersonated().getId());
        Set<ObjectId> objMembers = members.stream().map(ObjectId::new).collect(Collectors.toSet());
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForAllRealm();
        return changeType(userRepository.findAllByIdInAndState(objMembers, UserState.ACTIVE, pageable, mongoTemplate, collectionNames), pageable);
    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, com.netgrif.application.engine.objects.auth.domain.LoggedUser loggedUser, Pageable pageable) {
        Set<String> members = groupService.getAllCoMembers(loggedUser.getSelfOrImpersonated().transformToUser());
        members.add(loggedUser.getSelfOrImpersonated().getId());
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForAllRealm();
        return changeType(userRepository.findAll(buildPredicate(members.stream().map(ObjectId::new)
                .collect(Collectors.toSet()), query), pageable, mongoTemplate, collectionNames), pageable);

    }

    @Override
    public Page<IUser> searchAllCoMembers(String query, List<ProcessResourceId> roleIds, List<ProcessResourceId> negateRoleIds, com.netgrif.application.engine.objects.auth.domain.LoggedUser loggedUser, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty()))
            return searchAllCoMembers(query, loggedUser, pageable);

        if (negateRoleIds == null) {
            negateRoleIds = new ArrayList<>();
        }


        Set<String> members = groupService.getAllCoMembers(loggedUser.getSelfOrImpersonated().transformToUser());
        members.add(loggedUser.getSelfOrImpersonated().getId());
        BooleanExpression predicate = buildPredicate(members.stream().map(ObjectId::new).collect(Collectors.toSet()), query);
        if (!(roleIds == null || roleIds.isEmpty())) {
            predicate = predicate.and(QUser.user.processRoles.any()._id.in(roleIds));
        }
        predicate = predicate.and(QUser.user.processRoles.any()._id.in(negateRoleIds).not());
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForAllRealm();
        Page<User> users = userRepository.findAll(predicate, pageable, mongoTemplate, collectionNames);

        return changeType(users, pageable);
    }

    @Override
    public List<IUser> findAllByIds(Collection<String> ids, String realmId) {
        log.debug("Finding users by collection of IDs [{}]", ids);
        return ids.stream().map(id -> findById(id, realmId)).toList();
    }

    @Override
    public Page<IUser> findAllActiveByProcessRoles(Set<ProcessResourceId> roleIds, Pageable pageable, Collection<String> realmIds) {
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForRealms(realmIds);
        Page<User> users = userRepository.findDistinctByStateAndProcessRoles__idIn(
                UserState.ACTIVE,
                new ArrayList<>(roleIds),
                pageable,
                mongoTemplate,
                collectionNames
        );
        return changeType(users, pageable);
    }

    @Override
    public List<IUser> findAllByProcessRoles(Set<ProcessResourceId> roleIds, Collection<String> realmIds) {
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForRealms(realmIds);
        List<User> users = userRepository.findAllByProcessRoles__idIn(
                new ArrayList<>(roleIds),
                mongoTemplate,
                collectionNames
        );
        return changeType(users);
    }

    @Override
    public IUser assignAuthority(String userId, String realmId, String authorityId) {
        IUser user = findById(userId, realmId);
        Authority authority = authorityService.getOne(authorityId);
        user.addAuthority(authority);
        authority.addUser(user);
        return saveUser(user, realmId);
    }

    @Override
    public IUser getLoggedOrSystem() {
        try {
            if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String) {
                return getSystem();
            }
            return getLoggedUser();
        } catch (NullPointerException e) {
            return getSystem();
        }
    }

    @Override
    public IUser getLoggedUser() {
        com.netgrif.application.engine.objects.auth.domain.LoggedUser loggedUser = getLoggedUserFromContext();
        Optional<IUser> userOptional = findUserByUsername(loggedUser.getUsername(), loggedUser.getRealmId());
        IUser user = userOptional.orElseThrow(() -> new IllegalArgumentException("User with username [%s] in realm [%s] is not present in the system.".formatted(loggedUser.getUsername(), loggedUser.getRealmId())));
        if (loggedUser.isImpersonating()) {
            IUser impersonated = transformToUser((LoggedUserImpl) loggedUser.getImpersonated());
            Collection<ProcessResourceId> resourceIds = loggedUser.getImpersonated().getProcessRoles().stream().map(ProcessRole::get_id).toList();
            impersonated.setProcessRoles(new HashSet<>(processRoleService.findAllByIds(resourceIds)));
            user.setImpersonated(impersonated);
        }
        user.setWorkspaceId(loggedUser.getWorkspaceId());
        return user;
    }

    @Override
    public IUser getSystem() {
        if (systemUser == null) {
            systemUser = createSystemUser();
        }
        String workspaceId = WorkspaceContextHolder.getWorkspaceId() != null ? WorkspaceContextHolder.getWorkspaceId() : DefaultWorkspaceService.DEFAULT_WORKSPACE_ID;
        systemUser.setWorkspaceId(workspaceId);
        systemUser.setProcessRoles(new HashSet<>(processRoleService.findAllByWorkspaceId(workspaceId)));
        return systemUser;
    }

    @Override
    public com.netgrif.application.engine.objects.auth.domain.LoggedUser getLoggedUserFromContext() {
        return (com.netgrif.application.engine.objects.auth.domain.LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public IUser addRole(IUser user, ProcessResourceId roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        user.addProcessRole(role);
        return saveUser(user, user.getRealmId());
    }

    @Override
    public IUser addRole(IUser user, String roleString) {
        return this.addRole(user, new ProcessResourceId(roleString));
    }

    @Override
    public IUser removeRole(IUser user, ProcessRole role) {
        user.removeProcessRole(role);
        return saveUser(user, user.getRealmId());
    }

    @Override
    public IUser removeRole(IUser user, ProcessResourceId roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        return removeRole(user, role);
    }

    @Override
    public IUser removeRole(IUser user, String roleString) {
        return this.removeRole(user, new ProcessResourceId(roleString));
    }

    @Override
    public void removeRoleOfDeletedPetriNet(PetriNet petriNet, Collection<String> realmIds) {
        Set<String> collectionNames = collectionNameProvider.getCollectionNamesForRealms(realmIds);
        List<IUser> users = findAllByProcessRoles(petriNet.getRoles().values().stream().map(ProcessRole::get_id).collect(Collectors.toSet()), collectionNames);
        users.forEach(u -> {
            petriNet.getRoles().forEach((k, role) -> removeRole(u, role.get_id()));
        });
    }

    @Override
    public IUser createSystemUser() {
        User system = (User) findByEmail(UserConstants.SYSTEM_USER_EMAIL, null);
        if (system == null) {
            system = new com.netgrif.application.engine.adapter.spring.auth.domain.User();
            system.setUsername(UserConstants.SYSTEM_USER_EMAIL);
            system.setEmail(UserConstants.SYSTEM_USER_EMAIL);
            system.setPassword("n/a");
            system.setFirstName(UserConstants.SYSTEM_USER_NAME);
            system.setLastName(UserConstants.SYSTEM_USER_SURNAME);
            system.setState(UserState.ACTIVE);
            saveUser(system, system.getRealmId());
        }
        return system;
    }

    @Override
    public IUser transformToUser(LoggedUserImpl loggedUser) {
        User user = (User) findById(loggedUser.getId(), loggedUser.getRealmId());
        user.setUsername(loggedUser.getUsername());
        user.setEmail(loggedUser.getEmail());
        user.setFirstName(loggedUser.getFirstName());
        user.setLastName(loggedUser.getLastName());
        user.setState(UserState.ACTIVE);
        user.setRealmId(loggedUser.getRealmId());
        user.setProcessRoles(loggedUser.getProcessRoles());
        user.setGroups(loggedUser.getGroups());
        user.setAuthorities(loggedUser.getAuthoritySet());
        user.setAttributes(loggedUser.getAttributes());
        user.setWorkspaceId(loggedUser.getWorkspaceId());
        if (loggedUser.getImpersonated() != null) {
            user.setImpersonated(transformToUser((LoggedUserImpl) loggedUser.getImpersonated()));
        }
        return user;
    }

    @Override
    public LoggedUserImpl transformToLoggedUser(IUser user) {
        resolveRelatedAuthorities(user);
        resolveRelatedProcessRoles(user);
        String password = "";
        Set<String> mfaMethods = Set.of();
        if (user instanceof com.netgrif.application.engine.objects.auth.domain.User u) {
            password = u.getPassword();
            mfaMethods = u.getEnabledMFAMethods();
        } else if (!(user instanceof com.netgrif.application.engine.adapter.spring.auth.domain.AnonymousUser)) {
            throw new IllegalArgumentException("Unsupported user type: " + user.getClass().getSimpleName());
        }
        LoggedUserImpl loggedUser = new LoggedUserImpl(
                user.getStringId(),
                user.getUsername(),
                password,
                user.getAuthorities(),
                user.getProcessRoles()
        );
        loggedUser.setEmail(user.getEmail());
        loggedUser.setFirstName(user.getFirstName());
        loggedUser.setLastName(user.getLastName());
        loggedUser.setRealmId(user.getRealmId());
        loggedUser.setGroups(user.getGroups());
        loggedUser.setMfaMethod(mfaMethods);
        loggedUser.setWorkspaceId(user.getWorkspaceId());
        if (user.getImpersonated() != null) {
            loggedUser.setImpersonated(transformToLoggedUser(user.getImpersonated()));
        }
        return loggedUser;
    }

    @Override
    public IUser transformToUser(Author author) {
        return findById(author.getId(), author.getRealmId());
    }

    @Override
    public Author transformToAuthor(IUser user) {
        return new Author(user.getStringId(), user.getUsername(), user.getEmail(), user.getName(), user.getRealmId());
    }

    @Override
    public void populateGroups(IUser user) {
        user.getGroupIds().forEach(id -> {
            user.getGroups().add(groupService.findById(id));
        });

    }

    protected User initializeNewUser(String username, String email, String firstName, String lastName, String realmId) {
        log.trace("Initializing new user [{}] in realm [{}]", username, realmId);
        User user = new com.netgrif.application.engine.adapter.spring.auth.domain.User();
        user.setRealmId(realmId);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setState(UserState.ACTIVE);
        log.debug("User [{}] initialized in realm [{}]", username, realmId);
        return user;
    }

    protected void setPassword(User user, String password) {
        log.trace("Setting password for user [{}]", user.getUsername());
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        log.debug("Password set for user [{}]", user.getUsername());
    }

    protected void setDisablePassword(User user) {
        user.setPassword("N/A");
        log.debug("Password N/A set for user [{}]", user.getUsername());
    }

    private <T> Page<IUser> changeType(Page<T> users, Pageable pageable) {
        return new PageImpl<>(changeType(users.getContent()), pageable, users.getTotalElements());
    }

    private <T> List<IUser> changeType(List<T> users) {
        return users.stream().map(IUser.class::cast).collect(Collectors.toList());
    }

    private BooleanExpression buildPredicate(Set<ObjectId> members, String query) {
        BooleanExpression predicate = QUser.user
                .id.in(members)
                .and(QUser.user.state.eq(UserState.ACTIVE));
        for (String word : query.split(" ")) {
            predicate = predicate
                    .andAnyOf(QUser.user.email.containsIgnoreCase(word),
                            QUser.user.firstName.containsIgnoreCase(word),
                            QUser.user.lastName.containsIgnoreCase(word));
        }
        return predicate;
    }

    private void resolveRelatedAuthorities(IUser user) {
        user.getAuthorities().addAll(user.getGroups().stream().map(Group::getAuthorities).flatMap(Set::stream).collect(Collectors.toSet()));
    }

    private void resolveRelatedProcessRoles(IUser user) {
        user.getAuthorities().addAll(user.getGroups().stream().map(Group::getAuthorities).flatMap(Set::stream).collect(Collectors.toSet()));
    }

}
