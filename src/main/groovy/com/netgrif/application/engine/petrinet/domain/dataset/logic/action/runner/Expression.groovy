package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType
import lombok.Data
import org.bson.types.ObjectId

class Expression {

    protected ObjectId id

    protected String definition

    Expression() {
        this.id = new ObjectId()
    }

    Expression(String definition) {
        this()
        this.definition = definition
    }

    String getStringId() {
        return id.toString()
    }

    String getDefinition() {
        return definition
    }

    @Override
    String toString() {
        return "[$stringId] $definition"
    }

    Expression clone() {
        Expression clone =  new Expression()
        clone.id = this.id
        clone.definition = this.definition
        return clone
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
