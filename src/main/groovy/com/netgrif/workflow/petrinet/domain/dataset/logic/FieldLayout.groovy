package com.netgrif.workflow.petrinet.domain.dataset.logic

import com.netgrif.workflow.petrinet.domain.layout.Layout
import com.querydsl.core.annotations.PropertyType
import com.querydsl.core.annotations.QueryType

class FieldLayout extends Layout {
    private int x
    private int y
    private int offset
    private String template
    private String appearance
    private String alignment

    FieldLayout() {
        super()
    }

    FieldLayout(int x, int y, int rows, int cols, int offset, String template, String appearance, String alignment) {
        super(rows, cols)
        this.x = x
        this.y = y
        this.offset = offset
        this.template = template?.toLowerCase()
        this.appearance = appearance?.toLowerCase()
        this.alignment = alignment
    }

    FieldLayout clone() {
        return new FieldLayout(this.getX(), this.getY(), this.getRows(), this.getCols(),this.getOffset(), this.getTemplate(), this.getAppearance(), this.getAlignment())
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

    int getOffset() {
        return offset
    }

    void setOffset(int offset) {
        this.offset = offset
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

    String getAlignment() {
        return alignment
    }

    void setAlignment(String alignment) {
        this.alignment = alignment
    }

    boolean layoutFilled() {
        return (this.rows != null && this.rows != 0) && (this.cols != null && this.cols != 0)
    }

    @Override
    @QueryType(PropertyType.NONE)
    MetaClass getMetaClass() {
        return this.metaClass
    }
}
