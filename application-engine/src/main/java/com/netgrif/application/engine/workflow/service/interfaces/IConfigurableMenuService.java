package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.MultichoiceMapField;

import java.util.Locale;
import java.util.Map;

public interface IConfigurableMenuService {

    Map<String, I18nString> getNetsByAuthorAsMapOptions(AbstractUser author, Locale locale);

    Map<String, I18nString> getAvailableRolesFromNet(EnumerationMapField processField, MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles);

    Map<String, I18nString> addSelectedRoles(MultichoiceMapField selected, EnumerationMapField netField, MultichoiceMapField roles);

    Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles);

}
