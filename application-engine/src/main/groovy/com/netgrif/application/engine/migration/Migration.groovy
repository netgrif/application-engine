package com.netgrif.application.engine.migration

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType
import org.bson.types.ObjectId
import org.codehaus.groovy.runtime.metaclass.MetaClassRegistryImpl
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

import java.time.LocalDateTime

@Document
class Migration {

    @Id
    private ObjectId id

    private LocalDateTime runDateTime

    private String title

    Migration(String title) {
        this.title = title
        this.runDateTime = LocalDateTime.now()
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass != null ? this.metaClass  : ((MetaClassRegistryImpl) GroovySystem.getMetaClassRegistry()).getMetaClass(this)
    }
}