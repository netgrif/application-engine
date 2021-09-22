package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.workflow.petrinet.domain.version.StringToVersionConverter;
import com.netgrif.workflow.petrinet.domain.version.Version;
import com.netgrif.workflow.petrinet.service.PetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.utils.FullPageRequest;
import com.netgrif.workflow.workflow.service.interfaces.IConfigurableMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ConfigurableMenuService implements IConfigurableMenuService {

    @Autowired
    private PetriNetService petriNetService;
    @Autowired
    private StringToVersionConverter converter;

    /**
     * Returns processes whose author is currently logged user.
     *      Format of returned multichoiceMap field keys: NET_IMPORT_ID:VERSION
     *      Mongo doesn't allow dots inside map keys, that's why they are replaced with dashes in version string.
     * @param user currently logged user
     */
    @Override
    public Map<String, I18nString> getNetsByAuthor(User user, Locale locale){
        LoggedUser author = user.transformToLoggedUser();
        Map<String, Object> requestQuery = new HashMap<>();
        requestQuery.put("author.email", user.getEmail());
        List<PetriNetReference> nets = this.petriNetService.search(requestQuery, author, new FullPageRequest(), locale).getContent();

        Map<String, I18nString> options = new HashMap<>();

        for(PetriNetReference net : nets){
            String[] versionSplit = net.getVersion().split("\\.");
            I18nString titleAndVersion = new I18nString(net.getTitle() + " :" + net.getVersion());
            options.put(net.getIdentifier() + ":" + versionSplit[0] + "-" + versionSplit[1] + "-" + versionSplit[2], titleAndVersion);
        }

        return options;
    }

    @Override
    public Map<String, I18nString> getAvailableRolesFromNet (EnumerationMapField processField, MultichoiceMapField permittedRoles, MultichoiceMapField bannedRoles) {

        String netImportId = processField.getValue().split(":")[0];
        String versionString = processField.getOptions().get(processField.getValue()).toString().split(":")[1].replace("-", ".");
        Version version = converter.convert(versionString);
        PetriNet net = petriNetService.getPetriNet(netImportId, version);

        return net.getRoles().values().stream()
                .filter(role -> (!permittedRoles.getOptions().containsKey(role.getImportId() + ":" + netImportId)
                && !bannedRoles.getOptions().containsKey(role.getImportId() + ":" + netImportId)))
                .map(role -> new AbstractMap.SimpleEntry<>(role.getImportId() + ":" + netImportId, new I18nString(role.getName())))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
  }

    @Override
    public Map<String, I18nString> removeSelectedRoles(MultichoiceMapField addedRoles) {

        Map<String, I18nString> updatedRoles = new LinkedHashMap<>(addedRoles.getOptions());
        updatedRoles.keySet().removeAll(addedRoles.getValue());
        return updatedRoles;
    }

    @Override
    public Map<String, I18nString> addSelectedRoles(MultichoiceMapField addedRoles, EnumerationMapField processField, MultichoiceMapField rolesAvailable) {

        String netName = " (" + processField.getOptions().get(processField.getValue()).toString().split(":")[0] + ")";
        Map<String, I18nString> updatedRoles = new LinkedHashMap<>(addedRoles.getOptions());

        updatedRoles.putAll(rolesAvailable.getValue().stream()
                .collect(Collectors.toMap(x -> x, v -> new I18nString(rolesAvailable.getOptions().get(v).toString() + netName))));

        return updatedRoles;
    }
}
