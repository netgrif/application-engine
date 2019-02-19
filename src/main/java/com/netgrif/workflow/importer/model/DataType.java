
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for data_type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="data_type"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="number"/&gt;
 *     &lt;enumeration value="text"/&gt;
 *     &lt;enumeration value="enumeration"/&gt;
 *     &lt;enumeration value="multichoice"/&gt;
 *     &lt;enumeration value="boolean"/&gt;
 *     &lt;enumeration value="date"/&gt;
 *     &lt;enumeration value="file"/&gt;
 *     &lt;enumeration value="user"/&gt;
 *     &lt;enumeration value="caseref"/&gt;
 *     &lt;enumeration value="dateTime"/&gt;
 *     &lt;enumeration value="button"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "data_type")
@XmlEnum
public enum DataType {

    @XmlEnumValue("number")
    NUMBER("number"),
    @XmlEnumValue("text")
    TEXT("text"),
    @XmlEnumValue("enumeration")
    ENUMERATION("enumeration"),
    @XmlEnumValue("multichoice")
    MULTICHOICE("multichoice"),
    @XmlEnumValue("boolean")
    BOOLEAN("boolean"),
    @XmlEnumValue("date")
    DATE("date"),
    @XmlEnumValue("file")
    FILE("file"),
    @XmlEnumValue("user")
    USER("user"),
    @XmlEnumValue("caseref")
    CASEREF("caseref"),
    @XmlEnumValue("dateTime")
    DATE_TIME("dateTime"),
    @XmlEnumValue("button")
    BUTTON("button");
    private final String value;

    DataType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DataType fromValue(String v) {
        for (DataType c: DataType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
