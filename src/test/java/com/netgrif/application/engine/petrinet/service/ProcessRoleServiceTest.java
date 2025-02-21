package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.authorization.domain.ProcessRole;
import com.netgrif.application.engine.authorization.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
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

    private final static String ROLE_IMPORT_ID2 = "process_role2";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IProcessRoleService roleService;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }


    @Test
    void shouldFindAllRoles() throws IOException, MissingPetriNetMetaDataException {
        List<ProcessRole> processRoles = roleService.findAll();
        int originalRoles = processRoles.size();
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/role_all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        processRoles = roleService.findAll();
        assertNotNull(processRoles);
        assertFalse(processRoles.isEmpty());
        assertEquals(originalRoles + 3, processRoles.size()); // + 2 roles from all_data and 1 role from role_all_data
    }

    @Test
    void shouldFindAllRolesByPetriNet() throws IOException, MissingPetriNetMetaDataException {
        ImportPetriNetEventOutcome eventOutcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        List<ProcessRole> processRoles = roleService.findAll(eventOutcome.getNet().getStringId());
        assertNotNull(processRoles);
        assertFalse(processRoles.isEmpty());
        assertEquals(2, processRoles.size());
        assertTrue(processRoles.stream().anyMatch(role -> ROLE_IMPORT_ID.equals(role.getImportId())));
        assertTrue(processRoles.stream().anyMatch(role -> ROLE_IMPORT_ID2.equals(role.getImportId())));
    }

    @Test
    void shouldGetDefaultRole() {
        ProcessRole processRole = roleService.defaultRole();
        assertNotNull(processRole);
        assertEquals(ProcessRole.DEFAULT_ROLE, processRole.getImportId());
        assertEquals(ProcessRole.DEFAULT_ROLE, processRole.getTitle().getDefaultValue());
    }

    @Test
    void shouldGetAnonymousRole() {
        ProcessRole processRole = roleService.anonymousRole();
        assertNotNull(processRole);
        assertEquals(ProcessRole.ANONYMOUS_ROLE, processRole.getTitle().getDefaultValue());
        assertEquals(ProcessRole.ANONYMOUS_ROLE, processRole.getImportId());
    }

    @Test
    void shouldFindAllRolesByImportId() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Set<ProcessRole> processRoles = roleService.findAllByImportId(ROLE_IMPORT_ID);
        assertNotNull(processRoles);
        assertFalse(processRoles.isEmpty());
        assertEquals(1, processRoles.size());
        assertEquals(ROLE_IMPORT_ID, processRoles.stream().findFirst().get().getImportId());
    }

    @Test
    void shouldFindAllRolesByName() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Set<ProcessRole> processRoles = roleService.findAllByDefaultTitle("Process role");
        assertNotNull(processRoles);
        assertFalse(processRoles.isEmpty());
        assertEquals(1, processRoles.size());
        assertEquals(ROLE_IMPORT_ID, processRoles.stream().findFirst().get().getImportId());
    }
}
