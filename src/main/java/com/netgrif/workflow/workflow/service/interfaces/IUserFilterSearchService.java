package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.workflow.domain.Case;

import java.util.List;

public interface IUserFilterSearchService {
    List<Case> autocompleteFindFilters(String userInput);
}
