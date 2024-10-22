package com.netgrif.application.engine.petrinet.domain.dataset

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class Storage implements Serializable {
    static final long serialVersionUID = 9172755427878929926L
    private StorageType type
    private String host

    Storage() {
        this.type = StorageType.LOCAL
    }

    Storage(StorageType type) {
        this()
        this.type = type
    }

    Storage(StorageType type, String host) {
        this(type)
        this.host = host
    }

    StorageType getType() {
        return type
    }

    void setType(StorageType type) {
        this.type = type
    }

    String getHost() {
        return host
    }

    void setHost(String host) {
        this.host = host
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
