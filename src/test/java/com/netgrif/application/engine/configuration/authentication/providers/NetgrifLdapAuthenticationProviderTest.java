package com.netgrif.application.engine.configuration.authentication.providers;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.ldap.domain.LdapGroupRef;
import com.netgrif.application.engine.ldap.domain.LdapUser;
import com.netgrif.application.engine.ldap.service.LdapUserService;
import com.netgrif.application.engine.ldap.service.interfaces.ILdapGroupRefService;
import com.netgrif.application.engine.orgstructure.web.requestbodies.LdapGroupRoleAssignRequestBody;
import com.netgrif.application.engine.orgstructure.web.requestbodies.LdapGroupSearchBody;
import com.netgrif.application.engine.orgstructure.web.responsebodies.LdapGroupResponseBody;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import org.bson.types.ObjectId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles({"test-ldap"})
@ExtendWith(SpringExtension.class)
class NetgrifLdapAuthenticationProviderTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private ILdapGroupRefService ldapGroupRefService;

    @Autowired
    private LdapUserService userService;

    @Autowired
    private SuperCreator superCreator;

    @Autowired
    private IPetriNetService petriNetService;
    @Autowired
    private TestHelper testHelper;

    private static final String USER_EMAIL_Test1 = "ben@netgrif.com";
    private static final String USER_PASSWORD_Test1 = "benpassword";

    private static final String USER_EMAIL_Test2 = "simpson@netgrif.com";
    private static final String USER_PASSWORD_Test2 = "password";

    private static final String USER_EMAIL_Test3 = "watson@netgrif.com";
    private static final String USER_PASSWORD_Test3 = "password";

    private MockMvc mvc;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        mvc = MockMvcBuilders
                .webAppContextSetup(wac)
                .apply(springSecurity())
                .build();
    }

    @Test
    void testLogin() throws Exception {
        mvc.perform(get("/api/user/me")
                        .with(httpBasic(USER_EMAIL_Test1, USER_PASSWORD_Test1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void testLogin2() throws Exception {

        mvc.perform(get("/api/auth/login")
                        .with(httpBasic(USER_EMAIL_Test2, USER_PASSWORD_Test2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Test
    void getMyLDAPGroups() throws Exception {

        MvcResult result = mvc.perform(get("/api/auth/login")
                        .with(httpBasic(USER_EMAIL_Test2, USER_PASSWORD_Test2))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        IUser ldapUser = userService.findByEmail(USER_EMAIL_Test2, false);
        assert ldapUser != null;
        assert ldapUser instanceof LdapUser;
        assert ((LdapUser) ldapUser).getMemberOf().size() == 2;

    }

    @Test
    void noLDAPGroups() throws Exception {

        MvcResult result = mvc.perform(get("/api/auth/login")
                        .with(httpBasic(USER_EMAIL_Test3, USER_PASSWORD_Test3))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        IUser ldapUser = userService.findByEmail(USER_EMAIL_Test3, false);
        assert ldapUser != null;
        assert ldapUser instanceof LdapUser;
        assert ((LdapUser) ldapUser).getMemberOf().size() == 0;

    }

    @Test
    void getMyProcessRole() throws Exception {

        MvcResult result = mvc.perform(get("/api/user/me")
                        .with(httpBasic(USER_EMAIL_Test1, USER_PASSWORD_Test1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        String string = result.getResponse().getContentAsString();

        JSONObject json = new JSONObject(string);
        JSONArray countProcessRole = (JSONArray) json.get("processRoles");
        assert countProcessRole.length() == 1;
    }

    @Test
    void getAllGroups() {
        List<LdapGroupRef> ldapGroups = ldapGroupRefService.findAllGroups();
        assert ldapGroups.size() == 3;
    }

    @Test
    void searchGroups() {
        List<LdapGroupRef> ldapGroups = ldapGroupRefService.searchGroups("Testik");
        assert ldapGroups.get(0).getCn().equals("test1");

        List<LdapGroupRef> ldapGroupsAll = ldapGroupRefService.searchGroups("tes");
        assert ldapGroupsAll.size() == 3;

        List<LdapGroupRef> ldapGroupsTest = ldapGroupRefService.searchGroups("test1");
        assert ldapGroupsTest.size() == 1;

        List<LdapGroupRef> ldapGroupsNothing = ldapGroupRefService.searchGroups("nothing");
        assert ldapGroupsNothing.size() == 0;
    }

    @Test
    void assignRoleGroup() throws Exception {
        PetriNet net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/role_all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet();
        assert net != null;
        Map<String, ProcessRole> roles = net.getRoles();
        assert roles != null;

        List<LdapGroupRef> ldapGroupsTest = ldapGroupRefService.searchGroups("test1");
        assert ldapGroupsTest.size() == 1;
        Set<String> role = new HashSet<>();
        roles.forEach((k, v) -> {
            role.add(v.getStringId());
        });
        assert role.size() == roles.size();
        ldapGroupRefService.setRoleToLdapGroup(ldapGroupsTest.get(0).getDn().toString(), role, superCreator.getLoggedSuper());
        Set<String> group = new HashSet<>();
        group.add(ldapGroupsTest.get(0).getDn().toString());
        Set<ProcessRole> getRole = ldapGroupRefService.getProcessRoleByLdapGroup(group);
        assert getRole.size() == roles.size();
    }

    @Test
    void assignRoleGroupAndCheck() throws Exception {

        MvcResult result = mvc.perform(get("/api/user/me")
                        .with(httpBasic(USER_EMAIL_Test1, USER_PASSWORD_Test1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        String string = result.getResponse().getContentAsString();

        JSONObject json = new JSONObject(string);
        JSONArray countProcessRole = (JSONArray) json.get("processRoles");
        assert countProcessRole.length() == 1;

        PetriNet net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/role_all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet();
        assert net != null;
        Map<String, ProcessRole> roles = net.getRoles();
        assert roles != null;

        List<LdapGroupRef> ldapGroupsTest = ldapGroupRefService.searchGroups("test1");
        assert ldapGroupsTest.size() == 1;
        Set<String> role = new HashSet<>();
        roles.forEach((k, v) -> {
            role.add(v.getStringId());
        });
        assert role.size() == roles.size();
        ldapGroupRefService.setRoleToLdapGroup(ldapGroupsTest.get(0).getDn().toString(), role, superCreator.getLoggedSuper());

        Set<String> group = new HashSet<>();
        group.add(ldapGroupsTest.get(0).getDn().toString());
        Set<ProcessRole> getRole = ldapGroupRefService.getProcessRoleByLdapGroup(group);
        assert getRole.size() == roles.size();


        MvcResult result2 = mvc.perform(get("/api/auth/login")
                        .with(httpBasic(USER_EMAIL_Test1, USER_PASSWORD_Test1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        String response2 = result2.getResponse().getContentAsString();

        JSONObject json2 = new JSONObject(response2);
        JSONArray countProcessRole2 = (JSONArray) json2.get("processRoles");
        assert countProcessRole2.length() == 1 + roles.size();


        MvcResult result3 = mvc.perform(get("/api/auth/login")
                        .with(httpBasic(USER_EMAIL_Test1, USER_PASSWORD_Test1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8"))
                .andExpect(status().isOk())
                .andReturn();

        String response3 = result3.getResponse().getContentAsString();

        JSONObject json3 = new JSONObject(response3);
        JSONArray countProcessRole3 = (JSONArray) json3.get("processRoles");
        assert countProcessRole3.length() == 1 + roles.size();


    }

    @Test
    void getProcessRole() {
        Set<String> findDn = Set.of("nothing");
        Set<ProcessRole> processRoles = ldapGroupRefService.getProcessRoleByLdapGroup(findDn);
        assert processRoles.size() == 0;
    }

    @Test
    void LdapUserTest() {
        LdapUser user = new LdapUser("dn", "commonName", "uid", "homeDirectory", "email", "name", "surname", null, "telNumber");
        assert user.getDn().equals("dn");
        assert user.getUid().equals("uid");
        assert user.getCommonName().equals("commonName");
        assert user.getHomeDirectory().equals("homeDirectory");
        assert user.getTelNumber().equals("telNumber");
    }

    @Test
    void LdapGroupResponseBodyTest() {
        LdapGroupResponseBody ldapGroupResponseBody = new LdapGroupResponseBody("dn", "cn", "description", null);
        assert ldapGroupResponseBody.getDn().equals("dn");
        assert ldapGroupResponseBody.getCn().equals("cn");
        assert ldapGroupResponseBody.getDescription().equals("description");
        assert ldapGroupResponseBody.toString() != null;
        ldapGroupResponseBody.setDn("aaa");
        ldapGroupResponseBody.setCn("aaa");
        ldapGroupResponseBody.setDescription("aaa");
        assert ldapGroupResponseBody.getDn().equals("aaa");
        assert ldapGroupResponseBody.getCn().equals("aaa");
        assert ldapGroupResponseBody.getDescription().equals("aaa");

    }

    @Test
    void LdapGroupSearchBodyTest() {
        LdapGroupSearchBody test = new LdapGroupSearchBody("fulltext");
        assert test.getFulltext().equals("fulltext");
        assert test.toString() != null;
        test.setFulltext("aaa");
        assert test.getFulltext().equals("aaa");
    }

    @Test
    void LdapGroupRoleAssignRequestBodyTest() {
        LdapGroupRoleAssignRequestBody test = new LdapGroupRoleAssignRequestBody("groupDn", null);
        assert test.getGroupDn().equals("groupDn");
        assert test.toString() != null;

        LdapGroupRoleAssignRequestBody ldapGroupRoleAssignRequestBody = new LdapGroupRoleAssignRequestBody();
        ldapGroupRoleAssignRequestBody.setGroupDn("aaa");
        assert ldapGroupRoleAssignRequestBody.getGroupDn().equals("aaa");
    }

    @Test
    void createLdapUserTest() {
        LdapUser user = new LdapUser();
        assert user != null;
        User test = new User();
        user.loadFromUser(test);
        assert user!= null;
        LdapUser user2 = new LdapUser(new ObjectId());
        assert user2 != null;
        assert user2.getStringId() != null;
        LdapUser ldapUser = new LdapUser("dn", "commonName", "uid", "homeDirectory", "email", "name", "surname", null, "telNumber");
        assert ldapUser != null;
        assert ldapUser.getDn().equals("dn");

    }

}
