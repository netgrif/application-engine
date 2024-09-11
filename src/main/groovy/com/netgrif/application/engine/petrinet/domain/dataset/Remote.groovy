package com.netgrif.application.engine.petrinet.domain.dataset

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class Remote {
    private String host
    private String bucket
    private String credentials

    String getHost() {
        return host
    }

    void setHost(String host) {
        this.host = host
    }

    String getBucket() {
        return bucket
    }

    void setBucket(String bucket) {
        this.bucket = bucket
    }

    String getCredentials() {
        return credentials
    }

    void setCredentials(String credentials) {
        this.credentials = credentials
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
