package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class FieldLayout {
    private Integer x
    private Integer y
    private Integer rows
    private Integer cols

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }

    FieldLayout() {
    }

    FieldLayout(Integer x, Integer y, Integer rows, Integer cols) {
        this.x = x
        this.y = y
        this.rows = rows
        this.cols = cols
    }

    Integer getX() {
        return x
    }

    void setX(Integer x) {
        this.x = x
    }

    Integer getY() {
        return y
    }

    void setY(Integer y) {
        this.y = y
    }

    Integer getRows() {
        return rows
    }

    void setRows(Integer rows) {
        this.rows = rows
    }

    Integer getCols() {
        return cols
    }

    void setCols(Integer cols) {
        this.cols = cols
    }
}
