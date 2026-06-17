package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType
import org.bson.types.ObjectId
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl

class Expression implements Serializable {

    private static final long serialVersionUID = 3687481111847498422L

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
        return this.metaClass != null ? this.metaClass  : ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(this)
    }

    @Override
     Expression clone() {
        return new Expression(this.definition)
    }
}
