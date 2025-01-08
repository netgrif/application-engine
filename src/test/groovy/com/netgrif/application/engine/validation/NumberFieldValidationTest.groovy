package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.workflow.domain.dataset.NumberField
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ValidationDelegate
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import static org.junit.jupiter.api.Assertions.assertThrows

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class NumberFieldValidationTest {

    private static final INF = 'inf'

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
    void odd() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 5)

        assert delegate.odd()
    }

    @Test
    void odd_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 4)

        assert !delegate.odd()
    }

    @Test
    void even() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 4)

        assert delegate.even()
    }

    @Test
    void even_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 5)

        assert !delegate.even()
    }

    @Test
    void positive() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 4)

        assert delegate.positive()
    }

    @Test
    void positive_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: -4)

        assert !delegate.positive()
    }

    @Test
    void negative() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: -4)

        assert delegate.negative()
    }

    @Test
    void negative_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 4)

        assert !delegate.negative()
    }

    @Test
    void decimal() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 4)

        assert delegate.decimal()
    }

    @Test
    void decimal_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 4.1)

        assert !delegate.decimal()
    }

    @Test
    void inrange() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 7)

        assert delegate.inrange(5, 10)

        assert delegate.inrange(INF, 10)

        assert delegate.inrange(5, INF)

        assert delegate.inrange(INF, INF)

        assert delegate.inrange("5", "10")
    }

    @Test
    void inrange_fail() {
        ValidationDelegate delegate = getValidationDelegate()
        delegate.field = new NumberField(rawValue: 7)

        assertThrows(NumberFormatException.class, { !delegate.inrange('totok', INF) })
    }
}
