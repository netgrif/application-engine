package com.netgrif.application.engine.elastic.domain;

public class ElasticQueryConstants {
    /**
     * Should be replaced by user id in elastic query string queries
     */
    public static final String USER_ID_TEMPLATE = "<<me>>";
    public static final String DYNAMIC_USER_ID_TEMPLATE = "me";
    public static final String LOCAL_DATE_NOW_TEMPLATE = "now";
    public static final String LOCAL_DATE_TODAY_TEMPLATE = "today";
    public static final String LOGGED_USER_TEMPLATE = "loggedUser";
}
