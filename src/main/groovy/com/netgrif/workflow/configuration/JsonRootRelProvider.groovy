package com.netgrif.workflow.configuration

import com.fasterxml.jackson.annotation.JsonRootName
import org.atteo.evo.inflector.English
import org.springframework.hateoas.RelProvider
import org.springframework.hateoas.core.DefaultRelProvider
import org.springframework.util.StringUtils

class JsonRootRelProvider implements RelProvider {

    DefaultRelProvider defaultProvider = new DefaultRelProvider()

    @Override
    String getItemResourceRelFor(Class<?> aClass) {
        return defaultProvider.getItemResourceRelFor(aClass)
    }

    @Override
    String getCollectionResourceRelFor(Class<?> type) {
        JsonRootName rootName = type.getAnnotationsByType(JsonRootName)?.find{true}
        return rootName ? English.plural(rootName.value()) : English.plural(StringUtils.uncapitalize(type.getSimpleName()))
    }

    @Override
    boolean supports(Class<?> aClass) {
        return defaultProvider.supports(aClass)
    }
}