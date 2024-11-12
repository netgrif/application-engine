package com.netgrif.application.engine.integration.plugins.services

import com.netgrif.application.engine.integration.plugins.config.PluginProperties
import com.netgrif.application.engine.integration.plugins.exceptions.IncompatibleClassLoaderException
import com.netgrif.application.engine.integration.plugins.exceptions.IncompatiblePluginException
import com.netgrif.application.engine.integration.plugins.exceptions.RestrictedPackageException
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
//import extensions.NaeExtensionPoint
//import groovy.util.logging.Slf4j
//import org.pf4j.ExtensionPoint
//import org.pf4j.PluginClassLoader
//import org.pf4j.PluginWrapper
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.stereotype.Component
//import wrapper.NaePlugin
//import wrapper.PluginExtensionHolder
//
//import java.util.stream.Collectors
//
//@Slf4j
//@Component
//class PluginInjector {
//
//    @Autowired
//    private PluginProperties pluginProperties
//
//    @Value('${project.version}')
//    private String projectVersion
//
//    protected void injectExtension(PluginWrapper pluginWrapper) {
//        pluginWrapper.getPluginManager().getExtensions(NaeExtensionPoint.class).each { extension ->
//            checkCompatibility(extension)
//            checkClassLoader(extension as NaeExtensionPoint)
//            secureCheck(extension as NaeExtensionPoint)
//        }
//
//        PluginExtensionHolder extensionHolder = ((NaePlugin) pluginWrapper.getPlugin()).getExtensionHolder()
//        MetaClass actionDelegateMc = ActionDelegate.getMetaClass()
//        actionDelegateMc[pluginWrapper.pluginId] = extensionHolder
//    }
//
//    protected void secureCheck(NaeExtensionPoint extension) {
//        checkPackages(extension.getClass())
//    }
//
//    private void checkPackages(Class<?> aClass) {
//        List<Package> filteredRestrictedPackages = filterRestrictedPackages(aClass)
//        if (!filteredRestrictedPackages.isEmpty()) {
//            String message = "Plugin " + aClass.getName() + " contains restricted packages: " +
//                    filteredRestrictedPackages.toString()
//            log.error(message)
//            throw new RestrictedPackageException(message)
//        }
//        aClass.declaredFields.each { field ->
//            checkPackages(field.class)
//        }
//    }
//
//    private List<Package> filterRestrictedPackages(Class<?> aClass) {
//        return aClass.classLoader.definedPackages.toList().stream().filter(p -> {
//            pluginProperties.restrictedPackages.toList().stream().anyMatch(rp -> {
//                return p.getName().startsWith(rp) || p.getName().contains(rp)
//            })
//        }).collect(Collectors.toList())
//    }
//
//    private void checkCompatibility(NaeExtensionPoint extension) {
//        if (extension.getPluginVersion() != projectVersion)
//            log.warn("Netgrif Application Engine end imported library versions are not the same. Some functions may work improperly.")
//    }
//
//    private void checkClassLoader(NaeExtensionPoint extensionPoint) {
//        ClassLoader classLoader = extensionPoint.class.classLoader
//        if (!classLoader instanceof PluginClassLoader) {
//            String message = "Plugin class loader [" + classLoader.getClass().getName() + "] is not compatible with class loader for plugins."
//            log.error(message)
//            throw new IncompatibleClassLoaderException(message)
//        }
//    }
//}
