package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.TextField


class TextValidationDelegate extends ValidationDelegate {

    TextValidationDelegate(Field field) {
        super(field)
    }

    def length = { n -> ((String) field.value).length() <= n }
    def regex = { r -> true}
}
