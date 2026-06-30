package com.netgrif.application.engine.configuration.properties;

import org.junit.jupiter.api.Test;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataConfigurationPropertiesTest {

    @Test
    void mongoPropertiesUseTopLevelDefaultsWhenUnset() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        properties.setDrop(true);
        properties.setDatabaseName("workflow");
        properties.getMongodb().setDrop(null);
        properties.getMongodb().setDatabase(null);

        DataConfigurationProperties.MongoProperties mongo = properties.mongoProperties();

        assertTrue(mongo.getDrop());
        assertEquals("workflow", mongo.getDatabase());
    }

    @Test
    void mongoPropertiesKeepExplicitValues() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        properties.setDrop(true);
        properties.setDatabaseName("workflow");
        properties.getMongodb().setDrop(false);
        properties.getMongodb().setDatabase("custom");

        DataConfigurationProperties.MongoProperties mongo = properties.mongoProperties();

        assertFalse(mongo.getDrop());
        assertEquals("custom", mongo.getDatabase());
    }

    @Test
    void elasticsearchPropertiesCreateDefaultIndexesFromDatabaseName() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        properties.setDrop(true);
        properties.setDatabaseName("workflow");
        properties.getElasticsearch().setDrop(null);
        properties.getElasticsearch().setIndex(null);

        DataConfigurationProperties.ElasticsearchProperties elasticsearch = properties.elasticsearchProperties();

        assertTrue(elasticsearch.getDrop());
        assertEquals("workflow_petrinet", elasticsearch.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.PETRI_NET_INDEX));
        assertEquals("workflow_case", elasticsearch.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX));
        assertEquals("workflow_task", elasticsearch.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.TASK_INDEX));
    }

    @Test
    void elasticsearchPropertiesKeepExplicitIndexMap() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        Map<String, String> configuredIndexes = new HashMap<>();
        configuredIndexes.put(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX, "case_v2");
        properties.getElasticsearch().setIndex(configuredIndexes);

        DataConfigurationProperties.ElasticsearchProperties elasticsearch = properties.elasticsearchProperties();

        assertSame(configuredIndexes, elasticsearch.getIndex());
        assertEquals("case_v2", elasticsearch.getIndex().get(DataConfigurationProperties.ElasticsearchProperties.CASE_INDEX));
    }

    @Test
    void redisPropertiesCreateSessionAndNamespaceFromDatabaseName() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        properties.setDatabaseName("workflow");
        properties.getRedis().setSession(null);

        DataConfigurationProperties.RedisProperties redis = properties.redisProperties();

        assertNotNull(redis.getSession());
        assertEquals(RedisIndexedSessionRepository.DEFAULT_NAMESPACE + ":workflow", redis.getSession().getNamespace());
    }

    @Test
    void redisPropertiesReplaceDefaultNamespaceWithApplicationNamespace() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        properties.setDatabaseName("engine");
        properties.getRedis().getSession().setNamespace(RedisIndexedSessionRepository.DEFAULT_NAMESPACE);

        DataConfigurationProperties.RedisProperties redis = properties.redisProperties();

        assertEquals(RedisIndexedSessionRepository.DEFAULT_NAMESPACE + ":engine", redis.getSession().getNamespace());
    }

    @Test
    void redisPropertiesKeepCustomNamespace() {
        DataConfigurationProperties properties = new DataConfigurationProperties();
        DataConfigurationProperties.RedisProperties.EngineRedisSessionProperties session =
                new DataConfigurationProperties.RedisProperties.EngineRedisSessionProperties();
        session.setNamespace("custom:sessions");
        properties.getRedis().setSession(session);

        DataConfigurationProperties.RedisProperties redis = properties.redisProperties();

        assertSame(session, redis.getSession());
        assertEquals("custom:sessions", redis.getSession().getNamespace());
    }

    @Test
    void elasticsearchInitAddsDefaultsAndAnalyzerFiltersWithoutOverwritingConfiguredValues() {
        DataConfigurationProperties.ElasticsearchProperties elasticsearch =
                new DataConfigurationProperties.ElasticsearchProperties();
        elasticsearch.setAnalyzerEnabled(true);
        elasticsearch.getIndexSettings().put("max_result_window", 10);
        elasticsearch.getMappingSettings().put("date_detection", true);

        elasticsearch.init();

        assertEquals(10, elasticsearch.getIndexSettings().get("max_result_window"));
        assertEquals(true, elasticsearch.getMappingSettings().get("date_detection"));
        assertEquals(List.of("lowercase", "asciifolding", "keyword_repeat", "unique"), elasticsearch.getDefaultFilters());
        assertEquals(List.of("lowercase", "asciifolding", "unique"), elasticsearch.getDefaultSearchFilters());
    }

    @Test
    void elasticsearchInitAddsBaseSettingsWhenMissing() {
        DataConfigurationProperties.ElasticsearchProperties elasticsearch =
                new DataConfigurationProperties.ElasticsearchProperties();

        elasticsearch.init();

        assertEquals(10000000, elasticsearch.getIndexSettings().get("max_result_window"));
        assertEquals(false, elasticsearch.getMappingSettings().get("date_detection"));
        assertTrue(elasticsearch.getDefaultFilters().isEmpty());
        assertTrue(elasticsearch.getDefaultSearchFilters().isEmpty());
    }

    @Test
    void classSpecificSettingsReturnConfiguredSettingsOrEmptyMap() {
        DataConfigurationProperties.ElasticsearchProperties elasticsearch =
                new DataConfigurationProperties.ElasticsearchProperties();
        Map<String, Object> configured = Map.of("number_of_shards", 1);
        elasticsearch.getClassSpecificIndexSettings().put("Case", configured);

        assertSame(configured, elasticsearch.getClassSpecificSettings("Case"));
        assertTrue(elasticsearch.getClassSpecificSettings("Missing").isEmpty());
    }
}
