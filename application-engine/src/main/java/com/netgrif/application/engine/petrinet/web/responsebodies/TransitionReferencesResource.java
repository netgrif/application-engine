package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.petrinet.DataFieldReferenceDto;
import com.netgrif.application.engine.objects.dto.response.petrinet.TransitionReferenceDto;
import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.core.Relation;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

public class TransitionReferencesResource extends CollectionModel<TransitionReferencesResource.TransitionReferenceWrapper> {

    public TransitionReferencesResource(Iterable<TransitionReferenceDto> content) {
        super(StreamSupport.stream(content.spliterator(), false)
                .map(TransitionReferenceWrapper::new)
                .toList());
        buildLinks();
    }

    private void buildLinks() {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getTransitionReferences(new ArrayList<>(), null, null)).withSelfRel());
    }

    @Relation(collectionRelation = "transitionReferences", itemRelation = "transitionReference")
    public static class TransitionReferenceWrapper implements Serializable {

        private final TransitionReferenceDto delegate;

        public TransitionReferenceWrapper(TransitionReferenceDto dto) {
            this.delegate = dto;
        }

        public String getStringId() {
            return delegate.stringId();
        }

        public String getTitle() {
            return delegate.title();
        }

        public String getPetriNetId() {
            return delegate.petriNetId();
        }

        public List<DataFieldReferenceDto> getImmediateData() {
            return delegate.immediateData();
        }
    }
}
