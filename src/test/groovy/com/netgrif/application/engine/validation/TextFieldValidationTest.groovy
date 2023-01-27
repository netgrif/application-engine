package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.validation.domain.ValidationDataInput
import com.netgrif.application.engine.validation.models.TextFieldValidation
import com.netgrif.application.engine.workflow.domain.DataField
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

    public static final String ErrorMessage = "Invalid Field value"
    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void minlength_Exception() {
        TextFieldValidation textFieldValidation = new TextFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue("totok"  as String)
        I18nString validMessage = new I18nString(ErrorMessage)
        List<String> rules = ["minlength","6"]
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            textFieldValidation.minlength(input)
        })
        Assertions.assertEquals(ErrorMessage, thrown.getMessage());
    }

    @Test
    void maxlength_Exception() {
        TextFieldValidation textFieldValidation = new TextFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue("totok" as String)
        I18nString validMessage = new I18nString(ErrorMessage)
        List<String> rules = ["maxlength","4"]
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            textFieldValidation.maxlength(input)
        })
        Assertions.assertEquals(ErrorMessage, thrown.getMessage());
    }



}
