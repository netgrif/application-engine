
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for assignPolicyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="assignPolicyType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="auto"/&gt;
 *     &lt;enumeration value="manual"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "assignPolicyType")
@XmlEnum
public enum AssignPolicyType {

    @XmlEnumValue("auto")
    AUTO("auto"),
    @XmlEnumValue("manual")
    MANUAL("manual");
    private final String value;

    AssignPolicyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static AssignPolicyType fromValue(String v) {
        for (AssignPolicyType c: AssignPolicyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
