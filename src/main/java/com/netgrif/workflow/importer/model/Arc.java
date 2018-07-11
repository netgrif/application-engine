
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;all&gt;
 *         &lt;element ref="{}id"/&gt;
 *         &lt;element name="type" type="{}arc_type"/&gt;
 *         &lt;element ref="{}sourceId"/&gt;
 *         &lt;element ref="{}destinationId"/&gt;
 *         &lt;element ref="{}multiplicity"/&gt;
 *         &lt;element ref="{}breakPoint" minOccurs="0"/&gt;
 *       &lt;/all&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {

})
@XmlRootElement(name = "arc")
public class Arc {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true, defaultValue = "regular")
    @XmlSchemaType(name = "string")
    protected ArcType type;
    @XmlElement(required = true)
    protected String sourceId;
    @XmlElement(required = true)
    protected String destinationId;
    protected int multiplicity;
    protected BreakPoint breakPoint;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ArcType }
     *     
     */
    public ArcType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ArcType }
     *     
     */
    public void setType(ArcType value) {
        this.type = value;
    }

    /**
     * Gets the value of the sourceId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * Sets the value of the sourceId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSourceId(String value) {
        this.sourceId = value;
    }

    /**
     * Gets the value of the destinationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDestinationId() {
        return destinationId;
    }

    /**
     * Sets the value of the destinationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDestinationId(String value) {
        this.destinationId = value;
    }

    /**
     * Gets the value of the multiplicity property.
     * 
     */
    public int getMultiplicity() {
        return multiplicity;
    }

    /**
     * Sets the value of the multiplicity property.
     * 
     */
    public void setMultiplicity(int value) {
        this.multiplicity = value;
    }

    /**
     * Gets the value of the breakPoint property.
     * 
     * @return
     *     possible object is
     *     {@link BreakPoint }
     *     
     */
    public BreakPoint getBreakPoint() {
        return breakPoint;
    }

    /**
     * Sets the value of the breakPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link BreakPoint }
     *     
     */
    public void setBreakPoint(BreakPoint value) {
        this.breakPoint = value;
    }

}
