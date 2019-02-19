
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for behavior.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="behavior"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="forbidden"/&gt;
 *     &lt;enumeration value="hidden"/&gt;
 *     &lt;enumeration value="visible"/&gt;
 *     &lt;enumeration value="editable"/&gt;
 *     &lt;enumeration value="required"/&gt;
 *     &lt;enumeration value="immediate"/&gt;
 *     &lt;enumeration value="optional"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "behavior")
@XmlEnum
public enum Behavior {

    @XmlEnumValue("forbidden")
    FORBIDDEN("forbidden"),
    @XmlEnumValue("hidden")
    HIDDEN("hidden"),
    @XmlEnumValue("visible")
    VISIBLE("visible"),
    @XmlEnumValue("editable")
    EDITABLE("editable"),
    @XmlEnumValue("required")
    REQUIRED("required"),
    @XmlEnumValue("immediate")
    IMMEDIATE("immediate"),
    @XmlEnumValue("optional")
    OPTIONAL("optional");
    private final String value;

    Behavior(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static Behavior fromValue(String v) {
        for (Behavior c: Behavior.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
