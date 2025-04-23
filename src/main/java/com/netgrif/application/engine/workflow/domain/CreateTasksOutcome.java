package com.netgrif.application.engine.workflow.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CreateTasksOutcome {
    private Case useCase;
    private List<Task> tasks;
}
