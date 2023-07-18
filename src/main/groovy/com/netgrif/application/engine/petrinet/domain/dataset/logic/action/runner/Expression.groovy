package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType
import org.bson.types.ObjectId

class Expression {

    protected ObjectId _id

    protected String definition

    Expression() {
        this._id = new ObjectId()
    }

    Expression(String definition) {
        this()
        this.definition = definition
    }

    String getStringId() {
        return _id.toString()
    }

    String getDefinition() {
        return definition
    }

    @Override
    String toString() {
        return "[$stringId] $definition"
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }

    @Override
     Expression clone() {
        return new Expression(this.definition)
    }
}
