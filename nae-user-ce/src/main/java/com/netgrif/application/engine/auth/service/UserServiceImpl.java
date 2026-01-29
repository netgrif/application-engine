package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.adapter.spring.utils.PaginationProperties;
import com.netgrif.application.engine.adapter.spring.workflow.service.FilterImportExportService;
import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.provider.CollectionNameProvider;
import com.netgrif.application.engine.auth.repository.UserRepository;
import com.netgrif.application.engine.auth.web.requestbodies.UpdateUserRequest;
import com.netgrif.application.engine.objects.auth.constants.UserConstants;
import com.netgrif.application.engine.objects.auth.domain.*;
import com.netgrif.application.engine.objects.auth.domain.enums.UserState;
import com.netgrif.application.engine.objects.auth.domain.enums.UserType;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private User systemUser;

    @Getter
    private PaginationProperties paginationProperties;

    private RealmService realmService;

    private static final String EMPTY_VALUE_STRING = "EMPTY_VALUE";

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
    public User saveUser(User user, String realmId) {
        user.setRealmId(realmId);
        return saveUser(user);
    }

    @Override
    public User saveUser(User user) {
        log.debug("Saving user [{}] in realm with id [{}]", user.getUsername(), user.getRealmId());
        user.setModifiedAt(LocalDateTime.now());
        String collectionName = collectionNameProvider.getCollectionNameForRealm(user.getRealmId());
        user.setType(resolveUserType(user.getEmail(), user.getRealmId()));
        user = userRepository.saveUser(user, mongoTemplate, collectionName);
        log.trace("User [{}] saved in collection [{}]", user.getUsername(), collectionName);
        return user;
    }

    @Override
    public List<User> saveUsers(Collection<User> users) {
        return users.stream().map(this::saveUser).toList();
    }

    @Override
    public Optional<User> findUserByUsername(String username, String realmId) {
        log.debug("Finding user by username [{}] in realm [{}]", username, realmId);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        Optional<User> userOpt = userRepository.findByUsername(username, mongoTemplate, collectionName);
        if (userOpt.isPresent()) {
            log.debug("User [{}] found in realm [{}]", username, realmId);
        } else {
            log.warn("User [{}] not found in realm [{}]", username, realmId);
        }
        return userOpt;
    }

    @Override
    public Page<User> findAllUsersByQuery(Query query, String realmName, Pageable pageable) {
        log.trace("Retrieving all users in realm [{}]", realmName);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmName);
        Page<User> users = userRepository.findAllByQuery(query, pageable, mongoTemplate, collectionName);
        log.debug("Found [{}] users in realm [{}]", users.getContent().size(), realmName);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<User> findAllUsers(String realmName, Pageable pageable) {
        log.trace("Retrieving all users in realm [{}]", realmName);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmName);
        Page<User> users = userRepository.findAllByQuery(new Query(), pageable, mongoTemplate, collectionName);
        log.debug("Found [{}] users in realm [{}]", users.getContent().size(), realmName);
        return changeType(users, users.getPageable());
    }

    @Override
    public User createUser(String username, String email, String firstName, String lastName, String rawPassword, String realmId) {
        User user = initializeNewUser(username, email, firstName, lastName, rawPassword, realmId);
        return createUser(user, realmId);
    }

    @Override
    public User createUser(User user, String realmId) {
        log.info("Creating user [{}] in realm [{}]", user.getUsername(), realmId);
        addDefaultAuthorities(user);
        addDefaultRole(user);
        setPassword(user, user.getPassword());
        user = this.saveUser(user, realmId);;

        filterImportExportService.createFilterImport(user);
        filterImportExportService.createFilterExport(user);

        if (groupConfigurationProperties.isDefaultEnabled())
            groupService.create(user);

        if (groupConfigurationProperties.isSystemEnabled())
            groupService.addUserToDefaultSystemGroup(user);

        user = this.saveUser(user, realmId);
        log.info("User [{}] successfully created in realm [{}]", user.getUsername(), realmId);
        return user;
    }

    protected UserType resolveUserType(String userMail, String realmId) {
        return UserType.INTERNAL;
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
            collectionNameProvider.getCollectionNamesForAllRealm().forEach(collectionName -> removeAllByStateAndExpirationDateBeforeFromCollection(state, expirationDate, collectionName));
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
    public User changePassword(User user, String newPassword, String oldPassword) {
        canUpdatePassword(user, newPassword);

        if (!verifyPasswords(user, oldPassword)) {
            throw new IllegalArgumentException("Old password does not match.");
        }

        log.debug("Setting password for user [{}]", user.getUsername());
        user.setPassword(passwordEncoder.encode(newPassword));
        return saveUser(user);
    }

    @Override
    public void addDefaultAuthorities(User user) {
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
    public void addDefaultRole(User user) {
        log.trace("Assigning default role to user [{}]", user.getUsername());
        user.addProcessRole(processRoleService.getDefaultRole());
        saveUser(user);
    }

    @Override
    public void addAnonymousAuthorities(User user) {
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
        Optional<User> userOptional = userRepository.findByUsername(username, mongoTemplate, collectionName).map(user -> user);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("Admin user with username [%s] cannot be found.".formatted(username));
        }
        User user = userOptional.get();

        Page<ProcessRole> processRoles = processRoleService.findAll(Pageable.unpaged());
        user.getProcessRoles().addAll(processRoles.getContent());

        saveUser(user, user.getRealmId());
    }

    @Override
    public void addAnonymousRole(User user) {
        log.trace("Assigning anonymous role to user [{}]", user.getUsername());
        user.addProcessRole(processRoleService.getAnonymousRole());
    }

    @Override
    public User findById(String id, String realmId) {
        log.debug("Finding user by ID [{}]", id);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        Optional<User> userOpt = userRepository.findById(new ObjectId(id), mongoTemplate, collectionName);
        return userOpt.orElse(null);
    }

    @Override
    public void deleteUser(User user) {
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
    public User findByAuth(Authentication auth, String realmId) {
        return findByEmail(auth.getName(), realmId);
    }

    @Override
    public User update(User user, User updatedUser) {
        log.debug("Updating user with ID [{}]", user.getStringId());
        UserMapper userMapper = new UserMapperImpl();
        userMapper.update(user, updatedUser);
        return saveUser(user);
    }

    @Override
    public User update(String userId, String realmId, UpdateUserRequest userUpdate) {
        if(userId == null) {
            log.warn("Cannot update user, userId is null");
            return null;
        }
        User user = findById(userId, realmId);
        if(user == null) {
            log.warn("User with id [{}] does not exist", userId);
            return null;
        }
        return this.update(user, userUpdate);
    }

    @Override
    public User update(User user, UpdateUserRequest userUpdate) {
        log.info("Updating user with id [{}]", user.getStringId());
        user.setAvatar(resolveUserUpdateValue(user.getAvatar(), userUpdate.getAvatar()));
        user.setFirstName(resolveUserUpdateValue(user.getFirstName(), userUpdate.getFirstName()));
        user.setMiddleName(resolveUserUpdateValue(user.getMiddleName(), userUpdate.getMiddleName()));
        user.setLastName(resolveUserUpdateValue(user.getLastName(), userUpdate.getLastName()));
        user.setEmail(resolveUserUpdateValue(user.getEmail(), userUpdate.getEmail()));
        if(userUpdate.getType() != null) {
            user.setType(userUpdate.getType());
        }
        return saveUser(user);
    }

    private String resolveUserUpdateValue(String oldValue, String newValue) {
        if(newValue != null) {
            return newValue.equals(EMPTY_VALUE_STRING) ? null : newValue;
        }
        return oldValue;
    }

    @Override
    public User findByEmail(String email, String realmId) {
        log.debug("Finding user by email [{}]", email);
        Optional<User> userOpt = userRepository.findByEmail(email, mongoTemplate, collectionNameProvider.getCollectionNameForRealm(realmId));
        return userOpt.orElse(null);
    }

    @Override
    public Page<User> findAllCoMembers(LoggedUser loggedUser, Pageable pageable) {
        return this.searchAllCoMembers(null, loggedUser, pageable);
    }

    @Override
    public Page<User> searchAllCoMembers(String query, LoggedUser loggedUser, Pageable pageable) {
//        User user = this.findById(loggedUser.getSelfOrImpersonated().getId(), loggedUser.getSelfOrImpersonated().getRealmId());
        // TODO: impersonation
        User user = this.findById(loggedUser.getStringId(), loggedUser.getRealmId());
        BooleanExpression predicate = buildPredicate(user, query);
        String collectionName = collectionNameProvider.getCollectionNameForRealm(loggedUser.getRealmId());
        Page<User> users = userRepository.findAllByQuery(predicate, pageable, mongoTemplate, collectionName);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<User> searchAllCoMembers(String query, Collection<ProcessResourceId> roleIds, Collection<ProcessResourceId> negateRoleIds, LoggedUser loggedUser, Pageable pageable) {
        if ((roleIds == null || roleIds.isEmpty()) && (negateRoleIds == null || negateRoleIds.isEmpty())) {
            return searchAllCoMembers(query, loggedUser, pageable);
        }

//        User user = this.findById(loggedUser.getSelfOrImpersonated().getId(), loggedUser.getSelfOrImpersonated().getRealmId());
        // TODO: impersonation
        User user = this.findById(loggedUser.getStringId(), loggedUser.getRealmId());
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
    public Page<User> findAllByIds(Collection<String> ids, String realmId, Pageable pageable) {
        log.debug("Finding users by collection of IDs [{}]", ids);
        String collection = collectionNameProvider.getCollectionNameForRealm(realmId);
        Page<User> users = userRepository.findAllByIds(ids.stream().map(ObjectId::new).toList(), pageable, mongoTemplate, collection);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<User> findAllActiveByProcessRoles(Collection<ProcessResourceId> roleIds, Pageable pageable, String realmId) {
        String collection = collectionNameProvider.getCollectionNameForRealm(realmId);
        Page<User> users = userRepository.findDistinctByStateAndProcessRoles__idIn(UserState.ACTIVE, roleIds, pageable, mongoTemplate, collection);
        return changeType(users, users.getPageable());
    }

    @Override
    public Page<User> findAllByProcessRoles(Collection<ProcessResourceId> roleIds, String realmId, Pageable pageable) {
        String collectionName = collectionNameProvider.getCollectionNameForRealm(realmId);
        return searchUsersByRoleIds(roleIds, collectionName, pageable);
    }

    protected Page<User> searchUsersByRoleIds(Collection<ProcessResourceId> roleIds, String collectionName, Pageable pageable) {
        Page<User> users = userRepository.findAllByProcessRoles__idIn(roleIds, pageable, mongoTemplate, collectionName);
        return changeType(users, users.getPageable());
    }

    @Override
    public User assignAuthority(String userId, String realmId, String authorityId) {
        User user = findById(userId, realmId);
        Authority authority = authorityService.getOne(authorityId);
        user.addAuthority(authority);
        return saveUser(user, realmId);
    }

    @Override
    public User getLoggedOrSystem() {
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
    public User getLoggedUser() {
        LoggedUser loggedUser = getLoggedUserFromContext();
        Optional<User> userOptional = findUserByUsername(loggedUser.getUsername(), loggedUser.getRealmId());
        User user = userOptional.orElseThrow(() -> new IllegalArgumentException("User with username [%s] in realm [%s] is not present in the system.".formatted(loggedUser.getUsername(), loggedUser.getRealmId())));
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
    public User getSystem() {
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
    public User addRole(User user, ProcessResourceId roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        user.addProcessRole(role);
        return saveUser(user, user.getRealmId());
    }

    @Override
    public User addRole(User user, String roleString) {
        return this.addRole(user, new ProcessResourceId(roleString));
    }

    @Override
    public User removeRolesById(User user, Collection<ProcessResourceId> processRolesIds) {
        Set<ProcessRole> processRoles = new HashSet<>(processRoleService.findAllByIds(processRolesIds));
        return removeRoles(user, processRoles);
    }

    @Override
    public User removeRoles(User user, Collection<ProcessRole> processRoles) {
        processRoles.forEach(user::removeProcessRole);
        return saveUser(user);
    }

    @Override
    public User removeRole(User user, ProcessRole role) {
        user.removeProcessRole(role);
        return saveUser(user);
    }

    @Override
    public User removeRole(User user, ProcessResourceId roleStringId) {
        ProcessRole role = processRoleService.findById(roleStringId);
        return removeRole(user, role);
    }

    @Override
    public User removeRole(User user, String roleString) {
        return this.removeRole(user, new ProcessResourceId(roleString));
    }

    @Override
    public void removeRoleOfDeletedPetriNet(PetriNet petriNet) {
        removeRoleOfDeletedPetriNet(new HashSet<>(petriNet.getRoles().values()));
    }

    @Override
    public void removeRoleOfDeletedPetriNet(Set<ProcessRole> petriNetRoles) {
        String defaultRealmCollection = collectionNameProvider.getDefaultRealmCollection();
        Pageable pageable = PageRequest.of(0, paginationProperties.getBackendPageSize());
        Collection<ProcessResourceId> roleIds = petriNetRoles.stream().map(ProcessRole::get_id).collect(Collectors.toSet());
        Page<User> users;
        do {
            users = searchUsersByRoleIds(roleIds, defaultRealmCollection, pageable);
            users.getContent().forEach(u -> removeRoles(u, petriNetRoles));
            pageable = pageable.next();
        } while (users.hasNext());
    }

    @Override
    public User createSystemUser() {
        User system = findByEmail(UserConstants.SYSTEM_USER_EMAIL, null);
        if (system == null) {
            system = new User();
            system.setUsername(UserConstants.SYSTEM_USER_EMAIL);
            system.setEmail(UserConstants.SYSTEM_USER_EMAIL);
            system.setPassword("n/a");
            system.setFirstName(UserConstants.SYSTEM_USER_NAME);
            system.setLastName(UserConstants.SYSTEM_USER_SURNAME);
            system.setState(UserState.ACTIVE);
            system.setType(UserType.SYSTEM);
            saveUser(system);
        }
        return system;
    }

    @Override
    public User transformToUser(ActorRef author) {
        return findById(author.getId(), author.getRealmId());
    }

    @Override
    public User transformToUser(LoggedUser loggedUser) {
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

    private <T> Page<User> changeType(Page<T> users, Pageable pageable) {
        return new PageImpl<>(changeType(new LinkedHashSet<>(users.getContent())), pageable, users.getTotalElements());
    }

    private <T> List<User> changeType(Collection<T> users) {
        return users.stream().map(User.class::cast).toList();
    }

    private BooleanExpression buildPredicate(User user, String query) {
        User system = this.getSystem();
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

    private void resolveRelatedAuthorities(User user) {
        user.getAuthoritySet().addAll(getUserGroups(user).stream().map(Group::getAuthoritySet).flatMap(Set::stream).collect(Collectors.toSet()));
    }

    private void resolveRelatedProcessRoles(User user) {
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
