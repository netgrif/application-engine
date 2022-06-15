package com.netgrif.application.engine.navtree.service.interfaces;

import com.netgrif.application.engine.navtree.domain.NavNode;

public interface INavNodeService {
    NavNode save(NavNode navNode);

    NavNode findById(String id);

    NavNode findByName(String name);
}
