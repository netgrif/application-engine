package com.netgrif.application.engine.elastic;

import com.netgrif.application.engine.ApplicationEngine;
import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@ActiveProfiles({"test"})
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ApplicationEngine.class
)
@TestPropertySource(
        locations = "classpath:application-test.properties"
)
class ElasticsearchQuerySanitizerTest {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchQuerySanitizerTest.class);

    @Test
    void shouldSanitizeQuery() {
        String query = "identifier: some_value AND field.keyword.value <> other_value";
        String sanitized = ElasticsearchQuerySanitizer.sanitize(query);
        log.info("Sanitized query: {}", sanitized);
        assertNotNull(sanitized);
        assertEquals("identifier\\:\\ some_value\\ \\A\\N\\D\\ field.keyword.value\\ \\ \\ " +
                "\\ other_value", sanitized);
    }

}
