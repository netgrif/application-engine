package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
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

    @Autowired
    private IUserService userService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Override
    public Page<IUser> getConfiguredImpersonationUsers(String query, LoggedUser impersonator, Pageable pageable) {
        if (impersonator.isAdmin()) {
            return userService.searchAllCoMembers(query, null, null, impersonator, true, pageable);
        } else {
            CaseSearchRequest request = new CaseSearchRequest();
            List<String> queries = new ArrayList<>();
            request.process = Collections.singletonList(new CaseSearchRequest.PetriNet(IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER));
            queries.add("(dataSet.impersonators.keyValue:" + impersonator.getId() + ")");
            queries.add("(dataSet.valid_from.timestampValue:>" + DateUtils.localDateTimeToDate(LocalDateTime.now()) + ")");
            queries.add("(dataSet.valid_to.timestampValue:<" + DateUtils.localDateTimeToDate(LocalDateTime.now()) + ")");
            request.query = "(" + String.join(" AND ", queries) + ")";

            Page<Case> cases = elasticCaseService.search(Collections.singletonList(request), impersonator, pageable, Locale.getDefault(), false);
            List<IUser> users = cases.getContent().stream().map(c -> {
                String impersonatedEmail = (String) c.getDataSet().get("impersonated_email").getValue();
                return userService.findByEmail(impersonatedEmail, true);
            }).collect(Collectors.toList());
            return new PageImpl<IUser>(users, pageable, cases.getTotalElements());

        }
    }

    @Override
    public boolean canImpersonate(LoggedUser impersonator, String userId) {
        IUser impersonated = userService.findById(userId, true);
        Case impersonationConfig = searchConfig(impersonated.getEmail());
        if (impersonationConfig != null) {
            return ((Collection) impersonationConfig.getDataSet().get("impersonators").getValue()).contains(impersonator.getId());
        } else {
            return false;
        }
    }

    private Case searchConfig(String userEmail) {
        CaseSearchRequest request = new CaseSearchRequest();
        request.process = Collections.singletonList(new CaseSearchRequest.PetriNet(IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER));
        request.query = "(dataSet.impersonated_email.fulltextValue.keyword:*" + userEmail + "*)";

        Page<Case> cases = elasticCaseService.search(Collections.singletonList(request), userService.getLoggedOrSystem().transformToLoggedUser(), PageRequest.of(0, 1), Locale.getDefault(), false);
        return !cases.isEmpty() ? cases.toList().get(0) : null;
    }
}
