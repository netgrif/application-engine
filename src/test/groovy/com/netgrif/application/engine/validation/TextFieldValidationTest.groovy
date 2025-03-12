package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ValidationDelegate

//import com.netgrif.application.engine.validation.domain.ValidationDataInput
//import com.netgrif.application.engine.validation.models.TextFieldValidation
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.util.stream.Collectors

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class TextFieldValidationTest {

    @Autowired
    private TestHelper testHelper

    private static ValidationDelegate getValidationDelegate() {
        return new ValidationDelegate()
    }

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void minLength() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.thisField = new TextField(rawValue: 'totok')

        assert !delegate.minLength(6)

        assert delegate.minLength(5)

        assert delegate.minLength(4)
    }

    @Test
    void maxLength() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.thisField = new TextField(rawValue: 'totok')

        assert !delegate.maxLength(4)

        assert delegate.maxLength(5)

        assert delegate.maxLength(6)
    }
}
