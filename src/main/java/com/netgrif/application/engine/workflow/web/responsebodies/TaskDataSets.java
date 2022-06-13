package com.netgrif.application.engine.workflow.web.responsebodies;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskDataSets {

    private Map<String, DataSet> tasks;

}
