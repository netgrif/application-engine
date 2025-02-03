package com.netgrif.application.engine.workflow.service;

import com.netgrif.adapter.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.startup.runner.FilterRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IUserFilterSearchService;
import com.netgrif.core.auth.domain.LoggedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserFilterSearchService implements IUserFilterSearchService {

    @Autowired
    private IElasticCaseService caseSearchService;

    @Autowired
    private UserService userService;

    @Override
    public List<Case> autocompleteFindFilters(String userInput) {
        Page<Case> page = this.caseSearchService.search(Collections.singletonList(
                        CaseSearchRequest.builder()
                                .process(Collections.singletonList(new CaseSearchRequest.PetriNet(FilterRunner.FILTER_PETRI_NET_IDENTIFIER)))
                                .query(
                                        String.format("(title:%s*) AND ((dataSet.visibility.keyValue:private AND authorEmail:%s) OR (dataSet.visibility.keyValue:public))",
                                                userInput,
                                                userService.getLoggedUser().getEmail())
                                )
                                .transition(Collections.singletonList("view_filter"))
                                .build()
                ),
                (LoggedUser) userService.transformToLoggedUser(userService.getLoggedOrSystem()),
                PageRequest.of(0, 100),
                LocaleContextHolder.getLocale(),
                true);
        if (page.hasContent()) {
            return page.getContent();
        }
        return Collections.emptyList();
    }
}
