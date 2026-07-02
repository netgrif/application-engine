package com.netgrif.application.engine.action

import com.icegreen.greenmail.configuration.GreenMailConfiguration
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest
import com.netgrif.application.engine.configuration.properties.SecurityConfigurationProperties
import com.netgrif.application.engine.objects.auth.constants.UserConstants
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue
import com.netgrif.application.engine.objects.workflow.domain.Case
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.startup.runner.DefaultFiltersRunner
import com.netgrif.application.engine.startup.runner.FilterRunner
import com.netgrif.application.engine.workflow.service.interfaces.IFilterImportExportService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import com.netgrif.application.engine.workflow.web.responsebodies.MessageResource
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

import static java.util.Base64.getEncoder

@SpringBootTest
@ActiveProfiles(["test"])
@TestPropertySource(properties = "netgrif.engine.filter.create-default-filters=true")
@ExtendWith(SpringExtension.class)
class ActionDelegateTest {

    @Autowired
    private TestHelper testHelper

    @Autowired
    private ActionDelegate actionDelegate

    @Autowired
    private IFilterImportExportService importExportService

    @Autowired
    private FilterRunner filterRunner

    @Autowired
    private DefaultFiltersRunner defaultFiltersRunner

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private UserService userService

    @Autowired
    private SecurityConfigurationProperties.WebProperties webProperties

    private AbstractUser systemUser

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        systemUser = userService.findByEmail(UserConstants.SYSTEM_USER_EMAIL, null)
        def loggedUser = ActorTransformer.toLoggedUser(systemUser)
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUser, null, loggedUser.authoritySet as Set<AuthorityImpl>))
    }

    @AfterEach
    void after() {
        SecurityContextHolder.clearContext()
    }

    @Test
    void importFiltersTest(){
        prepareFilterImportFile()

        List<String> actionDelegateList = actionDelegate.importFilters()
        assert actionDelegateList.size() == 2
    }

    private void prepareFilterImportFile() {
        filterRunner.run(null)
        defaultFiltersRunner.run(null)
        List<Case> filters = workflowService.search(
                QCase.case$.processIdentifier.eq(FilterRunner.FILTER_PETRI_NET_IDENTIFIER),
                Pageable.ofSize(2)
        ).content
        assert filters.size() == 2

        FileFieldValue exportedFilters = importExportService.exportFiltersToFile(filters.collect { it.stringId })
        importExportService.createFilterImport(systemUser)
        Case importCase = workflowService.searchOne(
                QCase.case$.processIdentifier.eq(FilterRunner.IMPORT_NET_IDENTIFIER)
                        .and(QCase.case$.author.id.eq(systemUser.stringId))
        )
        assert importCase != null
        importCase.dataSet.get("upload_file").value = exportedFilters
        workflowService.save(importCase)
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
}
