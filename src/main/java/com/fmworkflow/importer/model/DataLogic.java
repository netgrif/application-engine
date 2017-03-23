package com.fmworkflow.importer.model;

import com.fmworkflow.importer.model.datalogic.ImportAutoPlus;

public interface DataLogic {
    Boolean getEditable();

    void setEditable(Boolean editable);

    Boolean getVisible();

    void setVisible(Boolean visible);

    ImportAutoPlus getAutoPlus();

    void setAutoPlus(ImportAutoPlus autoPlus);

    Boolean getRequired();

    void setRequired(Boolean required);
}