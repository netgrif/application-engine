package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.objects.dto.response.petrinet.PetriNetReferenceDto;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.adapter.spring.common.web.responsebodies.ResponseMessage;
import lombok.Data;

import java.util.Locale;

@Data
public class PetriNetReferenceWithMessage extends ResponseMessage {

    private PetriNetReferenceDto net;

    public PetriNetReferenceWithMessage(String msg) {
        super();
        setError(msg);
    }

    public PetriNetReferenceWithMessage(String msg, PetriNet net, Locale locale) {
        super();
        setSuccess(msg);
        setNet(PetriNetReferenceDto.fromPetriNet(net, locale));
    }

    public PetriNetReferenceDto getNet() {
        return net;
    }

    public void setNet(PetriNetReferenceDto net) {
        this.net = net;
    }
}
