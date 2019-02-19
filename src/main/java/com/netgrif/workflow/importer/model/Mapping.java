
package com.netgrif.workflow.importer.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
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
 *       &lt;sequence&gt;
 *         &lt;element ref="{}id"/&gt;
 *         &lt;element ref="{}transitionRef"/&gt;
 *         &lt;element ref="{}roleRef" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}dataRef" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}dataGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}trigger" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "id",
    "transitionRef",
    "roleRef",
    "dataRef",
    "dataGroup",
    "trigger"
})
@XmlRootElement(name = "mapping")
public class Mapping {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected String transitionRef;
    protected List<RoleRef> roleRef;
    protected List<DataRef> dataRef;
    protected List<DataGroup> dataGroup;
    protected List<Trigger> trigger;

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
     * Gets the value of the transitionRef property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransitionRef() {
        return transitionRef;
    }

    /**
     * Sets the value of the transitionRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransitionRef(String value) {
        this.transitionRef = value;
    }

    /**
     * Gets the value of the roleRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the roleRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRoleRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link RoleRef }
     * 
     * 
     */
    public List<RoleRef> getRoleRef() {
        if (roleRef == null) {
            roleRef = new ArrayList<RoleRef>();
        }
        return this.roleRef;
    }

    /**
     * Gets the value of the dataRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataRef }
     * 
     * 
     */
    public List<DataRef> getDataRef() {
        if (dataRef == null) {
            dataRef = new ArrayList<DataRef>();
        }
        return this.dataRef;
    }

    /**
     * Gets the value of the dataGroup property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the dataGroup property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDataGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DataGroup }
     * 
     * 
     */
    public List<DataGroup> getDataGroup() {
        if (dataGroup == null) {
            dataGroup = new ArrayList<DataGroup>();
        }
        return this.dataGroup;
    }

    /**
     * Gets the value of the trigger property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the trigger property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTrigger().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Trigger }
     * 
     * 
     */
    public List<Trigger> getTrigger() {
        if (trigger == null) {
            trigger = new ArrayList<Trigger>();
        }
        return this.trigger;
    }

}
