package com.netgrif.application.engine.workflow.service.interfaces;


import com.netgrif.application.engine.petrinet.domain.dataset.Field;

public interface IFieldSanitizationService {

    String sanitize(String value, Field<?> field);

}