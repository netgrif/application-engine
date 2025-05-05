package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

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
    private SuperCreator superCreator;

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
    void testCreate() {
        String name = "group name";
        Group parentGroup = createGroup("parent group name");
        Group group = groupService.create(GroupParams.with()
                .name(new TextField(name))
                .memberIds(CaseField.withValue(List.of(superCreator.getSuperIdentity().getMainActorId())))
                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
                .build());

        assert group != null && group.getCase() != null;
        assert group.getName().equals(name);
        assert group.getMemberIds().size() == 1;
        assert group.getMemberIds().contains(superCreator.getSuperIdentity().getMainActorId());
        assert group.getParentGroupId() != null;
        assert group.getParentGroupId().equals(parentGroup.getStringId());

        assertThrows(IllegalArgumentException.class, () -> groupService.create(null));
        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with().build()));
        // todo: release/8.0.0 allowed nets validation is not working
//        assertThrows(IllegalArgumentException.class, () -> groupService.create(GroupParams.with()
//                .name(new TextField(name))
//                .memberIds(CaseField.withValue(List.of(superCreator.getSuperIdentity().getStringId()))) // wrong process identifier
//                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
//                .build()));
    }

    @Test
    void testUpdate() {
        String name = "group name";

        Group parentGroup = createGroup("parent group name");
        Group group = createGroup(name);
        assert group.getName().equals(name);
        assert group.getMemberIds() == null;
        assert group.getParentGroupId() == null; // todo 2058 default group

        assertThrows(IllegalArgumentException.class, () -> groupService.update(group, null));
        assertThrows(IllegalArgumentException.class, () -> groupService.update(null, GroupParams.with()
                .name(new TextField("some name"))
                .memberIds(CaseField.withValue(List.of(superCreator.getSuperIdentity().getMainActorId())))
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

        String newName = "new group name";
        Group updatedGroup = groupService.update(group, GroupParams.with()
                .name(new TextField(newName))
                .memberIds(CaseField.withValue(List.of(superCreator.getSuperIdentity().getMainActorId())))
                .parentGroupId(CaseField.withValue(List.of(parentGroup.getStringId())))
                .build());

        assert group.getStringId().equals(updatedGroup.getStringId());
        assert updatedGroup.getName().equals(newName);
        assert updatedGroup.getMemberIds().size() == 1;
        assert updatedGroup.getMemberIds().contains(superCreator.getSuperIdentity().getMainActorId());
        assert updatedGroup.getParentGroupId() != null;
        assert updatedGroup.getParentGroupId().equals(parentGroup.getStringId());
    }

    private Group createGroup(String name) {
        Case groupCase = workflowService.createCaseByIdentifier(GroupConstants.PROCESS_IDENTIFIER, name, "", null).getCase();
        return new Group(dataService.setData(groupCase, GroupParams.with()
                .name(new TextField(name))
                .build()
                .toDataSet(), null).getCase());
    }
}
