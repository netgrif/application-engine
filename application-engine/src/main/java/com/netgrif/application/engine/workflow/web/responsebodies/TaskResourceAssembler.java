package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.task.TaskDto;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import org.springframework.hateoas.server.RepresentationModelAssembler;

import java.util.Locale;

public class TaskResourceAssembler implements RepresentationModelAssembler<Task, LocalisedTaskResource> {

    private Locale locale;

    public TaskResourceAssembler(Locale locale) {
        this.locale = locale;
    }

    @Override
    public LocalisedTaskResource toModel(com.netgrif.application.engine.objects.workflow.domain.Task task) {
        return new LocalisedTaskResource(TaskDto.fromTask(task, locale));
    }
}
