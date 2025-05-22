package com.netgrif.application.engine.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

@Slf4j
@Component
@Endpoint(id = "loadedModules")
public class LoadedModulesEndpoint {

    private static final String CACHE_NAME = "loadedModules";
    private static final String MANIFEST_PATH = "META-INF/MANIFEST.MF";
    private static final String NAME = "Netgrif-Name";
    private static final String VERSION = "Netgrif-Version";
    private static final String AUTHOR = "Netgrif-Author";
    private static final String DESCRIPTION = "Netgrif-Description";
    private static final String GROUP_ID = "Netgrif-GroupId";
    private static final String ARTIFACT_ID = "Netgrif-ArtifactId";
    private static final String NETGRIF_URL = "Netgrif-Url";
    private static final String SCM_CONNECTION = "Netgrif-SCM-Connection";
    private static final String SCM_URL = "Netgrif-SCM-URL";
    private static final String BUILD_TIME = "Netgrif-BuildTime";
    private static final String LICENSE = "Netgrif-License";
    private static final String ORGANIZATION = "Netgrif-Organization";
    private static final String ORGANIZATION_URL = "Netgrif-OrganizationUrl";
    private static final String ISSUE_SYSTEM = "Netgrif-IssueSystem";
    private static final String ISSUE_URL = "Netgrif-IssueUrl";
    private static final String BUILD_JDK = "Netgrif-BuildJdk";

    @ReadOperation
    @Cacheable(CACHE_NAME)
    public List<LoadModule> getLoadedModules() {
        log.debug("Starting module manifest search...");
        List<Manifest> manifests = findAllManifests();
        log.debug("Found {} manifests on classpath", manifests.size());

        if (manifests.isEmpty()) {
            log.error("No manifests found in classpath! Check your dependencies and classloader setup.");
        }

        List<LoadModule> modules = new ArrayList<>();
        for (Manifest manifest : manifests) {
            extractModuleFromManifest(manifest).ifPresent(module -> {
                log.trace("Loaded module: {}", module);
                modules.add(module);
            });
        }

        log.info("Loaded {} modules with Netgrif attributes", modules.size());
        return modules;
    }

    private List<Manifest> findAllManifests() {
        List<Manifest> manifests = new ArrayList<>();
        boolean foundAny = false;
        Exception error = null;

        log.debug("Searching manifests via ContextClassLoader...");
        try {
            foundAny = findAndAddManifests(Thread.currentThread().getContextClassLoader(), manifests, "ContextClassLoader");
        } catch (Exception e) {
            error = e;
            log.warn("ContextClassLoader manifest search failed: {}", e.toString());
        }

        if (!foundAny) {
            log.debug("No manifests found via ContextClassLoader, trying SystemClassLoader...");
            try {
                boolean systemFound = findAndAddManifests(ClassLoader.getSystemClassLoader(), manifests, "SystemClassLoader");
                foundAny = foundAny || systemFound;
            } catch (Exception e) {
                log.warn("SystemClassLoader manifest search failed: {}", e.toString());
                if (error == null) {
                    error = e;
                }
            }
        }

        if (!foundAny && error != null) {
            log.error("Failed to find any manifests on classpath.", error);
        }

        return manifests;
    }

    private boolean findAndAddManifests(ClassLoader cl, List<Manifest> manifests, String loaderName) {
        boolean found = false;
        try {
            Enumeration<URL> manifestUrls = cl.getResources(MANIFEST_PATH);
            int manifestCount = 0;
            while (manifestUrls.hasMoreElements()) {
                found = true;
                URL url = manifestUrls.nextElement();
                manifestCount++;
                try (InputStream is = url.openStream()) {
                    Manifest manifest = new Manifest(is);
                    manifests.add(manifest);
                    log.trace("[{}] Loaded manifest from: {}", loaderName, url);
                } catch (Exception e) {
                    log.warn("[{}] Failed to read manifest from URL: {}", loaderName, url, e);
                }
            }
            log.debug("[{}] Found {} manifest(s).", loaderName, manifestCount);
        } catch (Exception e) {
            log.warn("[{}] Failed to get resources: {}", loaderName, cl, e);
        }
        return found;
    }

    private Optional<LoadModule> extractModuleFromManifest(Manifest manifest) {
        Attributes attrs = manifest.getMainAttributes();
        if (log.isTraceEnabled()) {
            attrs.forEach((key, value) -> log.trace("Manifest attribute: {} = {}", key, value));
        }
        String name = attrs.getValue(NAME);
        if (name == null) {
            log.trace("Manifest does not contain {} attribute, skipping.", NAME);
            return Optional.empty();
        }
        LoadModule module = new LoadModule();
        module.setName(name);
        module.setVersion(attrs.getValue(VERSION));
        module.setAuthor(attrs.getValue(AUTHOR));
        module.setDescription(attrs.getValue(DESCRIPTION));

        module.setGroupId(attrs.getValue(GROUP_ID));
        module.setArtifactId(attrs.getValue(ARTIFACT_ID));
        module.setUrl(attrs.getValue(NETGRIF_URL));
        module.setScmConnection(attrs.getValue(SCM_CONNECTION));
        module.setScmUrl(attrs.getValue(SCM_URL));
        module.setBuildTime(attrs.getValue(BUILD_TIME));

        module.setLicense(attrs.getValue(LICENSE));
        module.setOrganization(attrs.getValue(ORGANIZATION));
        module.setOrganizationUrl(attrs.getValue(ORGANIZATION_URL));
        module.setIssueSystem(attrs.getValue(ISSUE_SYSTEM));
        module.setIssueUrl(attrs.getValue(ISSUE_URL));
        module.setBuildJdk(attrs.getValue(BUILD_JDK));

        log.trace("module name: {}, version: {}, author: {}, description: {}, groupId: {}, artifactId: {}, url: {}, scmConnection: {}, scmUrl: {}, buildTime: {}",
                name, module.getVersion(), module.getAuthor(), module.getDescription(),
                module.getGroupId(), module.getArtifactId(),
                module.getUrl(), module.getScmConnection(), module.getScmUrl(), module.getBuildTime());

        return Optional.of(module);
    }

}
