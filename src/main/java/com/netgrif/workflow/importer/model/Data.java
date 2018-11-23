
package com.netgrif.workflow.importer.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
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
 *         &lt;element ref="{}title"/&gt;
 *         &lt;element ref="{}placeholder" minOccurs="0"/&gt;
 *         &lt;element ref="{}desc" minOccurs="0"/&gt;
 *         &lt;element ref="{}values" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}valid" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}init" minOccurs="0"/&gt;
 *         &lt;element ref="{}format" minOccurs="0"/&gt;
 *         &lt;element ref="{}encryption" minOccurs="0"/&gt;
 *         &lt;element ref="{}action" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}actionRef" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element ref="{}documentRef" minOccurs="0"/&gt;
 *         &lt;element ref="{}remote" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="type" use="required" type="{}data_type" /&gt;
 *       &lt;attribute name="immediate" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
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
    "title",
    "placeholder",
    "desc",
    "values",
    "valid",
    "init",
    "format",
    "encryption",
    "action",
    "actionRef",
    "documentRef",
    "remote"
})
@XmlRootElement(name = "data")
public class Data {

    @XmlElement(required = true)
    protected String id;
    @XmlElement(required = true)
    protected I18NStringType title;
    protected I18NStringType placeholder;
    protected I18NStringType desc;
    protected List<I18NStringType> values;
    protected List<String> valid;
    protected String init;
    protected Format format;
    protected EncryptionType encryption;
    protected List<ActionType> action;
    protected List<ActionRefType> actionRef;
    protected DocumentRef documentRef;
    protected String remote;
    @XmlAttribute(name = "type", required = true)
    protected DataType type;
    @XmlAttribute(name = "immediate")
    protected Boolean immediate;

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
     * Gets the value of the placeholder property.
     * 
     * @return
     *     possible object is
     *     {@link I18NStringType }
     *     
     */
    public I18NStringType getPlaceholder() {
        return placeholder;
    }

    /**
     * Sets the value of the placeholder property.
     * 
     * @param value
     *     allowed object is
     *     {@link I18NStringType }
     *     
     */
    public void setPlaceholder(I18NStringType value) {
        this.placeholder = value;
    }

    /**
     * Gets the value of the desc property.
     * 
     * @return
     *     possible object is
     *     {@link I18NStringType }
     *     
     */
    public I18NStringType getDesc() {
        return desc;
    }

    /**
     * Sets the value of the desc property.
     * 
     * @param value
     *     allowed object is
     *     {@link I18NStringType }
     *     
     */
    public void setDesc(I18NStringType value) {
        this.desc = value;
    }

    /**
     * Gets the value of the values property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the values property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValues().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link I18NStringType }
     * 
     * 
     */
    public List<I18NStringType> getValues() {
        if (values == null) {
            values = new ArrayList<I18NStringType>();
        }
        return this.values;
    }

    /**
     * Gets the value of the valid property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the valid property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getValid().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link String }
     * 
     * 
     */
    public List<String> getValid() {
        if (valid == null) {
            valid = new ArrayList<String>();
        }
        return this.valid;
    }

    /**
     * Gets the value of the init property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInit() {
        return init;
    }

    /**
     * Sets the value of the init property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInit(String value) {
        this.init = value;
    }

    /**
     * Gets the value of the format property.
     * 
     * @return
     *     possible object is
     *     {@link Format }
     *     
     */
    public Format getFormat() {
        return format;
    }

    /**
     * Sets the value of the format property.
     * 
     * @param value
     *     allowed object is
     *     {@link Format }
     *     
     */
    public void setFormat(Format value) {
        this.format = value;
    }

    /**
     * Gets the value of the encryption property.
     * 
     * @return
     *     possible object is
     *     {@link EncryptionType }
     *     
     */
    public EncryptionType getEncryption() {
        return encryption;
    }

    /**
     * Sets the value of the encryption property.
     * 
     * @param value
     *     allowed object is
     *     {@link EncryptionType }
     *     
     */
    public void setEncryption(EncryptionType value) {
        this.encryption = value;
    }

    /**
     * Gets the value of the action property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the action property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActionType }
     * 
     * 
     */
    public List<ActionType> getAction() {
        if (action == null) {
            action = new ArrayList<ActionType>();
        }
        return this.action;
    }

    /**
     * Gets the value of the actionRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ActionRefType }
     * 
     * 
     */
    public List<ActionRefType> getActionRef() {
        if (actionRef == null) {
            actionRef = new ArrayList<ActionRefType>();
        }
        return this.actionRef;
    }

    /**
     * Gets the value of the documentRef property.
     * 
     * @return
     *     possible object is
     *     {@link DocumentRef }
     *     
     */
    public DocumentRef getDocumentRef() {
        return documentRef;
    }

    /**
     * Sets the value of the documentRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link DocumentRef }
     *     
     */
    public void setDocumentRef(DocumentRef value) {
        this.documentRef = value;
    }

    /**
     * Gets the value of the remote property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemote() {
        return remote;
    }

    /**
     * Sets the value of the remote property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemote(String value) {
        this.remote = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link DataType }
     *     
     */
    public DataType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link DataType }
     *     
     */
    public void setType(DataType value) {
        this.type = value;
    }

    /**
     * Gets the value of the immediate property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isImmediate() {
        return immediate;
    }

    /**
     * Sets the value of the immediate property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setImmediate(Boolean value) {
        this.immediate = value;
    }

}
