package com.netgrif.workflow.petrinet.web.responsebodies;

import org.springframework.hateoas.Resource;

import java.util.ArrayList;
import java.util.Locale;

public class TransactionResource extends Resource<Transaction> {

    public TransactionResource(com.netgrif.workflow.petrinet.domain.Transaction content, Locale locale) {
        super(new Transaction(content.getTransitions(), content.getTitle().getTranslation(locale)), new ArrayList<>());
    }
}