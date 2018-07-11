
package com.netgrif.workflow.importer.model;

import java.util.ArrayList;
import java.util.List;
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
 *       &lt;sequence&gt;
 *         &lt;element ref="{}id"/&gt;
 *         &lt;element ref="{}x"/&gt;
 *         &lt;element ref="{}y"/&gt;
 *         &lt;element ref="{}label"/&gt;
 *         &lt;element ref="{}icon" minOccurs="0"/&gt;
 *         &lt;element ref="{}priority" minOccurs="0"/&gt;
 *         &lt;element ref="{}assignPolicy" minOccurs="0"/&gt;
 *         &lt;element ref="{}dataFocusPolicy" minOccurs="0"/&gt;
 *         &lt;element ref="{}finishPolicy" minOccurs="0"/&gt;
 *         &lt;element ref="{}trigger" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}transactionRef" minOccurs="0"/&gt;
 *         &lt;element ref="{}roleRef" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}dataRef" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}dataGroup" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}event" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "x",
    "y",
    "label",
    "icon",
    "priority",
    "assignPolicy",
    "dataFocusPolicy",
    "finishPolicy",
    "trigger",
    "transactionRef",
    "roleRef",
    "dataRef",
    "dataGroup",
    "event"
})
@XmlRootElement(name = "transition")
public class Transition {

    @XmlElement(required = true)
    protected String id;
    protected short x;
    protected short y;
    @XmlElement(required = true)
    protected I18NStringType label;
    protected String icon;
    protected Integer priority;
    @XmlSchemaType(name = "string")
    protected AssignPolicyType assignPolicy;
    @XmlSchemaType(name = "string")
    protected DataFocusPolicyType dataFocusPolicy;
    @XmlSchemaType(name = "string")
    protected FinishPolicyType finishPolicy;
    protected List<Trigger> trigger;
    protected TransactionRef transactionRef;
    protected List<RoleRef> roleRef;
    protected List<DataRef> dataRef;
    protected List<DataGroup> dataGroup;
    protected List<Event> event;

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
     * Gets the value of the x property.
     * 
     */
    public short getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     */
    public void setX(short value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     */
    public short getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     */
    public void setY(short value) {
        this.y = value;
    }

    /**
     * Gets the value of the label property.
     * 
     * @return
     *     possible object is
     *     {@link I18NStringType }
     *     
     */
    public I18NStringType getLabel() {
        return label;
    }

    /**
     * Sets the value of the label property.
     * 
     * @param value
     *     allowed object is
     *     {@link I18NStringType }
     *     
     */
    public void setLabel(I18NStringType value) {
        this.label = value;
    }

    /**
     * Gets the value of the icon property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Sets the value of the icon property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIcon(String value) {
        this.icon = value;
    }

    /**
     * Gets the value of the priority property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the value of the priority property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPriority(Integer value) {
        this.priority = value;
    }

    /**
     * Gets the value of the assignPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link AssignPolicyType }
     *     
     */
    public AssignPolicyType getAssignPolicy() {
        return assignPolicy;
    }

    /**
     * Sets the value of the assignPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssignPolicyType }
     *     
     */
    public void setAssignPolicy(AssignPolicyType value) {
        this.assignPolicy = value;
    }

    /**
     * Gets the value of the dataFocusPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link DataFocusPolicyType }
     *     
     */
    public DataFocusPolicyType getDataFocusPolicy() {
        return dataFocusPolicy;
    }

    /**
     * Sets the value of the dataFocusPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataFocusPolicyType }
     *     
     */
    public void setDataFocusPolicy(DataFocusPolicyType value) {
        this.dataFocusPolicy = value;
    }

    /**
     * Gets the value of the finishPolicy property.
     * 
     * @return
     *     possible object is
     *     {@link FinishPolicyType }
     *     
     */
    public FinishPolicyType getFinishPolicy() {
        return finishPolicy;
    }

    /**
     * Sets the value of the finishPolicy property.
     * 
     * @param value
     *     allowed object is
     *     {@link FinishPolicyType }
     *     
     */
    public void setFinishPolicy(FinishPolicyType value) {
        this.finishPolicy = value;
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

    /**
     * Gets the value of the transactionRef property.
     * 
     * @return
     *     possible object is
     *     {@link TransactionRef }
     *     
     */
    public TransactionRef getTransactionRef() {
        return transactionRef;
    }

    /**
     * Sets the value of the transactionRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link TransactionRef }
     *     
     */
    public void setTransactionRef(TransactionRef value) {
        this.transactionRef = value;
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
     * Gets the value of the event property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the event property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEvent().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Event }
     * 
     * 
     */
    public List<Event> getEvent() {
        if (event == null) {
            event = new ArrayList<Event>();
        }
        return this.event;
    }

}
