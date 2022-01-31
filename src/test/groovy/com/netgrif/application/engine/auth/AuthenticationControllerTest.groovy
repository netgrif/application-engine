package com.netgrif.application.engine.auth

import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetup
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.domain.Authority
import com.netgrif.application.engine.auth.domain.User
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository
import com.netgrif.application.engine.auth.domain.repositories.UserRepository
import com.netgrif.application.engine.auth.web.AuthenticationController
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.mail.EmailType
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import org.jsoup.Jsoup
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import javax.mail.BodyPart
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@Disabled("ClassCast")
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
    private AuthorityRepository authorityRepository

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private TestHelper testHelper

    @Autowired
    private SuperCreator superCreator

    private GreenMail smtpServer

    private Map<String, ProcessRole> processRoles

    @BeforeEach
    void before() {
        testHelper.truncateDbs()
        smtpServer = new GreenMail(new ServerSetup(2525, null, "smtp"))
        smtpServer.start()

        def net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/insurance_portal_demo_test.xml"), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert net.getNet() != null
        if (authorityRepository.count() == 0)
            importHelper.createAuthority(Authority.user)
//        group = importHelper.createGroup(GROUP_NAME)
//        processRoles = importHelper.getProcessRoles(net.getNet())
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void inviteTest() {
        controller.invite(new NewUserRequest(email: EMAIL, groups: [], processRoles: []), null)

        MimeMessage[] messages = smtpServer.getReceivedMessages()
        assertMessageReceived(messages)

        String content = getTextFromMimeMultipart(messages[0].content as MimeMultipart)
        String token = content.substring(content.indexOf("/signup/") + "/signup/".length(), content.lastIndexOf(" This is"))

        controller.signup(new RegistrationRequest(token: token, name: NAME, surname: SURNAME, password: PASSWORD))

        User user = userRepository.findByEmail(EMAIL)
        assert user

    }

    @AfterEach
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