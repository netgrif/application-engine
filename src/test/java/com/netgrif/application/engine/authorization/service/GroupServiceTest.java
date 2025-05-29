package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.DefaultGroupRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.SetDataParams;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.SystemCaseFactoryRegistry;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.service.throwable.CaseAlreadyExistsException;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class GroupServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private GroupService groupService;

    @Autowired
    private DefaultGroupRunner defaultGroupRunner;

    @Autowired
    private SystemCaseFactoryRegistry systemCaseFactoryRegistry;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void testFindByName() throws InterruptedException {
        assert groupService.findByName(null).isEmpty();
        assert groupService.findByName("non existing name").isEmpty();

        String name = "group name";
        createGroup(name);

        Thread.sleep(2000);
        assert groupService.findByName(name).isPresent();
    }

    @Test
    void testExistsByName() throws InterruptedException {
        assert !groupService.existsByName(null);
        assert !groupService.existsByName("non existing name");

        String name = "group name";
        createGroup(name);

        Thread.sleep(2000);
        assert groupService.existsByName(name);
    }

    @Test
    void testFindById() {
        assert groupService.findById(null).isEmpty();
        assert groupService.findById(new ObjectId().toString()).isEmpty();

        String name = "group name";
        Group group = createGroup(name);

        assert groupService.findById(group.getStringId()).isPresent();
    }

    @Test
    void testExistsById() {
        assert !groupService.existsById(null);
        assert !groupService.existsById(new ObjectId().toString());

        String name = "group name";
        Group group = createGroup(name);

        assert groupService.existsById(group.getStringId());
    }

    @Test
    void testFindAll() throws InterruptedException {
        Process groupProcess = petriNetService.getNewestVersionByIdentifier(GroupConstants.PROCESS_IDENTIFIER);
        caseRepository.deleteAllByPetriNetObjectId(groupProcess.getId());

        assert groupService.findAll().isEmpty();

        createGroup("group name");
        createGroup("group name 2");

        Thread.sleep(2000);
        assert groupService.findAll().size() == 2;
    }

    @Test
    void testCreate() throws InterruptedException {
        String name = "group name";
        String propertyKey = "property";
        String propertyValue = "isActive";
        Group parentGroup = createGroup("parent group");
        Group group = groupService.create(GroupParams.with()
                .name(new TextField(name))
                .groupIds(CaseField.withValue(List.of(defaultGroupRunner.getDefaultGroup().getStringId())))
                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
                .properties(Map.of(propertyKey, propertyValue))
                .build());

        assert group != null && group.getCase() != null;
        assert group.getName().equals(name);
        assert group.getParentGroupId() != null;
        assert group.getParentGroupId().equals(parentGroup.getStringId());
        assert group.getGroupIds() != null;
        assert group.getGroupIds().size() == 1;
        assert group.getGroupIds().contains(defaultGroupRunner.getDefaultGroup().getStringId());
        assert group.getCase().getProperties() != null;
        assert group.getCase().getProperties().size() == 1;
        assert group.getCase().getProperties().containsKey(propertyKey);
        assert group.getCase().getProperties().get(propertyKey).equals(propertyValue);

        assertThrows(IllegalArgumentException.class, () -> groupService.create(null));
        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with().build()));
        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with()
                .name(new TextField(GroupConstants.DEFAULT_GROUP_NAME))
                .build()));
        assertThrows(IllegalArgumentException.class, () -> groupService.create(UserParams.with()
                .email(new TextField("wrong type of params"))
                .build()));
        // todo: release/8.0.0 allowed nets validation is not working
