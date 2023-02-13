package com.netgrif.application.engine.elastic.service.interfaces;

import java.util.Map;

public interface IElasticCasePrioritySearch {

    Map<String, Float> fullTextFields();
}
