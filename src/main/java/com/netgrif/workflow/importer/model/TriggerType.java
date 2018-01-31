
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for triggerType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="triggerType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="auto"/&gt;
 *     &lt;enumeration value="user"/&gt;
 *     &lt;enumeration value="time"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "triggerType")
@XmlEnum
public enum TriggerType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("user")
    USER("user"),
    @XmlEnumValue("time")
    TIME("time");
    private final String value;

    TriggerType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TriggerType fromValue(String v) {
        for (TriggerType c: TriggerType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
