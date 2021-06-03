package com.netgrif.workflow.workflow.domain.eventoutcomes.taskoutcomes;

import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldsTree;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.EventOutcome;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class TaskEventOutcome extends EventOutcome {

    private Task task;
    private ChangedFieldsTree data;

    protected TaskEventOutcome(Task task) {
        this.task = task;
        this.data = ChangedFieldsTree.createNew(task.getCaseId(), task);
    }

    public void addChangedFieldsTree(ChangedFieldsTree changedFieldsTree){
        this.data.mergeChangedFields(changedFieldsTree);
    }
}
