package com.netgrif.application.engine.petrinet.domain.dataset

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
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
class I18nFieldTest {

    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void beforeAll() {
        testHelper.truncateDbs()
    }

    @Test
    void testClearValue() {
        I18nField field = new I18nField()
        field.value = new I18nString("This is default value", ["sk": "SK: This is default value", "de": "DE: This is default value"])

        field.clearValue()

        assert field.value.defaultValue == null
        assert field.value.translations.size() == 0
    }
}
