package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.workflow.domain.dataset.MultichoiceMapField;

import java.util.Locale;
import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> getNetsByAuthorAsMapOptions(IUser author, Locale locale);

    Map<String, I18nString> getAvailableRolesFromNet(EnumerationMapField processField, MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles);

    Map<String, I18nString> addSelectedRoles(MultichoiceMapField selected, EnumerationMapField netField, MultichoiceMapField roles);

    Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles);

}
