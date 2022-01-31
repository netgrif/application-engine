package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.workflow.domain.Case;

import java.util.List;

public interface IUserFilterSearchService {
    List<Case> autocompleteFindFilters(String userInput);
}
