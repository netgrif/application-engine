package com.netgrif.application.engine.authorization.service.interfaces;

public interface ITaskAuthorizationService {
    boolean canCallAssign(String taskId);
    boolean canCallCancel(String taskId);
    boolean canCallReassign(String taskId);
    boolean canCallFinish(String taskId);
    boolean canCallSetData(String taskId);
    boolean canCallSaveFile(String taskId);
}
