package com.netgrif.application.engine.configuration

import com.fasterxml.jackson.annotation.JsonRootName
import org.atteo.evo.inflector.English
import org.springframework.hateoas.LinkRelation
import org.springframework.hateoas.server.LinkRelationProvider
import org.springframework.util.StringUtils

class JsonRootRelProvider implements LinkRelationProvider {


    @Override
    LinkRelation getItemResourceRelFor(Class<?> aClass) {
        return LinkRelationProvider.getItemResourceRelFor(aClass)
    }

    @Override
    LinkRelation getCollectionResourceRelFor(Class<?> type) {
        JsonRootName rootName = type.getAnnotationsByType(JsonRootName)?.find { true }
        return rootName ? English.plural(rootName.value()) : English.plural(StringUtils.uncapitalize(type.getSimpleName()))
    }

    @Override
    boolean supports(LookupContext delimiter) {
        return LinkRelationProvider.supports(delimiter)
    }

}