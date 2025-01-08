package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.workflow.domain.dataset.BooleanField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ValidationDelegate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class BooleanFieldValidationTest {

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
    void requiredTrue() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new BooleanField(rawValue: true)

        assert delegate.requiredTrue()
    }
    @Test
    void requiredTrue_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new BooleanField(rawValue: false)

        assert !delegate.requiredTrue()
    }

    @Test
    void notempty() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new BooleanField(rawValue: true)

        assert delegate.isNotEmpty()
    }

    @Test
    void notempty_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new BooleanField(rawValue: null)

        assert !delegate.isNotEmpty()
    }
}
