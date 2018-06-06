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

    /**
     * Get complete file path to the file
     * Path is generated as follow:
     * - always starts with directory storage/
     * - if generated flag is set to true nex folder generated/ is added
     * - saved file name consists of Case id, field import id and original file name separated by dash
     * @param caseId
     * @return path to the saved file
     */
    String getFilePath(String caseId) {
        return "storage/" + (this.generated ? "generated/" : "") + caseId + "-" + getStringId() + "-" + getValue()
    }

    boolean isGenerated() {
        return generated
    }
}