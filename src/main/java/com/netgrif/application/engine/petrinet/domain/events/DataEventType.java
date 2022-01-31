package com.netgrif.application.engine.petrinet.domain.events;

public enum DataEventType {
    GET("get"),
    SET("set");

    public final String value;

    DataEventType(String value) {
        this.value = value;
    }

    public static DataEventType fromString(String type){
        if(type == null){
            return null;
        }
        return valueOf(type.toUpperCase());
    }
}
