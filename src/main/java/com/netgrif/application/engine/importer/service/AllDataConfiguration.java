package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.importer.model.Transition;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data
public class AllDataConfiguration {

    private Transition allData;
// TODO: release/8.0.0 implement logic of creating all data transition here, move from importer

}