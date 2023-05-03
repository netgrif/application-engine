package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

@Service
public class ElasticPetriNetService implements IElasticPetriNetService {
    private static final Logger log = LoggerFactory.getLogger(ElasticPetriNetService.class);

    private final ElasticPetriNetRepository repository;

    private final Executor executors;

    public ElasticPetriNetService(ElasticPetriNetRepository repository, Executor executors) {
        this.repository = repository;
        this.executors = executors;
    }

    @Override
    public void index(ElasticPetriNet net) {
        executors.execute(net.getStringId(), () -> {
            try {
                ElasticPetriNet elasticPetriNet = repository.findByStringId(net.getStringId());
                if (elasticPetriNet == null) {
                    repository.save(net);
                } else {
                    elasticPetriNet.update(net);
                    repository.save(elasticPetriNet);
                }
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" indexed");
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" has duplicates, will be reindexed");
                repository.deleteAllByStringId(net.getStringId());
                repository.save(net);
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" indexed");
            }
        });
    }

    @Override
    public void indexNow(ElasticPetriNet net) {
        index(net);
    }

    @Override
    public void remove(String id) {
        executors.execute(id, () -> {
            repository.deleteAllByStringId(id);
            log.info("[" + id + "]: PetriNet \"" + id + "\" deleted");
        });
    }

    @Override
    public String findUriNodeId(PetriNet net) {
        if (net == null) {
            return null;
        }
        ElasticPetriNet elasticPetriNet = repository.findByStringId(net.getStringId());
        if (elasticPetriNet == null) {
            log.warn("[" + net.getStringId() + "] PetriNet with id [" + net.getStringId() + "] is not indexed.");
            return null;
        }

        return elasticPetriNet.getUriNodeId();
    }
}
