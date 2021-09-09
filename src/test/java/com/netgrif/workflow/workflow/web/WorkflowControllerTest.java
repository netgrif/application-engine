//package com.netgrif.workflow.workflow.web;
//
//import com.netgrif.workflow.auth.service.AuthorityService;
//import com.netgrif.workflow.petrinet.domain.PetriNet;
//import com.netgrif.workflow.petrinet.domain.VersionType;
//import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;
//import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
//import com.netgrif.workflow.startup.SuperCreator;
//import groovy.json.JsonOutput;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.util.Optional;
//
//import static org.springframework.http.MediaType.APPLICATION_JSON;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
//import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@ActiveProfiles({"test"})
//@ExtendWith(SpringExtension.class)
//public class WorkflowControllerTest {
//
////    @Autowired
////    private MongoTemplate mongoTemplate;
//
//    @Autowired
//    private AuthorityService authorityService;
//
//    @Autowired
//    private IPetriNetService petriNetService;
//
//    @Autowired
//    private SuperCreator superCreator;
//
////    @Autowired
////    private WorkflowController controller;
//
//    @Autowired
//    private WebApplicationContext wac;
//
//    private PetriNet net;
//
//    private MockMvc mvc;
//
//    private Authentication adminAuth;
//
//    private static final String CREATE_CASE_URL = "/api/workflow/case";
//    private static final String ADMIN_USER_EMAIL = "super@netgrif.com";
//
//// TODO:  Before -> beforeeach
//    @BeforeEach
//    public void beforeAll() throws IOException, MissingPetriNetMetaDataException {
//        //todo: drop db
//
//        Optional<PetriNet> net = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
//        assert net.isPresent();
//        this.net = net.get();
//
//        mvc = MockMvcBuilders
//                .webAppContextSetup(wac)
//                .apply(springSecurity())
//                .build();
//
//        adminAuth = new UsernamePasswordAuthenticationToken(ADMIN_USER_EMAIL, "password", authorityService.findAll());
//
//    }
//
//// TODO:  After -> AfterEach
//    @@AfterEach
//    public void after() {
//        //TODO: drop DB
//
//    }
//
//
//    @Test
//    public void createCase() throws Exception {
//        // TODO: 4. 2. 2017
////        workflowService.createCase(net.getStringId(), "Storage Unit " + i, randomColor());
//
//        mvc.perform(post(CREATE_CASE_URL)
//                .content(JsonOutput.toJson("{'color':'color-fg-fm-500', 'netId': '" + net.getStringId() + "', 'title': 'Test'}"))
//                .contentType(APPLICATION_JSON)
//                .with(authentication(this.adminAuth)))
//                .andExpect(status().isOk());
//
//    }
//}