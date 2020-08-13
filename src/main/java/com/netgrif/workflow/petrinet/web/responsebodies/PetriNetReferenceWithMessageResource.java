package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;
import java.util.Locale;

public class PetriNetReferenceWithMessageResource extends Resource<PetriNetReferenceWithMessage> {

    public PetriNetReferenceWithMessageResource(PetriNetReferenceWithMessage content) {
        super(content, new ArrayList<Link>());
    }

    public static PetriNetReferenceWithMessageResource successMessage(String msg, PetriNet net, Locale locale){
        return new PetriNetReferenceWithMessageResource(new PetriNetReferenceWithMessage(msg, net, locale));
    }

    public static PetriNetReferenceWithMessageResource errorMessage(String msg){
        return new PetriNetReferenceWithMessageResource(new PetriNetReferenceWithMessage(msg));
    }
}
