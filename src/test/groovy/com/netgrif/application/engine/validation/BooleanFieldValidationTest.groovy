package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.validation.domain.ValidationDataInput
import com.netgrif.application.engine.validation.models.BooleanFieldValidation
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
class BooleanFieldValidationTest {


    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }


    @Test
    void requiredTrue() {
        BooleanFieldValidation booleanFieldValidation = new BooleanFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(true)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        booleanFieldValidation.requiredtrue(input)
    }

    @Test
    void notempty() {
        BooleanFieldValidation booleanFieldValidation = new BooleanFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue(true)
        I18nString validMessage = new I18nString("Invalid Field value")
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        booleanFieldValidation.notempty(input)
    }



}
