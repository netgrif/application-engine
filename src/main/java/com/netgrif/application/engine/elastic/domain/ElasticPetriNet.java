package com.netgrif.application.engine.elastic.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.core.petrinet.domain.I18nString;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.core.petrinet.domain.version.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "#{@elasticPetriNetIndex}")
public class ElasticPetriNet {

    @Id
    private String id;

    @Field(type = Keyword)
    private String identifier;

    private Version version;

    @Field(type = Keyword)
    private String uriNodeId;

    @Field(type = Keyword)
    private String stringId;

    private I18nString title;

    @Field(type = Keyword)
    private String initials;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime creationDate;

    public ElasticPetriNet(PetriNet net) {
        this.identifier = net.getIdentifier();
        this.version = net.getVersion();
        this.uriNodeId = net.getUriNodeId();
        this.stringId = net.getStringId();
        this.title = net.getTitle();
        this.initials = net.getInitials();
        this.creationDate = net.getCreationDate();
    }

    public void update(ElasticPetriNet net) {
        this.version = net.getVersion();
        if (net.getUriNodeId() != null) {
            this.uriNodeId = net.getUriNodeId();
        }
        this.title = net.getTitle();
        this.initials = net.getInitials();
    }
}
