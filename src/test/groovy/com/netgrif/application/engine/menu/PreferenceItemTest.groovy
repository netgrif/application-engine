package com.netgrif.application.engine.menu

import com.netgrif.application.engine.EngineTest
import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.startup.ImportHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PreferenceItemTest {

    @Autowired
    ImportHelper importHelper
    @Autowired
    TestHelper testHelper

    @Test
    void createMenuItem() {
//        testHelper.truncateDbs()
        def menu_import = importHelper.createNet("menu_import.xml")
    }
}
