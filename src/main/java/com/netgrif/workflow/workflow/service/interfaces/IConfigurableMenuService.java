package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;

import java.util.List;
import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> getNetsByAuthor(Long authorId);
    Map<String, I18nString> getNetRoles (EnumerationMapField field, String value);
    Map<String, String> addSelectedRoles(String netId, String netVersion, String roleId);
    Map<String, String> addSelectedRoles(EnumerationMapField netField, MultichoiceMapField roles);

}
