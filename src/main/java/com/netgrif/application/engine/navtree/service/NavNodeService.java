package com.netgrif.application.engine.navtree.service;

import com.netgrif.application.engine.navtree.domain.NavNode;
import com.netgrif.application.engine.navtree.domain.repository.NavNodeRepository;
import com.netgrif.application.engine.navtree.service.interfaces.INavNodeService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class NavNodeService implements INavNodeService {

    private final NavNodeRepository navNodeRepository;

    public NavNodeService(NavNodeRepository navNodeRepository) {
        this.navNodeRepository = navNodeRepository;
    }

    @Override
    public NavNode save(NavNode navNode) {
        return navNodeRepository.save(navNode);
    }

    @Override
    public NavNode findById(String id) {
        Optional<NavNode> navNodeOptional = navNodeRepository.findById(id);
        if (navNodeOptional.isEmpty())
            throw new IllegalArgumentException("Could not find NavNode with id [" + id + "]");
        return navNodeOptional.get();
    }

    @Override
    public NavNode findByName(String name) {
        return navNodeRepository.findByName(name);
    }

}
