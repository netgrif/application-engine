package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> getNetsByAuthor(Long authorId);
    Map<String, I18nString> getNetRoles (EnumerationMapField field, String value, HashSet<String> addedRoleIds);
    Map<String, I18nString> addSelectedRoles(MultichoiceMapField selected, EnumerationMapField netField, MultichoiceMapField roles);
    void removeSelectedRoles(MultichoiceMapField addedRoles);

}
