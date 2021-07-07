package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.oauth.domain.RemoteGroupResource;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.server.core.Relation;

import java.util.List;

@Relation(collectionRelation = "users")
public class OAuthUser extends User {

    @Getter
    @Setter
    protected List<RemoteGroupResource> remoteGroups;

    public OAuthUser(com.netgrif.workflow.oauth.domain.OAuthUser user) {
        super(user);
        setTelNumber(user.getTelNumber());
//        setGroups(user.getGroups());
        setAuthorities(user.getAuthorities());
        setNextGroups(user.getNextGroups());
        this.remoteGroups = user.getRemoteGroups();
    }
}
