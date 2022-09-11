package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import com.netgrif.application.engine.utils.DateUtils;
import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.startup.ImpersonationRunner.IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER;

@Service
public class ImpersonationAuthorizationService implements IImpersonationAuthorizationService {

    public static final int MAX_CONFIGS_PER_USER = 1000;

    @Autowired
    private IUserService userService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IAuthorityService authorityService;

    @Autowired
    private IProcessRoleService processRoleService;

    @Override
    public Page<IUser> getConfiguredImpersonationUsers(String query, LoggedUser impersonator, Pageable pageable) {
        if (impersonator.isAdmin()) {
            return userService.searchAllCoMembers(query, null, null, impersonator, true, pageable);

        } else {
            Page<Case> cases = searchConfigs(impersonator.getId(), pageable);
            List<IUser> users = cases.getContent().stream()
                    .map(c -> ((UserFieldValue) c.getDataSet().get("impersonated").getValue()).getId())
                    .distinct()
                    .map(id -> userService.findById(id, true))
                    .collect(Collectors.toList());
            return new PageImpl<>(users, pageable, cases.getTotalElements());
        }
    }

    @Override
    public boolean canImpersonate(LoggedUser impersonator, String userId) {
        IUser impersonated = userService.findById(userId, true);
        List<Case> impersonationConfig = searchConfigs(impersonator.getId(), impersonated.getStringId());
        if (impersonationConfig.isEmpty()) {
            return false;
        }
        return impersonationConfig.stream().anyMatch(it -> ((Collection) it.getDataSet().get("impersonators").getValue()).contains(impersonator.getId()));
    }

    @Override
    public Page<Case> searchConfigs(String impersonatorId, Pageable pageable) {
        return findCases(userService.getSystem().transformToLoggedUser(), request(impersonatorId, null), pageable);
    }

    @Override
    public List<Case> searchConfigs(String impersonatorId, String impersonatedId) {
        Page<Case> cases = findCases(userService.getSystem().transformToLoggedUser(), request(impersonatorId, impersonatedId), PageRequest.of(0, MAX_CONFIGS_PER_USER));
        return cases.getContent();
    }

    @Override
    public List<Authority> getAuthorities(List<Case> configs) {
        if (configs.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> authIds = (List) configs.get(0).getDataSet().get("impersonated_authorities").getValue();
        authIds = authIds != null ? authIds : new ArrayList<>();
        return authorityService.findAllByIds(authIds);
    }

    @Override
    public List<ProcessRole> getRoles(List<Case> configs) {
        if (configs.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> roleIds = (List) configs.get(0).getDataSet().get("impersonated_roles").getValue();
        roleIds = roleIds != null ? roleIds : new ArrayList<>();
        return new ArrayList<>(processRoleService.findByIds(new HashSet<>(roleIds)));
    }

    protected CaseSearchRequest request(String impersonatorId, String impersonatedId) {
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

    protected Page<Case> findCases(LoggedUser impersonator, CaseSearchRequest request, Pageable pageable) {
        return elasticCaseService.search(Collections.singletonList(request), impersonator, pageable, Locale.getDefault(), false);
    }
}
