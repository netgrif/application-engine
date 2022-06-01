package api.petrinet.domain.layout;

public class TaskLayoutDto extends FormLayoutDto {

    private Integer offset;

    private String fieldAlignment;

    public TaskLayoutDto() {
    }

    public TaskLayoutDto(Integer rows, Integer cols) {
        super(rows, cols);
    }

    public TaskLayoutDto(String type, String hideEmptyRows, String compactDirection) {
        super(type, hideEmptyRows, compactDirection);
    }

    public TaskLayoutDto(Integer rows, Integer cols, String type, String hideEmptyRows, String compactDirection) {
        super(rows, cols, type, hideEmptyRows, compactDirection);
    }

    public TaskLayoutDto(Integer offset, String fieldAlignment) {
        this.offset = offset;
        this.fieldAlignment = fieldAlignment;
    }

    public TaskLayoutDto(Integer rows, Integer cols, Integer offset, String fieldAlignment) {
        super(rows, cols);
        this.offset = offset;
        this.fieldAlignment = fieldAlignment;
    }

    public TaskLayoutDto(String type, String hideEmptyRows, String compactDirection, Integer offset, String fieldAlignment) {
        super(type, hideEmptyRows, compactDirection);
        this.offset = offset;
        this.fieldAlignment = fieldAlignment;
    }

    public TaskLayoutDto(Integer rows, Integer cols, String type, String hideEmptyRows, String compactDirection, Integer offset, String fieldAlignment) {
        super(rows, cols, type, hideEmptyRows, compactDirection);
        this.offset = offset;
        this.fieldAlignment = fieldAlignment;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public String getFieldAlignment() {
        return fieldAlignment;
    }

    public void setFieldAlignment(String fieldAlignment) {
        this.fieldAlignment = fieldAlignment;
    }
}
