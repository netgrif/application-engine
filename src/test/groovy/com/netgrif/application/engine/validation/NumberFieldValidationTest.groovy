package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.validation.domain.ValidationDataInput
import com.netgrif.application.engine.validation.models.NumberFieldValidation
import com.netgrif.application.engine.workflow.domain.DataField
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
class NumberFieldValidationTest {


    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }


    @Test
    void odd() {
        NumberFieldValidation numberFieldValidation = new NumberFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(5)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        numberFieldValidation.odd(input)
    }


    @Test
    void even() {
        NumberFieldValidation numberFieldValidation = new NumberFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(4)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        numberFieldValidation.even(input)
    }

    @Test
    void positive() {
        NumberFieldValidation numberFieldValidation = new NumberFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(4)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        numberFieldValidation.positive(input)
    }

    @Test
    void positivenegative() {
        NumberFieldValidation numberFieldValidation = new NumberFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(-4)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        numberFieldValidation.negative(input)
    }

    @Test
    void decimal() {
        NumberFieldValidation numberFieldValidation = new NumberFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(4)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        numberFieldValidation.decimal(input)
    }

    @Test
    void inrange() {
        NumberFieldValidation numberFieldValidation = new NumberFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(7)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = ["5,10"]
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        numberFieldValidation.inrange(input)
    }

}
