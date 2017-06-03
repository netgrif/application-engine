package com.netgrif.workflow.workflow.web.responsebodies;


import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.hateoas.ResourceAssembler;

public class TaskResourceAssembler implements ResourceAssembler<Task, TaskResource>{
    @Override
    public TaskResource toResource(Task task) {
        return new TaskResource(task);
    }
}
