package com.netgrif.workflow.workflow

import com.netgrif.workflow.petrinet.domain.DataGroup
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.startup.SuperCreator
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.service.interfaces.IDataService
import com.netgrif.workflow.workflow.service.interfaces.ITaskService
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService
import com.netgrif.workflow.workflow.web.responsebodies.LocalisedField
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class TaskRefPropagationTest {

    @Autowired
    private ImportHelper importHelper

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private SuperCreator superCreator

    @Autowired
    private IDataService dataService

    @Autowired
    private IWorkflowService workflowService

    @Autowired
    private ITaskService taskService

    PetriNet netParent
    PetriNet netChild

    @Before
    void beforeAll() {
        def parent = petriNetService.importPetriNet(new FileInputStream("src/test/resources/taskRef_propagation_test_parent.xml"), "major", superCreator.getLoggedSuper())
        def child = petriNetService.importPetriNet(new FileInputStream("src/test/resources/taskRef_propagation_test_child.xml"), "major", superCreator.getLoggedSuper())

        assert parent.isPresent()
        assert child.isPresent()

        netParent = parent.get()
        netChild = child.get()
    }

    public static final String PARENT_FIELD_TEXT_ID = "text"
    public static final String PARENT_FIELD_TEXT_TITLE = "Text"

    public static final String PARENT_FIELD_TEXT_FROM_CHILD_ID = "text_from_child"
    public static final String PARENT_FIELD_TEXT_FROM_CHILD_TITLE = "Text from child"

    public static final String PARENT_FIELD_TEXT_FROM_CHILD_SETTER_ID = "text_from_child_setter"
    public static final String PARENT_FIELD_TEXT_FROM_CHILD_SETTER_TITLE = "Text from child setter"

    public static final String PARENT_FIELD_MULTICHOICE_ID = "multichoice"
    public static final String PARENT_FIELD_MULTICHOICE_TITLE = "Multichoice"

    public static final String PARENT_FIELD_MULTICHOICE_SETTER_ID = "multichoice_setter"
    public static final String PARENT_FIELD_MULTICHOICE_SETTER_TITLE = "Multichoice Setter"

    public static final String CHILD_FIELD_TEXT1_ID = "text1"
    public static final String CHILD_FIELD_TEXT1_TITLE = "Text1"

    public static final String CHILD_FIELD_TEXT2_ID = "text2"
    public static final String CHILD_FIELD_TEXT2_TITLE = "Text2"

    public static final String CHILD_FIELD_TEXT3_ID = "text3"
    public static final String CHILD_FIELD_TEXT3_TITLE = "Text3"

    public static final String CHILD_FIELD_TEXT4_ID = "text4"
    public static final String CHILD_FIELD_TEXT4_TITLE = "Text4"

    public static final String CHILD_FIELD_TEXT5_ID = "text5"
    public static final String CHILD_FIELD_TEXT5_TITLE = "Text5"

    public static final String CHILD_FIELD_TEXT6_ID = "text6"
    public static final String CHILD_FIELD_TEXT6_TITLE = "Text6"

    @Test
    void testTaskRefSetDataPropagation() {
        /* init */
        Case parent = importHelper.createCase("PARENT", netParent)
        Case child = importHelper.createCase("CHILD", netChild)

        String parentTaskId = parent.tasks.find { it.transition == "4" }.task
        String childTaskId = child.tasks.find { it.transition == "4" }.task
        parent.dataSet["children_tasks"].value = [childTaskId]
        child.dataSet["parentId"].value = parent.stringId

        workflowService.save(parent)
        workflowService.save(child)

        /* validate getDataGroups object and taskRef field ids */
        List<DataGroup> parentData = dataService.getDataGroups(parentTaskId, Locale.forLanguageTag("SK"))
        LocalisedField parentText = findField(parentData, PARENT_FIELD_TEXT_TITLE)
        LocalisedField parentMultichoice = findField(parentData, PARENT_FIELD_MULTICHOICE_TITLE)
        LocalisedField parentMultichoiceSetter = findField(parentData, PARENT_FIELD_MULTICHOICE_SETTER_TITLE)

        assert parentText.stringId == PARENT_FIELD_TEXT_ID

        LocalisedField childText1 = findField(parentData, CHILD_FIELD_TEXT1_TITLE)
        LocalisedField childText2 = findField(parentData, CHILD_FIELD_TEXT2_TITLE)
        LocalisedField childText3 = findField(parentData, CHILD_FIELD_TEXT3_TITLE)
        LocalisedField childText4 = findField(parentData, CHILD_FIELD_TEXT4_TITLE)
        LocalisedField childText5 = findField(parentData, CHILD_FIELD_TEXT5_TITLE)
        LocalisedField childText6 = findField(parentData, CHILD_FIELD_TEXT6_TITLE)
        assert childText1.stringId == childTaskId + "-" + CHILD_FIELD_TEXT1_ID
        assert childText2.stringId == childTaskId + "-" + CHILD_FIELD_TEXT2_ID
        assert childText3.stringId == childTaskId + "-" + CHILD_FIELD_TEXT3_ID
        assert childText4.stringId == childTaskId + "-" + CHILD_FIELD_TEXT4_ID
        assert childText5.stringId == childTaskId + "-" + CHILD_FIELD_TEXT5_ID
        assert childText6.stringId == childTaskId + "-" + CHILD_FIELD_TEXT6_ID

        /* test propagation Parent -> Child -> Parent */
        ChangedFieldsTree changed = dataService.setData(parentTaskId, ImportHelper.populateDataset([
                (PARENT_FIELD_TEXT_ID): ["value": "VALUE", "type": "text"]
        ]))

        ChangedFieldContainer container = changed.flatten()
        assert container.changedFields[PARENT_FIELD_TEXT_FROM_CHILD_SETTER_ID].get("value") == "VALUE-propagated-down-post-propagated-up"
        assert container.changedFields[PARENT_FIELD_TEXT_FROM_CHILD_ID].get("value") == "VALUE-propagated-down-post-propagated-up"

        assert container.changedFields[childText3.stringId].get("value") == "-propagated-down-pre"
        assert container.changedFields[childText1.stringId].get("value") == "VALUE-propagated-down-post"
        assert container.changedFields[childText2.stringId].get("value") == "VALUE-propagated-down-post"
        assert container.changedFields[childText4.stringId].get("value") == "TEXT_4_VALUE"

        parent = workflowService.findOne(parent.stringId)
        assert parent.dataSet[PARENT_FIELD_TEXT_ID].value == "VALUE"
        assert parent.dataSet[PARENT_FIELD_TEXT_FROM_CHILD_ID].value == "VALUE-propagated-down-post-propagated-up"
        assert parent.dataSet[PARENT_FIELD_TEXT_FROM_CHILD_SETTER_ID].value == "VALUE-propagated-down-post-propagated-up"

        child = workflowService.findOne(child.stringId)
        assert child.dataSet[CHILD_FIELD_TEXT3_ID].value == "-propagated-down-pre"
        assert child.dataSet[CHILD_FIELD_TEXT1_ID].value == "VALUE-propagated-down-post"
        assert child.dataSet[CHILD_FIELD_TEXT2_ID].value == "VALUE-propagated-down-post"
        assert child.dataSet[CHILD_FIELD_TEXT4_ID].value == "TEXT_4_VALUE"


        /* test multichoice value and choices setting Child -> Parent */
        List<String> choices = ["CHOICE1", "CHOICE2", "CHOICE3"].sort()
        String setterValue = choices.join(";")
        changed = dataService.setData(parentTaskId, ImportHelper.populateDataset([
                (childText5.stringId): ["value": setterValue, "type": "text"],
        ]))
        container = changed.flatten()

        parent = workflowService.findOne(parent.stringId)
        assert parent.dataSet[PARENT_FIELD_MULTICHOICE_ID].choices.collect {it as String }.sort() == choices
        assert parent.dataSet[PARENT_FIELD_MULTICHOICE_SETTER_ID].value == setterValue

        assert (container.changedFields[parentMultichoice.stringId].get("choices") as List).collect { it as String }.sort() == choices
        assert container.changedFields[parentMultichoiceSetter.stringId].get("value") == setterValue

        String multiValue = "CHOICE1"
        changed = dataService.setData(parentTaskId, ImportHelper.populateDataset([
                (childText6.stringId): ["value": multiValue, "type": "text"],
        ]))
        container = changed.flatten()

        parent = workflowService.findOne(parent.stringId)
        assert parent.dataSet[PARENT_FIELD_MULTICHOICE_ID].value.collect { it as String }.sort() == [multiValue]
        assert (container.changedFields[parentMultichoice.stringId].get("value") as List).collect { it as String }.sort() == [multiValue]

    }

    LocalisedField findField(List<DataGroup> dataGroups, String fieldTitle) {
        def fieldDataGroup = dataGroups.find {it -> it.fields.find( {field -> (field.name == fieldTitle) }) != null}
        assert fieldDataGroup != null
        LocalisedField field = fieldDataGroup.fields.find( { field -> (field.name == fieldTitle) }) as LocalisedField
        assert field != null
        return field
    }

}
