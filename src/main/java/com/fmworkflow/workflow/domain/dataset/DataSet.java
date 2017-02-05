package com.fmworkflow.workflow.domain.dataset;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class DataSet {

    @org.springframework.data.mongodb.core.mapping.Field("lastupdate")
    private DateTime lastUpdate;
    private List<Field> fields;

    public DataSet() {
        lastUpdate = new DateTime();
        fields = new ArrayList<>();
    }

    public DataSet(DateTime lastUpdate) {
        this();
        this.lastUpdate = lastUpdate;
    }

    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    public void addField(Field field){
        fields.add(field);
    }

    public DataSet getFieldsForTransition(String transitionId){
        DataSet partial = new DataSet(this.getLastUpdate());
        for(Field field:this.getFields()){
            if(field.getDisplayForTransitions().contains(transitionId)){
                partial.addField(field);
            } else if(field.getEditableForTransitions().contains(transitionId)){
                field.setEditable(true);
                partial.addField(field);
            }
        }

        return partial;
    }

    public DataSet copy(){
        DataSet copied = new DataSet(this.getLastUpdate());
        for(Field field:this.getFields()){
            copied.addField(field.copy());
        }

        return copied;
    }
}
