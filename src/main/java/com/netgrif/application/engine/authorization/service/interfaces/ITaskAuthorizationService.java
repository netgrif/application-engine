package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Task;

public interface ITaskAuthorizationService {
    boolean canCallAssign(String taskId);
    boolean canCallCancel(String taskId);
    boolean canCallReassign(String taskId);
    boolean canCallFinish(String taskId);
    boolean canCallSetData(String taskId);
    boolean canCallGetData(String taskId);
    boolean canCallGetData(Task task);
    boolean canCallSaveFile(String taskId);
}
