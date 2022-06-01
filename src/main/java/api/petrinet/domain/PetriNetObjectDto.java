package api.petrinet.domain;

public class PetriNetObjectDto extends ImportedDto {

    private String id;

    public PetriNetObjectDto() {
    }

    public PetriNetObjectDto(String id) {
        this.id = id;
    }

    public String getStringId() {
        return id;
    }

    public void setStringId(String id) {
        this.id = id;
    }
}
