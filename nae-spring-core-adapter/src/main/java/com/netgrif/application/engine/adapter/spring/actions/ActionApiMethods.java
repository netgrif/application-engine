package com.netgrif.application.engine.adapter.spring.actions;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum ActionApiMethods {

    GET_DATA("getData"),
    SET_DATA("setData"),
    FIND_CASE("findCase"),
    COUNT_CASES("countCases"),
    SEARCH_CASES("searchCases"),
    CREATE_CASE_BY_IDENTIFIER("createCaseByIdentifier"),
    DELETE_CASE("deleteCase"),
    FIND_TASK("findTask"),
    SEARCH_TASKS("searchTasks"),
    ASSIGN_TASK("assignTask"),
    CANCEL_TASK("cancelTask"),
    FINISH_TASK("finishTask"),
    SEARCH_USERS("searchUsers"),
    GET_SYSTEM_USER("getSystemUser");

    private String methodName;

    private static final Map<String, ActionApiMethods> METHODS_BY_NAME_MAP = Arrays.stream(values()).collect(Collectors.toUnmodifiableMap(ActionApiMethods::getMethodName, e -> e));

    ActionApiMethods(String methodName) {
        this.methodName = methodName;
    }

    public String getMethodName() {
        return methodName;
    }


    public static ActionApiMethods fromMethodName(String methodName) {
        return METHODS_BY_NAME_MAP.get(methodName);
    }
}
