package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNetRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.objects.elastic.domain.ElasticPetriNet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ElasticPetriNetService implements IElasticPetriNetService {

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
                com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet elasticPetriNet = repository.findByStringId(net.getStringId());
                if (elasticPetriNet == null) {
                    repository.save((com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet) net);
                } else {
                    elasticPetriNet.update(net);
                    repository.save(elasticPetriNet);
                }
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" indexed");
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[" + net.getStringId() + "]: PetriNet \"" + net.getTitle() + "\" has duplicates, will be reindexed");
                repository.deleteAllByStringId(net.getStringId());
                repository.save((com.netgrif.application.engine.adapter.spring.elastic.domain.ElasticPetriNet) net);
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
}
