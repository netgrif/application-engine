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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
class ActionApiImplTest {

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
    void before() {
        testHelper.truncateDbs();
        superUserPrincipal = new AuthPrincipalDto(
                superCreator.getSuperUser().getUsername(),
                superCreator.getSuperUser().getRealmId(),
                null
        );
    }

    @Test
    void processAvailabilityReflectsImportedProcesses() {
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
    void processAvailabilityAggregatesMixedProcessState() {
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
    void createFindAndDeleteCaseByActionApiUsesRealWorkflowServices() {
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
        String deletedCaseId = found.getStringId();
        assertThrows(
                IllegalArgumentException.class,
                () -> actionApi.findCase(deletedCaseId)
        );
    }

    @Test
    void projectedCaseOperationsAndPartialSaveViaActionApi() {
        importAllDataNet();

        CreateCaseEventOutcome created = actionApi.createCaseByIdentifier(
                PROCESS_IDENTIFIER,
                "Projected API case",
                "green",
                superUserPrincipal,
                Map.of("source", "action-api-test")
        );

        String caseId = created.getCase().getStringId();
        Case projectedCase = actionApi.findCase(caseId, List.of("text"));
        assertNotNull(projectedCase);
        assertTrue(projectedCase.isPartiallyLoaded());
        assertTrue(projectedCase.getDataSet().containsKey("text"));

        List<Case> projectedList = actionApi.findCases(List.of(caseId), List.of("text"));
        assertEquals(1, projectedList.size());
        assertTrue(projectedList.get(0).isPartiallyLoaded());

        projectedCase.setTitle("Updated Projected Case Title");
        Case savedPartial = actionApi.savePartial(projectedCase);
        assertNotNull(savedPartial);
        assertEquals("Updated Projected Case Title", savedPartial.getTitle());

        Case refreshed = actionApi.findCase(caseId);
        assertEquals("Updated Projected Case Title", refreshed.getTitle());

        com.netgrif.application.engine.objects.workflow.domain.QCase qCase = new com.netgrif.application.engine.objects.workflow.domain.QCase("case");
        org.springframework.data.domain.Page<Case> searched = actionApi.searchCases(
                PROCESS_IDENTIFIER,
                qCase.title.eq("Updated Projected Case Title"),
                org.springframework.data.domain.PageRequest.of(0, 10),
                List.of("text")
        );
        assertEquals(1, searched.getTotalElements());
        assertTrue(searched.getContent().get(0).isPartiallyLoaded());
    }

    @Test
    void getSystemUserDtoMatchesSystemUser() {
        AuthPrincipalDto systemUserDto = actionApi.getSystemUserDto();

        assertNotNull(actionApi.getSystemUser());
        assertEquals(actionApi.getSystemUser().getUsername(), systemUserDto.username());
        assertEquals(actionApi.getSystemUser().getRealmId(), systemUserDto.realmId());
    }

    @Test
    void createCaseRejectsMissingAuthPrincipal() {
        importAllDataNet();
        Map<String, String> params = Map.of();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> actionApi.createCaseByIdentifier(PROCESS_IDENTIFIER, "Invalid", "red", null, params)
        );

        assertEquals("AuthPrincipalDto cannot be null.", exception.getMessage());
    }

    @Test
    void importCaseReaderPluginTestPetriNet() {
        Optional<PetriNet> netOptional = importHelper.createNet("case_reader_plugin_test.xml");
        assertTrue(netOptional.isPresent());
        PetriNet net = netOptional.get();
        assertEquals("case_reader_plugin_test", net.getIdentifier());
        assertNotNull(net.getTransitions().get("t_main"));
        assertNotNull(net.getTransitions().get("helper_task"));
        assertEquals(21 * 2 + 3, net.getDataSet().size());
    }

    private PetriNet importAllDataNet() {
        Optional<PetriNet> net = importHelper.createNet("all_data.xml");
        assertTrue(net.isPresent());
        return net.get();
    }
}
