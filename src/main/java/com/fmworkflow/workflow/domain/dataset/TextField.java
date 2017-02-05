package com.fmworkflow.workflow.domain.dataset;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Milan on 5.2.2017.
 */
@Document
public class TextField extends Field {

    public static final int TEXTFIELD_TYPE = 1;

    private String value;
    private Validation validate;

    public TextField() {
        super();
        this.setType(TEXTFIELD_TYPE);
    }

    public TextField(String value, Validation validate) {
        this();
        this.value = value;
        this.validate = validate;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Validation getValidate() {
        return validate;
    }

    public void setValidate(Validation validate) {
        this.validate = validate;
    }

    enum Validation {
        EMAIL,
        TEXT,
        URL
    }

    @Override
    public void modifyValue(String newValue) {
        this.value = newValue;
    }

    @Override
    public TextField copy(){
        TextField copied = new TextField(this.getValue(), this.getValidate());
        copied.set_id(this.get_id());
        copied.setDescription(this.getDescription());
        copied.setName(this.getName());

        Set<String> copiedDisplay = new HashSet<>();
        for(String transId:this.getDisplayForTransitions()){
            copiedDisplay.add(transId);
        }
        copied.setDisplayForTransitions(copiedDisplay);

        Set<String> copiedEdit = new HashSet<>();
        for(String transId: this.getEditableForTransitions()){
            copiedEdit.add(transId);
        }
        copied.setEditableForTransitions(copiedEdit);

        return copied;
    }


}
