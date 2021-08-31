package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;

import java.util.Locale;
import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> getNetsByAuthor(User author, Locale locale);
    Map<String, I18nString> getAvailableRolesFromNet (EnumerationMapField processField,MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles);
    Map<String, I18nString> addSelectedRoles(MultichoiceMapField selected, EnumerationMapField netField, MultichoiceMapField roles);
    Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles);

}
