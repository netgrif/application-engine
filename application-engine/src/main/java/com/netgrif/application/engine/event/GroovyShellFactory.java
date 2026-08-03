package com.netgrif.application.engine.event;

import com.netgrif.application.engine.configuration.properties.ActionsProperties;
import groovy.lang.GroovyShell;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroovyShellFactory implements IGroovyShellFactory {

    private static final List<String> ACTION_IMPORT_PACKAGES = List.of(
            "com.netgrif.application.engine.objects.*",
            "com.netgrif.application.engine.workflow.domain.*",
            "com.netgrif.application.engine.adapter.spring.*"
    );

    @Autowired
    private CompilerConfiguration configuration;

    @Autowired
    private ActionsProperties actionsProperties;

    private volatile GroovyShell shell;

    @Override
    public GroovyShell getGroovyShell() {
        GroovyShell local = shell;
        if (local == null) {
            synchronized (this) {
                local = shell;
                if (local == null) {
                    ImportCustomizer importCustomizer = new ImportCustomizer();

                    Set<String> classNames = findAllActionImportClasses();
                    importCustomizer.addImports(classNames.toArray(new String[0]));

                    configuration.addCompilationCustomizers(importCustomizer);

                    local = new GroovyShell(this.getClass().getClassLoader(), new groovy.lang.Binding(), this.configuration);
                    shell = local;
                }
            }
        }
        return local;
    }

    private Set<String> findAllActionImportClasses() {
        Set<String> configuredImportNames = actionsProperties.getImports().stream()
                .map(this::simpleName)
                .collect(Collectors.toSet());

        Set<Class<?>> classes = ACTION_IMPORT_PACKAGES.stream()
                .flatMap(packageName -> findAllClassesUsingClassLoader(packageName).stream())
                .map(this::loadClass)
                .collect(Collectors.toCollection(HashSet::new));

        return classes.stream()
                .collect(Collectors.groupingBy(Class::getSimpleName))
                .entrySet().stream()
                .filter(entry -> !configuredImportNames.contains(entry.getKey()))
                .map(entry -> selectActionImport(entry.getValue()))
                .flatMap(Optional::stream)
                .map(Class::getName)
                .collect(Collectors.toSet());
    }

    private String simpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }

    private Optional<Class<?>> selectActionImport(List<Class<?>> candidates) {
        int highestSpecificity = candidates.stream()
                .mapToInt(candidate -> importSpecificity(candidate, candidates))
                .max()
                .orElseThrow();

        List<Class<?>> mostSpecificCandidates = candidates.stream()
                .filter(candidate -> importSpecificity(candidate, candidates) == highestSpecificity)
                .toList();

        if (mostSpecificCandidates.size() != 1) {
            List<String> collidingClassNames = candidates.stream()
                    .map(Class::getName)
                    .sorted()
                    .toList();
            log.warn("Skipping automatic action import for ambiguous class name [{}]. " +
                            "Conflicting candidates: {}. Configure an explicit import to resolve the conflict.",
                    candidates.getFirst().getSimpleName(), collidingClassNames);
            return Optional.empty();
        }

        return Optional.of(mostSpecificCandidates.getFirst());
    }

    private int importSpecificity(Class<?> candidate, List<Class<?>> candidates) {
        return (int) candidates.stream()
                .filter(other -> other != candidate && other.isAssignableFrom(candidate))
                .count();
    }

    private Class<?> loadClass(String className) {
        try {
            return Class.forName(className, false, getClass().getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load discovered action import class " + className, e);
        }
    }

    private Set<String> findAllClassesUsingClassLoader(String packagePattern) {
        boolean recursive = packagePattern.endsWith(".*");
        String packageName = recursive
                ? packagePattern.substring(0, packagePattern.length() - 2)
                : packagePattern;
        return findAllClassesUsingClassLoader(packageName, recursive);
    }

    private Set<String> findAllClassesUsingClassLoader(String packageName, boolean recursive) {
        String path = packageName.replace(".", "/");
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(getClass().getClassLoader());
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);
        String classPattern = recursive ? "/**/*.class" : "/*.class";

        try {
            Resource[] resources = resolver.getResources(
                    ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + path + classPattern
            );
            Set<String> classNames = new HashSet<>();
            for (Resource resource : resources) {
                if (resource.getDescription().contains("test-classes")) {
                    continue;
                }
                String className = metadataReaderFactory.getMetadataReader(resource)
                        .getClassMetadata()
                        .getClassName();
                if (!className.contains("$") && !className.endsWith("package-info") && !className.endsWith("module-info")) {
                    classNames.add(className);
                }
            }
            return classNames;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to discover classes in package " + packageName, e);
        }
    }
}
