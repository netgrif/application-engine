package com.netgrif.application.engine

import com.netgrif.application.engine.workflow.domain.DataField
import groovy.json.JsonSlurper
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToDataSetConverter implements Converter<String, LinkedHashMap<String, DataField>> {

    private JsonSlurper slurper = new JsonSlurper()

    @Override
    LinkedHashMap<String, DataField> convert(String s) {
        return (slurper.parseText(s) as Map).collectEntries {
            return [(it.key): new DataField(it.value)]
        } as LinkedHashMap<String, DataField>
    }
}