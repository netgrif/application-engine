package com.netgrif.application.engine.action

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.auth.domain.IUser
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import groovy.transform.CompileStatic
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import javax.mail.internet.MimeMessage

@SpringBootTest
@ActiveProfiles(["test"])
@CompileStatic
@ExtendWith(SpringExtension.class)
class ActionDelegateTest extends EngineTest {

    @BeforeEach
    void before() {
        truncateDbs()
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
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()

        MessageResource messageResource = actionDelegate.inviteUser("test@netgrif.com")
        assert messageResource.getContent().success

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        smtpServer.stop()
    }

    @Test
    void deleteUser(){
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()
        String mail = "test@netgrif.com";
        MessageResource messageResource = actionDelegate.inviteUser(mail)
        assert messageResource.getContent().success
        IUser user = userService.findByEmail(mail, false)
        assert user != null
        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assert messages
        actionDelegate.deleteUser(mail)
        IUser user2 = userService.findByEmail(mail, false)
        assert user2 == null
        smtpServer.stop()
    }


    @Test
    void inviteUserNewUserRequest(){
        GreenMail smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
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
}
