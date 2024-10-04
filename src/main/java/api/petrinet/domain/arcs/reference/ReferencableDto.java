package api.petrinet.domain.arcs.reference;

public final class ReferencableDto {

    private int multiplicity;

    public ReferencableDto() {
    }

    public ReferencableDto(int multiplicity) {
        this.multiplicity = multiplicity;
    }

    public int getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(int multiplicity) {
        this.multiplicity = multiplicity;
    }
}
