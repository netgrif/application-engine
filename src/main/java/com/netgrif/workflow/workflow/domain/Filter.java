package com.netgrif.workflow.workflow.domain;

import com.netgrif.workflow.auth.domain.Author;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.petrinet.web.responsebodies.TransitionReference;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class Filter {

    @Id
    private ObjectId _id;

    @Getter @Setter
    private String name;

    @Getter @Setter
    private String organization;

    @Getter @Setter
    private Author user;

    @Getter @Setter
    private List<PetriNetReference> petriNets;

    @Getter @Setter
    private List<TransitionReference> transitions;

    @Getter @Setter
    private List<String> roles;

    public Filter() {
        petriNets = new ArrayList<>();
        transitions = new ArrayList<>();
        roles = new ArrayList<>();
    }

    public Filter(String name) {
        this.name = name;
    }

    public Filter(String name, String organization, Author user) {
        this.name = name;
        this.organization = organization;
        this.user = user;
    }

    public ObjectId get_id() {
        return _id;
    }

    public void resolveVisibility(int visibility, LoggedUser user){
        switch (visibility){
            case CreateFilterBody.GLOBAL:
                organization = null;
                this.user = null;
                break;
            case CreateFilterBody.PRIVATE:
                organization = null;
                this.user = user.transformToAuthor();
                break;
            case CreateFilterBody.ORGANIZATION:
                organization = user.getFullName(); //TODO: 24.2.2017 change to organization id
                this.user = null;
                break;
        }
    }
}
