package com.netgrif.application.engine.petrinet.domain.dataset

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryExclude
import com.querydsl.core.annotations.QueryType

import java.time.ZoneId

class TimeZone {

    private ZoneId zoneId

    TimeZone() {
    }

    TimeZone(ZoneId zoneId) {
        this.zoneId = zoneId
    }

    ZoneId getZoneId() {
        return zoneId
    }

    void setZoneId(ZoneId zoneId) {
        this.zoneId = zoneId
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
