package com.netgrif.application.engine

import groovy.json.JsonSlurper
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToMapConverter implements Converter<String, Map> {

    @Override
    Map convert(String s) {
        return new JsonSlurper().parseText(s) as Map
    }
}