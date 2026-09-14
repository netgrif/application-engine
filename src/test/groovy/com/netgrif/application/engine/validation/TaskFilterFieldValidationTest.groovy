package com.netgrif.application.engine.validation

import com.netgrif.application.engine.TestHelper
import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.validation.domain.ValidationDataInput
import com.netgrif.application.engine.validation.models.TaskFilterFieldValidation
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

import static org.junit.jupiter.api.Assertions.assertThrows

@SpringBootTest
@ActiveProfiles(["test"])
@ExtendWith(SpringExtension.class)
class TaskFilterFieldValidationTest {

    public static final String ErrorMessage = "Invalid Field value"
    @Autowired
    private TestHelper testHelper

    @BeforeEach
    void setup() {
        testHelper.truncateDbs()
    }

    @Test
    void querySuccessTest() {
        TaskFilterFieldValidation validation = new TaskFilterFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue("tasks: caseId eq 'someCaseId'")
        I18nString validMessage = new I18nString(ErrorMessage)
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        validation.query(input)
    }

    @Test
    void queryWrongResourceTypeTest() {
        TaskFilterFieldValidation validation = new TaskFilterFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue("cases: title eq 'myTitle'")
        I18nString validMessage = new I18nString(ErrorMessage)
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        assertThrows(IllegalArgumentException.class, () -> validation.query(input))
    }


    @Test
    void queryWrongQueryTest() {
        TaskFilterFieldValidation validation = new TaskFilterFieldValidation()
        DataField dataField = new DataField()
        dataField.setValue("tasks: caseIddddddd eq 'someCaseId'")
        I18nString validMessage = new I18nString(ErrorMessage)
        List<String> rules = []
        ValidationDataInput input = new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" ")))

        assertThrows(IllegalArgumentException.class, () -> validation.query(input))
    }

}
