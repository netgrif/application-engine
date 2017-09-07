package com.netgrif.workflow.premiuminsurance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OfferIdRepository extends JpaRepository<OfferId, Long> {
}