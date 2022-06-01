package api.workflow.domain.triggers;

import api.petrinet.domain.ImportedDto;

public class TriggerDto extends ImportedDto {

    private String id;

    public TriggerDto() {
    }

    public TriggerDto(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
