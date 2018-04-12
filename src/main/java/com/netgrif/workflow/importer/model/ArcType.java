
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for arc_type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="arc_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="regular"/&gt;
 *     &lt;enumeration value="reset"/&gt;
 *     &lt;enumeration value="inhibitor"/&gt;
 *     &lt;enumeration value="read"/&gt;
 *     &lt;enumeration value="variable"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "arc_type")
@XmlEnum
public enum ArcType {

    @XmlEnumValue("regular")
    REGULAR("regular"),
    @XmlEnumValue("reset")
    RESET("reset"),
    @XmlEnumValue("inhibitor")
    INHIBITOR("inhibitor"),
    @XmlEnumValue("read")
    READ("read"),
    @XmlEnumValue("variable")
    VARIABLE("variable");
    private final String value;

    ArcType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ArcType fromValue(String v) {
        for (ArcType c: ArcType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
