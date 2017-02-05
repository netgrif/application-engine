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
}
