package com.netgrif.application.engine.elastic.service.interfaces;

import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IElasticIndexService {

    boolean indexExists(String indexName);

    boolean createIndex(Class<?> clazz, String... placeholders);

    Map<String, Object> prepareAnalysisSettings();

    boolean deleteIndex(Class<?> clazz, String... placeholders);

    boolean putMapping(Class<?> clazz, String... placeholders);

    void applyMappingSettings(Document mapping);

    <T> String index(Class<T> clazz, T source, String... placeholders);

    SearchHits<?> search(Query query, Class<?> clazz, String... placeholders);

    void applySettings(HashMap<String, Object> settingMap, Class<?> clazz);

//    void clearScrollHits(List<String> scrollIds);
}
