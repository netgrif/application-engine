package api.petrinet.domain.arcs.reference;

public class ReferenceDto {

    private String reference;

    private String type;

    private ReferencableDto referencable;


    public ReferenceDto() {
    }

    public ReferenceDto(String reference, String type, ReferencableDto referencable) {
        this.reference = reference;
        this.type = type;
        this.referencable = referencable;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ReferencableDto getReferencable() {
        return referencable;
    }

    public void setReferencable(ReferencableDto referencable) {
        this.referencable = referencable;
    }
}
