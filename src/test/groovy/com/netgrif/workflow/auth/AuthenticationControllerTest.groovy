package com.netgrif.workflow.auth

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.netgrif.workflow.auth.domain.Authority
import com.netgrif.workflow.auth.domain.User
import com.netgrif.workflow.auth.domain.UserProcessRole
import com.netgrif.workflow.auth.domain.repositories.AuthorityRepository
import com.netgrif.workflow.auth.domain.repositories.UserRepository
import com.netgrif.workflow.auth.web.AuthenticationController
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest
import com.netgrif.workflow.importer.service.Config
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.mail.EmailType
import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.orgstructure.domain.GroupRepository
import com.netgrif.workflow.orgstructure.domain.Member
import com.netgrif.workflow.orgstructure.domain.MemberRepository
import com.netgrif.workflow.startup.ImportHelper
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

import javax.mail.BodyPart
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class AuthenticationControllerTest {

    private static final String EMAIL = "tets@test.com"
    private static final String NAME = "name"
    private static final String SURNAME = "surname"
    private static final String PASSWORD = "password"
    private static final String CASE_NAME = "Test case"
    private static final String CASE_INITIALS = "TC"
    public static final String GROUP_NAME = "Insurance Company"

    @Autowired
    private AuthenticationController controller

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private UserRepository userRepository

    @Autowired
    private MemberRepository memberRepository

    @Autowired
    private GroupRepository groupRepository

    @Autowired
    private AuthorityRepository authorityRepository

    private GreenMail smtpServer

    private Group group
    private Map<String, UserProcessRole> processRoles

    @Before
    void before() {
        smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()

        def net = importer.importPetriNet(new File("src/test/resources/insurance_portal_demo_test.xml"), CASE_NAME, CASE_INITIALS, new Config())
        assert net.isPresent()
        if (authorityRepository.count() == 0)
            importHelper.createAuthority(Authority.user)
        group = importHelper.createGroup(GROUP_NAME)
        processRoles = importHelper.createUserProcessRoles(["agent": "Agent", "company": "Company"], net.get())
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void inviteTest() {
        controller.invite(new NewUserRequest(email: EMAIL, groups: [group.id], processRoles: processRoles.values().collect {
            it.roleId
        }), null)

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assertMessageReceived(messages)

        String content = getTextFromMimeMultipart(messages[0].content as MimeMultipart)
        String token = content.substring(content.indexOf("/signup/") + "/signup/".length(), content.indexOf(" This is"))

        controller.signup(new RegistrationRequest(token: token, email: EMAIL, name: NAME, surname: SURNAME, password: PASSWORD))

        User user = userRepository.findByEmail(EMAIL)
        Member member = memberRepository.findByEmail(EMAIL)
        Group userGroup = groupRepository.findAll().first()

        assert user
        assert !member.groups.empty
        assert member.groups.first().name == GROUP_NAME
        assert userGroup
    }

    @After
    void after() {
        smtpServer.stop()
    }

    @SuppressWarnings("GrMethodMayBeStatic")
    private void assertMessageReceived(MimeMessage[] messages) throws MessagingException {
        assert messages.length > 0

        MimeMessage message = messages[0]

        assert "noreply@netgrif.com".equalsIgnoreCase(message.getFrom()[0].toString())
        assert EmailType.REGISTRATION.getSubject().equalsIgnoreCase(message.getSubject())
    }

    private String getTextFromMimeMultipart(MimeMultipart mimeMultipart) throws MessagingException, IOException {
        String result = ""
        int count = mimeMultipart.getCount()
        for (int i = 0; i < count; i++) {
            BodyPart bodyPart = mimeMultipart.getBodyPart(i)
            if (bodyPart.isMimeType("text/plain")) {
                result = result + "\n" + bodyPart.getContent()
                break // without break same text appears twice in my tests
            } else if (bodyPart.isMimeType("text/html")) {
                String html = (String) bodyPart.getContent()
                result = result + "\n" + Jsoup.parse(html).text()
            } else if (bodyPart.getContent() instanceof MimeMultipart) {
                result = result + getTextFromMimeMultipart((MimeMultipart) bodyPart.getContent())
            }
        }
        return result
    }
}