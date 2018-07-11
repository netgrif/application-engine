package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.TestHelper
import com.netgrif.workflow.importer.service.Importer
import com.netgrif.workflow.ipc.TaskApiTest
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.startup.ImportHelper
import com.netgrif.workflow.workflow.domain.Case
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
class ChoiceFieldTest {

    public static final String LIMITS_NET_FILE = "case_choices_test.xml"
    public static final String LIMITS_NET_TITLE = "CCT"
    public static final String LIMITS_NET_INITIALS = "CCT"
    public static final String LEASING_NET_TASK_EDIT_COST = "Tran"

    @Autowired
    private Importer importer

    @Autowired
    private ImportHelper helper

    @Autowired
    private TestHelper testHelper

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @Before
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testChoices() {
        def netOptional = importer.importPetriNet(stream(LIMITS_NET_FILE), LIMITS_NET_TITLE, LIMITS_NET_INITIALS)
        assert netOptional.isPresent()
        def net = netOptional.get()

        Set<I18nString> choices = ((ChoiceField) net.dataSet["enum"]).choices
        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice 1" }
        assert choices.find { it.defaultValue == "Choice 2" }
        assert choices.find { it.defaultValue == "Choice 3" }

        Case choiceCase = helper.createCase("Choices", net)
        helper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        helper.setTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId, [
                "bool": [
                        value: true,
                        type : helper.FIELD_BOOLEAN
                ]
        ])
        List<Field> fields = helper.getTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Enum" }).choices

        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice 1" }
        assert choices.find { it.defaultValue == "Choice 2" }
        assert choices.find { it.defaultValue == "Choice 3" }

        helper.setTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId, [
                "bool": [
                        value: false,
                        type : helper.FIELD_BOOLEAN
                ]
        ])

        fields = helper.getTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Enum" }).choices

        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice A" }
        assert choices.find { it.defaultValue == "Choice B" }
        assert choices.find { it.defaultValue == "Choice C" }

        helper.setTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId, [
                "bool": [
                        value: true,
                        type : helper.FIELD_BOOLEAN
                ]
        ])

        fields = helper.getTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Enum" }).choices

        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice 1" }
        assert choices.find { it.defaultValue == "Choice 2" }
        assert choices.find { it.defaultValue == "Choice 3" }
    }
}