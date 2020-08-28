package com.netgrif.workflow.rules.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Document
@Data
@Builder
@AllArgsConstructor
public class StoredRule {

    @Id
    private ObjectId _id;

    @Indexed
    private String identifier;

    @NotNull
    private String when;

    @NotNull
    private String then;

    private boolean enabled = true;

    private LocalDate dateEffective;

    private LocalDate dateExpires;

    private int salience = 1;

    @Indexed
    private LocalDateTime lastUpdate;

    @Indexed
    private LocalDateTime creationDate;

    public StoredRule() {
        _id = new ObjectId();
        identifier = _id.toString();
        lastUpdate = LocalDateTime.now();
        creationDate = LocalDateTime.now();
    }

    public String getStringId() {
        return _id.toString();
    }

}
