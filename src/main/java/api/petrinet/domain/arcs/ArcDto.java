package api.petrinet.domain.arcs;

import api.petrinet.domain.NodeDto;
import api.petrinet.domain.arcs.reference.ReferenceDto;

public class ArcDto {

    protected NodeDto source;
    
    protected String sourceId;

    protected NodeDto destination;
    
    protected String destinationId;
    
    protected Integer multiplicity;
    
    protected ReferenceDto reference;

    public ArcDto() {
    }

    public ArcDto(NodeDto source, String sourceId, NodeDto destination, String destinationId, Integer multiplicity, ReferenceDto reference) {
        this.source = source;
        this.sourceId = sourceId;
        this.destination = destination;
        this.destinationId = destinationId;
        this.multiplicity = multiplicity;
        this.reference = reference;
    }

    public NodeDto getSource() {
        return source;
    }

    public void setSource(NodeDto source) {
        this.source = source;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public NodeDto getDestination() {
        return destination;
    }

    public void setDestination(NodeDto destination) {
        this.destination = destination;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public Integer getMultiplicity() {
        return multiplicity;
    }

    public void setMultiplicity(Integer multiplicity) {
        this.multiplicity = multiplicity;
    }

    public ReferenceDto getReference() {
        return reference;
    }

    public void setReference(ReferenceDto reference) {
        this.reference = reference;
    }
}
