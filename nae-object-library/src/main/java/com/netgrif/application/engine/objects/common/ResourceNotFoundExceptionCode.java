package com.netgrif.application.engine.objects.common;

import lombok.Getter;

@Getter
public enum ResourceNotFoundExceptionCode {

    DEFAULT_SYSTEM_GROUP_NOT_FOUND("defaultSystemGroupNotFound"),
    DEFAULT_USER_GROUP_NOT_FOUND("defaultUserGroupNotFound");

    private final String key;

    ResourceNotFoundExceptionCode(String key) {
        this.key = key;
    }

}
