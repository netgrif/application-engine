package com.netgrif.application.engine.workflow

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.dataset.Field
import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService
import com.netgrif.application.engine.startup.ImportHelper
import com.netgrif.application.engine.startup.SuperCreator
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.junit.Assert
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.context.WebApplicationContext

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class InitValuesTest extends EngineTest {

    @Test
    void testInitValues() {
        def importResult = importHelper.createNet("init_values_test.xml")
        assert importResult.isPresent()

        def net = importResult.get()
        def createCase = importHelper.createCase("Test case", net)
        assert createCase

        def staticFieldIds = createCase.dataSet.fields.keySet().findAll { it.endsWith("_static") }
        def dynamicFieldIds = createCase.dataSet.fields.keySet().findAll { it.endsWith("_dynamic") }
        staticFieldIds.each { staticId ->
            def id = staticId.replace("_static", "")
            Field<?> staticInitField = createCase.dataSet.get("${id}_static")
            Field<?> dynamicInitField = createCase.dataSet.get("${id}_dynamic")
            dynamicFieldIds.remove(dynamicInitField.importId)

            assert staticInitField.rawValue == dynamicInitField.rawValue && staticInitField.rawValue != null
        }
        dynamicFieldIds.each { dynamicId ->
            Field<?> dynamicInitField = createCase.dataSet.get(dynamicId)

            Assert.assertTrue(dynamicId, dynamicInitField.rawValue != null)
            if (dynamicInitField.rawValue instanceof Collection) {
                Assert.assertFalse(dynamicId, (dynamicInitField.rawValue as Collection).isEmpty())
            }
        }
    }
}
