package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;

import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> addSelectedRoles(MultichoiceMapField selected, EnumerationMapField netField, MultichoiceMapField roles);

    Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles);

}
