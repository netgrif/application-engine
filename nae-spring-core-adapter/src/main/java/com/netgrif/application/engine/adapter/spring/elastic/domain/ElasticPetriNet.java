package com.netgrif.application.engine.adapter.spring.elastic.domain;

import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

import static org.springframework.data.elasticsearch.annotations.FieldType.Keyword;

@NoArgsConstructor
@Document(indexName = "#{@elasticPetriNetIndex}")
public class ElasticPetriNet extends com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet {

    public ElasticPetriNet(PetriNet net) {
        super(net);
    }

    public void update(ElasticPetriNet net) {
        super.update(net);
    }

    @Id
    @Override
    public String getId() {
        return super.getId();
    }

    @Field(type = Keyword)
    @Override
    public String getIdentifier() {
        return super.getIdentifier();
    }

    @Field(type = Keyword)
    @Override
    public String getUriNodeId() {
        return super.getUriNodeId();
    }

    @Field(type = Keyword)
    @Override
    public String getWorkspaceId() {
        return super.getWorkspaceId();
    }

    @Field(type = Keyword)
    @Override
    public String getStringId() {
        return super.getStringId();
    }

    @Field(type = Keyword)
    @Override
    public String getInitials() {
        return super.getInitials();
    }

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @Override
    public LocalDateTime getCreationDate() {
        return super.getCreationDate();
    }
}
