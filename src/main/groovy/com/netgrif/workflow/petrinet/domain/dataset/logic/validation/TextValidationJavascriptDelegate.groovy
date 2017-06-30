package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field


class TextValidationJavascriptDelegate extends TextValidationDelegate{

    TextValidationJavascriptDelegate(Field field) {
        super(field)
    }

    def length = { int n -> "if(field.value.length > ${n}) return false;" }

    def regex = { r -> "if(!(new RegExp(\"${r}\").test(field.value))) return false;" }
}
