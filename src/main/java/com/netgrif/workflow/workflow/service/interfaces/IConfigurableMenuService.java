package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;

import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> getNetsByAuthor(Long authorId);
    Map<String, I18nString> getAvailableRolesFromNet (EnumerationMapField processField,MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles);
    Map<String, I18nString> addSelectedRoles(MultichoiceMapField selected, EnumerationMapField netField, MultichoiceMapField roles);
    Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles);

}
