package com.netgrif.application.engine.workflow.domain;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlType(name = "authorizationType")
@XmlEnum
public enum AuthorizationType {

    @XmlEnumValue("allowed")
    allowed("allowed"),
    @XmlEnumValue("banned")
    banned("banned");
    private final String value;

    AuthorizationType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AuthorizationType fromValue(String v) {
        for (AuthorizationType c : AuthorizationType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}