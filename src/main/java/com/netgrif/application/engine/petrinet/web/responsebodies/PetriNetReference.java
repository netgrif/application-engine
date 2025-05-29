package com.netgrif.application.engine.petrinet.web.responsebodies;


import com.netgrif.application.engine.authorization.domain.permissions.AccessPermissions;
import com.netgrif.application.engine.authorization.domain.permissions.CasePermission;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.utils.UniqueKeyMapWrapper;
import com.netgrif.application.engine.workflow.web.responsebodies.DataFieldReference;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Data
public class PetriNetReference extends Reference {

    private String identifier;
    private String version;
    private String defaultCaseName;
    private String icon;
    private LocalDateTime createdDate;
    private String authorId;
    private List<DataFieldReference> immediateData;
    private AccessPermissions<CasePermission> processRolePermissions;
    private UniqueKeyMapWrapper<String> properties;


    public PetriNetReference() {
        super();
    }

    public PetriNetReference(String stringId, String identifier, String version, String title, String authorId, String defaultCaseName) {
        super(stringId, title);
        this.identifier = identifier;
        this.version = version;
        this.defaultCaseName = defaultCaseName;
        this.authorId = authorId;
    }

    public PetriNetReference(Process net, Locale locale) {
        this(net.getStringId(), net.getIdentifier(), net.getVersion().toString(), net.getTitle().getTranslation(locale), net.getAuthorId(), net.getTranslatedDefaultCaseName(locale));
        this.icon = net.getIcon();
        this.createdDate = net.getCreationDate();
        this.immediateData = net.getImmediateFields().stream().map(field -> new DataFieldReference(field, locale)).collect(Collectors.toList());
        this.processRolePermissions = net.getProcessRolePermissions();
        this.properties = net.getProperties();
    }
}