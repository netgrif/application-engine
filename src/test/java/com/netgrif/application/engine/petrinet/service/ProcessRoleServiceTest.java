package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class ProcessRoleServiceTest {

    private final static String ROLE_IMPORT_ID = "process_role";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }


    @Test
    void shouldFindAllProcessRoles() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/role_all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        List<ProcessRole> roles = processRoleService.findAll();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(6, roles.size());
    }

    @Test
    void shouldFindAllProcessRolesByPetriNet() throws IOException, MissingPetriNetMetaDataException {
        ImportPetriNetEventOutcome eventOutcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        List<ProcessRole> roles = processRoleService.findAll(eventOutcome.getNet().getStringId());
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
        assertEquals(ROLE_IMPORT_ID, roles.get(0).getImportId());
    }

    @Test
    void shouldGetDefaultRole() {
        ProcessRole role = processRoleService.defaultRole();
        assertNotNull(role);
        assertEquals(ProcessRole.DEFAULT_ROLE, role.getName().getDefaultValue());
    }

    @Test
    void shouldGetAnonymousRole() {
        ProcessRole role = processRoleService.anonymousRole();
        assertNotNull(role);
        assertEquals(ProcessRole.ANONYMOUS_ROLE, role.getName().getDefaultValue());
        assertEquals(ProcessRole.ANONYMOUS_ROLE, role.getImportId());
    }

    @Test
    void shouldFindAllProcessRolesByImportId() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Set<ProcessRole> roles = processRoleService.findAllByImportId(ROLE_IMPORT_ID);
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
        assertEquals(ROLE_IMPORT_ID, roles.stream().findFirst().get().getImportId());
    }

    @Test
    void shouldFindAllProcessRolesByName() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Set<ProcessRole> roles = processRoleService.findAllByDefaultName("Process role");
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
        assertEquals(ROLE_IMPORT_ID, roles.stream().findFirst().get().getImportId());
    }
}
