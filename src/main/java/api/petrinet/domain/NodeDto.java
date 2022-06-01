package api.petrinet.domain;

public class NodeDto extends PetriNetObjectDto {

    private PositionDto position;

    private I18nStringDto title;

    public NodeDto() {
    }

    public NodeDto(String id) {
        super(id);
    }

    public NodeDto(PositionDto position, I18nStringDto title) {
        this.position = position;
        this.title = title;
    }

    public NodeDto(String id, PositionDto position, I18nStringDto title) {
        super(id);
        this.position = position;
        this.title = title;
    }

    public PositionDto getPosition() {
        return position;
    }

    public void setPosition(PositionDto position) {
        this.position = position;
    }

    public I18nStringDto getTitle() {
        return title;
    }

    public void setTitle(I18nStringDto title) {
        this.title = title;
    }
}