//        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with()
//                .name(new TextField(name))
//                .memberIds(CaseField.withValue(List.of(superCreator.getSuperIdentity().getStringId()))) // wrong process identifier
//                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
//                .build()));

        Thread.sleep(2000);
        assertThrows(CaseAlreadyExistsException.class, () -> groupService.create(GroupParams.with()
                .name(new TextField(name))
                .build()));
    }
    @Test
    void testCreateDefaultParent() {
        String name = "group name";
        Group group = groupService.create(GroupParams.with()
                .name(new TextField(name))
                .build());

        assert group.getParentGroupId() != null;
        assert group.getParentGroupId().equals(defaultGroupRunner.getDefaultGroup().getStringId());
    }

    @Test
    void testUpdate() {
        String name = "group name";

        Group parentGroup = createGroup("parent group name");
        Group group = createGroup(name);
        assert group.getName().equals(name);
        assert group.getGroupIds() == null || group.getGroupIds().isEmpty();
        assert group.getParentGroupId() != null;
        assert group.getParentGroupId().equals(defaultGroupRunner.getDefaultGroup().getStringId());
        assert group.getCase().getProperties() == null || group.getCase().getProperties().isEmpty();

        assertThrows(IllegalArgumentException.class, () -> groupService.update(group, null));
        assertThrows(IllegalArgumentException.class, () -> groupService.update(null, GroupParams.with()
                .name(new TextField("some name"))
                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
                .build()));

        // todo: release/8.0.0 allowed nets validation is not working
//        assertThrows(IllegalArgumentException.class, () -> groupService.update(group, GroupParams.with()
//                // wrong process identifier
//                .memberIds(CaseField.withValue(List.of(superCreator.getSuperIdentity().getStringId())))
//                .build()));

        assertThrows(IllegalArgumentException.class, () -> groupService.update(group, GroupParams.with()
                // self reference should be forbidden
                .parentGroupId(CaseField.withValue(List.of(group.getStringId())))
                .build()));
        assert ((Group) systemCaseFactoryRegistry.fromCase(workflowService.findOne(group.getStringId())))
                .getParentGroupId().equals(defaultGroupRunner.getDefaultGroup().getStringId());

        assertThrows(IllegalArgumentException.class, () -> groupService.update(group, GroupParams.with()
                // self reference should be forbidden
                .groupIds(CaseField.withValue(List.of(group.getStringId())))
                .build()));
        assert ((Group) systemCaseFactoryRegistry.fromCase(workflowService.findOne(group.getStringId())))
                .getGroupIds() == null || group.getGroupIds().isEmpty();

        String newName = "new group name";
        String propertyKey = "property";
        String propertyValue = "isActive";
        Group updatedGroup = groupService.update(group, GroupParams.with()
                .name(new TextField(newName))
                .groupIds(CaseField.withValue(List.of(defaultGroupRunner.getDefaultGroup().getStringId())))
                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
                .properties(Map.of(propertyKey, propertyValue))
                .build());

        assert group.getStringId().equals(updatedGroup.getStringId());
        assert updatedGroup.getName().equals(newName);
        assert updatedGroup.getGroupIds() != null;
        assert updatedGroup.getGroupIds().size() == 1;
        assert updatedGroup.getGroupIds().contains(defaultGroupRunner.getDefaultGroup().getStringId());
        assert updatedGroup.getParentGroupId() != null;
        assert updatedGroup.getParentGroupId().equals(parentGroup.getStringId());
        assert updatedGroup.getCase().getProperties() != null;
        assert updatedGroup.getCase().getProperties().size() == 1;
        assert updatedGroup.getCase().getProperties().containsKey(propertyKey);
        assert updatedGroup.getCase().getProperties().get(propertyKey).equals(propertyValue);
    }

    @Test
    void testGetDefaultGroup() {
        Process process = petriNetService.getNewestVersionByIdentifier(GroupConstants.PROCESS_IDENTIFIER);
        caseRepository.deleteAllByPetriNetObjectId(process.getObjectId());
        defaultGroupRunner.clearCache();

        assert caseRepository.findAllByProcessIdentifier(GroupConstants.PROCESS_IDENTIFIER).isEmpty();

        Group defaultGroup = groupService.getDefaultGroup();

        assert defaultGroup != null;
        assert defaultGroup.getName().equals(GroupConstants.DEFAULT_GROUP_NAME);
        assert defaultGroup.getParentGroupId() == null;
        assert caseRepository.findAllByProcessIdentifier(GroupConstants.PROCESS_IDENTIFIER).size() == 1;
        Optional<Case> groupCaseOpt = caseRepository.findById(defaultGroup.getStringId());
        assert groupCaseOpt.isPresent();
        assert groupCaseOpt.get().getStringId().equals(defaultGroup.getStringId());
    }

    @Test
    void testAddGroup() {
        Group group = createGroup("test group");
        Group memberGroup = createGroup("member group");
        assert memberGroup.getGroupIds() == null || memberGroup.getGroupIds().isEmpty();

        assertThrows(IllegalArgumentException.class, () -> groupService.addGroup(null, group.getStringId()));
        final Group finalGroup = memberGroup;
        assertThrows(IllegalArgumentException.class, () -> groupService.addGroup(finalGroup, null));
        assertThrows(IllegalArgumentException.class, () -> groupService.addGroup(finalGroup, finalGroup.getStringId()));
        Group actualMemberGroup = (Group) systemCaseFactoryRegistry.fromCase(workflowService.findOne(finalGroup.getStringId()));
        assert actualMemberGroup.getGroupIds() == null || actualMemberGroup.getGroupIds().isEmpty();

        memberGroup = groupService.addGroup(memberGroup, group.getStringId());
        assert memberGroup.getGroupIds() != null;
        assert memberGroup.getGroupIds().size() == 1;
        assert memberGroup.getGroupIds().get(0).equals(group.getStringId());
    }

    @Test
    void testAddGroups() {
        Group group1 = createGroup("test group 1");
        Group group2 = createGroup("test group 2");
        Group memberGroup = createGroup("member group");
        assert memberGroup.getGroupIds() == null || memberGroup.getGroupIds().isEmpty();

        assertThrows(IllegalArgumentException.class, () -> groupService.addGroups(null, Set.of(group1.getStringId(),
                group2.getStringId())));
        final Group finalGroup = memberGroup;
        assertThrows(IllegalArgumentException.class, () -> groupService.addGroups(finalGroup, null));
        assertThrows(IllegalArgumentException.class, () -> groupService.addGroups(finalGroup, Set.of(group1.getStringId(),
                group2.getStringId(), finalGroup.getStringId())));
        Group actualMemberGroup = (Group) systemCaseFactoryRegistry.fromCase(workflowService.findOne(finalGroup.getStringId()));
        assert actualMemberGroup.getGroupIds() == null || actualMemberGroup.getGroupIds().isEmpty();

        Set<String> groupIdsToAdd = new HashSet<>(List.of(group1.getStringId(), group2.getStringId(),
                defaultGroupRunner.getDefaultGroup().getStringId()));
        groupIdsToAdd.add(null);

        memberGroup = groupService.addGroups(memberGroup, groupIdsToAdd);
        assert memberGroup.getGroupIds() != null;
        assert memberGroup.getGroupIds().size() == 3;
        assert memberGroup.getGroupIds().contains(defaultGroupRunner.getDefaultGroup().getStringId());
        assert memberGroup.getGroupIds().contains(group1.getStringId());
        assert memberGroup.getGroupIds().contains(group2.getStringId());
    }

    @Test
    void testRemoveGroup() {
        Group group = createGroup("test group");
        Group memberGroup = createGroup("member group", List.of(group.getStringId()));
        assert memberGroup.getGroupIds() != null;
        assert memberGroup.getGroupIds().size() == 1;

        assertThrows(IllegalArgumentException.class, () -> groupService.removeGroup(null, group.getStringId()));
        final Group finalGroup = memberGroup;
        assertThrows(IllegalArgumentException.class, () -> groupService.removeGroup(finalGroup, null));

        memberGroup = groupService.removeGroup(memberGroup, defaultGroupRunner.getDefaultGroup().getStringId());
        assert memberGroup.getGroupIds() != null;
        assert memberGroup.getGroupIds().size() == 1;

        memberGroup = groupService.removeGroup(memberGroup, group.getStringId());
        assert memberGroup.getGroupIds() == null || memberGroup.getGroupIds().isEmpty();

        assert groupService.removeGroup(memberGroup, group.getStringId()) != null;
    }

    @Test
    void testRemoveGroups() {
        Group group1 = createGroup("test group 1");
        Group group2 = createGroup("test group 2");
        Group memberGroup = createGroup("member group", List.of(group1.getStringId(), group2.getStringId()));
        assert memberGroup.getGroupIds() != null;
        assert memberGroup.getGroupIds().size() == 2;

        assertThrows(IllegalArgumentException.class, () -> groupService.removeGroups(null, Set.of(group1.getStringId())));
        final Group finalGroup = memberGroup;
        assertThrows(IllegalArgumentException.class, () -> groupService.removeGroups(finalGroup, null));

        Set<String> groupIdsToRemove = new HashSet<>(Set.of(group1.getStringId(), group2.getStringId(),
                defaultGroupRunner.getDefaultGroup().getStringId()));
        groupIdsToRemove.add(null);

        memberGroup = groupService.removeGroups(memberGroup, groupIdsToRemove);
        assert memberGroup.getGroupIds() == null || memberGroup.getGroupIds().isEmpty();
    }

    @Test
    void testForbiddenKeywords() {
        assert !groupService.registerForbiddenKeywords(null);
        assert !groupService.registerForbiddenKeywords(Set.of());

        assert !groupService.removeForbiddenKeywords(null);
        assert !groupService.removeForbiddenKeywords(Set.of());

        Set<String> keywords = Set.of("keyword1", "keyword2", "keyword3");
        assert !groupService.removeForbiddenKeywords(keywords);
        assert groupService.registerForbiddenKeywords(keywords);

        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with()
                .name(new TextField("keyword1"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with()
                .name(new TextField("keyword2"))
                .build()));

        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with()
                .name(new TextField("keyword3"))
                .build()));

        assert groupService.removeForbiddenKeywords(Set.of("keyword1", "keyword2"));

        Group group = groupService.create(GroupParams.with().name(new TextField("keyword1")).build());
        assert group != null;

        group = groupService.create(GroupParams.with().name(new TextField("keyword2")).build());
        assert group != null;

        assertThrows(IllegalArgumentException.class, () -> groupService.create(UserParams.with()
                .email(new TextField("keyword3"))
                .build()));

        groupService.clearForbiddenKeywords();

        group = groupService.create(GroupParams.with().name(new TextField("keyword3")).build());
        assert group != null;
    }

    private Group createGroup(String name) {
        return createGroup(name, new ArrayList<>());
    }

    /**
     * @param groupIds Ids of groups to be member of
     * */
    private Group createGroup(String name, List<String> groupIds) {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .processIdentifier(GroupConstants.PROCESS_IDENTIFIER)
                .title(name)
                .build();
        Case groupCase = workflowService.createCase(createCaseParams).getCase();
        return new Group(dataService.setData(new SetDataParams(groupCase, GroupParams.with()
                .name(new TextField(name))
                .parentGroupId(CaseField.withValue(List.of(defaultGroupRunner.getDefaultGroup().getStringId())))
                .groupIds(CaseField.withValue(groupIds))
                .build()
                .toDataSet(), null)).getCase());
    }
}
