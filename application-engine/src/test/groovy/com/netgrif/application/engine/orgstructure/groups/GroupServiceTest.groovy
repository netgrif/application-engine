package com.netgrif.application.engine.orgstructure.groups

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.adapter.spring.auth.domain.User
import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService
import com.netgrif.application.engine.auth.service.AuthorityService
import com.netgrif.application.engine.auth.service.GroupService
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.objects.auth.constants.UserConstants
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.Group
import com.netgrif.application.engine.objects.auth.domain.QGroup
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.runner.GroupRunner
import com.netgrif.application.engine.utils.FullPageRequest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.util.Pair
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class GroupServiceTest {

    public static final String DUMMY_USER_MAIL = "dummy@netgrif.com"
    public static final String CUSTOMER_USER_MAIL = "customer@netgrif.com"

    @Autowired
    GroupService groupService

    @Autowired
    GroupRunner groupRunner

    @Autowired
    UserService userService

    @Autowired
    ProcessRoleService processRoleService

    @Autowired
    ImportHelper importHelper

    @Autowired
    IPetriNetService petriNetService

    @Autowired
    TestHelper testHelper

    @Autowired
    AuthorityService authorityService

    @Autowired
    ActionDelegate actionDelegate

    AbstractUser dummy, customer

    @BeforeEach
    void init() {
        testHelper.truncateDbs()
        def auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])
        dummy = importHelper.createUser(new User(firstName: "Dummy", lastName: "User", email: DUMMY_USER_MAIL, username: DUMMY_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])
        customer = importHelper.createUser(new User(firstName: "Customer", lastName: "User", email: CUSTOMER_USER_MAIL, username: CUSTOMER_USER_MAIL, password: "password", state: UserState.ACTIVE),
                [auths.get("user")] as Authority[],
                [] as ProcessRole[])

    }

    private void setSecurityContext(AbstractUser user) {
        def loggedUser = ActorTransformer.toLoggedUser(user)
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUser, null, loggedUser.authoritySet as Set<AuthorityImpl>))
    }

    // ==================== GroupService Tests ====================

    @Test
    void createGroup() {
        groupService.create("CUSTOM_GROUP_1", "CUSTOM_GROUP_1", userService.findUserByUsername(DUMMY_USER_MAIL, null).get())
        Optional<Group> groupOpt = groupService.findByIdentifier("CUSTOM_GROUP_1")
        assert groupOpt.isPresent()

    }

    @Test
    void findGroup() {
        QGroup qGroup = new QGroup("group")
        Page<Group> groupPage = groupService.findByPredicate(qGroup.ownerUsername.eq(DUMMY_USER_MAIL), Pageable.ofSize(1))
        assert !groupPage.isEmpty()
    }

    @Test
    void addAndRemoveUser() {
        QGroup qGroup = new QGroup("group")
        Group group = groupService.findByPredicate(qGroup.identifier.eq(DUMMY_USER_MAIL), new FullPageRequest()).getContent().get(0)
        group = groupService.addUser(group, userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get())
        assert group.getMemberIds().size() == 2
        group = groupService.removeUser(group, userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get())
        assert group.getMemberIds().size() == 1
    }

    @Test
    void addAndRemoveRole() {
        FileInputStream file = new FileInputStream("src/test/resources/simple_role.xml");
        ImportPetriNetEventOutcome netWithRoleOutcome = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(file)
                .releaseType(VersionType.MAJOR)
                .author(userService.getSystem())
                .build())
        file.close()
        ProcessRole role = netWithRoleOutcome.getNet().getRoles().values().find { it.importId == "simple_role"}
        Group group = groupService.create("addAndRemoveRole", "Add and remove role test group", dummy)
        group = groupService.addRole(group.getStringId(), role.getStringId())
        assert group.getProcessRoles().any {it.getStringId() == role.getStringId()}
        group = groupService.removeRole(group.getStringId(), role.getStringId())
        assert !group.getProcessRoles().any() {it.getStringId() == role.getStringId()}
    }

    @Test
    void addAndRemoveSubgroup() {
        Group group = groupService.findByIdentifier(dummy.getUsername()).orElse(null)
        assert group != null
        Group subGroup = groupService.create("addAndRemoveSubgroup", "Add and remove role test group", dummy)

        Pair<Group, Group> groupPair = groupService.addSubgroup(group.getStringId(), subGroup.getStringId())
        assert groupPair.getFirst().getSubgroupIds().contains(subGroup.getStringId())
        assert groupPair.getSecond().getGroupIds().contains(group.getStringId())

        groupPair = groupService.removeSubgroup(group.getStringId(), subGroup.getStringId())
        assert !groupPair.getFirst().getSubgroupIds().contains(subGroup.getStringId())
        assert !groupPair.getSecond().getGroupIds().contains(group.getStringId())
    }

    @Test
    void deleteGroup() {
        Group group = groupService.create("deleteGroup", "Delete group test", dummy)
        assert groupService.findByIdentifier("deleteGroup").isPresent()
        groupService.delete(group)
        assert !groupService.findByIdentifier("deleteGroup").isPresent()
    }

    @Test
    void deleteGroupRemovesMembersAndSubgroups() {
        Group parentGroup = groupService.create("parentForDelete", "Parent group", dummy)
        Group childGroup = groupService.create("childForDelete", "Child group", dummy)
        Pair<Group, Group> parentChildGroupPair = groupService.addSubgroup(parentGroup.getStringId(), childGroup.getStringId())
        parentGroup = parentChildGroupPair.getFirst()
        childGroup = parentChildGroupPair.getSecond()
        groupService.addUser(parentGroup, customer)

        groupService.delete(groupService.findById(parentGroup.getStringId()))

        assert !groupService.findByIdentifier("parentForDelete").isPresent()
        Group updatedChild = groupService.findById(childGroup.getStringId())
        assert !updatedChild.getGroupIds().contains(parentGroup.getStringId())
        AbstractUser updatedCustomer = userService.findUserByUsername(CUSTOMER_USER_MAIL, null).get()
        assert !updatedCustomer.getGroupIds().contains(parentGroup.getStringId())
    }

    @Test
    void saveGroup() {
        Group group = groupService.create("saveGroupTest", "Original title", dummy)
        group.setDisplayName("Updated title")
        Group saved = groupService.save(group)
        assert saved.getDisplayName() == "Updated title"
        assert saved.getModifiedAt() != null
    }

    @Test
    void findById() {
        Group group = groupService.create("findByIdTest", "Find by id test", dummy)
        Group found = groupService.findById(group.getStringId())
        assert found != null
        assert found.getIdentifier() == "findByIdTest"
    }

    @Test
    void findByIdNotFound() {
        try {
            groupService.findById("nonexistent_id")
            assert false : "Should have thrown exception"
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("does not exist")
        }
    }

    @Test
    void findByIdentifier() {
        groupService.create("findByIdentifierTest", "Find by identifier test", dummy)
        Optional<Group> found = groupService.findByIdentifier("findByIdentifierTest")
        assert found.isPresent()
        assert found.get().getDisplayName() == "Find by identifier test"
    }

    @Test
    void findByIdentifierNotFound() {
        Optional<Group> found = groupService.findByIdentifier("nonexistent_identifier")
        assert !found.isPresent()
    }

    @Test
    void findAll() {
        Page<Group> allGroups = groupService.findAll(new FullPageRequest())
        assert !allGroups.isEmpty()
    }

    @Test
    void findAllByIds() {
        Group group1 = groupService.create("findAllByIds1", "Group 1", dummy)
        Group group2 = groupService.create("findAllByIds2", "Group 2", dummy)
        Page<Group> found = groupService.findAllByIds([group1.getStringId(), group2.getStringId()], new FullPageRequest())
        assert found.getContent().size() == 2
    }

    @Test
    void getDefaultSystemGroup() {
        Group defaultGroup = groupService.getDefaultSystemGroup()
        assert defaultGroup != null
        assert defaultGroup.getIdentifier() != null
    }

    @Test
    void getDefaultUserGroup() {
        Group defaultUserGroup = groupService.getDefaultUserGroup(dummy)
        assert defaultUserGroup != null
        assert defaultUserGroup.getIdentifier() == dummy.getUsername()
    }

    @Test
    void addAndRemoveAuthority() {
        Group group = groupService.create("authorityTest", "Authority test group", dummy)
        Authority authority = authorityService.getOrCreate(Authority.user)
        group = groupService.addAuthority(group, authority)
        assert group.getAuthorityIds().contains(authority.getStringId())
        group = groupService.removeAuthority(group, authority)
        assert !group.getAuthorityIds().contains(authority.getStringId())
    }

    @Test
    void addAndRemoveAuthorityById() {
        Group group = groupService.create("authorityByIdTest", "Authority by id test group", dummy)
        Authority authority = authorityService.getOrCreate(Authority.admin)
        group = groupService.addAuthority(group.getStringId(), authority.getStringId())
        assert group.getAuthorityIds().contains(authority.getStringId())
        group = groupService.removeAuthority(group.getStringId(), authority.getStringId())
        assert !group.getAuthorityIds().contains(authority.getStringId())
    }

    @Test
    void assignUsersToGroup() {
        Group group = groupService.create("assignUsersTest", "Assign users test group", dummy)
        Set<String> userIds = [customer.getStringId()] as Set
        group = groupService.assignUsersToGroup(group.getStringId(), userIds)
        assert group.getMemberIds().contains(customer.getStringId())
    }

    @Test
    void assignSubgroups() {
        Group parentGroup = groupService.create("assignSubgroupsParent", "Parent", dummy)
        Group child1 = groupService.create("assignSubgroupsChild1", "Child 1", dummy)
        Group child2 = groupService.create("assignSubgroupsChild2", "Child 2", dummy)
        Set<String> childIds = [child1.getStringId(), child2.getStringId()] as Set
        groupService.assignSubgroups(parentGroup.getStringId(), childIds)
        Group updated = groupService.findById(parentGroup.getStringId())
        assert updated.getSubgroupIds().contains(child1.getStringId())
        assert updated.getSubgroupIds().contains(child2.getStringId())
    }

    @Test
    void getGroupParentGroupsAndSubgroups() {
        Group parentGroup = groupService.create("parentGroupNav", "Parent", dummy)
        Group childGroup = groupService.create("childGroupNav", "Child", dummy)
        groupService.addSubgroup(parentGroup.getStringId(), childGroup.getStringId())

        List<Group> subgroups = groupService.getGroupSubgroupsById(parentGroup.getStringId())
        assert subgroups.any { it.getStringId() == childGroup.getStringId() }

        List<Group> parents = groupService.getGroupParentGroupsById(childGroup.getStringId())
        assert parents.any { it.getStringId() == parentGroup.getStringId() }
    }

    @Test
    void getGroupOwnerEmail() {
        Group group = groupService.create("ownerEmailTest", "Owner email test", dummy)
        String ownerEmail = groupService.getGroupOwnerEmail(group.getStringId())
        assert ownerEmail == DUMMY_USER_MAIL
    }

    @Test
    void addSubgroupToItselfThrowsException() {
        Group group = groupService.create("selfSubgroup", "Self subgroup test", dummy)
        try {
            groupService.addSubgroup(group.getStringId(), group.getStringId())
            assert false : "Should have thrown exception"
        } catch (IllegalArgumentException e) {
            assert e.getMessage().contains("itself")
        }
    }

    @Test
    void addUserByIdAndRealmId() {
        Group group = groupService.create("addUserByIdTest", "Add user by id test", dummy)
        group = groupService.addUser(group.getStringId(), customer.getStringId(), customer.getRealmId())
        assert group.getMemberIds().contains(customer.getStringId())
        group = groupService.removeUser(group.getStringId(), customer.getStringId(), customer.getRealmId())
        assert !group.getMemberIds().contains(customer.getStringId())
    }

    @Test
    void assignAuthorities() {
        Group group = groupService.create("assignAuthoritiesTest", "Assign authorities test", dummy)
        Authority userAuth = authorityService.getOrCreate(Authority.user)
        Authority adminAuth = authorityService.getOrCreate(Authority.admin)
        Set<String> authorityIds = [userAuth.getStringId(), adminAuth.getStringId()] as Set
        group = groupService.assignAuthorities(group.getStringId(), authorityIds)
        assert group.getAuthorityIds().contains(userAuth.getStringId())
        assert group.getAuthorityIds().contains(adminAuth.getStringId())
    }

    // ==================== ActionDelegate Group Method Tests ====================

    @Test
    void actionDelegateCreateAndDeleteGroup() {
        setSecurityContext(dummy)
        try {
            Group group = actionDelegate.createGroup("adCreateGroup", "AD Create Group", dummy)
            assert group != null
            assert group.getIdentifier() == "adCreateGroup"

            actionDelegate.deleteGroup(group)
            assert !groupService.findByIdentifier("adCreateGroup").isPresent()
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateFindGroupByIdentifier() {
        setSecurityContext(dummy)
        try {
            groupService.create("adFindByIdentifier", "AD Find test", dummy)
            Group found = actionDelegate.findGroupByIdentifier("adFindByIdentifier")
            assert found != null
            assert found.getIdentifier() == "adFindByIdentifier"

            Group notFound = actionDelegate.findGroupByIdentifier("nonexistent")
            assert notFound == null
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateFindGroupById() {
        setSecurityContext(dummy)
        try {
            Group group = groupService.create("adFindById", "AD Find by id test", dummy)
            Group found = actionDelegate.findGroupById(group.getStringId())
            assert found != null
            assert found.getIdentifier() == "adFindById"
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateFindGroups() {
        setSecurityContext(dummy)
        try {
            groupService.create("adFindGroups1", "AD Find groups 1", dummy)
            groupService.create("adFindGroups2", "AD Find groups 2", dummy)
            Page<Group> groups = actionDelegate.findGroups()
            assert groups.getTotalElements() >= 2
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateSaveGroup() {
        setSecurityContext(dummy)
        try {
            Group group = groupService.create("adSaveGroup", "Original", dummy)
            group.setDisplayName("Modified")
            Group saved = actionDelegate.saveGroup(group)
            assert saved.getDisplayName() == "Modified"
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateAddAndRemoveUserFromGroup() {
        setSecurityContext(dummy)
        try {
            Group group = groupService.create("adUserGroup", "AD User group", dummy)
            Group updated = actionDelegate.addUserToGroup(group.getStringId(), customer.getStringId(), customer.getRealmId())
            assert updated.getMemberIds().contains(customer.getStringId())

            updated = actionDelegate.removeUserFromGroup(group.getStringId(), customer.getStringId(), customer.getRealmId())
            assert !updated.getMemberIds().contains(customer.getStringId())
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateAddAndRemoveAuthorityFromGroup() {
        setSecurityContext(dummy)
        try {
            Group group = groupService.create("adAuthorityGroup", "AD Authority group", dummy)
            Authority authority = authorityService.getOrCreate(Authority.user)
            Group updated = actionDelegate.addAuthorityToGroup(group.getStringId(), authority.getStringId())
            assert updated.getAuthorityIds().contains(authority.getStringId())

            updated = actionDelegate.removeAuthorityFromGroup(group.getStringId(), authority.getStringId())
            assert !updated.getAuthorityIds().contains(authority.getStringId())
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateAddAndRemoveRoleFromGroup() {
        setSecurityContext(dummy)
        try {
            ImportPetriNetEventOutcome netOutcome = petriNetService.importPetriNet(ImportPetriNetParams.with()
                    .xmlFile(new FileInputStream("src/test/resources/simple_role.xml"))
                    .releaseType(VersionType.MAJOR)
                    .author(userService.getSystem())
                    .build())
            ProcessRole role = netOutcome.getNet().getRoles().values().find { it.importId == "simple_role" }
            Group group = groupService.create("adRoleGroup", "AD Role group", dummy)

            Group updated = actionDelegate.addRoleToGroup(group.getStringId(), role.getStringId())
            assert updated.getProcessRoles().any { it.getStringId() == role.getStringId() }

            updated = actionDelegate.removeRoleFromGroup(group.getStringId(), role.getStringId())
            assert !updated.getProcessRoles().any { it.getStringId() == role.getStringId() }
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

    @Test
    void actionDelegateAddAndRemoveSubGroup() {
        setSecurityContext(dummy)
        try {
            Group parentGroup = groupService.create("adSubGroupParent", "AD Parent", dummy)
            Group childGroup = groupService.create("adSubGroupChild", "AD Child", dummy)

            Pair<Group, Group> result = actionDelegate.addSubGroup(parentGroup.getStringId(), childGroup.getStringId())
            assert result.getFirst().getSubgroupIds().contains(childGroup.getStringId())
            assert result.getSecond().getGroupIds().contains(parentGroup.getStringId())

            result = actionDelegate.removeSubGroup(parentGroup.getStringId(), childGroup.getStringId())
            assert !result.getFirst().getSubgroupIds().contains(childGroup.getStringId())
            assert !result.getSecond().getGroupIds().contains(parentGroup.getStringId())
        } finally {
            SecurityContextHolder.clearContext()
        }
    }

}
