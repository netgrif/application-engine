package com.netgrif.workflow.insurance

import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.TextField
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Insurance
import com.netgrif.workflow.premiuminsurance.IdGenerator
import com.netgrif.workflow.premiuminsurance.OfferId
import com.netgrif.workflow.premiuminsurance.OfferIdRepository
import com.netgrif.workflow.workflow.domain.Case
import com.netgrif.workflow.workflow.domain.DataField
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
    void offerIdGenerationTest() {
        Case mockCase = new Case()
        Field mockField = new TextField()
        mockCase.dataSet.put(mockField.getStringId(), new DataField())
        repository.save(new OfferId())
        def insurance = new Insurance(mockCase, mockField)

        def offerId = insurance.offerId()

        assertValidOfferId(offerId)
    }

    @Test
    void concurrentOfferIdGenerationTest() {
        repository.save(new OfferId())
        def threads = []
        def ids = []
        (0..1000).each {
            threads << new Thread({
                ids.add(generator.getId())
            })
        }

        threads.each { it.start() }
        threads.each { it.join() }

        assert ids.size() == ids.toSet().size()
    }

    private static def assertValidOfferId(String offerId) {
        assert offerId ==~ /311[0-9]{7}/
        assert offerId[9] as int == offerId.substring(0, 9).collect { it as int }.sum() % 10
    }
}