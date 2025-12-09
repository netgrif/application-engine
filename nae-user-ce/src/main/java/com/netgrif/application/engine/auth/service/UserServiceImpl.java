package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.adapter.spring.workflow.service.FilterImportExportService;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.UserRepository;
import com.netgrif.application.engine.objects.auth.constants.UserConstants;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
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

    private AbstractUser systemUser;

    private RealmService realmService;

    @Getter
    private PaginationProperties paginationProperties;

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

    @Lazy
    @Autowired
    public void setPaginationProperties(PaginationProperties paginationProperties) {
        this.paginationProperties = paginationProperties;
    }

    @Autowired
    public void setRealmService(RealmService realmService) {
        this.realmService = realmService;
    }

    @Override
    public AbstractUser saveUser(AbstractUser user, String realmId) {
        user.setRealmId(realmId);
        return saveUser(user);
    }

    @Override
    public AbstractUser saveUser(AbstractUser user) {
        log.debug("Saving user [{}] in realm with id [{}]", user.getUsername(), user.getRealmId());
        if (user instanceof User u) {
            u.setModifiedAt(LocalDateTime.now());
        }
        String collectionName = collectionNameProvider.getCollectionNameForRealm(user.getRealmId());
        user = userRepository.saveUser((User) user, mongoTemplate, collectionName);
        log.trace("User [{}] saved in collection [{}]", user.getUsername(), collectionName);
        return user;
    }

    @Override
    public List<User> saveUsers(Collection<AbstractUser> users) {
        return users.stream().map(u -> (User) this.saveUser(u)).collect(Collectors.toList());
    }

    @Override
    public Optional<AbstractUser> findUserByUsername(String username, String realmId) {
        log.debug("Finding user by username [{}] in realm [{}]", username, realmId);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        Optional<AbstractUser> userOpt = userRepository.findByUsername(username, mongoTemplate, collectionName).map(user -> (AbstractUser) user);
        if (userOpt.isPresent()) {
            log.debug("User [{}] found in realm [{}]", username, realmId);
        } else {
            log.warn("User [{}] not found in realm [{}]", username, realmId);
        }
        return userOpt;
    }

    @Override
    public Page<AbstractUser> findAllUsersByQuery(Query query, String realmName, Pageable pageable) {
        log.trace("Retrieving all users in realm [{}]", realmName);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmName);
        Page<User> users = userRepository.findAllByQuery(query, pageable, mongoTemplate, collectionName);
        log.debug("Found [{}] users in realm [{}]", users.getContent().size(), realmName);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<AbstractUser> findAllUsers(String realmName, Pageable pageable) {
        log.trace("Retrieving all users in realm [{}]", realmName);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmName);
        Page<User> users = userRepository.findAllByQuery(new Query(), pageable, mongoTemplate, collectionName);
        log.debug("Found [{}] users in realm [{}]", users.getContent().size(), realmName);
        return changeType(users, users.getPageable());
    }

    @Override
    public AbstractUser createUser(String username, String email, String firstName, String lastName, String rawPassword, String realmId) {
        User user = initializeNewUser(username, email, firstName, lastName, rawPassword, realmId);
        return createUser(user, realmId);
    }

    @Override
    public AbstractUser createUser(AbstractUser user, String realmId) {
        log.info("Creating user [{}] in realm [{}]", user.getUsername(), realmId);
        addDefaultAuthorities(user);
        addDefaultRole(user);
        setPassword(user, user.getPassword());

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

    // TODO JOFO: auth methods no longer exists ... use credentials?
    @Override
    public User createUserFromThirdParty(String username, String email, String firstName, String lastName, String realmId, String authMethod) {
        log.info("Creating user [{}] from third-party auth [{}] in realm [{}] without password", username, authMethod, realmId);
        User user = initializeNewUser(username, email, firstName, lastName, "N/A", realmId);
        addDefaultAuthorities(user);
        addDefaultRole(user);
        setDisablePassword(user);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        userRepository.saveUser(user, mongoTemplate, collectionName);
        log.info("User [{}] from third-party [{}] successfully created in realm [{}]", username, realmId);
        return user;
    }

    @Override
    public void removeAllByStateAndExpirationDateBeforeForRealms(UserState state, LocalDateTime expirationDate, Collection<String> realmIds) {
        // TODO: delete whole group or change owner of group?
        if (realmIds == null || realmIds.isEmpty()) {
            collectionNameProvider.getCollectionNamesForAllRealm().forEach(collectionName -> {
                removeAllByStateAndExpirationDateBeforeFromCollection(state, expirationDate, collectionName);
            });
        } else {
            realmIds.forEach(realmId -> removeAllByStateAndExpirationDateBefore(state, expirationDate, realmId));
        }
    }

    @Override
    public void removeAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, String realmId) {
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        removeAllByStateAndExpirationDateBeforeFromCollection(state, expirationDate, collectionName);
    }

    private void removeAllByStateAndExpirationDateBeforeFromCollection(UserState state, LocalDateTime expirationDate, String collectionName) {
        Pageable pageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Page<User> users;
        do {
            users = userRepository.findAllByStateAndExpirationDateBefore(state, expirationDate, pageable, mongoTemplate, collectionName);
            Set<String> userIds = users.getContent().stream().map(AbstractActor::getStringId).collect(Collectors.toSet());
            Pageable groupsPageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
            Page<Group> groups;
            do { // TODO refactor because this iterates all groups multiple times :(
                groups = groupService.findAllFromRealm(collectionNameProvider.getRealmIdFromCollectionName(collectionName), groupsPageable);
                groups.forEach(group -> {
                    group.getMemberIds().removeAll(userIds);
                    groupService.save(group);
                });
                groupsPageable = groupsPageable.next();
            } while (groups.hasNext());
            pageable = pageable.next();
        } while (users.hasNext());
        userRepository.removeAllByStateAndExpirationDateBefore(state, expirationDate, mongoTemplate, Set.of(collectionName));
    }

    @Override
    public Page<User> findAllByStateAndExpirationDateBefore(UserState state, LocalDateTime expirationDate, String realmId, Pageable pageable) {
        String collection = collectionNameProvider.getCollectionNameForRealm(realmId);
        return userRepository.findAllByStateAndExpirationDateBefore(state, expirationDate, pageable, mongoTemplate, collection);
    }

    @Override
    public List<Group> getUserGroups(AbstractActor actor) {
        return groupService.findAllByIds(actor.getGroupIds(), Pageable.unpaged()).stream().toList();
    }

    @Override
    public AbstractUser changePassword(AbstractUser user, String newPassword, String oldPassword) {
        canUpdatePassword(user, newPassword);

        if (!verifyPasswords(user, oldPassword)) {
            throw new IllegalArgumentException("Old password does not match.");
        }

        log.debug("Setting password for user [{}]", user.getUsername());
        user.setPassword(passwordEncoder.encode(newPassword));
        return saveUser(user);
    }

    @Override
    public void addDefaultAuthorities(AbstractUser user) {
        log.trace("Assigning default authorities to user [{}]", user.getUsername());
        if (user.getAuthoritySet().isEmpty()) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(authorityService.getOrCreate(Authority.user));
            user.setAuthoritySet(authorities);
            log.debug("Default authority [user] assigned to user [{}]", user.getUsername());
        } else {
            log.debug("User [{}] already has authorities, skipping default assignment", user.getUsername());
        }
    }

    @Override
    public void addDefaultRole(AbstractUser user) {
        log.trace("Assigning default role to user [{}]", user.getUsername());
        user.addProcessRole(processRoleService.getDefaultRole());
        saveUser(user);
    }

    @Override
    public void addAnonymousAuthorities(AbstractUser user) {
        log.trace("Assigning anonymous authorities to user [{}]", user.getUsername());
        if (user.getAuthoritySet().isEmpty()) {
            Set<Authority> authorities = new HashSet<>();
            authorities.add(authorityService.getOrCreate(Authority.anonymous));
            user.setAuthoritySet(authorities);
            log.debug("Anonymous authority assigned to user [{}]", user.getUsername());
        } else {
            log.debug("User [{}] already has authorities, skipping anonymous assignment", user.getUsername());
        }
    }

    @Override
    public void addAllRolesToAdminByUsername(String username) {
        String collectionName = collectionNameProvider.getAdminRealmCollection();
        Optional<AbstractUser> userOptional = userRepository.findByUsername(username, mongoTemplate, collectionName).map(user -> user);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Admin user with username [%s] cannot be found.".formatted(username));
        }
        AbstractUser user = userOptional.get();

        Page<ProcessRole> processRoles = processRoleService.findAll(Pageable.unpaged());
        user.getProcessRoles().addAll(processRoles.getContent());

        saveUser(user, user.getRealmId());
    }

    @Override
    public void addAnonymousRole(AbstractUser user) {
        log.trace("Assigning anonymous role to user [{}]", user.getUsername());
        user.addProcessRole(processRoleService.getAnonymousRole());
    }

    @Override
    public AbstractUser findById(String id, String realmId) {
        log.debug("Finding user by ID [{}]", id);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        Optional<User> userOpt = userRepository.findById(new ObjectId(id), mongoTemplate, collectionName);
        return userOpt.orElse(null);
    }

    @Override
    public void deleteUser(AbstractUser user) {
        log.warn("Deleting user [{}]", user.getUsername());
        String collectionName = collectionNameProvider.getCollectionNameForRealm(user.getRealmId());
        groupService.findAllByIds(user.getGroupIds(), Pageable.unpaged()).forEach(group -> {
            group.removeMemberId(user.getStringId());
            groupService.save(group);
        });
        mongoTemplate.remove(user, collectionName);
        log.info("User [{}] deleted from realm [{}]", user.getUsername(), user.getRealmId());
    }

    @Override
    public void deleteAllUsers(Collection<String> realmIds) {
        log.debug("Deleting all users in realms [{}]", realmIds);
        groupService.removeAllByRealmIdIn(realmIds);
        userRepository.deleteAll(mongoTemplate, collectionNameProvider.getCollectionNamesForRealms(realmIds));
    }

    @Override
    public void deleteAllUsers() {
        log.debug("Deleting all users from all realms");
        groupService.removeAllGroups();
        userRepository.deleteAll(mongoTemplate, collectionNameProvider.getCollectionNamesForAllRealm());
    }

    @Override
    public AbstractUser findByAuth(Authentication auth, String realmId) {
        return findByEmail(auth.getName(), realmId);
    }

    @Override
    public AbstractUser update(AbstractUser user, AbstractUser updatedUser) {
        log.debug("Updating user with ID [{}]", user.getStringId());
        UserMapper userMapper = new UserMapperImpl();
        userMapper.update((User) user, (User) updatedUser);
        return saveUser(user);
    }

    @Override
    public AbstractUser findByEmail(String email, String realmId) {
        log.debug("Finding user by email [{}]", email);
        Optional<User> userOpt = userRepository.findByEmail(email, mongoTemplate, collectionNameProvider.getCollectionNameForRealm(realmId));
        return userOpt.orElse(null);
    }

    @Override
    public Page<AbstractUser> findAllCoMembers(LoggedUser loggedUser, Pageable pageable) {
        return this.searchAllCoMembers(null, loggedUser, pageable);
    }

    @Override
    public Page<AbstractUser> searchAllCoMembers(String query, LoggedUser loggedUser, Pageable pageable) {
//        AbstractUser user = this.findById(loggedUser.getSelfOrImpersonated().getId(), loggedUser.getSelfOrImpersonated().getRealmId());
        // TODO: impersonation
        AbstractUser user = this.findById(loggedUser.getStringId(), loggedUser.getRealmId());
        BooleanExpression predicate = buildPredicate(user, query);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(loggedUser.getRealmId());
        Page<User> users = userRepository.findAllByQuery(predicate, pageable, mongoTemplate, collectionName);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<AbstractUser> searchAllCoMembers(String query, Collection<ProcessResourceId> roleIds, Collection<ProcessResourceId> negateRoleIds, LoggedUser loggedUser, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty())) {
            return searchAllCoMembers(query, loggedUser, pageable);
        }

