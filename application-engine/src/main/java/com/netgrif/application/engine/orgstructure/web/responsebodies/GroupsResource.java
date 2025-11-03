package com.netgrif.application.engine.orgstructure.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.authority.AuthorityDto;
import com.netgrif.application.engine.objects.dto.response.group.GroupDto;
import com.netgrif.application.engine.objects.dto.response.petrinet.ProcessRoleDto;
import com.netgrif.application.engine.orgstructure.web.GroupController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.StreamSupport;

public class GroupsResource extends CollectionModel<GroupsResource.GroupReferenceWrapper> {

    public GroupsResource(Iterable<GroupDto> content) {
        super(StreamSupport.stream(content.spliterator(), false).map(GroupReferenceWrapper::new).toList());
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(GroupController.class)
                .getAllGroups(null)).withSelfRel());
    }

    @Relation(collectionRelation = "groups", itemRelation = "group")
    public static class GroupReferenceWrapper implements Serializable {

        private final GroupDto delegate;

        public GroupReferenceWrapper(GroupDto group) {
            this.delegate = group;
        }

        public String getId() {
            return delegate.id();
        }

        public String getName() {
            return delegate.displayName();
        }

        public String getIdentifier() {
            return delegate.identifier();
        }

        public String getOwnerUsername() {
            return delegate.ownerUsername();
        }

        public Set<ProcessRoleDto> getProcessRoles() {
            return delegate.processRoles();
        }

        public Set<AuthorityDto> getAuthorities() {
            return delegate.authoritySet();
        }
    }

}