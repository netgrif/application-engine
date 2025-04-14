package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.elastic.domain.ElasticPetriNet;
import com.netgrif.application.engine.elastic.domain.repoitories.ElasticPetriNetRepository;
import com.netgrif.application.engine.elastic.service.executors.Executor;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticPetriNetService;
import com.netgrif.application.engine.petrinet.domain.Process;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ElasticPetriNetService implements IElasticPetriNetService {

    private final ElasticPetriNetRepository repository;

    private final Executor executors;

    private IPetriNetService petriNetService;

    @Lazy
    @Autowired
    public void setPetriNetService(IPetriNetService petriNetService) {
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
                log.debug("[{}]: PetriNet \"{}\" indexed", net.getStringId(), net.getTitle());
            } catch (InvalidDataAccessApiUsageException ignored) {
                log.debug("[{}]: PetriNet \"{}\" has duplicates, will be reindexed", net.getStringId(), net.getTitle());
                repository.deleteAllByStringId(net.getStringId());
                repository.save(net);
                log.debug("[{}]: PetriNet \"{}\" indexed", net.getStringId(), net.getTitle());
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
            log.info("[{}]: PetriNet \"{}\" deleted", id, id);
        });
    }

    @Override
    public String findUriNodeId(Process net) {
        if (net == null) {
            return null;
        }
        // todo 2058 authorisation? must be admin?
        ElasticPetriNet elasticPetriNet = repository.findByStringId(net.getStringId());
        if (elasticPetriNet == null) {
            log.warn("[{}] PetriNet with id [{}] is not indexed.", net.getStringId(), net.getStringId());
            return null;
        }

        return elasticPetriNet.getUriNodeId();
    }

    @Override
    public List<Process> findAllByUriNodeId(String uriNodeId) {
        List<ElasticPetriNet> elasticPetriNets = repository.findAllByUriNodeId(uriNodeId);
        return petriNetService.findAllById(elasticPetriNets.stream().map(ElasticPetriNet::getStringId).collect(Collectors.toList()));
    }
}