//        AbstractUser user = this.findById(loggedUser.getSelfOrImpersonated().getId(), loggedUser.getSelfOrImpersonated().getRealmId());
        // TODO: impersonation
        AbstractUser user = this.findById(loggedUser.getStringId(), loggedUser.getRealmId());
        BooleanExpression predicate = buildPredicate(user, query);
        if (roleIds != null && !roleIds.isEmpty()) {
            predicate = predicate.and(QUser.user.processRoles.any()._id.in(roleIds));
        }
        if (negateRoleIds != null && !negateRoleIds.isEmpty()) {
            predicate = predicate.and(QUser.user.processRoles.any()._id.in(negateRoleIds).not());
        }

        String collectionName = collectionNameProvider.getCollectionNameForRealm(loggedUser.getRealmId());
        Page<User> users = userRepository.findAllByQuery(predicate, pageable, mongoTemplate, collectionName);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<AbstractUser> findAllByIds(Collection<String> ids, String realmId, Pageable pageable) {
        log.debug("Finding users by collection of IDs [{}]", ids);
        String collection = collectionNameProvider.getCollectionNameForRealm(realmId);
        Page<User> users = userRepository.findAllByIds(ids.stream().map(ObjectId::new).toList(), pageable, mongoTemplate, collection);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<AbstractUser> findAllActiveByProcessRoles(Collection<ProcessResourceId> roleIds, Pageable pageable, String realmId) {
        String collection = collectionNameProvider.getCollectionNameForRealm(realmId);
        Page<User> users = userRepository.findDistinctByStateAndProcessRoles__idIn(UserState.ACTIVE, roleIds, pageable, mongoTemplate, collection);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<AbstractUser> findAllByProcessRoles(Collection<ProcessResourceId> roleIds, String realmId, Pageable pageable) {
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        return searchUsersByRoleIds(roleIds, collectionName, pageable);
    }

    protected Page<AbstractUser> searchUsersByRoleIds(Collection<ProcessResourceId> roleIds, String collectionName, Pageable pageable) {
        Page<User> users = userRepository.findAllByProcessRoles__idIn(roleIds, pageable, mongoTemplate, collectionName);
        return changeType(users, users.getPageable());
    }

    @Override
    public AbstractUser assignAuthority(String userId, String realmId, String authorityId) {
        AbstractUser user = findById(userId, realmId);
        Authority authority = authorityService.getOne(authorityId);
        user.addAuthority(authority);
        return saveUser(user, realmId);
    }

    @Override
    public AbstractUser getLoggedOrSystem() {
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
    public AbstractUser getLoggedUser() {
        LoggedUser loggedUser = getLoggedUserFromContext();
        Optional<AbstractUser> userOptional = findUserByUsername(loggedUser.getUsername(), loggedUser.getRealmId());
        AbstractUser user = userOptional.orElseThrow(() -> new IllegalArgumentException("User with username [%s] in realm [%s] is not present in the system.".formatted(loggedUser.getUsername(), loggedUser.getRealmId())));
        // TODO: impersonation
//        if (loggedUser.isImpersonating()) {
//            IUser impersonated = transformToUser((LoggedUserImpl) loggedUser.getImpersonated());
//            Collection<ProcessResourceId> resourceIds = loggedUser.getImpersonated().getProcessRoles().stream().map(ProcessRole::get_id).toList();
//            impersonated.setProcessRoles(new HashSet<>(processRoleService.findAllByIds(resourceIds)));
//            user.setImpersonated(impersonated);
//        }
        return user;
    }

    @Override
    public AbstractUser getSystem() {
        if (systemUser == null) {
            systemUser = createSystemUser();
        }
        systemUser.setProcessRoles(new HashSet<>(processRoleService.findAll(Pageable.unpaged()).getContent()));
        return systemUser;
    }

    @Override
    public LoggedUser getLoggedUserFromContext() {
        return (LoggedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @Override
    public AbstractUser addRole(AbstractUser user, ProcessResourceId roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        user.addProcessRole(role);
        return saveUser(user, user.getRealmId());
    }

    @Override
    public AbstractUser addRole(AbstractUser user, String roleString) {
        return this.addRole(user, new ProcessResourceId(roleString));
    }

    @Override
    public AbstractUser removeRolesById(AbstractUser user, Collection<ProcessResourceId> processRolesIds) {
        Set<ProcessRole> processRoles = new HashSet<>(processRoleService.findAllByIds(processRolesIds));
        return removeRoles(user, processRoles);
    }

    @Override
    public AbstractUser removeRoles(AbstractUser user, Collection<ProcessRole> processRoles) {
        processRoles.forEach(user::removeProcessRole);
        return saveUser(user);
    }

    @Override
    public AbstractUser removeRole(AbstractUser user, ProcessRole role) {
        user.removeProcessRole(role);
        return saveUser(user);
    }

    @Override
    public AbstractUser removeRole(AbstractUser user, ProcessResourceId roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        return removeRole(user, role);
    }

    @Override
    public AbstractUser removeRole(AbstractUser user, String roleString) {
        return this.removeRole(user, new ProcessResourceId(roleString));
    }

    @Override
    public void removeRoleOfDeletedPetriNet(PetriNet petriNet) {
        removeRoleOfDeletedPetriNet(new HashSet<>(petriNet.getRoles().values()));
    }

    @Override
    public void removeRoleOfDeletedPetriNet(Set<ProcessRole> petriNetRoles) {
        Set<ProcessRole> nonGlobalPetriNetRoles = petriNetRoles.stream().filter(r -> !r.isGlobal()).collect(Collectors.toSet());
        Collection<ProcessResourceId> roleIds = nonGlobalPetriNetRoles.stream().map(ProcessRole::get_id).collect(Collectors.toSet());
        Pageable realmPageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Pageable pageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Page<Realm> realms;
        do {
            realms = realmService.getSmallRealm(realmPageable);
            for (Realm realm : realms.getContent()) {
                Page<AbstractUser> users = searchUsersByRoleIds(roleIds, collectionNameProvider.getCollectionNameForRealm(realm.getName()), pageable);
                while (users.hasContent()) {
                    users.getContent().forEach(u -> removeRoles(u, nonGlobalPetriNetRoles));
                    users = searchUsersByRoleIds(roleIds, collectionNameProvider.getCollectionNameForRealm(realm.getName()), pageable);
                }
            }
            realmPageable = realmPageable.next();
        } while (realms.hasNext());
    }

    @Override
    public AbstractUser createSystemUser() {
        User system = (User) findByEmail(UserConstants.SYSTEM_USER_EMAIL, null);
        if (system == null) {
            system = new User();
            system.setUsername(UserConstants.SYSTEM_USER_EMAIL);
            system.setEmail(UserConstants.SYSTEM_USER_EMAIL);
            system.setPassword("n/a");
            system.setFirstName(UserConstants.SYSTEM_USER_NAME);
            system.setLastName(UserConstants.SYSTEM_USER_SURNAME);
            system.setState(UserState.ACTIVE);
            saveUser(system);
        }
        return system;
    }

    @Override
    public AbstractUser transformToUser(ActorRef author) {
        return findById(author.getId(), author.getRealmId());
    }

    @Override
    public AbstractUser transformToUser(LoggedUser loggedUser) {
        return findById(loggedUser.getStringId(), loggedUser.getRealmId());
    }

    @Override
    public void updateAdminWithRoles(Collection<ProcessRole> roles) {
        log.info("Assigning [{}] roles to admin user(s)", roles != null ? roles.size() : 0);
        User admin = (User) findByEmail(UserConstants.ADMIN_USER_EMAIL, null);
        admin.setProcessRoles(new HashSet<>(roles));
        saveUser(admin);
        log.debug("Admin [{}] now has [{}] process roles", admin.getUsername(), admin.getProcessRoles().size());
    }


    /**
     * Searches for users in the specified realm based on the provided predicate and pagination parameters.
     *
     * @param predicate the query conditions to filter users
     * @param pageable  the pagination parameters for the search results
     * @param realmId   the name of the realm, used to determine which collection to query
     * @return a paginated list of users matching the predicate within the specified realm
     */
    @Override
    public Page<User> search(Predicate predicate, Pageable pageable, String realmId) {
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        return userRepository.findAllByQuery(predicate, pageable, mongoTemplate, collectionName);
    } 

    protected User initializeNewUser(String username, String email, String firstName, String lastName, String password, String realmId) {
        log.trace("Initializing new user [{}] in realm [{}]", username, realmId);
        User user = new User();
        user.setRealmId(realmId);
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmailVerified(false);
        user.setCreatedAt(LocalDateTime.now());
        user.setState(UserState.ACTIVE);
        user.setPassword(password);
        log.debug("User [{}] initialized in realm [{}]", username, realmId);
        return user;
    }

    protected void setPassword(AbstractUser user, String password) {
        log.trace("Setting password for user [{}]", user.getUsername());
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        log.debug("Password set for user [{}]", user.getUsername());
    }

    protected void setDisablePassword(AbstractUser user) {
        user.setPassword("N/A");
        log.debug("Password N/A set for user [{}]", user.getUsername());
    }

    private <T> Page<AbstractUser> changeType(Page<T> users, Pageable pageable) {
        return new PageImpl<>(changeType(new LinkedHashSet<>(users.getContent())), pageable, users.getTotalElements());
    }

    private <T> List<AbstractUser> changeType(Collection<T> users) {
        return users.stream().map(AbstractUser.class::cast).toList();
    }

    private BooleanExpression buildPredicate(AbstractUser user, String query) {
        AbstractUser system = this.getSystem();
        BooleanExpression predicate = QUser.user
                .groupIds.any().in(user.getGroupIds())
                .and(QUser.user.id.ne(new ObjectId(system.getStringId())))
                .and(QUser.user.state.eq(UserState.ACTIVE));
        if (query == null) {
            return predicate;
        }
        for (String word : query.split(" ")) {
            predicate = predicate
                    .andAnyOf(QUser.user.email.containsIgnoreCase(word),
                            QUser.user.firstName.containsIgnoreCase(word),
                            QUser.user.lastName.containsIgnoreCase(word));
        }
        return predicate;
    }

    private void resolveRelatedAuthorities(AbstractUser user) {
        user.getAuthoritySet().addAll(getUserGroups(user).stream().map(Group::getAuthoritySet).flatMap(Set::stream).collect(Collectors.toSet()));
    }

    private void resolveRelatedProcessRoles(AbstractUser user) {
        user.getAuthoritySet().addAll(getUserGroups(user).stream().map(Group::getAuthoritySet).flatMap(Set::stream).collect(Collectors.toSet()));
    }

    private boolean verifyPasswords(AbstractUser user, String password) {
        if (password == null) {
            throw new IllegalArgumentException("confirmation password is not set");
        }

        log.trace("Verifying password for user [{}]", user.getUsername());
        return passwordEncoder.matches(password, user.getPassword());
    }

    protected void canUpdatePassword(AbstractUser user, String password) {
        if (!user.isCredentialEnabled("password")) {
            throw new RuntimeException("Password does not exists or authorization is not enabled");
        }

        if (password == null) {
            throw new IllegalArgumentException("Password is not set");
        }
    }
}
