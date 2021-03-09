package com.netgrif.workflow.workflow.domain;

import lombok.Getter;

public enum FilterType {
    CASE("Case"),
    TASK("Task");

    @Getter
    public final String stringType;

    private FilterType(String stringType) {
        this.stringType = stringType;
    }

    public static FilterType resolveType(String stringType) {
        for (FilterType item : values()) {
            if (item.stringType.equals(stringType)) {
                return item;
            }
        }
        return null;
    }
}
