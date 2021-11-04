package com.netgrif.workflow.importer

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.VersionType
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ImporterTestGroovy {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IWorkflowService workflowService

    public static final String FILE_NAME = "importer_upsert.xml"
    public static final String IDENTIFIER = "importer_upsert"

    private static final String NUMBER_FIELD = "number"
    private static final String TEXT_FIELD = "text"
    private static final String ENUMERATION_FIELD = "enumeration"
    private static final String ENUMERATION_MAP_FIELD = "enumeration_map"
    private static final String MULTICHOICE_FIELD = "multichoice"
    private static final String MULTICHOICE_MAP_FIELD = "multichoice_map"
    private static final String BOOLEAN_FIELD = "boolean"
    private static final String DATE_FIELD = "date"
    private static final String FILE_FIELD = "file"
    private static final String FILE_LIST_FIELD = "fileList"
    private static final String USER_FIELD = "user"
    private static final String DATETIME_FIELD = "datetime"
    private static final String BUTTON_FIELD = "button"

    @Test
    void upsertTest() {
        def net = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert net.present

        def upserted = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert upserted.present

        assert upserted.get().creationDate == net.get().creationDate
    }

    @Test
    void enumerationMultichoiceOptionsTest() throws IOException, MissingPetriNetMetaDataException {
        Optional<PetriNet> net = petriNetService.importPetriNet(new ClassPathResource("/initial_behavior.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper())

        assert net.isPresent()
        Case testCase = workflowService.createCase(net.get().stringId, "Test case", "", superCreator.loggedSuper)

        assert testCase.dataSet.get(NUMBER_FIELD).behavior.get("1") == [FieldBehavior.FORBIDDEN] as Set<FieldBehavior>
        assert testCase.dataSet.get(TEXT_FIELD).behavior.get("1") == [FieldBehavior.HIDDEN] as Set<FieldBehavior>
        assert testCase.dataSet.get(ENUMERATION_FIELD).behavior.get("1") == [FieldBehavior.VISIBLE] as Set<FieldBehavior>
        assert testCase.dataSet.get(ENUMERATION_MAP_FIELD).behavior.get("1") == [FieldBehavior.EDITABLE] as Set<FieldBehavior>
        assert testCase.dataSet.get(MULTICHOICE_FIELD).behavior.get("1") == [FieldBehavior.REQUIRED] as Set<FieldBehavior>
        assert testCase.dataSet.get(MULTICHOICE_MAP_FIELD).behavior.get("1") == [FieldBehavior.IMMEDIATE] as Set<FieldBehavior>
        assert testCase.dataSet.get(BOOLEAN_FIELD).behavior.get("1") == [FieldBehavior.OPTIONAL] as Set<FieldBehavior>
        assert testCase.dataSet.get(DATE_FIELD).behavior.get("1") == [FieldBehavior.EDITABLE, FieldBehavior.REQUIRED] as Set<FieldBehavior>
        assert testCase.dataSet.get(DATETIME_FIELD).behavior.get("1") == [FieldBehavior.IMMEDIATE, FieldBehavior.REQUIRED] as Set<FieldBehavior>
        assert testCase.dataSet.get(FILE_FIELD).behavior.get("1") == [FieldBehavior.IMMEDIATE, FieldBehavior.FORBIDDEN] as Set<FieldBehavior>
        assert testCase.dataSet.get(FILE_LIST_FIELD).behavior.get("1") == [FieldBehavior.HIDDEN, FieldBehavior.OPTIONAL] as Set<FieldBehavior>
        assert testCase.dataSet.get(USER_FIELD).behavior.get("1") == [FieldBehavior.HIDDEN, FieldBehavior.IMMEDIATE] as Set<FieldBehavior>
        assert testCase.dataSet.get(BUTTON_FIELD).behavior.get("1") == [FieldBehavior.EDITABLE, FieldBehavior.REQUIRED, FieldBehavior.IMMEDIATE] as Set<FieldBehavior>
    }
}
