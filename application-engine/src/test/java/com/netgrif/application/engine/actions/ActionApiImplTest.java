package com.netgrif.application.engine.actions;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.adapter.spring.actions.ActionApi;
import com.netgrif.application.engine.adapter.spring.actions.ProcessAvailabilities;
import com.netgrif.application.engine.adapter.spring.actions.ProcessAvailability;
import com.netgrif.application.engine.objects.auth.dto.AuthPrincipalDto;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.runner.SuperCreatorRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class ActionApiImplTest {

    private static final String PROCESS_IDENTIFIER = "data/all_data";
    private static final String MISSING_PROCESS_IDENTIFIER = "missing_process";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ActionApi actionApi;

    @Autowired
    private ImportHelper importHelper;

    @Autowired
    private SuperCreatorRunner superCreator;

    private AuthPrincipalDto superUserPrincipal;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        superUserPrincipal = new AuthPrincipalDto(
                superCreator.getSuperUser().getUsername(),
                superCreator.getSuperUser().getRealmId(),
                null
        );
    }

    @Test
    public void processAvailabilityReflectsImportedProcesses() {
        ProcessAvailability missingAvailability = actionApi.getProcessAvailability(PROCESS_IDENTIFIER);

        assertTrue(missingAvailability.isNotFound());
        assertFalse(actionApi.isProcessUp(PROCESS_IDENTIFIER));
        assertFalse(actionApi.isProcessDown(PROCESS_IDENTIFIER));

        PetriNet net = importAllDataNet();

        ProcessAvailability availability = actionApi.getProcessAvailability(net.getIdentifier());
        assertEquals(PROCESS_IDENTIFIER, availability.processIdentifier());
        assertTrue(availability.isUp());
        assertTrue(actionApi.isProcessUp(PROCESS_IDENTIFIER));
        assertFalse(actionApi.isProcessDown(PROCESS_IDENTIFIER));
    }

    @Test
    public void processAvailabilityAggregatesMixedProcessState() {
        importAllDataNet();

        ProcessAvailabilities availabilities = actionApi.getProcessAvailability(
                List.of(PROCESS_IDENTIFIER, MISSING_PROCESS_IDENTIFIER)
        );

        assertTrue(availabilities.isUp(PROCESS_IDENTIFIER));
        assertTrue(availabilities.isNotFound(MISSING_PROCESS_IDENTIFIER));
        assertTrue(availabilities.isAnyUp());
        assertTrue(availabilities.isAnyNotFound());
        assertFalse(availabilities.isAllUp());
    }

    @Test
    public void createFindAndDeleteCaseByActionApiUsesRealWorkflowServices() {
        importAllDataNet();

        CreateCaseEventOutcome created = actionApi.createCaseByIdentifier(
                PROCESS_IDENTIFIER,
                "Action API case",
                "blue",
                superUserPrincipal,
                Map.of("source", "action-api-test")
        );

        assertNotNull(created.getCase());
        assertEquals("Action API case", created.getCase().getTitle());

        Case found = actionApi.findCase(created.getCase().getStringId());
        assertNotNull(found);
        assertEquals(created.getCase().getStringId(), found.getStringId());
        assertEquals(PROCESS_IDENTIFIER, found.getProcessIdentifier());

        DeleteCaseEventOutcome deleted = actionApi.deleteCase(found.getStringId(), Map.of("source", "action-api-test"));

        assertNotNull(deleted);
        assertNotNull(deleted.getCase());
        assertEquals(found.getStringId(), deleted.getCase().getStringId());
    }

    @Test
    public void getSystemUserDtoMatchesSystemUser() {
        AuthPrincipalDto systemUserDto = actionApi.getSystemUserDto();

        assertNotNull(actionApi.getSystemUser());
        assertEquals(actionApi.getSystemUser().getUsername(), systemUserDto.username());
        assertEquals(actionApi.getSystemUser().getRealmId(), systemUserDto.realmId());
    }

    @Test
    public void createCaseRejectsMissingAuthPrincipal() {
        importAllDataNet();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> actionApi.createCaseByIdentifier(PROCESS_IDENTIFIER, "Invalid", "red", null, Map.of())
        );

        assertEquals("AuthPrincipalDto cannot be null.", exception.getMessage());
    }

    private PetriNet importAllDataNet() {
        Optional<PetriNet> net = importHelper.createNet("all_data.xml");
        assertTrue(net.isPresent());
        return net.get();
    }
}
