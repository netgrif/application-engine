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
class EnumerationFieldTest {

    public static final String ENUMERATION_NET_FILE = "case_enumeration_test.xml"
    public static final String NET_TITLE = "CET"
    public static final String NET_INITIALS = "CET"
    public static final String NET_TASK_EDIT_COST = "Tran"

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
    void testEnumerationField() {
        def netOptional = importer.importPetriNet(stream(ENUMERATION_NET_FILE), NET_TITLE, NET_INITIALS)
        assert netOptional.isPresent()
        def net = netOptional.get()
        //Deprecated choices defined as list in petrinet
        Map<String, I18nString> choices = ((ChoiceField) net.dataSet["deprecated_enum"]).choices
        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice 1" }
        assert choices.values().find { it.defaultValue == "Choice 2" }
        assert choices.values().find { it.defaultValue == "Choice 3" }

        Case enumerationCase = helper.createCase("Enumeration", net)
        helper.assignTaskToSuper(NET_TASK_EDIT_COST, enumerationCase.stringId)
        helper.setTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId, [
                "bool_1": [
                        value: true,
                        type : helper.FIELD_BOOLEAN
                ]
        ])
        List<Field> fields = helper.getTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Deprecated enum" }).choices

        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice 1" }
        assert choices.values().find { it.defaultValue == "Choice 2" }
        assert choices.values().find { it.defaultValue == "Choice 3" }

        helper.setTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId, [
                "bool_1": [
                        value: false,
                        type : helper.FIELD_BOOLEAN
                ]
        ])

        fields = helper.getTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Deprecated enum" }).choices

        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice A" }
        assert choices.values().find { it.defaultValue == "Choice B" }
        assert choices.values().find { it.defaultValue == "Choice C" }

        helper.setTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId, [
                "bool_1": [
                        value: true,
                        type : helper.FIELD_BOOLEAN
                ]
        ])

        fields = helper.getTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Deprecated enum" }).choices

        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice 1" }
        assert choices.values().find { it.defaultValue == "Choice 2" }
        assert choices.values().find { it.defaultValue == "Choice 3" }

        // Choices defined as map in petrinet
        choices = ((ChoiceField) net.dataSet["enum"]).choices

        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice 1" }
        assert choices.values().find { it.defaultValue == "Choice 2" }
        assert choices.values().find { it.defaultValue == "Choice 3" }
        assert choices.keySet().find { it == "key_1" }
        assert choices.keySet().find { it == "key_2" }
        assert choices.keySet().find { it == "key_3" }

        helper.setTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId, [
                "bool_2": [
                        value: true,
                        type : helper.FIELD_BOOLEAN
                ]
        ])
        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice 1" }
        assert choices.values().find { it.defaultValue == "Choice 2" }
        assert choices.values().find { it.defaultValue == "Choice 3" }
        assert choices.keySet().find { it == "key_1" }
        assert choices.keySet().find { it == "key_2" }
        assert choices.keySet().find { it == "key_3" }

        helper.setTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId, [
                "bool_2": [
                        value: false,
                        type : helper.FIELD_BOOLEAN
                ]
        ])
        fields = helper.getTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId)
        choices = ((EnumerationField) fields.find { it.name.defaultValue == "Enum" }).choices
        assert choices.size() == 3
        assert choices.values().find { it.defaultValue == "Choice A" }
        assert choices.values().find { it.defaultValue == "Choice B" }
        assert choices.values().find { it.defaultValue == "Choice C" }
        assert choices.keySet().find { it == "key_a" }
        assert choices.keySet().find { it == "key_b" }
        assert choices.keySet().find { it == "key_c" }

        helper.setTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId, [
                "bool_3": [
                        value: true,
                        type : helper.FIELD_BOOLEAN
                ]
        ])
        fields = helper.getTaskData(NET_TASK_EDIT_COST, enumerationCase.stringId)
        def value = ((EnumerationField) fields.find { it.name.defaultValue == "Enum" }).value
        assert value.defaultValue == "key_1"
    }
}
