package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class FieldLayout {
    private int x
    private int y
    private int rows
    private int cols
    private String template
    private String appearance

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }

    FieldLayout() {
    }

    FieldLayout(int x, int y, int rows, int cols, String template, String appearance) {
        this.x = x
        this.y = y
        this.rows = rows
        this.cols = cols
        this.template = template?.toLowerCase()
        this.appearance = appearance?.toLowerCase()
    }

    int getX() {
        return x
    }

    void setX(int x) {
        this.x = x
    }

    int getY() {
        return y
    }

    void setY(int y) {
        this.y = y
    }

    int getRows() {
        return rows
    }

    void setRows(int rows) {
        this.rows = rows
    }

    int getCols() {
        return cols
    }

    void setCols(int cols) {
        this.cols = cols
    }

    String getTemplate() {
        return template
    }

    void setTemplate(String template) {
        this.template = template
    }

    String getAppearance() {
        return appearance
    }

    void setAppearance(String appearance) {
        this.appearance = appearance
    }

    boolean layoutFilled() {
        return this.rows != 0 && this.cols != 0
    }
}
