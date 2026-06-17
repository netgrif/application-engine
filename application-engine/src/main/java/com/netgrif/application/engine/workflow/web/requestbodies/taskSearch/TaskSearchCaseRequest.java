package com.netgrif.application.engine.workflow.web.requestbodies.taskSearch;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
public class TaskSearchCaseRequest implements Serializable {

    private static final long serialVersionUID = 757156715249997075L;

    public String id;
    public String title;
}
