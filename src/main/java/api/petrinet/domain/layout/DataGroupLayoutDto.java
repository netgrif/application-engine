package api.petrinet.domain.layout;

public final class DataGroupLayoutDto extends FormLayoutDto {

    public DataGroupLayoutDto() {
    }

    public DataGroupLayoutDto(Integer rows, Integer cols) {
        super(rows, cols);
    }

    public DataGroupLayoutDto(String type, String hideEmptyRows, String compactDirection) {
        super(type, hideEmptyRows, compactDirection);
    }

    public DataGroupLayoutDto(Integer rows, Integer cols, String type, String hideEmptyRows, String compactDirection) {
        super(rows, cols, type, hideEmptyRows, compactDirection);
    }
}
