package com.netgrif.workflow.orgstructure.groups;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QCase;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class NextGroupService implements INextGroupService {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IUserService userService;

    private final static String GROUP_CASE_IDENTIFIER = "org_group";
    private final static String GROUP_MEMBERS_FIELD = "members";

    @Override
    public Case findGroup(String groupID){
        QCase qCase = new QCase("case");
        return workflowService.searchOne(qCase.processIdentifier.eq(GROUP_CASE_IDENTIFIER).and(qCase.stringId.eq(groupID)));
    }

    @Override
    public List<Case> findByPredicate(Predicate predicate){
        return workflowService.searchAll(predicate).getContent();
    }

    @Override
    public List<Case> findAllGroups(){
        QCase qCase = new QCase("case");
        return workflowService.searchAll(qCase.processIdentifier.eq(GROUP_CASE_IDENTIFIER)).getContent();
    }

    @Override
    public Map<String, I18nString> addUser(User user, Map<String, I18nString> existingUsers){
        existingUsers.put(user.getId().toString(), new I18nString(user.getFullName()));
        return existingUsers;
    }

    @Override
    public Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers){
        usersToRemove.forEach(existingUsers::remove);
        return existingUsers;
    }

    @Override
    public List<User> getMembers(Case groupCase){
        Set<String> userIds = groupCase.getDataSet().get(GROUP_MEMBERS_FIELD).getOptions().keySet();
        List<User> resultList = new ArrayList<>();
        userIds.forEach(id -> resultList.add(userService.findById(Long.parseLong(id), true)));
        return resultList;
    }

    @Override
    public Map<String, I18nString> listToMap(List<Case> cases){
        Map<String, I18nString> options = new HashMap<>();
        cases.forEach(aCase -> options.put(aCase.getStringId(), new I18nString(aCase.getTitle())));
        return  options;
    }

}
