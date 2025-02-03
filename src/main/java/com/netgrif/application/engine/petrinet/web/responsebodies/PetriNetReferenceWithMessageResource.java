package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.adapter.petrinet.domain.PetriNet;
import org.springframework.hateoas.CollectionModel;

import java.util.Collections;
import java.util.Locale;

public class PetriNetReferenceWithMessageResource extends CollectionModel<PetriNetReferenceWithMessage> {

    public PetriNetReferenceWithMessageResource(PetriNetReferenceWithMessage content) {
        super(Collections.singleton(content));
    }

    public static PetriNetReferenceWithMessageResource successMessage(String msg, PetriNet net, Locale locale) {
        return new PetriNetReferenceWithMessageResource(new PetriNetReferenceWithMessage(msg, net, locale));
    }

    public static PetriNetReferenceWithMessageResource errorMessage(String msg) {
        return new PetriNetReferenceWithMessageResource(new PetriNetReferenceWithMessage(msg));
    }
}
