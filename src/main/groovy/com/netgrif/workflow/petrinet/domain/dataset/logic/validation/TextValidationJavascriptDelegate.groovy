package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.TextField


class TextValidationJavascriptDelegate extends TextValidationDelegate{

    TextValidationJavascriptDelegate(Field field) {
        super(field)
    }

    def length = { int n ->
        ((TextField)this.field).setMaxLength((Integer)n)
        return javascriptNullControl("value","true")+setupJavascriptValidation("length","value.length > ${n}")
    }

    def regex = { r -> setupJavascriptValidation("regex","!(new RegExp(\"${r}\").test(value))") }
}
