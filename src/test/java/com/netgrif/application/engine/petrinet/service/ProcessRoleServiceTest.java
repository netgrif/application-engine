package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.core.petrinet.domain.VersionType;
import com.netgrif.core.petrinet.domain.roles.ProcessRole;
import com.netgrif.core.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.adapter.petrinet.service.PetriNetService;
import com.netgrif.adapter.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import com.netgrif.core.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
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
    private PetriNetService petriNetService;

    @Autowired
    private ProcessRoleService processRoleService;

    @Autowired
    private SuperCreatorRunner superCreator;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }


    @Test
    void shouldFindAllProcessRoles() throws IOException, MissingPetriNetMetaDataException {
        List<ProcessRole> roles = processRoleService.findAll();
        int originalRoles = roles.size();
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/role_all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        roles = processRoleService.findAll();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(originalRoles + 3, roles.size()); // + 2 roles from all_data and 1 role from role_all_data
    }

    @Test
    void shouldFindAllProcessRolesByPetriNet() throws IOException, MissingPetriNetMetaDataException {
        ImportPetriNetEventOutcome eventOutcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        List<ProcessRole> roles = processRoleService.findAll(eventOutcome.getNet().getStringId());
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(role -> ROLE_IMPORT_ID.equals(role.getImportId())));
        assertTrue(roles.stream().anyMatch(role -> ROLE_IMPORT_ID2.equals(role.getImportId())));
    }

    @Test
    void shouldGetDefaultRole() {
        ProcessRole role = processRoleService.defaultRole();
        assertNotNull(role);
        assertEquals(ProcessRole.DEFAULT_ROLE, role.getImportId());
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
        Collection<ProcessRole> roles = processRoleService.findAllByDefaultName("Process role");
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
        assertEquals(ROLE_IMPORT_ID, roles.stream().findFirst().get().getImportId());
    }
}
