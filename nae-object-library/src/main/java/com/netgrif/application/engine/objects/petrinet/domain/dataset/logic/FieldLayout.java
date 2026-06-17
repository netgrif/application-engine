package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic;

import com.netgrif.application.engine.objects.petrinet.domain.layout.Layout;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FieldLayout extends Layout {

    private int x;
    private int y;
    private int offset;
    private String template;
    private String appearance;
    private String alignment;

    public FieldLayout() {
        super();
    }

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
        return new FieldLayout(this.getX(), this.getY(), this.getRows(), this.getCols(), this.getOffset(), this.getTemplate(), this.getAppearance(), this.getAlignment());
    }

    public boolean layoutFilled() {
        return (this.getRows() != null || this.getCols() != null || this.x != 0 || this.y != 0 || this.offset != 0 || this.template != null || this.appearance != null || this.alignment != null);
    }
}
