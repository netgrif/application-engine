package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Transition;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AllDataConfiguration {

    private Transition allData;

}
