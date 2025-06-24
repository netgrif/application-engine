package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TextField extends Field<String> {
    public static final String SIMPLE_SUBTYPE = "simple";
    public static final String AREA_SUBTYPE = "area";
    private String subType;
    private Integer maxLength;
    private String formatting;

    public TextField() {
        super();
    }

    @Override
    public FieldType getType() {
        return FieldType.TEXT;
    }

    public TextField(String subtype) {
        this();
        this.subType = subtype != null ? subtype : SIMPLE_SUBTYPE;
    }

    @Override
    public Field<?> clone() {
        TextField clone = new TextField();
        super.clone(clone);
        clone.setSubType(this.subType);
        return clone;
    }
}
