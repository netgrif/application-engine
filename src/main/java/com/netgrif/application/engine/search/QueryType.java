package com.netgrif.application.engine.search;

public enum QueryType {
    PROCESS,
    CASE,
    TASK,
    USER;

    public static QueryType fromString(String type) {
        switch (type) {
            case "process":
                return PROCESS;
            case "case":
                return CASE;
            case "task":
                return TASK;
            case "user":
                return USER;
            default:
                return null;
        }
    }
}
