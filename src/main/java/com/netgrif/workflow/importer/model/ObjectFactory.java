
package com.netgrif.workflow.importer.model;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.netgrif.workflow.importer.model package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Id_QNAME = new QName("", "id");
    private final static QName _Title_QNAME = new QName("", "title");
    private final static QName _Placeholder_QNAME = new QName("", "placeholder");
    private final static QName _Desc_QNAME = new QName("", "desc");
    private final static QName _Values_QNAME = new QName("", "values");
    private final static QName _Valid_QNAME = new QName("", "valid");
    private final static QName _Init_QNAME = new QName("", "init");
    private final static QName _Code_QNAME = new QName("", "code");
    private final static QName _FractionSize_QNAME = new QName("", "fractionSize");
    private final static QName _Locale_QNAME = new QName("", "locale");
    private final static QName _Encryption_QNAME = new QName("", "encryption");
    private final static QName _Action_QNAME = new QName("", "action");
    private final static QName _ActionRef_QNAME = new QName("", "actionRef");
    private final static QName _Remote_QNAME = new QName("", "remote");
    private final static QName _Name_QNAME = new QName("", "name");
    private final static QName _Message_QNAME = new QName("", "message");
    private final static QName _TransitionRef_QNAME = new QName("", "transitionRef");
    private final static QName _Perform_QNAME = new QName("", "perform");
    private final static QName _Delegate_QNAME = new QName("", "delegate");
    private final static QName _View_QNAME = new QName("", "view");
    private final static QName _Cancel_QNAME = new QName("", "cancel");
    private final static QName _Alignment_QNAME = new QName("", "alignment");
    private final static QName _Stretch_QNAME = new QName("", "stretch");
    private final static QName _X_QNAME = new QName("", "x");
    private final static QName _Y_QNAME = new QName("", "y");
    private final static QName _Label_QNAME = new QName("", "label");
    private final static QName _Icon_QNAME = new QName("", "icon");
    private final static QName _Priority_QNAME = new QName("", "priority");
    private final static QName _AssignPolicy_QNAME = new QName("", "assignPolicy");
    private final static QName _DataFocusPolicy_QNAME = new QName("", "dataFocusPolicy");
    private final static QName _FinishPolicy_QNAME = new QName("", "finishPolicy");
    private final static QName _Tokens_QNAME = new QName("", "tokens");
    private final static QName _IsStatic_QNAME = new QName("", "isStatic");
    private final static QName _Static_QNAME = new QName("", "static");
    private final static QName _SourceId_QNAME = new QName("", "sourceId");
    private final static QName _DestinationId_QNAME = new QName("", "destinationId");
    private final static QName _Multiplicity_QNAME = new QName("", "multiplicity");
    private final static QName _I18NString_QNAME = new QName("", "i18nString");
    private final static QName _Version_QNAME = new QName("", "version");
    private final static QName _Initials_QNAME = new QName("", "initials");
    private final static QName _DefaultRole_QNAME = new QName("", "defaultRole");
    private final static QName _TransitionRole_QNAME = new QName("", "transitionRole");
    private final static QName _CaseName_QNAME = new QName("", "caseName");
    private final static QName _Type_QNAME = new QName("", "type");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.netgrif.workflow.importer.model
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Transaction }
     * 
     */
    public Transaction createTransaction() {
        return new Transaction();
    }

    /**
     * Create an instance of {@link I18NStringType }
     * 
     */
    public I18NStringType createI18NStringType() {
        return new I18NStringType();
    }

    /**
     * Create an instance of {@link Data }
     * 
     */
    public Data createData() {
        return new Data();
    }

    /**
     * Create an instance of {@link Format }
     *
     */
    public Format createFormat() {
        return new Format();
    }

    /**
     * Create an instance of {@link Currency }
     *
     */
    public Currency createCurrency() {
        return new Currency();
    }

    /**
     * Create an instance of {@link FieldView }
     *
     */
    public FieldView createFieldView() {
        return new FieldView();
    }

    /**
     * Create an instance of {@link EncryptionType }
     * 
     */
    public EncryptionType createEncryptionType() {
        return new EncryptionType();
    }

    /**
     * Create an instance of {@link ActionType }
     * 
     */
    public ActionType createActionType() {
        return new ActionType();
    }

    /**
     * Create an instance of {@link ActionRefType }
     * 
     */
    public ActionRefType createActionRefType() {
        return new ActionRefType();
    }

    /**
     * Create an instance of {@link DocumentRef }
     * 
     */
    public DocumentRef createDocumentRef() {
        return new DocumentRef();
    }

    /**
     * Create an instance of {@link Role }
     * 
     */
    public Role createRole() {
        return new Role();
    }

    /**
     * Create an instance of {@link Event }
     *
     */
    public Event createEvent() {
        return new Event();
    }

    /**
     * Create an instance of {@link Actions }
     *
     */
    public Actions createActions() {
        return new Actions();
    }

    /**
     * Create an instance of {@link Mapping }
     * 
     */
    public Mapping createMapping() {
        return new Mapping();
    }

    /**
     * Create an instance of {@link RoleRef }
     * 
     */
    public RoleRef createRoleRef() {
        return new RoleRef();
    }

    /**
     * Create an instance of {@link Logic }
     * 
     */
    public Logic createLogic() {
        return new Logic();
    }

    /**
     * Create an instance of {@link DataRef }
     * 
     */
    public DataRef createDataRef() {
        return new DataRef();
    }

    /**
     * Create an instance of {@link DataGroup }
     * 
     */
    public DataGroup createDataGroup() {
        return new DataGroup();
    }

    /**
     * Create an instance of {@link Trigger }
     * 
     */
    public Trigger createTrigger() {
        return new Trigger();
    }

    /**
     * Create an instance of {@link Transition }
     * 
     */
    public Transition createTransition() {
        return new Transition();
    }

    /**
     * Create an instance of {@link TransactionRef }
     * 
     */
    public TransactionRef createTransactionRef() {
        return new TransactionRef();
    }

    /**
     * Create an instance of {@link Place }
     * 
     */
    public Place createPlace() {
        return new Place();
    }

    /**
     * Create an instance of {@link Arc }
     * 
     */
    public Arc createArc() {
        return new Arc();
    }

    /**
     * Create an instance of {@link BreakPoint }
     * 
     */
    public BreakPoint createBreakPoint() {
        return new BreakPoint();
    }

    /**
     * Create an instance of {@link I18N }
     * 
     */
    public I18N createI18N() {
        return new I18N();
    }

    /**
     * Create an instance of {@link Document }
     * 
     */
    public Document createDocument() {
        return new Document();
    }

    /**
     * Create an instance of {@link BooleanImageView }
     *
     */
    public BooleanImageView createBooleanImageView() {
        return new BooleanImageView();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "id")
    public JAXBElement<String> createId(String value) {
        return new JAXBElement<String>(_Id_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "title")
    public JAXBElement<I18NStringType> createTitle(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Title_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "placeholder")
    public JAXBElement<I18NStringType> createPlaceholder(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Placeholder_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "desc")
    public JAXBElement<I18NStringType> createDesc(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Desc_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "values")
    public JAXBElement<I18NStringType> createValues(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Values_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "valid")
    public JAXBElement<String> createValid(String value) {
        return new JAXBElement<String>(_Valid_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "init")
    public JAXBElement<String> createInit(String value) {
        return new JAXBElement<String>(_Init_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "code", defaultValue = "EUR")
    public JAXBElement<String> createCode(String value) {
        return new JAXBElement<String>(_Code_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "fractionSize", defaultValue = "2")
    public JAXBElement<Integer> createFractionSize(Integer value) {
        return new JAXBElement<Integer>(_FractionSize_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "locale")
    public JAXBElement<String> createLocale(String value) {
        return new JAXBElement<String>(_Locale_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EncryptionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "encryption")
    public JAXBElement<EncryptionType> createEncryption(EncryptionType value) {
        return new JAXBElement<EncryptionType>(_Encryption_QNAME, EncryptionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActionType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "action")
    public JAXBElement<ActionType> createAction(ActionType value) {
        return new JAXBElement<ActionType>(_Action_QNAME, ActionType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActionRefType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "actionRef")
    public JAXBElement<ActionRefType> createActionRef(ActionRefType value) {
        return new JAXBElement<ActionRefType>(_ActionRef_QNAME, ActionRefType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "remote")
    public JAXBElement<String> createRemote(String value) {
        return new JAXBElement<String>(_Remote_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "name")
    public JAXBElement<I18NStringType> createName(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Name_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "message")
    public JAXBElement<I18NStringType> createMessage(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Message_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "transitionRef")
    public JAXBElement<String> createTransitionRef(String value) {
        return new JAXBElement<String>(_TransitionRef_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "perform")
    public JAXBElement<Boolean> createPerform(Boolean value) {
        return new JAXBElement<Boolean>(_Perform_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "delegate")
    public JAXBElement<Boolean> createDelegate(Boolean value) {
        return new JAXBElement<Boolean>(_Delegate_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "view")
    public JAXBElement<Boolean> createView(Boolean value) {
        return new JAXBElement<Boolean>(_View_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "", name = "cancel")
    public JAXBElement<Boolean> createCancel(Boolean value) {
        return new JAXBElement<Boolean>(_Cancel_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataGroupAlignment }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "alignment")
    public JAXBElement<DataGroupAlignment> createAlignment(DataGroupAlignment value) {
        return new JAXBElement<DataGroupAlignment>(_Alignment_QNAME, DataGroupAlignment.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "stretch")
    public JAXBElement<Boolean> createStretch(Boolean value) {
        return new JAXBElement<Boolean>(_Stretch_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "x")
    public JAXBElement<Short> createX(Short value) {
        return new JAXBElement<Short>(_X_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Short }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "y")
    public JAXBElement<Short> createY(Short value) {
        return new JAXBElement<Short>(_Y_QNAME, Short.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "label")
    public JAXBElement<I18NStringType> createLabel(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_Label_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "icon")
    public JAXBElement<String> createIcon(String value) {
        return new JAXBElement<String>(_Icon_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "priority")
    public JAXBElement<Integer> createPriority(Integer value) {
        return new JAXBElement<Integer>(_Priority_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AssignPolicyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "assignPolicy")
    public JAXBElement<AssignPolicyType> createAssignPolicy(AssignPolicyType value) {
        return new JAXBElement<AssignPolicyType>(_AssignPolicy_QNAME, AssignPolicyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DataFocusPolicyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "dataFocusPolicy")
    public JAXBElement<DataFocusPolicyType> createDataFocusPolicy(DataFocusPolicyType value) {
        return new JAXBElement<DataFocusPolicyType>(_DataFocusPolicy_QNAME, DataFocusPolicyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link FinishPolicyType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "finishPolicy")
    public JAXBElement<FinishPolicyType> createFinishPolicy(FinishPolicyType value) {
        return new JAXBElement<FinishPolicyType>(_FinishPolicy_QNAME, FinishPolicyType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "tokens")
    public JAXBElement<Integer> createTokens(Integer value) {
        return new JAXBElement<Integer>(_Tokens_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "isStatic")
    public JAXBElement<Boolean> createIsStatic(Boolean value) {
        return new JAXBElement<Boolean>(_IsStatic_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "static")
    public JAXBElement<Boolean> createStatic(Boolean value) {
        return new JAXBElement<Boolean>(_Static_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "sourceId")
    public JAXBElement<String> createSourceId(String value) {
        return new JAXBElement<String>(_SourceId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "destinationId")
    public JAXBElement<String> createDestinationId(String value) {
        return new JAXBElement<String>(_DestinationId_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Integer }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "multiplicity")
    public JAXBElement<Integer> createMultiplicity(Integer value) {
        return new JAXBElement<Integer>(_Multiplicity_QNAME, Integer.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "i18nString")
    public JAXBElement<I18NStringType> createI18NString(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_I18NString_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "version")
    public JAXBElement<String> createVersion(String value) {
        return new JAXBElement<String>(_Version_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "initials")
    public JAXBElement<String> createInitials(String value) {
        return new JAXBElement<String>(_Initials_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "defaultRole", defaultValue = "true")
    public JAXBElement<Boolean> createDefaultRole(Boolean value) {
        return new JAXBElement<Boolean>(_DefaultRole_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Boolean }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "transitionRole", defaultValue = "true")
    public JAXBElement<Boolean> createTransitionRole(Boolean value) {
        return new JAXBElement<Boolean>(_TransitionRole_QNAME, Boolean.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link I18NStringType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "caseName")
    public JAXBElement<I18NStringType> createCaseName(I18NStringType value) {
        return new JAXBElement<I18NStringType>(_CaseName_QNAME, I18NStringType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "type")
    public JAXBElement<String> createType(String value) {
        return new JAXBElement<String>(_Type_QNAME, String.class, null, value);
    }

}
