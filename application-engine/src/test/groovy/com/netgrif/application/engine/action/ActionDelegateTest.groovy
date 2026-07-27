package com.netgrif.application.engine.action

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.params.ImportPetriNetParams
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner
import com.netgrif.application.engine.workflow.params.CreateCaseParams
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static java.util.Base64.getEncoder

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class ActionDelegateTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private IFilterImportExportService importExportService

    @Autowired
    private UserService userService

    @Autowired
    private SecurityConfigurationProperties.WebProperties webProperties

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private SuperCreatorRunner superCreator

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    @Disabled("Context user")
    void importFiltersTest(){
        List<String> actionDelegateList = actionDelegate.importFilters()
        List<String> importedTasksIds = importExportService.importFilters()
        assert actionDelegateList.size() == importedTasksIds.size()
    }

    @Test
    void inviteUser(){
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp")).withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication())
        smtpServer.start()

        MessageResource messageResource = actionDelegate.inviteUser("test@netgrif.com")
        assert messageResource.getContent().success

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        smtpServer.stop()
    }

    @Test
    void deleteUser(){
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp")).withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication())
        smtpServer.start()
        String mail = "test@netgrif.com";
        MessageResource messageResource = actionDelegate.inviteUser(mail)
        assert messageResource.getContent().success
        AbstractUser user = userService.findByEmail(mail, null)
        assert user != null
        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        actionDelegate.deleteUser(mail)
        AbstractUser user2 = userService.findByEmail(mail, null)
        assert user2 == null
        smtpServer.stop()
    }


    @Test
    void inviteUserNewUserRequest(){
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp")).withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication())
        smtpServer.start()

        NewUserRequest newUserRequest = new NewUserRequest()
        newUserRequest.setEmail("test@netgrif.com")
        newUserRequest.groups = new HashSet<>()
        newUserRequest.processRoles = new HashSet<>()

        MessageResource messageResource = actionDelegate.inviteUser(newUserRequest)
        assert messageResource.getContent().success

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        smtpServer.stop()
    }

    @Test
    void makeUrlAction() {
        final String identifier = "identifier"
        final String url = "test.public.url/${getEncoder().encodeToString(identifier.bytes)}"
        assert actionDelegate.makeUrl(identifier) == url
        assert actionDelegate.makeUrl(webProperties.publicWeb.url, identifier) == url
        assert actionDelegate.makeUrl("test.netgrif.com/public", "identifier") == "test.netgrif.com/public/${getEncoder().encodeToString(identifier.bytes)}"
    }

    @Test
    void testAsyncRunAction() {
        ImportPetriNetEventOutcome net = petriNetService.importPetriNet(ImportPetriNetParams.with()
                .xmlFile(new FileInputStream("src/test/resources/petriNets/async_run.xml"))
                .releaseType(VersionType.MAJOR)
                .author(superCreator.getLoggedSuper())
                .build())
        assert net.getNet() != null
        CreateCaseEventOutcome outcome = workflowService.createCase(CreateCaseParams.with()
                .processId(net.getNet().getStringId())
                .title("Test title")
                .build())
        assert outcome.getCase() != null
    }
}
