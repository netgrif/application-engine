
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for eventPhaseType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="eventPhaseType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="pre"/&gt;
 *     &lt;enumeration value="post"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "eventPhaseType")
@XmlEnum
public enum EventPhaseType {

    @XmlEnumValue("pre")
    PRE("pre"),
    @XmlEnumValue("post")
    POST("post");
    private final String value;

    EventPhaseType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static EventPhaseType fromValue(String v) {
        for (EventPhaseType c: EventPhaseType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
