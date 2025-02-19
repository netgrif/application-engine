package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.roles.Role;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.service.interfaces.IRoleService;
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
class RoleServiceTest {

    private final static String ROLE_IMPORT_ID = "process_role";

    private final static String ROLE_IMPORT_ID2 = "process_role2";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IRoleService roleService;

    @Autowired
    private SuperCreator superCreator;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }


    @Test
    void shouldFindAllRoles() throws IOException, MissingPetriNetMetaDataException {
        List<Role> roles = roleService.findAll();
        int originalRoles = roles.size();
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/role_all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        roles = roleService.findAll();
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(originalRoles + 3, roles.size()); // + 2 roles from all_data and 1 role from role_all_data
    }

    @Test
    void shouldFindAllRolesByPetriNet() throws IOException, MissingPetriNetMetaDataException {
        ImportPetriNetEventOutcome eventOutcome = petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        List<Role> roles = roleService.findAll(eventOutcome.getNet().getStringId());
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(2, roles.size());
        assertTrue(roles.stream().anyMatch(role -> ROLE_IMPORT_ID.equals(role.getImportId())));
        assertTrue(roles.stream().anyMatch(role -> ROLE_IMPORT_ID2.equals(role.getImportId())));
    }

    @Test
    void shouldGetDefaultRole() {
        Role role = roleService.defaultRole();
        assertNotNull(role);
        assertEquals(Role.DEFAULT_ROLE, role.getImportId());
        assertEquals(Role.DEFAULT_ROLE, role.getName().getDefaultValue());
    }

    @Test
    void shouldGetAnonymousRole() {
        Role role = roleService.anonymousRole();
        assertNotNull(role);
        assertEquals(Role.ANONYMOUS_ROLE, role.getName().getDefaultValue());
        assertEquals(Role.ANONYMOUS_ROLE, role.getImportId());
    }

    @Test
    void shouldFindAllRolesByImportId() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Set<Role> roles = roleService.findAllByImportId(ROLE_IMPORT_ID);
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
        assertEquals(ROLE_IMPORT_ID, roles.stream().findFirst().get().getImportId());
    }

    @Test
    void shouldFindAllRolesByName() throws IOException, MissingPetriNetMetaDataException {
        petriNetService.importPetriNet(new FileInputStream("src/test/resources/all_data.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());
        Set<Role> roles = roleService.findAllByDefaultName("Process role");
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
        assertEquals(1, roles.size());
        assertEquals(ROLE_IMPORT_ID, roles.stream().findFirst().get().getImportId());
    }
}
