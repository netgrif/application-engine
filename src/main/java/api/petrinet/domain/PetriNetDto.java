package api.petrinet.domain;

import api.auth.AuthorDto;
import api.petrinet.domain.dataset.logic.action.runner.ExpressionDto;
import api.petrinet.domain.arcs.ArcDto;
import api.petrinet.domain.dataset.FieldDto;
import api.petrinet.domain.events.CaseEventDto;
import api.petrinet.domain.events.ProcessEventDto;
import api.petrinet.domain.roles.ProcessRoleDto;
import api.petrinet.version.VersionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class PetriNetDto extends PetriNetObjectDto {
    private String identifier;

    private I18nStringDto title;

    private boolean defaultRoleEnabled;

    private boolean anonymousRoleEnabled;

    private I18nStringDto defaultCaseName;

    private ExpressionDto defaultCaseNameExpression;

    private String initials;

    private String icon;

    private LocalDateTime creationDate;

    private VersionDto version;

    private AuthorDto author;

    private Map<String, PlaceDto> places;

    private Map<String, TransitionDto> transitions;

    private Map<String, List<ArcDto>> arcs;

    private Map<String, ? extends FieldDto> dataSet;

    private Map<String, ProcessRoleDto> roles;

    private Map<String, TransactionDto> transactions;

    private Map<String, ProcessEventDto> processEvents;

    private Map<String, CaseEventDto> caseEvents;

    private Map<String, Map<String, Boolean>> permissions;

    private List<String> negativeViewRoles;

    private Map<String, Map<String, Boolean>> userRefs;

    private List<FunctionDto> functions;

    private boolean initialized;

    private String importXmlPath;

    public PetriNetDto() {
    }

    public PetriNetDto(String identifier, I18nStringDto title, boolean defaultRoleEnabled, boolean anonymousRoleEnabled, I18nStringDto defaultCaseName, ExpressionDto defaultCaseNameExpression, String initials, String icon, LocalDateTime creationDate, VersionDto version, AuthorDto author, Map<String, PlaceDto> places, Map<String, TransitionDto> transitions, Map<String, List<ArcDto>> arcs, Map<String, FieldDto> dataSet, Map<String, ProcessRoleDto> roles, Map<String, TransactionDto> transactions, Map<String, ProcessEventDto> processEvents, Map<String, CaseEventDto> caseEvents, Map<String, Map<String, Boolean>> permissions, List<String> negativeViewRoles, Map<String, Map<String, Boolean>> userRefs, List<FunctionDto> functions, boolean initialized, String importXmlPath) {
        this.identifier = identifier;
        this.title = title;
        this.defaultRoleEnabled = defaultRoleEnabled;
        this.anonymousRoleEnabled = anonymousRoleEnabled;
        this.defaultCaseName = defaultCaseName;
        this.defaultCaseNameExpression = defaultCaseNameExpression;
        this.initials = initials;
        this.icon = icon;
        this.creationDate = creationDate;
        this.version = version;
        this.author = author;
        this.places = places;
        this.transitions = transitions;
        this.arcs = arcs;
        this.dataSet = dataSet;
        this.roles = roles;
        this.transactions = transactions;
        this.processEvents = processEvents;
        this.caseEvents = caseEvents;
        this.permissions = permissions;
        this.negativeViewRoles = negativeViewRoles;
        this.userRefs = userRefs;
        this.functions = functions;
        this.initialized = initialized;
        this.importXmlPath = importXmlPath;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public I18nStringDto getTitle() {
        return title;
    }

    public void setTitle(I18nStringDto title) {
        this.title = title;
    }

    public boolean isDefaultRoleEnabled() {
        return defaultRoleEnabled;
    }

    public void setDefaultRoleEnabled(boolean defaultRoleEnabled) {
        this.defaultRoleEnabled = defaultRoleEnabled;
    }

    public boolean isAnonymousRoleEnabled() {
        return anonymousRoleEnabled;
    }

    public void setAnonymousRoleEnabled(boolean anonymousRoleEnabled) {
        this.anonymousRoleEnabled = anonymousRoleEnabled;
    }

    public I18nStringDto getDefaultCaseName() {
        return defaultCaseName;
    }

    public void setDefaultCaseName(I18nStringDto defaultCaseName) {
        this.defaultCaseName = defaultCaseName;
    }

    public ExpressionDto getDefaultCaseNameExpression() {
        return defaultCaseNameExpression;
    }

    public void setDefaultCaseNameExpression(ExpressionDto defaultCaseNameExpression) {
        this.defaultCaseNameExpression = defaultCaseNameExpression;
    }

    public String getInitials() {
        return initials;
    }

    public void setInitials(String initials) {
        this.initials = initials;
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

    public VersionDto getVersion() {
        return version;
    }

    public void setVersion(VersionDto version) {
        this.version = version;
    }

    public AuthorDto getAuthor() {
        return author;
    }

    public void setAuthor(AuthorDto author) {
        this.author = author;
    }

    public Map<String, PlaceDto> getPlaces() {
        return places;
    }

    public void setPlaces(Map<String, PlaceDto> places) {
        this.places = places;
    }

    public Map<String, TransitionDto> getTransitions() {
        return transitions;
    }

    public void setTransitions(Map<String, TransitionDto> transitions) {
        this.transitions = transitions;
    }

    public Map<String, List<ArcDto>> getArcs() {
        return arcs;
    }

    public void setArcs(Map<String, List<ArcDto>> arcs) {
        this.arcs = arcs;
    }

    public Map<String, ? extends FieldDto> getDataSet() {
        return dataSet;
    }

    public void setDataSet(Map<String, FieldDto> dataSet) {
        this.dataSet = dataSet;
    }

    public Map<String, ProcessRoleDto> getRoles() {
        return roles;
    }

    public void setRoles(Map<String, ProcessRoleDto> roles) {
        this.roles = roles;
    }

    public Map<String, TransactionDto> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, TransactionDto> transactions) {
        this.transactions = transactions;
    }

    public Map<String, ProcessEventDto> getProcessEvents() {
        return processEvents;
    }

    public void setProcessEvents(Map<String, ProcessEventDto> processEvents) {
        this.processEvents = processEvents;
    }

    public Map<String, CaseEventDto> getCaseEvents() {
        return caseEvents;
    }

    public void setCaseEvents(Map<String, CaseEventDto> caseEvents) {
        this.caseEvents = caseEvents;
    }

    public Map<String, Map<String, Boolean>> getPermissions() {
        return permissions;
    }

    public void setPermissions(Map<String, Map<String, Boolean>> permissions) {
        this.permissions = permissions;
    }

    public List<String> getNegativeViewRoles() {
        return negativeViewRoles;
    }

    public void setNegativeViewRoles(List<String> negativeViewRoles) {
        this.negativeViewRoles = negativeViewRoles;
    }

    public Map<String, Map<String, Boolean>> getUserRefs() {
        return userRefs;
    }

    public void setUserRefs(Map<String, Map<String, Boolean>> userRefs) {
        this.userRefs = userRefs;
    }

    public List<FunctionDto> getFunctions() {
        return functions;
    }

    public void setFunctions(List<FunctionDto> functions) {
        this.functions = functions;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public String getImportXmlPath() {
        return importXmlPath;
    }

    public void setImportXmlPath(String importXmlPath) {
        this.importXmlPath = importXmlPath;
    }
}
