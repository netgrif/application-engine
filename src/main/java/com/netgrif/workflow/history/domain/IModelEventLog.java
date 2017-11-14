package com.netgrif.workflow.history.domain;

import java.io.File;

public interface IModelEventLog {

    void setModel(File model);

    File getModel();
}