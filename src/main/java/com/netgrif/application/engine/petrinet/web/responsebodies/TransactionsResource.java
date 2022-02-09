package com.netgrif.application.engine.petrinet.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.Transaction;
import com.netgrif.application.engine.petrinet.web.PetriNetController;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class TransactionsResource extends CollectionModel<TransactionResource> {

    public TransactionsResource(Iterable<TransactionResource> content, String netId) {
        super(content);
        buildLinks(netId);
    }

    public TransactionsResource(Collection<Transaction> content, String netId, Locale locale) {
        this(content.stream().map(t -> new TransactionResource(t, locale)).collect(Collectors.toList()), netId);
    }

    private void buildLinks(String netId) {
        add(WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PetriNetController.class)
                .getTransactions(netId, null)).withSelfRel());
    }
}
