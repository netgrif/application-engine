package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action
import org.springframework.data.mongodb.core.mapping.Document

@Document
public class FileField extends FieldWithDefault<String> {

    private boolean generated = false

    public FileField() {
        super();
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    @Override
    void addAction(String action, String trigger) {
        super.addAction(action, trigger)
        this.generated = (Action.ActionTrigger.fromString(trigger) == Action.ActionTrigger.GET && action.contains("generate")) || this.generated
    }

    String getFilePath(String fileName){
        return "storage/"+ (this.generated?"generated/":"") +getObjectId()+"-"+fileName
    }

    boolean isGenerated() {
        return generated
    }
}