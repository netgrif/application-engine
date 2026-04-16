package com.netgrif.application.engine.workflow.service.sanitization;

import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.service.interfaces.IFieldSanitizationService;
import lombok.extern.slf4j.Slf4j;
import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class FieldSanitizationService implements IFieldSanitizationService {

    public static final String SANITIZATION_MODE_KEY = "sanitizationMode";
    public static final String SANITIZATION_ACTION_KEY = "sanitizationAction";

    private static final PolicyFactory PLAIN_TEXT_POLICY =
            new HtmlPolicyBuilder().toFactory();

    private static final PolicyFactory SAFE_HTML_BASIC_POLICY =
            new HtmlPolicyBuilder()
                    .allowElements("b", "i", "u", "em", "strong", "s", "span", "br")
                    .toFactory();

    private static final PolicyFactory SAFE_HTML_LINKS_ONLY_POLICY =
            new HtmlPolicyBuilder()
                    .allowElements("a")
                    .allowAttributes("href").onElements("a")
                    .allowUrlProtocols("http", "https", "mailto")
                    .requireRelNofollowOnLinks()
                    .toFactory();

    private static final PolicyFactory SAFE_HTML_NO_LINKS_POLICY =
            Sanitizers.FORMATTING.and(Sanitizers.BLOCKS);

    private static final PolicyFactory SAFE_HTML_POLICY =
            Sanitizers.FORMATTING
                    .and(Sanitizers.LINKS)
                    .and(Sanitizers.BLOCKS);

    private static final PolicyFactory SAFE_HTML_RELAXED_POLICY =
            Sanitizers.FORMATTING
                    .and(Sanitizers.LINKS)
                    .and(Sanitizers.BLOCKS)
                    .and(Sanitizers.TABLES)
                    .and(new HtmlPolicyBuilder()
                            .allowElements("code", "pre", "span")
                            .toFactory());

    private static final PolicyFactory DISABLE_JAVASCRIPT_POLICY =
            new HtmlPolicyBuilder()
                    .allowElements(
                            "a", "abbr", "b", "blockquote", "br", "caption", "code", "col", "colgroup",
                            "dd", "del", "div", "dl", "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6",
                            "hr", "i", "img", "ins", "li", "ol", "p", "pre", "s", "small", "span",
                            "strong", "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead",
                            "tr", "u", "ul"
                    )
                    .allowWithoutAttributes(
                            "abbr", "b", "blockquote", "br", "caption", "code", "dd", "del", "div", "dl",
                            "dt", "em", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img", "ins",
                            "li", "ol", "p", "pre", "s", "small", "span", "strong", "sub", "sup",
                            "table", "tbody", "td", "tfoot", "th", "thead", "tr", "u", "ul"
                    )
                    .allowAttributes("href").onElements("a")
                    .allowAttributes("src", "alt", "title").onElements("img")
                    .allowAttributes("colspan", "rowspan").onElements("td", "th")
                    .allowUrlProtocols("http", "https", "mailto")
                    .requireRelNofollowOnLinks()
                    .toFactory();

    @Override
    public String sanitize(String value, Field<?> field) {
        if (value == null) {
            return null;
        }

        Component component = field.getComponent();
        SanitizationMode mode = SanitizationMode.from(getProperty(component, SANITIZATION_MODE_KEY));
        SanitizationAction action = SanitizationAction.from(getProperty(component, SANITIZATION_ACTION_KEY));

        if (mode == SanitizationMode.OFF) {
            log.debug("Sanitization mode OFF for field [{}]", field.getStringId());
            return value;
        }

        String sanitized = resolvePolicy(mode).sanitize(value);

        if (!value.equals(sanitized)) {
            log.warn("Content was modified by sanitizer for field [{}] (mode={}, action={})",
                    field.getStringId(), mode, action);
            if (action == SanitizationAction.REJECT) {
                throw new IllegalArgumentException(
                        "Field [" + field.getStringId() + "] contains unsafe content " +
                        "and the configured action is REJECT."
                );
            }
        }

        return sanitized;
    }

    protected PolicyFactory resolvePolicy(SanitizationMode mode) {
        switch (mode) {
            case SAFE_HTML:
                return SAFE_HTML_POLICY;
            case SAFE_HTML_BASIC:
                return SAFE_HTML_BASIC_POLICY;
            case SAFE_HTML_LINKS_ONLY:
                return SAFE_HTML_LINKS_ONLY_POLICY;
            case SAFE_HTML_NO_LINKS:
                return SAFE_HTML_NO_LINKS_POLICY;
            case SAFE_HTML_RELAXED:
                return SAFE_HTML_RELAXED_POLICY;
            case PLAIN_TEXT:
                return PLAIN_TEXT_POLICY;
            case DISABLE_JAVASCRIPT:
            default:
                return DISABLE_JAVASCRIPT_POLICY;
        }
    }

    protected String getProperty(Component component, String key) {
        if (component == null) {
            return null;
        }

        Map<String, String> properties = component.getProperties();
        if (properties == null) {
            return null;
        }

        return properties.get(key);
    }

}