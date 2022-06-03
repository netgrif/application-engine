package api.petrinet.domain.dataset.logic;

import api.petrinet.domain.layout.LayoutDto;

public final class FieldLayoutDto extends LayoutDto {

    private int x;

    private int y;

    private int offset;

    private String template;

    private String appearance;

    private String alignment;

    public FieldLayoutDto() {
    }

    public FieldLayoutDto(int x, int y, int offset, String template, String appearance, String alignment) {
        this.x = x;
        this.y = y;
        this.offset = offset;
        this.template = template;
        this.appearance = appearance;
        this.alignment = alignment;
    }

    public FieldLayoutDto(Integer rows, Integer cols, int x, int y, int offset, String template, String appearance, String alignment) {
        super(rows, cols);
        this.x = x;
        this.y = y;
        this.offset = offset;
        this.template = template;
        this.appearance = appearance;
        this.alignment = alignment;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getAppearance() {
        return appearance;
    }

    public void setAppearance(String appearance) {
        this.appearance = appearance;
    }

    public String getAlignment() {
        return alignment;
    }

    public void setAlignment(String alignment) {
        this.alignment = alignment;
    }
}
