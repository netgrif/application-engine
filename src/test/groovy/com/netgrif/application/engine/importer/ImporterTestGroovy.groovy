package com.netgrif.application.engine.importer


import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldBehavior
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ImporterTestGroovy {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private ITaskService taskService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IWorkflowService workflowService

    public static final String FILE_NAME = "importer_upsert.xml"
    public static final String IDENTIFIER = "importer_upsert"

    private static final String ENUMERATION_LIKE_MAP_FIELD = "enumeration_like_map"
    private static final String MULTICHOICE_LIKE_MAP_FIELD = "multichoice_like_map"
    private static final String ENUMERATION_FIELD = "enumeration"
    private static final String MULTICHOICE_FIELD = "multichoice"

    private static final String NUMBER_FIELD = "number"
    private static final String TEXT_FIELD = "text"
    private static final String ENUMERATION_MAP_FIELD = "enumeration_map"
    private static final String MULTICHOICE_MAP_FIELD = "multichoice_map"
    private static final String BOOLEAN_FIELD = "boolean"
    private static final String DATE_FIELD = "date"
    private static final String FILE_FIELD = "file"
    private static final String FILE_LIST_FIELD = "fileList"
    private static final String USER_FIELD = "user"
    private static final String DATETIME_FIELD = "datetime"
    private static final String BUTTON_FIELD = "button"

    @Test
    @Disabled
    void upsertTest() {
        def net = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert net.present

        def upserted = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert upserted.present

        assert upserted.get().creationDate == net.get().creationDate
    }

    @Test
    void thisKeywordInDataEventsTest() {
        PetriNet net = petriNetService.importPetriNet(new ClassPathResource("/this_kw_test.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net != null
        Case testCase = workflowService.createCase(net.stringId, "Test case", "", superCreator.loggedSuper).getCase()
        taskService.assignTask(testCase.getTasks().toList().get(0).getTask())
        testCase = workflowService.findOne(testCase.getStringId())
        assert testCase.getDataField("tester_text_field").getValue().equals("Hello world!")
    }

    @Test
    void initialBehaviorTest() {
        PetriNet net = petriNetService.importPetriNet(new ClassPathResource("/initial_behavior.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net
        Case testCase = workflowService.createCase(net.stringId, "Test case", "", superCreator.loggedSuper).getCase()

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

    @Test
    void enumerationMultichoiceOptionsTest() throws IOException, MissingPetriNetMetaDataException {
        PetriNet net = petriNetService.importPetriNet(new ClassPathResource("/enumeration_multichoice_options.xml").getInputStream(), VersionType.MAJOR, superCreator.getLoggedSuper()).getNet()

        assert net != null

        ChoiceField multichoice = (ChoiceField) net.getDataSet().get(MULTICHOICE_FIELD)
        ChoiceField multichoice_like_map = (ChoiceField) net.getDataSet().get(MULTICHOICE_LIKE_MAP_FIELD)
        ChoiceField enumeration = (ChoiceField) net.getDataSet().get(ENUMERATION_FIELD)
        ChoiceField enumeration_like_map = (ChoiceField) net.getDataSet().get(ENUMERATION_LIKE_MAP_FIELD)

        assert multichoice.getChoices() == multichoice_like_map.getChoices()
        assert enumeration.getChoices() == enumeration_like_map.getChoices()

        assert multichoice.getValue() == multichoice_like_map.getValue()
        assert enumeration.getValue() == enumeration_like_map.getValue()

        assert multichoice.getDefaultValue() == multichoice_like_map.getDefaultValue()
        assert enumeration.getDefaultValue() == enumeration_like_map.getDefaultValue()
    }
}
