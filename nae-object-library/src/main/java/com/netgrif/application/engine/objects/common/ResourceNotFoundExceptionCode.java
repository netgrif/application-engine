package com.netgrif.application.engine.objects.common;

import lombok.Getter;

@Getter
public enum ResourceNotFoundExceptionCode {

    DEFAULT_SYSTEM_GROUP_NOT_FOUND("defaultSystemGroupNotFound"),
    DEFAULT_USER_GROUP_NOT_FOUND("defaultUserGroupNotFound"),
    DEFAULT_PROCESS_NOT_FOUND("defaultProcessNotFound"),
    AUTHORITY_NOT_FOUND("authorityNotFound");

    private final String key;

    ResourceNotFoundExceptionCode(String key) {
        this.key = key;
    }

}
