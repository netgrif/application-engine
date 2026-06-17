package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.validation;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
public class LocalizedValidation implements Serializable {

    @Serial
    private static final long serialVersionUID = 412530951556364618L;
    private String validationRule;
    private String validationMessage;

    public LocalizedValidation(String validationRule, String validationMessage) {
        this.validationRule = validationRule;
        this.validationMessage = validationMessage;
    }

}
