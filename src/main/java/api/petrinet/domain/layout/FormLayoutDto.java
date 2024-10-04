package api.petrinet.domain.layout;

public abstract class FormLayoutDto extends LayoutDto {

    private String type;

    private String hideEmptyRows;

    private String compactDirection;

    public FormLayoutDto() {
    }

    public FormLayoutDto(Integer rows, Integer cols) {
        super(rows, cols);
    }

    public FormLayoutDto(String type, String hideEmptyRows, String compactDirection) {
        this.type = type;
        this.hideEmptyRows = hideEmptyRows;
        this.compactDirection = compactDirection;
    }

    public FormLayoutDto(Integer rows, Integer cols, String type, String hideEmptyRows, String compactDirection) {
        super(rows, cols);
        this.type = type;
        this.hideEmptyRows = hideEmptyRows;
        this.compactDirection = compactDirection;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHideEmptyRows() {
        return hideEmptyRows;
    }

    public void setHideEmptyRows(String hideEmptyRows) {
        this.hideEmptyRows = hideEmptyRows;
    }

    public String getCompactDirection() {
        return compactDirection;
    }

    public void setCompactDirection(String compactDirection) {
        this.compactDirection = compactDirection;
    }
}
