package com.netgrif.application.engine.validation.service

import com.netgrif.application.engine.petrinet.domain.I18nString
import com.netgrif.application.engine.petrinet.domain.dataset.*
import com.netgrif.application.engine.validation.service.interfaces.IValidationService
import com.netgrif.application.engine.validation.domain.ValidationDataInput
import com.netgrif.application.engine.validation.models.*
import groovy.util.logging.Slf4j
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Service

import java.util.stream.Collectors

@Slf4j
@Service
class ValidationService implements IValidationService {

    @Override
    void valid(Field<?> dataField) {
        if (dataField.getValidations() == null) {
            return
        }
        dataField.getValidations().forEach(validation -> {
            List<String> rules = validation.getValidationRule().trim().split(" ").toList()
            if (rules.size() >= 1) {
                AbstractFieldValidation instance = new AbstractFieldValidation()
                if (dataField instanceof NumberField) {
                    instance = new NumberFieldValidation()
                } else if (dataField instanceof TextField) {
                    instance = new TextFieldValidation()
                } else if (dataField instanceof BooleanField) {
                    instance = new BooleanFieldValidation()
                } else if (dataField instanceof DateField) {
                    instance = new DateFieldValidation()
//                } else if (field instanceof DateTimeField) {
//                    instance = new DateTimeFieldValidation()
//                } else if (field instanceof ButtonField) {
//
//                } else if (field instanceof UserField) {
//
//                } else if (field instanceof DateField) {
//
//                } else if (field instanceof DateTimeField) {
//
//                } else if (field instanceof EnumerationField) {
//
//                } else if (field instanceof EnumerationMapField) {
//
//                } else if (field instanceof MultichoiceMapField) {
//
//                } else if (field instanceof MultichoiceField) {
//
//                } else if (field instanceof FileField) {
//
//                } else if (field instanceof FileListField) {
//
//                } else if (field instanceof UserListField) {
//
//                } else if (field instanceof I18nField) {
                }
                MetaMethod method = instance.metaClass.getMethods().find { it.name.toLowerCase() == rules.first().toLowerCase() }
                if (method != null) {
                    I18nString validMessage = validation.getValidationMessage() != null ? validation.getValidationMessage() : new I18nString("Invalid Field value")
                    method.invoke(instance, new ValidationDataInput(dataField, validMessage, LocaleContextHolder.getLocale(), rules.stream().skip(1).collect(Collectors.joining(" "))))
                } else {
                    log.warn("Method [" + rules.first() + "] in dataField " + dataField.getImportId() + " not found")
                }
            }
        })


    }

}