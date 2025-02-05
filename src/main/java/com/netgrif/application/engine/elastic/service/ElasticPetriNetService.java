package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.core.petrinet.domain.PetriNet;
import com.netgrif.adapter.petrinet.service.PetriNetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ElasticPetriNetService implements IElasticPetriNetService {

    private final ElasticPetriNetRepository repository;

    private final Executor executors;

    private PetriNetService petriNetService;

    public ElasticPetriNetService(ElasticPetriNetRepository repository, Executor executors) {
        this.repository = repository;
        this.executors = executors;
    }

    @Lazy
    @Autowired
    public void setPetriNetService(PetriNetService petriNetService) {
        this.petriNetService = petriNetService;
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

    @Override
    public List<PetriNet> findAllByUriNodeId(String uriNodeId) {
        List<ElasticPetriNet> elasticPetriNets = repository.findAllByUriNodeId(uriNodeId);
        return petriNetService.findAllById(elasticPetriNets.stream().map(ElasticPetriNet::getStringId).collect(Collectors.toList()));
    }
}
