package com.netgrif.workflow.petrinet.domain.dataset.logic.validation

import com.netgrif.workflow.petrinet.domain.dataset.Field


class TextValidationDelegate extends ValidationDelegate {

    TextValidationDelegate(Field field) {
        super(field)
    }

    def length = { int n -> ((String) field.value).length() <= n }

    def regex = { r -> field.value ==~ r }

    def email = {
        regex(/[a-z0-9!#\u0024%&'*+\/=?^_`{|}~-]+(?:\.[a-z0-9!#\u0024%&'*+\/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?/)
    }

    def telnumber = {
        regex(/^\s*(?:\+?(\d{1,3}))?([-. (]*(\d{3})[-. )]*)?((\d{3})[-. ]*(\d{2,4})(?:[-.x ]*(\d+))?)\s*$/)
    }
}
