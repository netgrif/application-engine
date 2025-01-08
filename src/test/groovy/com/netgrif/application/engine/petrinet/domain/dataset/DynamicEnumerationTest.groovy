package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.workflow.domain.I18nString
import com.netgrif.application.engine.workflow.domain.VersionType
import com.netgrif.application.engine.utils.FullPageRequest
import com.netgrif.application.engine.workflow.domain.Task
import com.netgrif.application.engine.workflow.domain.dataset.EnumerationField
import com.netgrif.application.engine.workflow.domain.dataset.Field
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet
import groovy.transform.CompileStatic
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles(["test"])
@CompileStatic
@ExtendWith(SpringExtension.class)
class DynamicEnumerationTest extends EngineTest {

    @Test
    void testDynamicEnum() {
        ImportPetriNetEventOutcome optNet = petriNetService.importPetriNet(new FileInputStream("src/test/resources/test_autocomplete_dynamic.xml"), VersionType.MAJOR, superCreator.getLoggedSuper());

        assert optNet.getNet() != null;
        def net = optNet.getNet()

        def aCase = importHelper.createCase("Case", net)
        assert aCase != null

        Task task = taskService.findByCases(new FullPageRequest(), Collections.singletonList(aCase.getStringId())).stream().collect(Collectors.toList()).get(0);
        importHelper.assignTask("Autocomplete", aCase.getStringId(), superCreator.getLoggedSuper())

        def dataSet = new DataSet([
                "autocomplete": new EnumerationField(rawValue: new I18nString("Case"))
        ] as Map<String, Field<?>>)
        dataService.setData(task.stringId, dataSet, superCreator.getSuperUser())

        def caseOpt = caseRepository.findById(aCase.stringId)
        assert caseOpt.isPresent()
        aCase = caseOpt.get()

        EnumerationField field = aCase.dataSet.get("autocomplete") as EnumerationField
        assert field.choices.size() == 1
        assert field.choices.find { it.defaultValue == "Case" }
    }
}