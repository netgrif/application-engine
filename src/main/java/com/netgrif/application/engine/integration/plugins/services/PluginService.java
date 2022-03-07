package com.netgrif.application.engine.integration.plugins.services;

import com.netgrif.application.engine.integration.plugins.config.PluginProperties;
import com.netgrif.application.engine.integration.plugins.services.interfaces.IPluginService;
import extensions.NaeExtensionPoint;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.pf4j.Plugin;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wrapper.NaePlugin;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing plugins in NAE
 * */
@Slf4j
@Service
public class PluginService implements IPluginService {

    @Autowired
    private PluginProperties pluginProperties;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private PluginInjector pluginInjector;

    /**
     * Initialization of plugin system inside NAE. Sets properties and loads the plugins.
     * */
    @PostConstruct
    protected void init() {
        if (pluginProperties.getPluginFileNames().length == 1 && pluginProperties.getPluginFileNames()[0].equals("*"))
            load(pluginProperties.getDir());
        else if (pluginProperties.getPluginFileNames().length > 0)
            Arrays.stream(pluginProperties.getPluginFileNames()).forEach(fileName -> load(pluginProperties.getDir() + "/" + fileName));
    }

    /**
     * Returns a plugin by its ID
     * @param pluginId the plugin ID of required plugin
     * @return the required plugin
     * */
    @Override
    public final NaePlugin getPlugin(String pluginId) {
        Plugin plugin = this.pluginManager.getPlugin(pluginId).getPlugin();
        if (!(plugin instanceof NaePlugin)) {
            log.error("Plugin " + plugin.toString() + " is not type of NaePlugin.");
            return null;
        }
        return (NaePlugin) plugin;
    }

    /**
     * Returns all NaePlugin
     * @return the required plugins
     * */
    @Override
    public final List<NaePlugin> getAllPlugin() {
        List<NaePlugin> plugins = this.pluginManager.getPlugins().stream().map(pw -> {
            if (pw.getPlugin() instanceof NaePlugin)
                return (NaePlugin) pw.getPlugin();
            else
                return null;
        }).collect(Collectors.toList());
        return plugins;
    }

    /**
     * Calls a function of an extension point from a single plugin and returns its result.
     * @param pluginId ID of the plugin
     * @param extensionName name of the extension point class where the function is defined
     * @param method name of method to be called
     * @param argumentValues the input values of arguments of function
     * @param returnType the class type of the return value of the function
     * @return an object from the function inside plugin
     * */
    @Override
    public final <T> T call(String pluginId, String extensionName, String method, List<Object> argumentValues, Class<T> returnType) {
        NaeExtensionPoint extension = retrieveExtension(pluginId, extensionName);
        if (extension == null) {
            return null;
        }
        return extension.call(method, argumentValues, returnType);
    }

    /**
     * Loads the plugin from the specific path
     * @param path input path
     * */
    private void load(String path) {
        Path pluginPath = Paths.get(path);
        if (Files.isDirectory(pluginPath)) {
            File dir = new File(path);
            File[] pluginJars = dir.listFiles((FilenameFilter) new WildcardFileFilter("*.jar"));
            if (pluginJars != null)
                Arrays.stream(pluginJars).forEach(jar -> loadAndStartPlugin(Paths.get(jar.getPath())));
        } else if (FilenameUtils.getExtension(pluginPath.getFileName().toString()).equals("jar")) {
            loadAndStartPlugin(pluginPath);
        }
        injectPlugin();
    }

    /**
     * Injects the plugin and it's extension
     * */
    private void injectPlugin() {
        this.pluginManager.getPlugins().forEach(plugin -> {
            if (plugin.getPlugin() instanceof NaePlugin) {
                ((NaePlugin) plugin.getPlugin()).init();
                pluginInjector.injectExtension(plugin);
            }
        });
    }

    /**
     * Loads and starts the plugin.
     * @param pluginPath Path of the plugin to be loaded and started
     * */
    private void loadAndStartPlugin(Path pluginPath) {
        pluginManager.loadPlugin(pluginPath);
        pluginManager.startPlugins();
    }

    /**
     * Retrieves extension by the given plugin ID, extension name and method name.
     * @param pluginId ID of the plugin where the extension is defined
     * @param extensionName name of the extension class
     * @return the NaeExtensionPoint object
     * */
    private NaeExtensionPoint retrieveExtension(String pluginId, String extensionName) {
        Optional<NaeExtensionPoint> extension = pluginManager.getExtensions(pluginId)
                .stream()
                .filter(ext -> ext.getClass().getName().equals(extensionName))
                .findFirst();

        if (extension.isEmpty()) {
            log.error("There is no extension with given class name [" + extensionName + "]");
            return null;
        }
        return extension.get();
    }
}
