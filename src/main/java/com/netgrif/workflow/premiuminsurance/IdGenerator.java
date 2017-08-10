package com.netgrif.workflow.premiuminsurance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdGenerator {

    @Autowired
    private OfferIdRepository repository;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public Long getId() {
        OfferId id = repository.findAll().get(0);
        id.setOfferId(id.getOfferId() + 1);
        id = repository.save(id);

        return id.getOfferId();
    }
}