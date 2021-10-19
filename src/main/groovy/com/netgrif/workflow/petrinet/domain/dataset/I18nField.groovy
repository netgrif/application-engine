package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.I18nString
import org.springframework.data.mongodb.core.mapping.Document

@Document
class I18nField extends Field<I18nString> {

    I18nField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.I18N
    }

    @Override
    Field clone() {
        I18nField clone = new I18nField()
        super.clone(clone)
        return clone
    }
}
