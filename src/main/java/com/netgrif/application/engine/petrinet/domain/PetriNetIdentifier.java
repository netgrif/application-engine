package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.petrinet.domain.version.Version;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.index.CompoundIndex;

@Getter
@Setter
@CompoundIndex(name = "cmp-idx-one", def = "{'identifier': 1, 'version.major': -1, 'version.minor': -1, 'version.patch': -1}")
public class PetriNetIdentifier {

    private ObjectId id;

    private String identifier;

    private Version version;

    public PetriNetIdentifier() {
        identifier = "Default";
        version = new Version();
        this.id = new ObjectId();
    }

    public PetriNetIdentifier(String identifier, Version version, ObjectId id) {
        this.identifier = identifier;
        this.version = version;
        this.id = id;
    }

    @Override
    public PetriNetIdentifier clone() {
        PetriNetIdentifier clone = new PetriNetIdentifier();
        clone.setIdentifier(identifier);
        clone.setVersion(version == null ? null : version.clone());
        clone.setId(id);
        return clone;
    }
}
