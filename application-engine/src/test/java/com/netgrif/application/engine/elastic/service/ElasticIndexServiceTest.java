package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ElasticIndexServiceTest {

    @Test
    void prepareAnalysisSettingsReturnsNullWhenAnalyzerDisabled() {
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.setAnalyzerEnabled(false);

        assertNull(service(properties).prepareAnalysisSettings());
    }

    @Test
    void prepareAnalysisSettingsBuildsDefaultAnalyzerSettings() {
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.setAnalyzerEnabled(true);
        properties.setDefaultFilters(List.of("lowercase", "unique"));
        properties.setDefaultSearchFilters(List.of("lowercase"));

        Map<String, Object> settings = service(properties).prepareAnalysisSettings();

        Map<?, ?> analyzers = (Map<?, ?>) settings.get("analyzer");
        Map<?, ?> defaultAnalyzer = (Map<?, ?>) analyzers.get("default");
        Map<?, ?> defaultSearchAnalyzer = (Map<?, ?>) analyzers.get("default_search");
        assertEquals("custom", defaultAnalyzer.get("type"));
        assertEquals("standard", defaultAnalyzer.get("tokenizer"));
        assertEquals(List.of("html_strip"), defaultAnalyzer.get("char_filter"));
        assertEquals(List.of("lowercase", "unique"), defaultAnalyzer.get("filter"));
        assertEquals(List.of("lowercase"), defaultSearchAnalyzer.get("filter"));
    }

    @Test
    void prepareAnalysisSettingsReadsConfiguredResource() {
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.setAnalyzerEnabled(true);
        properties.setAnalyzerPathFile(new ByteArrayResource("""
                {
                  "analyzer": {
                    "custom": {
                      "type": "custom",
                      "tokenizer": "keyword"
                    }
                  }
                }
                """.getBytes(StandardCharsets.UTF_8)));

        Map<String, Object> settings = service(properties).prepareAnalysisSettings();

        Map<?, ?> analyzers = (Map<?, ?>) settings.get("analyzer");
        Map<?, ?> customAnalyzer = (Map<?, ?>) analyzers.get("custom");
        assertEquals("keyword", customAnalyzer.get("tokenizer"));
    }

    @Test
    void applySettingsMergesGeneralAndClassSpecificSettings() {
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        properties.setIndexSettings(new HashMap<>(Map.of(
                "max_result_window", 1000,
                "refresh_interval", "5s"
        )));
        properties.setClassSpecificIndexSettings(Map.of(
                "ElasticCase", Map.of("refresh_interval", "1s")
        ));
        HashMap<String, Object> settingMap = new HashMap<>();

        service(properties).applySettings(settingMap, ElasticCase.class);

        assertEquals(1000, settingMap.get("max_result_window"));
        assertEquals("1s", settingMap.get("refresh_interval"));
    }

    @Test
    void applyMappingSettingsCopiesConfiguredMappingSettings() {
        DataConfigurationProperties.ElasticsearchProperties properties = new DataConfigurationProperties.ElasticsearchProperties();
        Map<String, Object> mappingSettings = Map.of(
                "date_detection", false,
                "dynamic", "strict"
        );
        properties.setMappingSettings(mappingSettings);
        Document mapping = Document.from(new HashMap<>());

        service(properties).applyMappingSettings(mapping);

        assertSame(Boolean.FALSE, mapping.get("date_detection"));
        assertEquals("strict", mapping.get("dynamic"));
        assertTrue(mapping.keySet().containsAll(mappingSettings.keySet()));
    }

    private ElasticIndexService service(DataConfigurationProperties.ElasticsearchProperties properties) {
        return new ElasticIndexService(
                mock(ApplicationContext.class),
                mock(ElasticsearchTemplate.class),
                mock(ElasticsearchClient.class),
                mock(MongoTemplate.class),
                mock(IElasticCaseMappingService.class),
                mock(IElasticTaskMappingService.class),
                mock(IPetriNetService.class),
                properties
        );
    }
}
