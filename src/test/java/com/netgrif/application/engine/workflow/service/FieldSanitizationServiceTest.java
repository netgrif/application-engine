package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.workflow.service.sanitization.FieldSanitizationService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.TextField;
import org.mockito.Mockito;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class FieldSanitizationServiceTest {

    private FieldSanitizationService service;

    @BeforeEach
    void setUp() {
        service = new FieldSanitizationService();
    }

    @Test
    void shouldReturnNullWhenValueIsNull() {
        TextField field = new TextField();
        String result = service.sanitize(null, field);
        assertNull(result);
    }

    @Test
    void shouldReturnSameValueWhenInputIsPlainText() {
        TextField field = new TextField();
        String input = "Hello world 123";
        String result = service.sanitize(input, field);
        assertEquals("Hello world 123", result);
    }

    @Test
    void shouldStripHtmlTagsWhenDefaultModeIsPlainText() {
        TextField field = new TextField();
        String input = "<b>Hello</b> <script>alert('xss')</script> world";
        String result = service.sanitize(input, field);
        assertEquals("Hello  world", result);
        assertNotEquals(input, result);
    }

    @Test
    void shouldStripDangerousAttributesWhenDefaultModeIsPlainText() {
        TextField field = new TextField();
        String input = "<img src=\"x\" onerror=\"alert('xss')\">test";
        String result = service.sanitize(input, field);
        assertEquals("test", result);
    }

    @Test
    void shouldNotSanitizeWhenModeIsOff() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "OFF")
        );
        field.setComponent(component);
        String input = "<b>Hello</b><script>alert('xss')</script>";
        String result = service.sanitize(input, field);
        assertEquals(input, result);
    }

    @Test
    void shouldResolveModeCaseInsensitively() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "oFf")
        );

        field.setComponent(component);

        String input = "<b>Hello</b>";
        String result = service.sanitize(input, field);

        assertEquals(input, result);
    }

    @Test
    void shouldUsePlainTextModeWhenComponentIsNull() {
        TextField field = new TextField();
        field.setComponent(null);
        String input = "<b>Hello</b>";
        String result = service.sanitize(input, field);
        assertEquals("Hello", result);
    }

    @Test
    void shouldUsePlainTextModeWhenComponentPropertiesAreNull() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(null);
        field.setComponent(component);
        String input = "<b>Hello</b>";
        String result = service.sanitize(input, field);
        assertEquals("Hello", result);
    }

    @Test
    void shouldUsePlainTextModeWhenSanitizationModePropertyIsMissing() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(Map.of("otherKey", "true"));
        field.setComponent(component);
        String input = "<i>text</i>";
        String result = service.sanitize(input, field);
        assertEquals("text", result);
    }

    @Test
    void shouldKeepSafeFormattingWhenModeIsSafeHtml() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "SAFE_HTML")
        );

        field.setComponent(component);

        String input = "<b>Hello</b> <i>world</i>";
        String result = service.sanitize(input, field);

        assertEquals("<b>Hello</b> <i>world</i>", result);
    }

    @Test
    void shouldRemoveScriptWhenModeIsSafeHtml() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "SAFE_HTML")
        );

        field.setComponent(component);

        String input = "<b>Hello</b><script>alert('xss')</script>";
        String result = service.sanitize(input, field);

        assertEquals("<b>Hello</b>", result);
    }

    @Test
    void shouldRemoveJavascriptHrefWhenModeIsSafeHtml() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "SAFE_HTML")
        );

        field.setComponent(component);

        String input = "<a href=\"javascript:alert('xss')\">click</a>";
        String result = service.sanitize(input, field);

        assertNotEquals(input, result);
        assertFalse(result.contains("javascript:"));
        assertTrue(result.contains("click"));
    }



    @Test
    void shouldRemoveImgTagWhenModeIsSafeHtml() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "SAFE_HTML")
        );

        field.setComponent(component);

        String input = "<img src=\"x\" onerror=\"alert('xss')\">test";
        String result = service.sanitize(input, field);

        assertEquals("test", result);
    }

    @Test
    void shouldThrowExceptionWhenActionIsRejectAndPlainTextModeChangesContent() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(
                        FieldSanitizationService.SANITIZATION_MODE_KEY, "PLAIN_TEXT",
                        FieldSanitizationService.SANITIZATION_ACTION_KEY, "REJECT"
                )
        );
        field.setComponent(component);
        String input = "<script>alert('xss')</script>Hello";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.sanitize(input, field)
        );

        assertEquals("Field [null] contains unsafe content and the configured action is REJECT.", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenActionIsRejectAndSafeHtmlModeChangesContent() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(
                        FieldSanitizationService.SANITIZATION_MODE_KEY, "SAFE_HTML",
                        FieldSanitizationService.SANITIZATION_ACTION_KEY, "REJECT"
                )
        );

        field.setComponent(component);

        String input = "<b>Hello</b><script>alert('xss')</script>";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.sanitize(input, field)
        );

        assertEquals("Field [null] contains unsafe content and the configured action is REJECT.", exception.getMessage());
    }

    @Test
    void shouldNotThrowExceptionWhenActionIsSanitize() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(
                Map.of(
                        FieldSanitizationService.SANITIZATION_MODE_KEY, "PLAIN_TEXT",
                        FieldSanitizationService.SANITIZATION_ACTION_KEY, "SANITIZE"
                )
        );
        field.setComponent(component);
        String input = "<b>Hello</b>";
        String result = service.sanitize(input, field);
        assertEquals("Hello", result);
    }

    @Test
    void shouldUseSanitizeActionWhenActionPropertyIsMissing() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);

        when(component.getProperties()).thenReturn(
                Map.of(FieldSanitizationService.SANITIZATION_MODE_KEY, "PLAIN_TEXT")
        );

        field.setComponent(component);
        String input = "<b>Hello</b>";
        String result = service.sanitize(input, field);
        assertEquals("Hello", result);
    }

    @Test
    void shouldResolveActionCaseInsensitively() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(
                Map.of(
                        FieldSanitizationService.SANITIZATION_MODE_KEY, "PLAIN_TEXT",
                        FieldSanitizationService.SANITIZATION_ACTION_KEY, "rEjEcT"
                )
        );
        field.setComponent(component);

        String input = "<b>Hello</b>";

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.sanitize(input, field)
        );

        assertEquals("Field [null] contains unsafe content and the configured action is REJECT.", exception.getMessage());
    }

    @Test
    void shouldNotThrowWhenActionIsRejectButContentIsAlreadyClean() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(
                Map.of(
                        FieldSanitizationService.SANITIZATION_MODE_KEY, "PLAIN_TEXT",
                        FieldSanitizationService.SANITIZATION_ACTION_KEY, "REJECT"
                )
        );
        field.setComponent(component);

        String input = "Hello world";
        String result = service.sanitize(input, field);

        assertEquals("Hello world", result);
    }

    @Test
    void shouldIgnoreRejectActionWhenModeIsOff() {
        TextField field = new TextField();
        Component component = Mockito.mock(Component.class);
        when(component.getProperties()).thenReturn(
                Map.of(
                        FieldSanitizationService.SANITIZATION_MODE_KEY, "OFF",
                        FieldSanitizationService.SANITIZATION_ACTION_KEY, "REJECT"
                )
        );
        field.setComponent(component);

        String input = "<script>alert('xss')</script>Hello";
        String result = service.sanitize(input, field);

        assertEquals(input, result);
    }
}