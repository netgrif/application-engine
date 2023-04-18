package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.importer.service.Importer
import com.netgrif.application.engine.ipc.TaskApiTest
import com.netgrif.application.engine.petrinet.domain.DataRef
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
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
@CompileStatic
class ChoiceFieldTest extends EngineTest {

    public static final String LIMITS_NET_FILE = "case_choices_test.xml"
    public static final String LIMITS_NET_TITLE = "CCT"
    public static final String LIMITS_NET_INITIALS = "CCT"
    public static final String LEASING_NET_TASK_EDIT_COST = "Tran"

    private Closure<InputStream> stream = { String name ->
        return TaskApiTest.getClassLoader().getResourceAsStream(name)
    }

    @BeforeEach
    void setup() {
        truncateDbs()
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

        Case choiceCase = importHelper.createCase("Choices", net)
        importHelper.assignTaskToSuper(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        importHelper.setTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId, new DataSet([
                "bool": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>))
        List<DataRef> fields = importHelper.getTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        choices = ((EnumerationField) fields.find { it.field.name.defaultValue == "Enum" }.field).choices

        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice 1" }
        assert choices.find { it.defaultValue == "Choice 2" }
        assert choices.find { it.defaultValue == "Choice 3" }

        importHelper.setTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId, new DataSet([
                "bool": new BooleanField(rawValue: false)
        ] as Map<String, Field<?>>))

        fields = importHelper.getTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        choices = ((EnumerationField) fields.find { it.field.name.defaultValue == "Enum" }.field).choices

        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice A" }
        assert choices.find { it.defaultValue == "Choice B" }
        assert choices.find { it.defaultValue == "Choice C" }

        importHelper.setTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId, new DataSet([
                "bool": new BooleanField(rawValue: true)
        ] as Map<String, Field<?>>))

        fields = importHelper.getTaskData(LEASING_NET_TASK_EDIT_COST, choiceCase.stringId)
        choices = ((EnumerationField) fields.find { it.field.name.defaultValue == "Enum" }.field).choices

        assert choices.size() == 3
        assert choices.find { it.defaultValue == "Choice 1" }
        assert choices.find { it.defaultValue == "Choice 2" }
        assert choices.find { it.defaultValue == "Choice 3" }
    }
}