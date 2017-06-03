package com.netgrif.workflow.importer.model;

import com.netgrif.workflow.importer.model.datalogic.ImportAutoPlus;

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