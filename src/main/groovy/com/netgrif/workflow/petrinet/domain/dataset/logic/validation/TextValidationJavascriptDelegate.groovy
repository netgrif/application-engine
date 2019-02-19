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

    def regex = { r, String v = "regex" ->
        return setupJavascriptValidation(v,"!(new RegExp(\"${r}\").test(value))")
    }

    def email = {
        ((TextField)this.field).setFormatting("example@example.com")
        return regex(/[a-z0-9!#\\u0024%&'*+\\/=?^_`{|}~-]+(?:\\.[a-z0-9!#\\u0024%&'*+\\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/,"email")
    }

    def telnumber = {
        ((TextField)this.field).setFormatting("+421 907 123 456")
        return regex(/^(?:\\+?(\\d{1,3}))?([-. (]*(\\d{3})[-. )]*)?((\\d{3})[-. ]*(\\d{2,4})(?:[-.x ]*(\\d+))?)$/,"telnumber")
    }
}
