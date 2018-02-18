package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.Transaction;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;
import java.util.Locale;

public class LocalisedTransactionResource extends Resource<LocalisedTransaction> {

    public LocalisedTransactionResource(Transaction content, Locale locale) {
        super(new LocalisedTransaction(content.getTransitions(), content.getTitle().getTranslation(locale)), new ArrayList<>());
    }
}