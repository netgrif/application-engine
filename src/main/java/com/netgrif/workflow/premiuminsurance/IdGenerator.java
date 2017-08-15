package com.netgrif.workflow.premiuminsurance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IdGenerator {

    @Autowired
    private OfferIdRepository repository;

    public Long getId() {
        OfferId id = repository.saveAndFlush(new OfferId());
        return id.getId();
    }
}