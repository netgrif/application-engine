package com.netgrif.workflow.petrinet.domain.dataset

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import org.springframework.data.mongodb.core.mapping.Document

@Document
class FileField extends FieldWithDefault<String> {

    private boolean generated = false

    FileField() {
        super()
    }

    @Override
    FieldType getType() {
        return FieldType.FILE
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

//    @Override
    void addAction(String action, String trigger) {
//        super.addAction(action, trigger)
        this.generated = (Action.ActionTrigger.fromString(trigger) == Action.ActionTrigger.GET && action.contains("generate")) || this.generated
    }

    String getFilePath(String fileName) {
        return "storage/" + (this.generated ? "generated/" : "") + getStringId() + "-" + fileName
    }

    boolean isGenerated() {
        return generated
    }
}