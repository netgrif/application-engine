
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for booleanImageView complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="booleanImageView"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="true" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="false" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "booleanImageView", propOrder = {
    "_true",
    "_false"
})
public class BooleanImageView {

    @XmlElement(name = "true", required = true)
    protected String _true;
    @XmlElement(name = "false", required = true)
    protected String _false;

    /**
     * Gets the value of the true property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTrue() {
        return _true;
    }

    /**
     * Sets the value of the true property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTrue(String value) {
        this._true = value;
    }

    /**
     * Gets the value of the false property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFalse() {
        return _false;
    }

    /**
     * Sets the value of the false property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFalse(String value) {
        this._false = value;
    }

}
