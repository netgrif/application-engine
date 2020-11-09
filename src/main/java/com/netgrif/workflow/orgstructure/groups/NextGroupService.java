package com.netgrif.workflow.orgstructure.groups;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.interfaces.IRegistrationService;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.mail.interfaces.IMailAttemptService;
import com.netgrif.workflow.mail.interfaces.IMailService;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.startup.ImportHelper;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QCase;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.BooleanBuilder;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@ConditionalOnProperty(value = "nae.group.default.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Service
@Slf4j
public class NextGroupService implements INextGroupService {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IMailService mailService;

    @Autowired
    private IMailAttemptService mailAttemptService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IRegistrationService registrationService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private ITaskService taskService;


    private final static String GROUP_NET_IDENTIFIER = "org_group";
    private final static String GROUP_INIT_TASK_ID = "2";

    private final static String GROUP_CASE_IDENTIFIER = "org_group";
    private final static String GROUP_MEMBERS_FIELD = "members";
    private final static String GROUP_AUTHOR_FIELD = "author";
    private final static String GROUP_TITLE_FIELD = "group_name";

    @Override
    public Case createDefaultSystemGroup(User author){
        if(findDefaultGroup() != null) {
            log.info("Default system group has already been created.");
            return null;
        }
        return createGroup("Default system group", author);
    }

    @Override
    public Case createGroup(User author){
        return createGroup(author.getFullName(), author);
    }

    @Override
    public Case createGroup(String title, User author){
        Case userDefaultGroup = findUserDefaultGroup(author);
        if(userDefaultGroup != null && userDefaultGroup.getTitle().equals(title)){
            return null;
        }
        PetriNet orgGroupNet = petriNetService.getNewestVersionByIdentifier(GROUP_NET_IDENTIFIER);
        Case groupCase = workflowService.createCase(orgGroupNet.getStringId(), title, "", author.transformToLoggedUser());

        Map<String, Map<String,String>> taskData = getInitialGroupData(author, title, groupCase);
        Task initTask = getGroupInitTask(groupCase);
        dataService.setData(initTask.getStringId(), ImportHelper.populateDataset(taskData));

        try {
            taskService.assignTask(initTask.getStringId());
            taskService.finishTask(initTask.getStringId());
        } catch (TransitionNotExecutableException e) {
            log.error(e.getMessage());
        }
        return groupCase;
    }

    @Override
    public Case findGroup(String groupID){
        Case result = workflowService.searchOne(groupCase().and(QCase.case$._id.eq(new ObjectId(groupID))));
        if(!isGroupCase(result)){
            return null;
        }
        return result;
    }

    @Override
    public Case findDefaultGroup(){
        return workflowService.searchOne(groupCase().and(QCase.case$.title.eq("Default system group")));
    }

    @Override
    public List<Case> findByPredicate(Predicate predicate){
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
    public List<Case> findAllGroups(){
        return workflowService.searchAll(groupCase()).getContent();
    }

    @Override
    public Map<String, I18nString> inviteUser(String email, Map<String, I18nString> existingUsers, Case groupCase){
        if(!isGroupCase(groupCase)){
            return null;
        }
        User user = userService.findByEmail(email, true);
        if(user != null && user.isRegistered()){
            log.info("User [" + user.getFullName() + "] has already been registered.");
        }else{
            log.info("Inviting new user to group.");
            NewUserRequest newUserRequest = new NewUserRequest();
            newUserRequest.email = email;
            user = registrationService.createNewUser(newUserRequest);

            try {
                mailService.sendRegistrationEmail(user);
                mailAttemptService.mailAttempt(newUserRequest.email);
            } catch (MessagingException | IOException | TemplateException e) {
                log.error(e.getMessage());
            }
        }
        return addUser(user, existingUsers);
    }

    @Override
    public void addUserToDefaultGroup(User user){
        addUser(user, findDefaultGroup());
    }

    @Override
    public void addUser(User user, Case groupCase){
        Map<String, I18nString> existingUsers = groupCase.getDataField(GROUP_MEMBERS_FIELD).getOptions();
        if(existingUsers == null){
            existingUsers = new HashMap<>();
        }
        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(addUser(user, existingUsers));
        workflowService.save(groupCase);
    }

    @Override
    public Map<String, I18nString> addUser(User user, Map<String, I18nString> existingUsers){
        existingUsers.put(user.getId().toString(), new I18nString(user.getEmail()));
        return existingUsers;
    }

    @Override
    public void removeUser(User user, Case groupCase){
        HashSet<String> userIds = new HashSet<>();
        Map<String, I18nString> existingUsers = groupCase.getDataField(GROUP_MEMBERS_FIELD).getOptions();

        userIds.add(user.getId().toString());
        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(removeUser(userIds, existingUsers, groupCase));
        workflowService.save(groupCase);
    }

    @Override
    public Map<String, I18nString> removeUser(HashSet<String> usersToRemove, Map<String, I18nString> existingUsers, Case groupCase){
        String authorId = this.getGroupOwnerId(groupCase).toString();
        usersToRemove.forEach(user -> {
            if(user.equals(authorId)){
                log.error("Author with id [" + authorId + "] cannot be removed from group with ID [" + groupCase.get_id().toString() + "]");
            }else{
                existingUsers.remove(user);
            }
        });
        return existingUsers;
    }

    @Override
    public List<User> getMembers(Case groupCase){
        if(!isGroupCase(groupCase)){
            return null;
        }
        Set<String> userIds = groupCase.getDataSet().get(GROUP_MEMBERS_FIELD).getOptions().keySet();
        List<User> resultList = new ArrayList<>();
        userIds.forEach(id -> resultList.add(userService.findById(Long.parseLong(id), true)));
        return resultList;
    }

    @Override
    public Set<String> getAllGroupsOfUser(User groupUser) {
        List<String> groupList = workflowService.searchAll(groupCase().and(QCase.case$.dataSet.get(GROUP_MEMBERS_FIELD).options.containsKey(groupUser.getId().toString())))
                .map(aCase -> aCase.get_id().toString()).getContent();
        return new HashSet<>(groupList);
    }

    @Override
    public Long getGroupOwnerId(String groupId) {
        return this.getGroupOwnerId(this.findGroup(groupId));
    }

    @Override
    public Collection<Long> getGroupsOwnerIds(Collection<String> groupIds) {
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

    private static BooleanExpression groupCase() {
        return QCase.case$.processIdentifier.eq(GROUP_CASE_IDENTIFIER);
    }

    private boolean isGroupCase(Case aCase){
        if(aCase == null){
            log.error("The input case is a null object.");
            return false;
        }else if(!aCase.getProcessIdentifier().equals(GROUP_CASE_IDENTIFIER)){
            log.error("Case [" + aCase.getTitle() + "] is not an organization group case.");
            return false;
        }
        return true;
    }

    private boolean authorHasDefaultGroup(User author){
        List<Case> allGroups = findAllGroups();
        for (Case group : allGroups){
            if(group.getAuthor().getId().equals(author.getId())){
                return true;
            }
        }
        return false;
    }

    private Long getGroupOwnerId(Case groupCase) {
        return groupCase.getAuthor().getId();
    }

    private Case findUserDefaultGroup(User author){
        return workflowService.searchOne(QCase.case$.author.id.eq(author.getId()).and(QCase.case$.title.eq(author.getFullName())));
    }

    private Task getGroupInitTask(Case groupCase){
        List<TaskReference> taskList = taskService.findAllByCase(groupCase.getStringId(), LocaleContextHolder.getLocale());
        Optional<TaskReference> initTaskReference = taskList.stream().filter(taskReference ->
                taskReference.getTransitionId().equals(GROUP_INIT_TASK_ID))
                .findFirst();

        if(!initTaskReference.isPresent()){
            log.error("Initial task of group case is not present!");
            return null;
        }

        String initTaskId = initTaskReference.get().getStringId();
        return taskService.findById(initTaskId);
    }

    private Map<String, Map<String, String>> getInitialGroupData(User author, String title, Case groupCase){
        Map<String, Map<String,String>> taskData = new HashMap<>();

        groupCase.getDataField(GROUP_MEMBERS_FIELD).setOptions(addUser(author, new HashMap<>()));
        workflowService.save(groupCase);

        Map<String, String> authorData = new HashMap<>();
        authorData.put("type", "user");
        authorData.put("value", author.getId().toString());

        Map<String, String> titleData = new HashMap<>();
        titleData.put("type", "text");
        titleData.put("value", title);

        taskData.put(GROUP_TITLE_FIELD, titleData);
        taskData.put(GROUP_AUTHOR_FIELD, authorData);
        return taskData;
    }

    private String getGroupOwnerEmail(Case groupCase) {
        return groupCase.getAuthor().getEmail();
    }
}
