package com.fmworkflow.importer.model;

import com.fmworkflow.importer.model.datalogic.ImportPlusYears;

public interface DataLogic {
    Boolean getEditable();

    void setEditable(Boolean editable);

    Boolean getVisible();

    void setVisible(Boolean visible);

    ImportPlusYears getPlusYears();

    void setPlusYears(ImportPlusYears plusYears);

    Boolean getRequired();

    void setRequired(Boolean required);
}