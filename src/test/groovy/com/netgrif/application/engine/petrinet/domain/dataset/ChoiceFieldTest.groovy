package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
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

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private SuperCreator superCreator;

    private def stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void testChoices() {
        def netOptional = petriNetService.importPetriNet(stream(LIMITS_NET_FILE), VersionType.MAJOR, superCreator.getLoggedSuper())
        assert netOptional.getNet() != null
        def net = netOptional.getNet()

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