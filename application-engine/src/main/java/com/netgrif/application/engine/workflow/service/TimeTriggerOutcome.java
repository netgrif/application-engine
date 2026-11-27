package com.netgrif.application.engine.workflow.service;

public enum TimeTriggerOutcome {

    FIRED,
    RESUMED,
    RESUME_FAILED,
    SKIPPED_TASK_MISSING,
    SKIPPED_TRANSITION_MISSING,
    SKIPPED_NOT_EXECUTABLE,
    SKIPPED_ASSIGNED,
    SKIPPED_ALREADY_FINISHED
}
