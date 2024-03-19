package com.netgrif.application.engine.elastic.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.UriNode;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.util.List;

public interface IElasticIndexService {

    boolean indexExists(String indexName);

    boolean createIndex(Class<?> clazz, String... placeholders);

    boolean deleteIndex(Class<?> clazz, String... placeholders);

    boolean closeIndex(Class<?> clazz, String... placeholders);

    boolean openIndex(Class<?> clazz, String... placeholders);

    boolean putMapping(Class<?> clazz, String... placeholders);

    boolean putTemplate(String name, String source);

    <T> String index(Class<T> clazz, T source, String... placeholders);

    boolean bulkIndex(List<?> list, Class<?> clazz, String... placeholders);

    SearchScrollHits<?> scrollFirst(Query query, Class<?> clazz, String... placeholders);

    SearchScrollHits<?> scroll(String scrollId, Class<?> clazz, String... placeholders);

    SearchHits<?> search(Query query, Class<?> clazz, String... placeholders);

    void createIndex(UriNode node);

    void createIndex(String index);

    void deleteIndex(UriNode node);

    void deleteIndex(String index);

    String getDefaultIndex();

    String getIndex(String uriNodeId);

    String getIndex(UriNode node);

    String getIndexByMenuItemId(String menuItemId);

    List<String> getAllDynamicIndexes();

    List<String> getAllIndexes();

    void evictAllCaches();

    void evictCache(String uriNodeId);

    void evictCacheForMenuItem(String menuItemId);

    String makeName(UriNode node);

    String makeName(String nodeName);
}
