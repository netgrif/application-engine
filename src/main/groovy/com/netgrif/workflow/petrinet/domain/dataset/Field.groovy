package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.ObjectNode
import com.netgrif.workflow.petrinet.domain.DataEvent
import com.netgrif.workflow.petrinet.domain.Component
import com.netgrif.workflow.petrinet.domain.Format
import com.netgrif.workflow.petrinet.domain.I18nString
import com.netgrif.workflow.petrinet.domain.Imported
import com.netgrif.workflow.petrinet.domain.dataset.logic.FieldLayout
import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

@Document
abstract class Field<T> extends Imported {

    @Id
    protected ObjectId _id

    private I18nString name

    private I18nString description

    private I18nString placeholder

    @Transient
    private ObjectNode behavior

    @Transient
    private FieldLayout layout

    @Transient
    private T value

    private Long order

    @JsonIgnore
    private boolean immediate

    @JsonIgnore
    private LinkedHashSet<DataEvent> events;

    @JsonIgnore
    private String encryption

    private Format format

    private Integer length

    private Component component

    Field() {
        _id = new ObjectId()
        this.events = new LinkedHashSet<>();
    }

    Field(Long importId) {
        this()
        this.importId = importId
    }

    String getStringId() {
        return importId
    }

    ObjectId get_id() {
        return _id
    }

    void set_id(ObjectId _id) {
        this._id = _id
    }

    void setImportId(Long importId) {
        this.importId = importId
    }

    I18nString getName() {
        return name
    }

    void setName(I18nString name) {
        this.name = name
    }

    I18nString getDescription() {
        return description
    }

    void setDescription(I18nString description) {
        this.description = description
    }

    I18nString getPlaceholder() {
        return placeholder
    }

    void setPlaceholder(I18nString placeholder) {
        this.placeholder = placeholder
    }

    abstract FieldType getType()

    ObjectNode getBehavior() {
        return behavior
    }

    void setBehavior(ObjectNode behavior) {
        this.behavior = behavior
    }

    FieldLayout getLayout() {
        return layout
    }

    void setLayout(FieldLayout layout) {
        this.layout = layout
    }

    T getValue() {
        return value
    }

    void setValue(T value) {
        this.value = value
    }

    Long getOrder() {
        return order
    }

    void setOrder(Long order) {
        this.order = order
    }

    Boolean isImmediate() {
        return immediate != null && immediate
    }

    void setImmediate(Boolean immediate) {
        this.immediate = immediate != null && immediate
    }

    LinkedHashSet<DataEvent> getEvents() {
        return events
    }

    void setEvents(LinkedHashSet<DataEvent> events) {
        this.events = events
    }

    void addEvents(Collection<DataEvent> dataEvents) {
        dataEvents.each { addEvent(it) }
    }

    void addEvent(DataEvent event) {
        if (this.events == null)
            this.events = new LinkedHashSet<>()
        if (event == null) return

        this.events.add(event)
    }

    String getEncryption() {
        return encryption
    }

    void setEncryption(String encryption) {
        this.encryption = encryption
    }

    Component getComponent() {
        return component
    }

    void setComponent(Component component) {
        this.component = component
    }

    void clearValue() {}
//operators overloading
    T plus(final Field field) {
        return this.value + field.value
    }

    T minus(final Field field) {
        return this.value - field.value
    }

    T multiply(final Field field) {
        return this.value * field.value
    }

    String getTranslatedName(Locale locale) {
        return name?.getTranslation(locale)
    }

    String getTranslatedPlaceholder(Locale locale) {
        return placeholder?.getTranslation(locale)
    }

    String getTranslatedDescription(Locale locale) {
        return description?.getTranslation(locale)
    }

    Format getFormat() {
        return format
    }

    void setFormat(Format format) {
        this.format = format
    }

    Integer getLength() {
        return length
    }

    void setLength(Integer length) {
        this.length = length
    }

    @Override
    String toString() {
        return name.defaultValue
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }

    void clone(Field clone) {
        clone._id = this._id
        clone.importId = this.importId
        clone.name = this.name
        clone.description = this.description
        clone.placeholder = this.placeholder
        clone.order = this.order
        clone.immediate = this.immediate
        clone.events = this.events
        clone.encryption = this.encryption
        clone.format = this.format
        clone.length = this.length
        clone.component = this.component
    }

    abstract Field clone()
}