package com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner

import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import javax.inject.Provider

@Component
class PetriNetFieldsExpressionRunner extends FieldsExpressionRunner<PetriNet> {

    @Autowired
    private Provider<ActionDelegate> delegateProvider

    PetriNetFieldsExpressionRunner(@Value('${expressions.runner.cache-size}') int cacheSize) {
        super(cacheSize)
    }

    @Override
    protected void initCode(Object delegate, PetriNet petriNet, Map<String, String> fields) {
        ActionDelegate ad = ((ActionDelegate) delegate)
        ad.petriNet = petriNet
        ad.initFieldsMap(fields)
    }

    @Override
    protected Map<String, String> getFieldIds(PetriNet petriNet) {
        return petriNet.staticDataSet.keySet().collectEntries {[(it): (it)]} as Map<String, String>
    }

    @Override
    protected ActionDelegate actionDelegate() {
        return delegateProvider.get()
    }
}
