
package com.netgrif.workflow.importer.model;

import javax.xml.bind.annotation.*;


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

    protected long id;
    @XmlElement(required = true, defaultValue = "regular")
    @XmlSchemaType(name = "string")
    protected ArcType type;
    protected long sourceId;
    protected long destinationId;
    protected int multiplicity;
    protected BreakPoint breakPoint;

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
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
     */
    public long getSourceId() {
        return sourceId;
    }

    /**
     * Sets the value of the sourceId property.
     * 
     */
    public void setSourceId(long value) {
        this.sourceId = value;
    }

    /**
     * Gets the value of the destinationId property.
     * 
     */
    public long getDestinationId() {
        return destinationId;
    }

    /**
     * Sets the value of the destinationId property.
     * 
     */
    public void setDestinationId(long value) {
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
