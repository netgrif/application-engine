package com.netgrif.application.engine.authorization.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.domain.constants.IdentityConstants;
import com.netgrif.application.engine.authentication.domain.params.IdentityParams;
import com.netgrif.application.engine.authorization.domain.Actor;
import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants;
import com.netgrif.application.engine.authorization.domain.constants.UserConstants;
import com.netgrif.application.engine.authorization.domain.params.GroupParams;
import com.netgrif.application.engine.authorization.domain.params.UserParams;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
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
import java.util.Optional;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class AllActorServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private AllActorService allActorService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private CaseRepository caseRepository;

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testFindById() {
        assert allActorService.findById(null).isEmpty();
        assert allActorService.findById(new ObjectId().toString()).isEmpty();

        User user = createUser("s@meemail.com");

        Optional<Actor> foundActorOpt = allActorService.findById(user.getStringId());
        assert foundActorOpt.isPresent();
    }

    @Test
    public void testFindAll() {
        caseRepository.deleteAll();

        assert allActorService.findAll().isEmpty();

        Identity identity = createIdentity("not an actor");
        Actor actor1 = createUser("actor1@test.com");
        Actor actor2 = createGroup("actor 2");

        List<Actor> actors = allActorService.findAll();
        assert actors.size() == 2;
        assert actors.stream().anyMatch(actor -> actor.getStringId().equals(actor1.getStringId()));
        assert actors.stream().anyMatch(actor -> actor.getStringId().equals(actor2.getStringId()));
        assert actors.stream().noneMatch(actor -> actor.getStringId().equals(identity.getStringId()));
    }

    private Group createGroup(String name) {
        Case groupCase = workflowService.createCaseByIdentifier(GroupConstants.PROCESS_IDENTIFIER, name, "", null).getCase();
        return new Group(dataService.setData(groupCase, GroupParams.with()
                .name(new TextField(name))
                .build()
                .toDataSet(), null).getCase());
    }

    private User createUser(String email) {
        Case userCase = workflowService.createCaseByIdentifier(UserConstants.PROCESS_IDENTIFIER, email, "", null).getCase();
        return new User(dataService.setData(userCase, UserParams.with()
                .email(new TextField(email))
                .build()
                .toDataSet(), null).getCase());
    }

    private Identity createIdentity(String username) {
        Case identityCase = workflowService.createCaseByIdentifier(IdentityConstants.PROCESS_IDENTIFIER, username, "", null).getCase();
        return new Identity(dataService.setData(identityCase, IdentityParams.with()
                .username(new TextField(username))
                .build()
                .toDataSet(), null).getCase());
    }
}
