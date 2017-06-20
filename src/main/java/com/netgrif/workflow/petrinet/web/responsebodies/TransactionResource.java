package com.netgrif.workflow.petrinet.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.Transaction;
import org.springframework.hateoas.Resource;

import java.util.ArrayList;

public class TransactionResource extends Resource<Transaction> {

    public TransactionResource(Transaction content) {
        super(content, new ArrayList<>());
    }
}