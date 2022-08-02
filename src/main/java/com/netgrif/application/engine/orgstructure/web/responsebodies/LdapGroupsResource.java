package com.netgrif.application.engine.orgstructure.web.responsebodies;

import com.netgrif.application.engine.orgstructure.web.LdapController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

@ConditionalOnExpression("${nae.ldap.enabled:false}")
public class LdapGroupsResource extends CollectionModel<LdapGroupResponseBody> {

    public LdapGroupsResource(Iterable<LdapGroupResponseBody> content) {
        super(content);
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LdapController.class)
                .getAllLdapGroups(null,null)).withSelfRel())
        .add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(LdapController.class)
                .assignRolesToLdapGroup(null, null)).withRel("roleAssign"));
    }

}
