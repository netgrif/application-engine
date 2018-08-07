
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
 *         &lt;element ref="{}id" minOccurs="0"/&gt;
 *         &lt;element ref="{}version" minOccurs="0"/&gt;
 *         &lt;element ref="{}initials" minOccurs="0"/&gt;
 *         &lt;element name="title" type="{}i18nStringType" minOccurs="0"/&gt;
 *         &lt;element ref="{}icon" minOccurs="0"/&gt;
 *         &lt;element ref="{}defaultRole" minOccurs="0"/&gt;
 *         &lt;element ref="{}transitionRole" minOccurs="0"/&gt;
 *         &lt;element ref="{}caseName" minOccurs="0"/&gt;
 *         &lt;element ref="{}transaction" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}role" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}data" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}mapping" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}i18n" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}transition" maxOccurs="unbounded"/&gt;
 *         &lt;element ref="{}place" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}arc" maxOccurs="unbounded" minOccurs="0"/&gt;
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
    "version",
    "initials",
    "title",
    "icon",
    "defaultRole",
    "transitionRole",
    "caseName",
    "transaction",
    "role",
    "data",
    "mapping",
    "i18N",
    "transition",
    "place",
    "arc"
})
@XmlRootElement(name = "document")
public class Document {

    protected String id;
    protected String version;
    protected String initials;
    protected I18NStringType title;
    protected String icon;
    @XmlElement(defaultValue = "true")
    protected Boolean defaultRole;
    @XmlElement(defaultValue = "true")
    protected Boolean transitionRole;
    protected I18NStringType caseName;
    protected List<Transaction> transaction;
    protected List<Role> role;
    protected List<Data> data;
    protected List<Mapping> mapping;
    @XmlElement(name = "i18n")
    protected List<I18N> i18N;
    @XmlElement(required = true)
    protected List<Transition> transition;
    protected List<Place> place;
    protected List<Arc> arc;

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
     * Gets the value of the version property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

    /**
     * Gets the value of the initials property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInitials() {
        return initials;
    }

    /**
     * Sets the value of the initials property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInitials(String value) {
        this.initials = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link I18NStringType }
     *     
     */
    public I18NStringType getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link I18NStringType }
     *     
     */
    public void setTitle(I18NStringType value) {
        this.title = value;
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
     * Gets the value of the defaultRole property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isDefaultRole() {
        return defaultRole;
    }

    /**
     * Sets the value of the defaultRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefaultRole(Boolean value) {
        this.defaultRole = value;
    }

    /**
     * Gets the value of the transitionRole property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isTransitionRole() {
        return transitionRole;
    }

    /**
     * Sets the value of the transitionRole property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setTransitionRole(Boolean value) {
        this.transitionRole = value;
    }

    /**
     * Gets the value of the caseName property.
     * 
     * @return
     *     possible object is
     *     {@link I18NStringType }
     *     
     */
    public I18NStringType getCaseName() {
        return caseName;
    }

    /**
     * Sets the value of the caseName property.
     * 
     * @param value
     *     allowed object is
     *     {@link I18NStringType }
     *     
     */
    public void setCaseName(I18NStringType value) {
        this.caseName = value;
    }

    /**
     * Gets the value of the transaction property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transaction property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransaction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Transaction }
     * 
     * 
     */
    public List<Transaction> getTransaction() {
        if (transaction == null) {
            transaction = new ArrayList<Transaction>();
        }
        return this.transaction;
    }

    /**
     * Gets the value of the role property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the role property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRole().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Role }
     * 
     * 
     */
    public List<Role> getRole() {
        if (role == null) {
            role = new ArrayList<Role>();
        }
        return this.role;
    }

    /**
     * Gets the value of the data property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the data property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Data }
     * 
     * 
     */
    public List<Data> getData() {
        if (data == null) {
            data = new ArrayList<Data>();
        }
        return this.data;
    }

    /**
     * Gets the value of the mapping property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mapping property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMapping().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Mapping }
     * 
     * 
     */
    public List<Mapping> getMapping() {
        if (mapping == null) {
            mapping = new ArrayList<Mapping>();
        }
        return this.mapping;
    }

    /**
     * Gets the value of the i18N property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the i18N property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getI18N().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link I18N }
     * 
     * 
     */
    public List<I18N> getI18N() {
        if (i18N == null) {
            i18N = new ArrayList<I18N>();
        }
        return this.i18N;
    }

    /**
     * Gets the value of the transition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the transition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTransition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Transition }
     * 
     * 
     */
    public List<Transition> getTransition() {
        if (transition == null) {
            transition = new ArrayList<Transition>();
        }
        return this.transition;
    }

    /**
     * Gets the value of the place property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the place property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPlace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Place }
     * 
     * 
     */
    public List<Place> getPlace() {
        if (place == null) {
            place = new ArrayList<Place>();
        }
        return this.place;
    }

    /**
     * Gets the value of the arc property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the arc property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getArc().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Arc }
     * 
     * 
     */
    public List<Arc> getArc() {
        if (arc == null) {
            arc = new ArrayList<Arc>();
        }
        return this.arc;
    }

}
