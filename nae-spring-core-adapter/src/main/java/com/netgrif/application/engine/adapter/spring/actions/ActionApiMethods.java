package com.netgrif.application.engine.adapter.spring.actions;

import java.util.Arrays;

public enum ActionApiMethods {

    GET_DATA("getData"),
    SET_DATA("setData"),
    FIND_CASE("findCase"),
    SEARCH_CASES("searchCases"),
    CREATE_CASE_BY_IDENTIFIER("createCaseByIdentifier"),
    DELETE_CASE("deleteCase"),
    FIND_TASK("findTask"),
    SEARCH_TASKS("searchTasks"),
    ASSIGN_TASK("assignTask"),
    CANCEL_TASK("cancelTask"),
    FINISH_TASK("finishTask"),
    SEARCH_USER("searchUsers");

    private String methodName;

    ActionApiMethods(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }

    public static ActionApiMethods fromMethodName(String methodName) {
        return Arrays.stream(ActionApiMethods.values())
                .filter(type -> type.methodName.equals(methodName))
                .findFirst()
                .orElse(null);
    }
}
