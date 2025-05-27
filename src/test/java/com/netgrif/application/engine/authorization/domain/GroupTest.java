package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.petrinet.domain.dataset.CaseField;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import com.netgrif.application.engine.startup.DefaultGroupRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.domain.params.DeleteCaseParams;
import com.netgrif.application.engine.workflow.domain.params.SetDataParams;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class GroupTest {
    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private DefaultGroupRunner defaultGroupRunner;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testRemoveGroup() {
        Group parentGroup = createGroup("parent test group", defaultGroupRunner.getDefaultGroup().getStringId());
        Group childGroup = createGroup("child test group", parentGroup.getStringId());
        User user = createUser("test@user.com", List.of(childGroup.getStringId()));
        user = updateUserMembership(user, Set.of(parentGroup.getStringId()));

        assert childGroup.getParentGroupId() != null;
        assert childGroup.getParentGroupId().equals(parentGroup.getStringId());
        assert user.getGroupIds().size() == 1;

        workflowService.deleteCase(new DeleteCaseParams(parentGroup.getCase()));

        user = new User(workflowService.findOne(user.getStringId()));
        assert user.getGroupIds().isEmpty() || user.getGroupIds() == null;

        childGroup = new Group(workflowService.findOne(childGroup.getStringId()));
        assert childGroup.getParentGroupId() == null;
    }

    private User updateUserMembership(User user, Set<String> groupIds) {
        return new User(dataService.setData(new SetDataParams(user.getCase(), UserParams.with()
                .groupIds(CaseField.withValue(new ArrayList<>(groupIds)))
                .build()
                .toDataSet(), null)).getCase());
    }

    private Group createGroup(String name, String parentGroupId) {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .processIdentifier(GroupConstants.PROCESS_IDENTIFIER)
                .title(name)
                .build();
        Case groupCase = workflowService.createCase(createCaseParams).getCase();
        return new Group(dataService.setData(new SetDataParams(groupCase, GroupParams.with()
                .name(new TextField(name))
                .parentGroupId(CaseField.withValue(List.of(parentGroupId)))
                .build()
                .toDataSet(), null)).getCase());
    }

    private User createUser(String email, List<String> additionalGroupIds) {
        CreateCaseParams createCaseParams = CreateCaseParams.with()
                .processIdentifier(UserConstants.PROCESS_IDENTIFIER)
                .title(email)
                .build();
        Case userCase = workflowService.createCase(createCaseParams).getCase();
        return new User(dataService.setData(new SetDataParams(userCase, UserParams.with()
                .email(new TextField(email))
                .groupIds(CaseField.withValue(additionalGroupIds))
                .build()
                .toDataSet(), null)).getCase());
    }
}
