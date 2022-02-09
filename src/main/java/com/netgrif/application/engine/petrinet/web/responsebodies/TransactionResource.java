package com.netgrif.application.engine.petrinet.web.responsebodies;

import org.springframework.hateoas.EntityModel;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionResource extends EntityModel<Transaction> {

    public TransactionResource(com.netgrif.application.engine.petrinet.domain.Transaction content, Locale locale) {
        super(new Transaction(content.getTransitions(), content.getTitle().getTranslation(locale)), new ArrayList<>());
    }
}