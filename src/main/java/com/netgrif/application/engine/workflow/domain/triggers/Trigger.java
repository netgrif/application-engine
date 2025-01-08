package com.netgrif.application.engine.workflow.domain.triggers;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.workflow.domain.Imported;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
public abstract class Trigger extends Imported {

    @Id
    private ObjectId id;

    public Trigger() {
        this.id = new ObjectId();
    }

    @QueryType(PropertyType.NONE)
    public abstract TriggerType getType();

    public abstract Trigger clone();
}