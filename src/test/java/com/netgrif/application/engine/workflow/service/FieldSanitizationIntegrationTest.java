package com.netgrif.application.engine.workflow.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.domain.throwable.MissingPetriNetMetaDataException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.SuperCreator;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportPetriNetEventOutcome;
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository;
import com.netgrif.application.engine.workflow.domain.repositories.TaskRepository;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class FieldSanitizationIntegrationTest {

    private static final String XML_PATH = "src/test/resources/sanitization_modes.xml";
    private static final String TASK_TITLE = "Transition";

    private static final String PAYLOAD_SCRIPT_BOLD = "<b>Hello</b><script>alert('xss')</script>";
    private static final String PAYLOAD_IMG_ONERROR = "<p><img src=x onerror=alert(document.domain)></p>";
    private static final String PAYLOAD_JS_LINK = "<a href=\"javascript:alert('xss')\">click</a>";
    private static final String PAYLOAD_SAFE_LINK = "<a href=\"https://example.com\">click</a>";
    private static final String PAYLOAD_TABLE_SCRIPT = "<table><tr><td>Cell</td></tr></table><script>alert(1)</script>";
    private static final String PAYLOAD_CODE_PRE = "<code>System.out.println()</code><pre>line1\nline2</pre>";
    private static final String PAYLOAD_PLAIN = "Hello plain text";

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CaseRepository caseRepository;

    @Autowired
    private DataService dataService;

    @Autowired
    private SuperCreator superCreator;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void before() {
        testHelper.truncateDbs();
    }

    @Test
    void offModeShouldStoreRawScriptPayload() throws Exception {
        assertStoredEquals("text_off", PAYLOAD_SCRIPT_BOLD, PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void offModeShouldStoreRawImgOnErrorPayload() throws Exception {
        assertStoredEquals("text_off", PAYLOAD_IMG_ONERROR, PAYLOAD_IMG_ONERROR);
    }

    @Test
    void offModeShouldStoreRawJavascriptHrefPayload() throws Exception {
        assertStoredEquals("text_off", PAYLOAD_JS_LINK, PAYLOAD_JS_LINK);
    }

    @Test
    void offModeShouldKeepPlainTextUntouched() throws Exception {
        assertStoredEquals("text_off", PAYLOAD_PLAIN, PAYLOAD_PLAIN);
    }

    @Test
    void plainDefaultShouldKeepPlainTextUntouched() throws Exception {
        assertStoredEquals("text_plain_default", PAYLOAD_PLAIN, PAYLOAD_PLAIN);
    }

    @Test
    void plainTextSanitizeShouldStripHtml() throws Exception {
        assertStoredEquals("text_plain_sanitize", PAYLOAD_SCRIPT_BOLD, "Hello");
    }

    @Test
    void plainTextSanitizeShouldKeepPlainTextUntouched() throws Exception {
        assertStoredEquals("text_plain_sanitize", "Hello world 123", "Hello world 123");
    }

    @Test
    void plainTextSanitizeShouldRemoveImgTagAndKeepNoText() throws Exception {
        assertStoredEquals("text_plain_sanitize", PAYLOAD_IMG_ONERROR, "");
    }

    @Test
    void plainTextSanitizeShouldRemoveImgTagAndKeepText() throws Exception {
        assertStoredEquals("text_plain_sanitize", "<img src=\"x\" onerror=\"alert('xss')\">test", "test");
    }

    @Test
    void plainTextRejectShouldThrowExceptionForBoldHtml() throws Exception {
        assertRejected("text_plain_reject", "<b>Hello</b>");
    }

    @Test
    void plainTextRejectShouldThrowExceptionForScriptPayload() throws Exception {
        assertRejected("text_plain_reject", PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void plainTextRejectShouldThrowExceptionForImgPayload() throws Exception {
        assertRejected("text_plain_reject", PAYLOAD_IMG_ONERROR);
    }

    @Test
    void plainTextRejectShouldNotThrowWhenValueIsAlreadyClean() throws Exception {
        assertStoredEquals("text_plain_reject", "Hello clean", "Hello clean");
    }

    @Test
    void safeHtmlShouldKeepSafeFormattingAndRemoveScript() throws Exception {
        assertStoredEquals("text_safe_html", PAYLOAD_SCRIPT_BOLD, "<b>Hello</b>");
    }

    @Test
    void safeHtmlShouldRemoveImgOnError() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_html", PAYLOAD_IMG_ONERROR));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_html");
        assertFalse(value.contains("<img"));
        assertFalse(value.contains("onerror"));
    }

    @Test
    void safeHtmlShouldKeepParagraphAndListFormatting() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        String input = "<p>Hello</p><ul><li>One</li><li>Two</li></ul>";
        dataService.setData(task, textValue("text_safe_html", input));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_html");
        assertTrue(value.contains("<p>Hello</p>"));
        assertTrue(value.contains("<ul>"));
        assertTrue(value.contains("<li>One</li>"));
        assertTrue(value.contains("<li>Two</li>"));
    }

    @Test
    void safeHtmlShouldRemoveJavascriptHref() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_html", PAYLOAD_JS_LINK));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_html");
        assertFalse(value.contains("javascript:"));
        assertTrue(value.contains("click"));
    }

    @Test
    void safeHtmlShouldKeepSafeHttpsLink() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_html", PAYLOAD_SAFE_LINK));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_html");
        assertTrue(value.contains("click"));
        assertTrue(value.contains("https://example.com"));
    }

    @Test
    void safeHtmlRejectShouldThrowExceptionWhenUnsafeContentIsPresent() throws Exception {
        assertRejected("text_safe_html_reject", PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void safeHtmlRejectShouldThrowExceptionForImgPayload() throws Exception {
        assertRejected("text_safe_html_reject", PAYLOAD_IMG_ONERROR);
    }

    @Test
    void safeHtmlRejectShouldNotThrowWhenContentIsSafe() throws Exception {
        assertStoredEquals("text_safe_html_reject", "<b>Hello</b><i>world</i>", "<b>Hello</b><i>world</i>");
    }

    @Test
    void safeHtmlBasicShouldKeepInlineFormattingOnly() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        String input = "<b>Hello</b><span>world</span><div>BLOCK</div>";
        dataService.setData(task, textValue("text_safe_basic", input));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_basic");
        assertTrue(value.contains("<b>Hello</b>"));
        assertTrue(value.contains("world"));
        assertFalse(value.contains("<div>"));
        assertTrue(value.contains("BLOCK"));
    }

    @Test
    void safeHtmlBasicShouldRemoveImgOnError() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_basic", PAYLOAD_IMG_ONERROR));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_basic");
        assertFalse(value.contains("<img"));
        assertFalse(value.contains("onerror"));
    }

    @Test
    void safeHtmlLinksOnlyShouldKeepSafeLink() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_links_only", PAYLOAD_SAFE_LINK));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_links_only");
        assertTrue(value.contains("click"));
        assertTrue(value.contains("href"));
        assertTrue(value.contains("https://example.com"));
    }

    @Test
    void safeHtmlLinksOnlyShouldRemoveJavascriptHref() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_links_only", PAYLOAD_JS_LINK));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_links_only");
        assertFalse(value.contains("javascript:"));
        assertTrue(value.contains("click"));
    }

    @Test
    void safeHtmlLinksOnlyShouldRemoveNonLinkFormatting() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        String input = "<b>Hello</b><a href=\"https://example.com\">click</a>";
        dataService.setData(task, textValue("text_safe_links_only", input));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_links_only");
        assertFalse(value.contains("<b>"));
        assertTrue(value.contains("Hello"));
        assertTrue(value.contains("click"));
    }

    @Test
    void safeHtmlLinksOnlyShouldRemoveImgPayload() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_links_only", PAYLOAD_IMG_ONERROR));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_links_only");
        assertFalse(value.contains("<img"));
        assertFalse(value.contains("onerror"));
    }

    @Test
    void safeHtmlNoLinksShouldRemoveAnchorButKeepText() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        String input = "<a href=\"https://example.com\">click</a><b>Hello</b>";
        dataService.setData(task, textValue("text_safe_no_links", input));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_no_links");
        assertFalse(value.contains("<a"));
        assertTrue(value.contains("click"));
        assertTrue(value.contains("<b>Hello</b>"));
    }

    @Test
    void safeHtmlNoLinksShouldKeepBlockElements() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        String input = "<p>Hello</p><div>World</div>";
        dataService.setData(task, textValue("text_safe_no_links", input));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_no_links");
        assertTrue(value.contains("<p>Hello</p>"));
        assertTrue(value.contains("<div>World</div>"));
    }

    @Test
    void safeHtmlNoLinksShouldRemoveJavascriptLinkButKeepText() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_no_links", PAYLOAD_JS_LINK));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_no_links");
        assertFalse(value.contains("<a"));
        assertFalse(value.contains("javascript:"));
        assertTrue(value.contains("click"));
    }

    @Test
    void safeHtmlRelaxedShouldKeepTableFormatting() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_relaxed", "<table><tr><td>Cell</td></tr></table>"));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_relaxed");
        assertTrue(value.contains("<table"));
        assertTrue(value.contains("Cell"));
    }

    @Test
    void safeHtmlRelaxedShouldKeepCodeAndPre() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_relaxed", PAYLOAD_CODE_PRE));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_relaxed");
        assertTrue(value.contains("<code>System.out.println()</code>"));
        assertTrue(value.contains("<pre>"));
    }

    @Test
    void safeHtmlRelaxedShouldStillRemoveScript() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_relaxed", PAYLOAD_TABLE_SCRIPT));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_relaxed");
        assertTrue(value.contains("<table"));
        assertTrue(value.contains("Cell"));
        assertFalse(value.contains("<script>"));
    }

    @Test
    void safeHtmlRelaxedShouldRemoveImgOnError() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_safe_relaxed", PAYLOAD_IMG_ONERROR));

        String value = getStringValue(getCase(useCase.getStringId()), "text_safe_relaxed");
        assertFalse(value.contains("<img"));
        assertFalse(value.contains("onerror"));
    }

    @Test
    void safeHtmlRelaxedRejectShouldRejectUnsafeInput() throws Exception {
        assertRejected("text_safe_relaxed_reject", PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void safeHtmlRelaxedRejectShouldRejectNormalizedTableInput() throws Exception {
        assertRejected("text_safe_relaxed_reject", "<table><tr><td>Cell</td></tr></table>");
    }

    @Test
    void disableJavascriptShouldRemoveEventHandlerButKeepMarkup() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_disable_javascript", PAYLOAD_IMG_ONERROR));

        String value = getStringValue(getCase(useCase.getStringId()), "text_disable_javascript");
        assertTrue(value.contains("<p>"));
        assertTrue(value.contains("<img"));
        assertFalse(value.contains("onerror"));
        assertFalse(value.contains("alert"));
    }

    @Test
    void disableJavascriptRejectShouldRejectWhenJavascriptAttributeIsRemoved() throws Exception {
        assertRejected("text_disable_javascript_reject", PAYLOAD_IMG_ONERROR);
    }

    @Test
    void disableJavascriptRejectShouldRejectNormalizedSafeMarkup() throws Exception {
        assertRejected("text_disable_javascript_reject", "<p><img src=\"https://example.com/image.png\"></p>");
    }

    @Test
    void dataLevelSafeHtmlShouldApplyConfiguration() throws Exception {
        assertStoredEquals("text_safe_html", PAYLOAD_SCRIPT_BOLD, "<b>Hello</b>");
    }

    @Test
    void dataLevelSafeHtmlShouldKeepSafeHtmlUntouched() throws Exception {
        assertStoredEquals("text_safe_html", "<b>Hello</b><i>world</i>", "<b>Hello</b><i>world</i>");
    }

    @Test
    void dataRefOverrideOffShouldOverrideDataConfiguration() throws Exception {
        assertStoredEquals("text_override_off", PAYLOAD_SCRIPT_BOLD, PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void dataRefOverridePlainSanitizeShouldOverrideDataConfiguration() throws Exception {
        assertStoredEquals("text_override_plain_sanitize", PAYLOAD_SCRIPT_BOLD, "Hello");
    }

    @Test
    void dataRefOverridePlainRejectShouldRejectUnsafeInput() throws Exception {
        assertRejected("text_override_plain_reject", PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void dataRefOverrideSafeHtmlShouldOverridePlainDefaultConfiguration() throws Exception {
        assertStoredEquals("text_override_safe_html", PAYLOAD_SCRIPT_BOLD, "<b>Hello</b>");
    }

    @Test
    void dataRefOverrideSafeHtmlRejectShouldRejectUnsafeInput() throws Exception {
        assertRejected("text_override_safe_html_reject", PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void dataRefOverrideSafeBasicShouldOverrideDefaultConfiguration() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_override_safe_basic", "<b>Hello</b><span>world</span><div>BLOCK</div>"));

        String value = getStringValue(getCase(useCase.getStringId()), "text_override_safe_basic");
        assertTrue(value.contains("<b>Hello</b>"));
        assertTrue(value.contains("world"));
        assertFalse(value.contains("<div>"));
        assertTrue(value.contains("BLOCK"));
    }

    @Test
    void dataRefOverrideSafeLinksOnlyShouldKeepOnlyLinks() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_override_safe_links_only", "<b>Hello</b><a href=\"https://example.com\">click</a>"));

        String value = getStringValue(getCase(useCase.getStringId()), "text_override_safe_links_only");
        assertFalse(value.contains("<b>"));
        assertTrue(value.contains("Hello"));
        assertTrue(value.contains("click"));
        assertTrue(value.contains("https://example.com"));
    }

    @Test
    void dataRefOverrideSafeNoLinksShouldRemoveAnchorButKeepText() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_override_safe_no_links", "<a href=\"https://example.com\">click</a><b>Hello</b>"));

        String value = getStringValue(getCase(useCase.getStringId()), "text_override_safe_no_links");
        assertFalse(value.contains("<a"));
        assertTrue(value.contains("click"));
        assertTrue(value.contains("<b>Hello</b>"));
    }

    @Test
    void dataRefOverrideSafeRelaxedShouldAcceptTable() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_override_safe_relaxed", "<table><tr><td>Cell</td></tr></table>"));

        String value = getStringValue(getCase(useCase.getStringId()), "text_override_safe_relaxed");
        assertTrue(value.contains("<table"));
        assertTrue(value.contains("Cell"));
    }

    @Test
    void dataRefOverrideSafeRelaxedRejectShouldRejectUnsafeInput() throws Exception {
        assertRejected("text_override_safe_relaxed_reject", PAYLOAD_SCRIPT_BOLD);
    }

    @Test
    void dataRefOverrideSafeRelaxedRejectShouldRejectNormalizedTableInput() throws Exception {
        assertRejected("text_override_safe_relaxed_reject", "<table><tr><td>Cell</td></tr></table>");
    }

    @Test
    void dataRefOverrideDisableJavascriptShouldRemoveEventHandlerButKeepMarkup() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValue("text_override_disable_javascript", PAYLOAD_IMG_ONERROR));

        String value = getStringValue(getCase(useCase.getStringId()), "text_override_disable_javascript");
        assertTrue(value.contains("<p>"));
        assertTrue(value.contains("<img"));
        assertFalse(value.contains("onerror"));
        assertFalse(value.contains("alert"));
    }

    @Test
    void dataRefOverrideDisableJavascriptRejectShouldRejectUnsafeInput() throws Exception {
        assertRejected("text_override_disable_javascript_reject", PAYLOAD_IMG_ONERROR);
    }

    @Test
    void shouldStoreNullValueWhenInputIsNull() throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        dataService.setData(task, textValueWithNull("text_plain_sanitize"));

        Case stored = getCase(useCase.getStringId());
        assertNull(getCase(stored.getStringId()).getDataField("text_plain_sanitize").getValue());
    }

    private void assertStoredEquals(String fieldId, String input, String expected) throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        SetDataEventOutcome outcome = dataService.setData(task, textValue(fieldId, input));
        assertNotNull(outcome);

        Case stored = getCase(useCase.getStringId());
        assertEquals(expected, getStringValue(stored, fieldId));
    }

    private void assertRejected(String fieldId, String input) throws Exception {
        Case useCase = createCase();
        Task task = findTask(useCase);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> dataService.setData(task, textValue(fieldId, input))
        );

        assertTrue(ex.getMessage().contains("unsafe content"));
    }

    private Case createCase() throws IOException, MissingPetriNetMetaDataException {
        ImportPetriNetEventOutcome importOutcome = petriNetService.importPetriNet(
                new FileInputStream(XML_PATH),
                VersionType.MAJOR,
                superCreator.getLoggedSuper()
        );

        PetriNet net = importOutcome.getNet();
        assertNotNull(net);

        CreateCaseEventOutcome caseOutcome = workflowService.createCase(
                net.getStringId(),
                "Sanitization test case",
                "color",
                superCreator.getLoggedSuper()
        );

        assertNotNull(caseOutcome);
        assertNotNull(caseOutcome.getCase());
        return caseOutcome.getCase();
    }

    private Task findTask(Case useCase) {
        return taskRepository.findAll()
                .stream()
                .filter(task -> useCase.getStringId().equals(task.getCaseId()))
                .filter(task -> task.getTitle() != null && TASK_TITLE.equals(task.getTitle().getDefaultValue()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Task not found"));
    }

    private Case getCase(String caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> new AssertionError("Case not found"));
    }

    private String getStringValue(Case useCase, String fieldId) {
        DataField dataField = useCase.getDataField(fieldId);
        assertNotNull(dataField, "Field [" + fieldId + "] not found");
        return (String) dataField.getValue();
    }

    private ObjectNode textValue(String fieldId, String value) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode fieldNode = objectMapper.createObjectNode();
        fieldNode.put("type", "text");
        fieldNode.put("value", value);
        root.set(fieldId, fieldNode);
        return root;
    }

    private ObjectNode textValueWithNull(String fieldId) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode fieldNode = objectMapper.createObjectNode();
        fieldNode.put("type", "text");
        fieldNode.putNull("value");
        root.set(fieldId, fieldNode);
        return root;
    }
}