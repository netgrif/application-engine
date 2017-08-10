package com.netgrif.workflow.insurance

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Insurance
import com.netgrif.workflow.premiuminsurance.IdGenerator
import com.netgrif.workflow.premiuminsurance.OfferId
import com.netgrif.workflow.premiuminsurance.OfferIdRepository
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class InsuranceTest {

    @Autowired
    private OfferIdRepository repository

    @Autowired
    private IdGenerator generator

    @Test
    void testOfferIdGeneration() {
        repository.save(new OfferId(offerId: 0))

        def insurance = new Insurance(null, null)
        def offerId = insurance.offerId()

        assert offerId == "3110000016"
    }

    @Test
    void testConcurrentOfferIdGeneration() {
        repository.save(new OfferId(offerId: 0))
        def threads = []
        (0..100).each {
            threads << new Thread({
                try {
                    println generator.getId()
                } catch (Exception ignored) {

                }
            })
        }

        threads.each { it.start() }
        threads.each { it.join() }
    }
}