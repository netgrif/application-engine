package api.workflow.domain;

import api.auth.AuthorDto;
import api.petrinet.domain.PetriNetDto;
import api.petrinet.domain.dataset.FieldDto;

import java.time.LocalDateTime;
import java.util.*;

public class CaseDto {

    private String id;

    private LocalDateTime lastModified;

    private String visualId;

    private String petriNetId;

    private PetriNetDto petriNet;

    private String processIdentifier;

    private Map<String, Integer> activePlaces;

    private String title;

    private String color;

    private String icon;

    private LocalDateTime creationDate;

    private LinkedHashMap<String, DataFieldDto> dataSet;

    private LinkedHashSet<String> immediateDataFields;

    private List<FieldDto> immediateData;

    private AuthorDto author;

    private Map<String, Integer> consumedTokens;

    private Set<TaskPairDto> tasks;

    private Set<String> enabledRoles;

    private Map<String, Map<String, Boolean>> permissions;

    private Map<String, Map<String, Boolean>> userRefs;

    private Map<String, Map<String, Boolean>> users;

    private List<String> viewRoles;

    private List<String> viewUserRefs;

    private List<String> viewUsers;

    private List<String> negativeViewRoles;

    private List<String> negativeViewUsers;

    public CaseDto() {
    }

    public CaseDto(String id, LocalDateTime lastModified, String visualId, String petriNetId, PetriNetDto petriNet, String processIdentifier, Map<String, Integer> activePlaces, String title, String color, String icon, LocalDateTime creationDate, LinkedHashMap<String, DataFieldDto> dataSet, LinkedHashSet<String> immediateDataFields, List<FieldDto> immediateData, AuthorDto author, Map<String, Integer> consumedTokens, Set<TaskPairDto> tasks, Set<String> enabledRoles, Map<String, Map<String, Boolean>> permissions, Map<String, Map<String, Boolean>> userRefs, Map<String, Map<String, Boolean>> users, List<String> viewRoles, List<String> viewUserRefs, List<String> viewUsers, List<String> negativeViewRoles, List<String> negativeViewUsers) {
        this.id = id;
        this.lastModified = lastModified;
        this.visualId = visualId;
        this.petriNetId = petriNetId;
        this.petriNet = petriNet;
        this.processIdentifier = processIdentifier;
        this.activePlaces = activePlaces;
        this.title = title;
        this.color = color;
        this.icon = icon;
        this.creationDate = creationDate;
        this.dataSet = dataSet;
        this.immediateDataFields = immediateDataFields;
        this.immediateData = immediateData;
        this.author = author;
        this.consumedTokens = consumedTokens;
        this.tasks = tasks;
        this.enabledRoles = enabledRoles;
        this.permissions = permissions;
        this.userRefs = userRefs;
        this.users = users;
        this.viewRoles = viewRoles;
        this.viewUserRefs = viewUserRefs;
        this.viewUsers = viewUsers;
        this.negativeViewRoles = negativeViewRoles;
        this.negativeViewUsers = negativeViewUsers;
    }

    public String getStringId() {
        return id;
    }

    public void setStringId(String id) {
        this.id = id;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public String getVisualId() {
        return visualId;
    }

    public void setVisualId(String visualId) {
        this.visualId = visualId;
    }

    public String getPetriNetId() {
        return petriNetId;
    }

    public void setPetriNetId(String petriNetId) {
        this.petriNetId = petriNetId;
    }

    public PetriNetDto getPetriNet() {
        return petriNet;
    }

    public void setPetriNet(PetriNetDto petriNet) {
        this.petriNet = petriNet;
    }

    public String getProcessIdentifier() {
        return processIdentifier;
    }

    public void setProcessIdentifier(String processIdentifier) {
        this.processIdentifier = processIdentifier;
    }

    public Map<String, Integer> getActivePlaces() {
        return activePlaces;
    }

    public void setActivePlaces(Map<String, Integer> activePlaces) {
        this.activePlaces = activePlaces;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LinkedHashMap<String, DataFieldDto> getDataSet() {
        return dataSet;
    }

    public void setDataSet(LinkedHashMap<String, DataFieldDto> dataSet) {
        this.dataSet = dataSet;
    }

    public LinkedHashSet<String> getImmediateDataFields() {
        return immediateDataFields;
    }

    public void setImmediateDataFields(LinkedHashSet<String> immediateDataFields) {
        this.immediateDataFields = immediateDataFields;
    }

    public List<FieldDto> getImmediateData() {
        return immediateData;
    }

    public void setImmediateData(List<FieldDto> immediateData) {
        this.immediateData = immediateData;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public Map<String, Integer> getConsumedTokens() {
        return consumedTokens;
    }

    public void setConsumedTokens(Map<String, Integer> consumedTokens) {
        this.consumedTokens = consumedTokens;
    }

    public Set<TaskPairDto> getTasks() {
        return tasks;
    }

    public void setTasks(Set<TaskPairDto> tasks) {
        this.tasks = tasks;
    }

    public Set<String> getEnabledRoles() {
        return enabledRoles;
    }

    public void setEnabledRoles(Set<String> enabledRoles) {
        this.enabledRoles = enabledRoles;
    }

    public Map<String, Map<String, Boolean>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Map<String, Boolean>> permissions) {
        this.permissions = permissions;
    }

    public Map<String, Map<String, Boolean>> getUserRefs() {
        return userRefs;
    }

    public void setUserRefs(Map<String, Map<String, Boolean>> userRefs) {
        this.userRefs = userRefs;
    }

    public Map<String, Map<String, Boolean>> getUsers() {
        return users;
    }

    public void setUsers(Map<String, Map<String, Boolean>> users) {
        this.users = users;
    }

    public List<String> getViewRoles() {
        return viewRoles;
    }

    public void setViewRoles(List<String> viewRoles) {
        this.viewRoles = viewRoles;
    }

    public List<String> getViewUserRefs() {
        return viewUserRefs;
    }

    public void setViewUserRefs(List<String> viewUserRefs) {
        this.viewUserRefs = viewUserRefs;
    }

    public List<String> getViewUsers() {
        return viewUsers;
    }

    public void setViewUsers(List<String> viewUsers) {
        this.viewUsers = viewUsers;
    }

    public List<String> getNegativeViewRoles() {
        return negativeViewRoles;
    }

    public void setNegativeViewRoles(List<String> negativeViewRoles) {
        this.negativeViewRoles = negativeViewRoles;
    }

    public List<String> getNegativeViewUsers() {
        return negativeViewUsers;
    }

    public void setNegativeViewUsers(List<String> negativeViewUsers) {
        this.negativeViewUsers = negativeViewUsers;
    }
}
