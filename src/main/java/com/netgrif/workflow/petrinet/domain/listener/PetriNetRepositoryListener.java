package com.netgrif.workflow.petrinet.domain.listener;

import com.netgrif.workflow.petrinet.domain.PetriNet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

@Component
@SuppressWarnings({"unchecked"})
public class PetriNetRepositoryListener extends AbstractMongoEventListener<PetriNet> {

	private static final Logger log = LoggerFactory.getLogger(PetriNetRepositoryListener.class);

	@Override
	public void onBeforeSave(BeforeSaveEvent<PetriNet> petriNetEvent) {
		PetriNet petriNet = petriNetEvent.getSource();
		petriNet.getDataSet().forEach((id, field) -> field.setValue(null));
		log.debug("Cleared field values in net " + petriNet.getStringId());
	}
}