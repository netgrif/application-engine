package com.netgrif.application.engine.objects.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
//import org.springframework.data.annotation.Id;

public class PetriNetObject extends Imported {

//    @Id
    @JsonIgnore
    protected ObjectId _id;

    public PetriNetObject(PetriNetObject petriNetObject) {
        this.setImportId(petriNetObject.getImportId());
        this.setObjectId(petriNetObject.getObjectId());
    }

    public PetriNetObject() {
        super();
    }

    public String getStringId() {
        return importId;
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
    }
}
