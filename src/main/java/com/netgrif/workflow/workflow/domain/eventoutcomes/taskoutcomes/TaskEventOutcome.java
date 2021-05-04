package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public abstract class TaskEventOutcome extends EventOutcome {

    private Task task;
    private ChangedFieldsTree data;
    private User assignee;
    private LocalDateTime startDate;
    private LocalDateTime finishDate;
    private String caseId;

    public TaskEventOutcome(Task task, String caseId) {
        this.task = task;
        this.caseId = caseId;
        this.data = ChangedFieldsTree.createNew(caseId, task);
    }

    public void addChangedFieldsTree(ChangedFieldsTree changedFieldsTree){
        this.data.mergeChangedFields(changedFieldsTree);
    }
}
