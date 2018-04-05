package com.netgrif.workflow.petrinet.web.responsebodies;


import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Locale;

@Data
public class PetriNetReference extends Reference {

    private String identifier;
    private String version;
    private String initials;
    private String icon;
    private LocalDateTime createdDate;
    private Author author;


    public PetriNetReference() {
        super();
    }

    public PetriNetReference(String stringId, String identifier, String version, String title, String initials) {
        super(stringId, title);
        this.identifier = identifier;
        this.version = version;
        this.initials = initials;
    }

    public PetriNetReference(String stringId, String title, String identifier, String version, String initials, String icon, LocalDateTime createdDate, Author author) {
        super(stringId, title);
        this.identifier = identifier;
        this.version = version;
        this.initials = initials;
        this.icon = icon;
        this.createdDate = createdDate;
        this.author = author;
    }

    public PetriNetReference(PetriNet net, Locale locale) {
        this(net.getStringId(), net.getIdentifier(), net.getVersion(), net.getTitle().getTranslation(locale), net.getInitials());
        this.icon = net.getIcon();
        this.createdDate = net.getCreationDate();
        this.author = net.getAuthor();
    }
}