
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for finishPolicyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="finishPolicyType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="auto_no_data"/&gt;
 *     &lt;enumeration value="manual"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "finishPolicyType")
@XmlEnum
public enum FinishPolicyType {

    @XmlEnumValue("auto_no_data")
    AUTO_NO_DATA("auto_no_data"),
    @XmlEnumValue("manual")
    MANUAL("manual");
    private final String value;

    FinishPolicyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static FinishPolicyType fromValue(String v) {
        for (FinishPolicyType c: FinishPolicyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
