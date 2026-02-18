package com.netgrif.application.engine.objects.elastic.domain;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import com.netgrif.application.engine.objects.workspace.Workspaceable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ElasticPetriNet implements Workspaceable {

    private String id;

    private String identifier;

    private Version version;

    private boolean defaultVersion;

    private String uriNodeId;

    private String uri;

    private String workspaceId;

    private I18nField title;

    private String initials;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime creationDate;

    public ElasticPetriNet(PetriNet net) {
        this.id = net.getStringId();
        this.identifier = net.getIdentifier();
        this.version = net.getVersion();
        this.defaultVersion = net.isDefaultVersion();
        this.uriNodeId = net.getUriNodeId();
        this.uri = net.getUri();
        this.workspaceId = net.getWorkspaceId();
        this.title = this.transformToField(net.getTitle());
        this.initials = net.getInitials();
        this.creationDate = net.getCreationDate();
    }

    public void update(ElasticPetriNet net) {
        this.version = net.getVersion();
        this.defaultVersion = net.isDefaultVersion();
        if (net.getUriNodeId() != null) {
            this.uriNodeId = net.getUriNodeId();
        }
        this.uri = net.getUri();
        this.workspaceId = net.getWorkspaceId();
        this.title = net.getTitle();
        this.initials = net.getInitials();
    }

    protected abstract I18nField transformToField(I18nString field);
}
