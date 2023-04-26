package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.mapper.filters.AppearanceFilter;
import com.netgrif.application.engine.mapper.filters.TemplateFilter;
import com.netgrif.application.engine.petrinet.domain.layout.Layout;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldLayout extends Layout {

    private int x;

    private int y;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int offset;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = TemplateFilter.class)
    private String template;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = AppearanceFilter.class)
    private String appearance;

    private String alignment;

    public FieldLayout(Integer x, Integer y, Integer rows, Integer cols, Integer offset, String template, String appearance, String alignment) {
        super(rows, cols);
        this.x = nullToZero(x);
        this.y = nullToZero(y);
        this.offset = nullToZero(offset);
        this.template = template.toLowerCase();
        this.appearance = appearance.toLowerCase();
        this.alignment = alignment;
    }

    private int nullToZero(Integer i) {
        return i != null ? i : 0;
    }

    public FieldLayout clone() {
        return new FieldLayout(this.x, this.getY(), this.getRows(), this.getCols(), this.getOffset(), this.getTemplate(), this.getAppearance(), this.getAlignment());
    }

    @Override
    public boolean hasNonDefaultAttribute() {
        return super.hasNonDefaultAttribute() || !template.equals("material") || !appearance.equals("outline");
    }

    @JsonIgnore
    public boolean isLayoutFilled() {
        return (this.rows != null
                || this.cols != null
                || this.x != 0
                || this.y != 0
                || this.offset != 0
                || this.template != null
                || this.appearance != null
                || this.alignment != null);
    }
}
