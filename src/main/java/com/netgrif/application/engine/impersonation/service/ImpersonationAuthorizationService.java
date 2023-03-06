package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.properties.ImpersonationProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.petrinet.domain.dataset.BooleanField;
import com.netgrif.application.engine.petrinet.domain.dataset.DateTimeField;
import com.netgrif.application.engine.petrinet.domain.dataset.MultichoiceMapField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.startup.ImpersonationRunner.IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER;

@Service
public class ImpersonationAuthorizationService implements IImpersonationAuthorizationService {

    @Autowired
    protected ImpersonationProperties properties;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected IElasticCaseService elasticCaseService;

    @Autowired
    protected IAuthorityService authorityService;

    @Autowired
    protected IWorkflowService workflowService;

    @Autowired
    protected IProcessRoleService processRoleService;

    @Override
    public Page<IUser> getConfiguredImpersonationUsers(String query, LoggedUser impersonator, Pageable pageable) {
        if (impersonator.isAdmin()) {
            return userService.searchAllCoMembers(query, null, null, impersonator, true, pageable);

        } else {
            Page<Case> cases = searchConfigs(impersonator.getId(), pageable);
            List<IUser> users = cases.getContent().stream()
                    .map(c -> ((UserFieldValue) c.getDataSet().get("impersonated").getRawValue()).getId())
                    .distinct()
                    .map(id -> userService.findById(id, true))
                    .collect(Collectors.toList());
            return new PageImpl<>(users, pageable, cases.getTotalElements());
        }
    }

    @Override
    public boolean canImpersonate(LoggedUser impersonator, String configId) {
        Case config = getConfig(configId);
        return isValidAndContainsUser(config, impersonator.getId());
    }

    @Override
    public boolean canImpersonateUser(LoggedUser impersonator, String userId) {
        IUser impersonated = userService.findById(userId, true);
        return impersonator.isAdmin() || !searchConfigs(impersonator.getId(), impersonated.getStringId()).isEmpty();
    }

    @Override
    public Page<Case> searchConfigs(String impersonatorId, Pageable pageable) {
        return findCases(makeRequest(impersonatorId, null), pageable);
    }

    @Override
    public List<Case> searchConfigs(String impersonatorId, String impersonatedId) {
        Page<Case> cases = findCases(makeRequest(impersonatorId, impersonatedId), PageRequest.of(0, properties.getConfigsPerUser()));
        return cases.getContent();
    }

    @Override
    public List<Authority> getAuthorities(List<Case> configs, IUser impersonated) {
        if (configs.isEmpty()) {
            return new ArrayList<>();
        }
        Set<String> authIds = extractSetFromField(configs, "impersonated_authorities");
        return authorityService.findAllByIds(new ArrayList<>(authIds)).stream()
                .filter(configAuth -> impersonated.getAuthorities().stream().anyMatch(userAuth -> userAuth.getStringId().equals(configAuth.getStringId())))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProcessRole> getRoles(List<Case> configs, IUser impersonated) {
        List<ProcessRole> impersonatedRoles = new ArrayList<>();
        impersonatedRoles.add(processRoleService.defaultRole());
        if (configs.isEmpty()) {
            return impersonatedRoles;
        }
        Set<String> roleIds = extractSetFromField(configs, "impersonated_roles");
        impersonatedRoles.addAll((processRoleService.findByIds(roleIds)).stream()
                .filter(configRole -> impersonated.getProcessRoles().stream().anyMatch(userRole -> userRole.getStringId().equals(configRole.getStringId())))
                .collect(Collectors.toList()));
        return impersonatedRoles;
    }

    @Override
    public Case getConfig(String configId) {
        return workflowService.findOne(configId);
    }

    @Override
    public String getImpersonatedUserId(Case config) {
        return ((UserFieldValue) config.getDataSet().get("impersonated").getRawValue()).getId();
    }

    @Override
    public LocalDateTime getValidUntil(Case config) {
        return ((DateTimeField) config.getDataSet().get("valid_to")).getRawValue();
    }

    @Override
    public LocalDateTime getValidFrom(Case config) {
        return ((DateTimeField) config.getDataSet().get("valid_from")).getRawValue();
    }

    protected CaseSearchRequest makeRequest(String impersonatorId, String impersonatedId) {
        CaseSearchRequest request = new CaseSearchRequest();
        List<String> queries = new ArrayList<>();
        request.process = Collections.singletonList(new CaseSearchRequest.PetriNet(IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER));
        queries.add("(dataSet.impersonators.keyValue:" + impersonatorId + ")");
        queries.add("(dataSet.is_active.booleanValue:true)");
        queries.addAll(validityQueries());
        if (impersonatedId != null) {
            queries.add("(dataSet.impersonated.userIdValue.keyword:" + impersonatedId + ")");
        }
        request.query = combineQueries(queries);
        return request;
    }

    protected List<String> validityQueries() {
        List<String> queries = new ArrayList<>();
        queries.add("((!(_exists_:dataSet.valid_from.timestampValue)) OR (dataSet.valid_from.timestampValue:<" + DateUtils.localDateTimeToDate(LocalDateTime.now()).getTime() + "))");
        queries.add("((!(_exists_:dataSet.valid_to.timestampValue)) OR (dataSet.valid_to.timestampValue:>" + DateUtils.localDateTimeToDate(LocalDateTime.now()).getTime() + "))");
        return queries;
    }

    protected String combineQueries(List<String> queries) {
        return "(" + String.join(" AND ", queries) + ")";
    }

    protected Page<Case> findCases(CaseSearchRequest request, Pageable pageable) {
        return elasticCaseService.search(Collections.singletonList(request), userService.getSystem().transformToLoggedUser(), pageable, Locale.getDefault(), false);
    }

    protected boolean isValidAndContainsUser(Case config, String id) {
        MultichoiceMapField impersonators = (MultichoiceMapField) config.getDataSet().get("impersonators");
        BooleanField isActive = (BooleanField) config.getDataSet().get("is_active");
        Set<String> value = impersonators.getRawValue();
        if (value == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        return (value).contains(id) &&
                (isActive.getRawValue() != null && isActive.getRawValue()) &&
                validateTime(getValidFrom(config), now) &&
                validateTime(now, getValidUntil(config));

    }

    protected boolean validateTime(LocalDateTime first, LocalDateTime second) {
        if (first == null || second == null) {
            return true;
        }
        return first.isBefore(second) || first.equals(second);
    }

    protected Set<String> extractSetFromField(List<Case> cases, String fieldId) {
        Set<String> allStrings = new HashSet<>();
        for (Case c : cases) {
            Set<String> rawValue = ((MultichoiceMapField) c.getDataSet().get(fieldId)).getRawValue();
            if (rawValue == null) {
                continue;
            }
            allStrings.addAll(rawValue);
        }
        return allStrings;
    }
}
