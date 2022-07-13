package com.netgrif.application.engine.orgstructure.groups;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.RegisteredUser;
import com.netgrif.application.engine.auth.service.interfaces.IRegistrationService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.mail.interfaces.IMailAttemptService;
import com.netgrif.application.engine.mail.interfaces.IMailService;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import com.netgrif.application.engine.petrinet.domain.PetriNet;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class NextGroupService implements INextGroupService {

    @Autowired
    protected IWorkflowService workflowService;

    @Autowired
    protected IMailService mailService;

    @Autowired
    protected IMailAttemptService mailAttemptService;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected IDataService dataService;

    @Autowired
    protected IRegistrationService registrationService;

    @Autowired
    protected IPetriNetService petriNetService;

    @Autowired
    protected ITaskService taskService;

    @Autowired
    protected IElasticCaseService elasticCaseService;

    @Autowired
    protected ISecurityContextService securityContextService;


    protected final static String GROUP_NET_IDENTIFIER = "org_group";
    protected final static String GROUP_INIT_TASK_ID = "2";

    protected final static String GROUP_CASE_IDENTIFIER = "org_group";
    protected final static String GROUP_MEMBERS_FIELD = "members";
    protected final static String GROUP_AUTHOR_FIELD = "author";
    protected final static String GROUP_TITLE_FIELD = "group_name";

    @Override
    public CreateCaseEventOutcome createDefaultSystemGroup(IUser author){
        if(findDefaultGroup() != null) {
            log.info("Default system group has already been created.");
            return null;
        }
        return createGroup("Default system group", author);
    }

    @Override
    public CreateCaseEventOutcome createGroup(IUser author){
        return createGroup(author.getFullName(), author);
    }

    @Override
    public CreateCaseEventOutcome createGroup(String title, IUser author){
        Case userDefaultGroup = findUserDefaultGroup(author);
        if (userDefaultGroup != null && userDefaultGroup.getTitle().equals(title)) {
            return null;
        }
        PetriNet orgGroupNet = petriNetService.getNewestVersionByIdentifier(GROUP_NET_IDENTIFIER);
        CreateCaseEventOutcome outcome = workflowService.createCase(orgGroupNet.getStringId(), title, "", author.transformToLoggedUser());

        Map<String, Map<String,String>> taskData = getInitialGroupData(author, title, outcome.getCase());
        Task initTask = getGroupInitTask(outcome.getCase());
        dataService.setData(initTask.getStringId(), ImportHelper.populateDataset(taskData));

        try {
            taskService.assignTask(author.transformToLoggedUser(), initTask.getStringId());
            taskService.finishTask(author.transformToLoggedUser(), initTask.getStringId());
        } catch (TransitionNotExecutableException e) {
            log.error(e.getMessage());
        }
        author.addGroup(outcome.getCase().getStringId());
        userService.save(author);
        return outcome;
    }

    @Override
    public Case findGroup(String groupID) {
        Case result = workflowService.searchOne(groupCase().and(QCase.case$._id.eq(new ObjectId(groupID))));
        if (!isGroupCase(result)) {
            return null;
        }
        return result;
    }

    @Override
    public Case findDefaultGroup() {
        return findByName("Default system group");
    }

    @Override
    public Case findByName(String name) {
        CaseSearchRequest request = new CaseSearchRequest();
        request.query = "title.keyword:\"" + name + "\"";
        List<Case> result = elasticCaseService.search(Collections.singletonList(request), userService.getSystem().transformToLoggedUser(), PageRequest.of(0, 1), LocaleContextHolder.getLocale(), false).getContent();
        return !result.isEmpty() ? result.get(0) : null;
    }

    @Override
    public List<Case> findByPredicate(Predicate predicate) {
        return workflowService.searchAll(predicate).getContent();
    }

    @Override
    public List<Case> findByIds(Collection<String> groupIds) {
        List<BooleanExpression> groupQueries = groupIds.stream().map(ObjectId::new).map(QCase.case$._id::eq).collect(Collectors.toList());
        BooleanBuilder builder = new BooleanBuilder();
        groupQueries.forEach(builder::or);
        return this.workflowService.searchAll(groupCase().and(builder)).getContent();
    }

    @Override
    public List<Case> findAllGroups() {
        return workflowService.searchAll(groupCase()).getContent();
    }

    @Override
    public Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase) {
        if (!isGroupCase(groupCase)) {
            return null;
        }
        IUser user = userService.findByEmail(email, true);
        if (user != null && user.isActive()) {
            log.info("User [" + user.getFullName() + "] has already been registered.");
            user.addGroup(groupCase.getStringId());
            userService.save(user);
            return addUser(user, existingUsers);
        } else {
            log.info("Inviting new user to group.");
            NewUserRequest newUserRequest = new NewUserRequest();
            newUserRequest.email = email;
            RegisteredUser regUser = registrationService.createNewUser(newUserRequest);
            regUser.addGroup(groupCase.getStringId());
            userService.save(regUser);

            try {
                mailService.sendRegistrationEmail(regUser);
                mailAttemptService.mailAttempt(newUserRequest.email);
            } catch (MessagingException | IOException | TemplateException e) {
                log.error(e.getMessage());
            }
            return addUser(regUser, existingUsers);
        }
    }

    @Override
    public void addUserToDefaultGroup(IUser user) {
        addUser(user, findDefaultGroup());
    }

    @Override
    public void addUser(IUser user, String groupId){
        Case groupCase = this.findGroup(groupId);
        if(groupCase != null){
            this.addUser(user, groupCase);
        }
    }

    @Override
    public void addUser(IUser user, Case groupCase) {
        Map<String, I18nString> existingUsers = groupCase.getDataField(GROUP_MEMBERS_FIELD).getOptions();
        if (existingUsers == null) {
            existingUsers = new HashMap<>();
        }
        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(addUser(user, existingUsers));
        workflowService.save(groupCase);
        user.addGroup(groupCase.getStringId());
        userService.save(user);
        securityContextService.saveToken(user.getStringId());
    }

    @Override
    public Map<String, I18nString> addUser(IUser user, Map<String, I18nString> existingUsers) {
        existingUsers.put(user.getStringId(), new I18nString(user.getEmail()));
        return existingUsers;
    }

    @Override
    public void removeUser(IUser user, Case groupCase){
        HashSet<String> userIds = new HashSet<>();
        Map<String, I18nString> existingUsers = groupCase.getDataField(GROUP_MEMBERS_FIELD).getOptions();

        userIds.add(user.getStringId());
        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(removeUser(userIds, existingUsers, groupCase));
        workflowService.save(groupCase);
    }

    @Override
    public Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers, Case groupCase) {
        String authorId = this.getGroupOwnerId(groupCase);
        usersToRemove.forEach(user -> {
            if (user.equals(authorId)) {
                log.error("Author with id [" + authorId + "] cannot be removed from group with ID [" + groupCase.get_id().toString() + "]");
            } else {
                existingUsers.remove(user);
                securityContextService.saveToken(user);
            }
        });
        userService.findAllByIds(usersToRemove, false).forEach(user -> {
            if (!user.getStringId().equals(authorId)) {
                user.getNextGroups().remove(groupCase.getStringId());
                userService.save(user);
            }
        });
        return existingUsers;
    }

    @Override
    public Set<String> getAllCoMembers(IUser user) {
        Set<String> users = workflowService.searchAll(
                groupCase().and(QCase.case$.dataSet.get(GROUP_MEMBERS_FIELD).options.containsKey(user.getStringId())))
                .map(it -> it.getDataSet().get(GROUP_MEMBERS_FIELD).getOptions().keySet()).stream()
                .collect(HashSet::new, Set::addAll, Set::addAll);
        users.remove(user.getStringId());
        users.remove(userService.getSystem().getStringId());
        return users;
    }


    @Override
    public List<IUser> getMembers(Case groupCase) {
        if (!isGroupCase(groupCase)) {
            return null;
        }
        Set<String> userIds = groupCase.getDataSet().get(GROUP_MEMBERS_FIELD).getOptions().keySet();
        List<IUser> resultList = new ArrayList<>();
        userIds.forEach(id -> resultList.add(userService.resolveById(id, true)));
        return resultList;
    }

    @Override
    public Set<String> getAllGroupsOfUser(IUser groupUser) {
        List<String> groupList = workflowService.searchAll(groupCase().and(QCase.case$.dataSet.get(GROUP_MEMBERS_FIELD).options.containsKey(groupUser.getStringId())))
                .map(aCase -> aCase.get_id().toString()).getContent();
        return new HashSet<>(groupList);
    }

    @Override
    public String getGroupOwnerId(String groupId) {
        return this.getGroupOwnerId(this.findGroup(groupId));
    }

    @Override
    public Collection<String> getGroupsOwnerIds(Collection<String> groupIds) {
        return this.findByIds(groupIds).stream().map(this::getGroupOwnerId).collect(Collectors.toList());
    }

    @Override
    public String getGroupOwnerEmail(String groupId) {
        return this.getGroupOwnerEmail(this.findGroup(groupId));
    }

    @Override
    public Collection<String> getGroupsOwnerEmails(Collection<String> groupIds) {
        return this.findByIds(groupIds).stream().map(this::getGroupOwnerEmail).collect(Collectors.toList());
    }

    protected static BooleanExpression groupCase() {
        return QCase.case$.processIdentifier.eq(GROUP_CASE_IDENTIFIER);
    }

    protected boolean isGroupCase(Case aCase) {
        if (aCase == null) {
            log.error("The input case is a null object.");
            return false;
        } else if (!aCase.getProcessIdentifier().equals(GROUP_CASE_IDENTIFIER)) {
            log.error("Case [" + aCase.getTitle() + "] is not an organization group case.");
            return false;
        }
        return true;
    }

    protected boolean authorHasDefaultGroup(IUser author) {
        List<Case> allGroups = findAllGroups();
        for (Case group : allGroups){
            if(group.getAuthor().getId().equals(author.getStringId())) {
                return true;
            }
        }
        return false;
    }

    protected String getGroupOwnerId(Case groupCase) {
        return groupCase.getAuthor().getId();
    }

    protected Case findUserDefaultGroup(IUser author) {
        return workflowService.searchOne(QCase.case$.author.id.eq(author.getStringId()).and(QCase.case$.title.eq(author.getFullName())));
    }

    protected Task getGroupInitTask(Case groupCase) {
        List<TaskReference> taskList = taskService.findAllByCase(groupCase.getStringId(), LocaleContextHolder.getLocale());
        Optional<TaskReference> initTaskReference = taskList.stream().filter(taskReference ->
                taskReference.getTransitionId().equals(GROUP_INIT_TASK_ID))
                .findFirst();

        if (initTaskReference.isEmpty()) {
            log.error("Initial task of group case is not present!");
            return null;
        }

        String initTaskId = initTaskReference.get().getStringId();
        return taskService.findById(initTaskId);
    }

    protected Map<String, Map<String, String>> getInitialGroupData(IUser author, String title, Case groupCase) {
        Map<String, Map<String, String>> taskData = new HashMap<>();

        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(addUser(author, new HashMap<>()));
        workflowService.save(groupCase);

        Map<String, String> authorData = new HashMap<>();
        authorData.put("type", "user");
        authorData.put("value", author.getStringId());

        Map<String, String> titleData = new HashMap<>();
        titleData.put("type", "text");
        titleData.put("value", title);

        taskData.put(GROUP_TITLE_FIELD, titleData);
        taskData.put(GROUP_AUTHOR_FIELD, authorData);
        return taskData;
    }

    protected String getGroupOwnerEmail(Case groupCase) {
        return groupCase.getAuthor().getEmail();
    }
}
