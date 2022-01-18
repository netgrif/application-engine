package com.netgrif.workflow.pluginmanager.services;

import com.netgrif.workflow.pluginmanager.services.interfaces.IPluginService;
import org.pf4j.PluginLoader;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PluginService implements IPluginService {

//    @Autowired
//    private PluginManager pluginManager;
//
//    @Autowired
//    private PluginLoader pluginLoader;

    @Override
    public void load() {
        
    }

}
