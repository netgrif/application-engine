package com.netgrif.application.engine.elastic;

import com.netgrif.application.engine.ApplicationEngine;
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase
import com.netgrif.application.engine.auth.service.UserService
import com.netgrif.application.engine.elastic.domain.ElasticQueryConstants
import com.netgrif.application.engine.elastic.service.interfaces.IElasticQueryFactory
import com.netgrif.application.engine.objects.auth.domain.AbstractUser
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer
import com.netgrif.application.engine.objects.auth.domain.Authority
import com.netgrif.application.engine.objects.auth.domain.User
import com.netgrif.application.engine.objects.auth.domain.enums.UserState
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet
import com.netgrif.application.engine.objects.petrinet.domain.VersionType
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.startup.ImportHelper;
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

import java.sql.Timestamp
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@AutoConfigureMockMvc
class ElasticQueryFactoryTest {

    public static final String USER_EMAIL = "test@mail.sk"
    public static final String USER_PASSWORD = "password"

    @Autowired
    private TestHelper testHelper

    @Autowired
    protected IElasticQueryFactory queryFactory;

    @Autowired
    private UserService userService

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private WebApplicationContext wac

    private MockMvc mvc
    Map<String, Authority> auths

    @BeforeEach
    void before() {
        testHelper.truncateDbs()

        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build()

        auths = importHelper.createAuthorities(["user": Authority.user, "admin": Authority.admin])

        importHelper.createUser(new User(firstName: "Test", lastName: "Integration", email: USER_EMAIL, password: USER_PASSWORD, state: UserState.ACTIVE),
                [auths.get("user"), auths.get("admin")] as Authority[], [] as ProcessRole[])
    }

    @Test
    void dynamicQueryTest() {
        AbstractUser realUser = userService.findByEmail(USER_EMAIL, null)

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(ActorTransformer.toLoggedUser(realUser), ActorTransformer.toLoggedUser(realUser).getPassword(), ActorTransformer.toLoggedUser(realUser).getAuthoritySet() as Set<AuthorityImpl>);
        SecurityContextHolder.getContext().setAuthentication(token)

        def user = userService.getLoggedUser()
        def context = new HashMap<>(Map.of(
                ElasticQueryConstants.DYNAMIC_USER_ID_TEMPLATE, user.getId().toString(),
                ElasticQueryConstants.LOCAL_DATE_NOW_TEMPLATE, LocalDateTime.now(),
                ElasticQueryConstants.LOCAL_DATE_TODAY_TEMPLATE, LocalDate.now(),
                ElasticQueryConstants.LOGGED_USER_TEMPLATE, user
        ));

        String query1 = '${String.valueOf(java.sql.Timestamp.valueOf(now.plus(java.time.Duration.parse("P1D"))).getTime())}'
        String resultQuery1 = String.valueOf(Timestamp.valueOf(LocalDateTime.now() + Duration.parse("P1D")).getTime())

        assert queryFactory.populateQuery(query1, context) <= resultQuery1;

        String query2 = 'creationDate:${today.toString()}';
        String res2 = LocalDate.now().toString()
        String resultQuery2 = "creationDate:" + res2;

        assert queryFactory.populateQuery(query2, context) == resultQuery2;

        String query3 = 'author:${loggedUser.id}'
        String resultQuery3 = "author:" + user.id;

        assert queryFactory.populateQuery(query3, context) == resultQuery3;

        String query4 = 'author:${me}';
        String resultQuery4 = "author:" + user.id;

        assert queryFactory.populateQuery(query4, context) == resultQuery4;
    }
}
