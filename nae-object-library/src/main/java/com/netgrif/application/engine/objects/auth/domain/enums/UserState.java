package com.netgrif.application.engine.objects.auth.domain.enums;

/**
 * Enumeration representing possible states of a user account in the authentication system.
 * This enum defines different operational states that control user access and account functionality.
 */
public enum UserState {
    /**
     * Indicates that the user account is active and fully operational.
     * Users in this state have normal access to system features and functionality.
     */
    ACTIVE,

    /**
     * Indicates that the user account is inactive.
     * Users in this state typically cannot log in or access system features,
     * but their account data is preserved and can be reactivated.
     */
    INACTIVE,

    /**
     * Indicates that the user account is blocked.
     * Users in this state are explicitly prevented from accessing the system,
     * usually due to security concerns or policy violations.
     */
    BLOCKED
}