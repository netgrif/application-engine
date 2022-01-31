package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.workflow.web.responsebodies.ResponseMessage;
import lombok.Data;

import java.util.Locale;

@Data
public class PetriNetReferenceWithMessage extends ResponseMessage {

    private PetriNetReference net;

    public PetriNetReferenceWithMessage(String msg) {
        super();
        setError(msg);
    }

    public PetriNetReferenceWithMessage(String msg, PetriNet net, Locale locale) {
        super();
        setSuccess(msg);
        setNet(new PetriNetReference(net, locale));
    }

    public PetriNetReference getNet() {
        return net;
    }

    public void setNet(PetriNetReference net) {
        this.net = net;
    }
}
