package com.netgrif.workflow.auth

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.configuration.properties.NaeOAuthProperties
import com.netgrif.workflow.oauth.domain.RemoteGroupResource
import com.netgrif.workflow.oauth.domain.RemoteUserResource
import com.netgrif.workflow.oauth.service.interfaces.IRemoteGroupResourceService
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService
import com.netgrif.workflow.utils.FullPageRequest
import org.junit.Before
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Page
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@AutoConfigureMockMvc
@SpringBootTest(properties = [
        "nae.oauth.remote-user-base=true",
        "nae.oauth.keycloak=true"])
class RemoteUserResourceTest {

    @Autowired
    private NaeOAuthProperties oAuthProperties

    @Autowired
    private TestHelper testHelper

    @Autowired
    private IRemoteUserResourceService remoteUserResourceService

    @Autowired
    private IRemoteGroupResourceService remoteGroupResourceService

    @Before
    void before() {
        testHelper.truncateDbs()
    }

    @Test
    void testListUsers() {
        Page<RemoteUserResource> page = remoteUserResourceService.listUsers(new FullPageRequest())
        RemoteUserResource user = page.content.find { (it as RemoteUserResource).username == oAuthProperties.superUsername }
        assert user != null
        assert user.id != null
        assert user.firstName != null
        assert user.lastName != null

        long count = remoteUserResourceService.countUsers()
        assert count == page.totalElements

        user = remoteUserResourceService.findUser(user.id)
        assert user != null

        user = remoteUserResourceService.findUserByUsername(user.username)
        assert user != null

        user = remoteUserResourceService.findUserByEmail(user.email)
        assert user != null

        List<RemoteGroupResource> groups = remoteGroupResourceService.groupsOfUser(user.id)
        assert groups != null
    }

    @Test
    void testListGroups() {
        Page<RemoteGroupResource> page = remoteGroupResourceService.listGroups(new FullPageRequest())
        assert page != null

        long count = remoteGroupResourceService.countGroups()
        assert count == page.totalElements
    }

//    @Test(expected = IllegalArgumentException.class)
//    void testGroupExceptionMembers() {
//        remoteGroupResourceService.members("INVALID_ID")
//    }
//
//    @Test(expected = IllegalArgumentException.class)
//    void testGroupExceptionGroupsOfUser() {
//        remoteGroupResourceService.groupsOfUser("INVALID_ID")
//    }

    @Test
    void testFindInvalidUser() {
        RemoteUserResource resource = remoteUserResourceService.findUser("INVALID_ID")
        assert resource == null

        resource = remoteUserResourceService.findUserByEmail("INVALID_EMAIL")
        assert resource == null

        resource = remoteUserResourceService.findUserByUsername("INVALID_USERNAME")
        assert resource == null
    }

    @Test
    void testFindInvalidGroup() {
        RemoteGroupResource group = remoteGroupResourceService.find("INVALID_ID")
        assert group == null
    }
}
