package com.netgrif.application.engine.elastic.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CloseIndexResponse;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;

@Slf4j
@Service
public class ElasticIndexService implements IElasticIndexService {

    private static final String PLACEHOLDERS = "petriNetIndex, caseIndex, taskIndex";

    @Autowired
    private ApplicationContext context;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchTemplate;

    @Autowired
    private ElasticsearchOperations operations;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Override
    public boolean indexExists(String indexName) {
        try {
            return elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName)).exists();
        } catch (Exception e) {
            log.error("indexExists:", e);
            return false;
        }
    }

    @Override
    public <T> String index(Class<T> clazz, T source, String... placeholders) {
        String indexName = getIndexName(clazz, placeholders);
        return elasticsearchTemplate.index(new IndexQueryBuilder().withId(getIdFromSource(source))
                .withObject(source).build(), IndexCoordinates.of(indexName));
    }


    @Override
    public boolean bulkIndex(List<?> list, Class<?> clazz, String... placeholders) {
        String indexName = getIndexName(clazz, placeholders);
        try {
            if (list != null && !list.isEmpty()) {
                List<IndexQuery> indexQueries = new ArrayList<>();
                list.forEach(source ->
                        indexQueries.add(new IndexQueryBuilder().withId(getIdFromSource(source)).withObject(source).build()));
                elasticsearchTemplate.bulkIndex(indexQueries, IndexCoordinates.of(indexName));
            }
        } catch (Exception e) {
            log.error("bulkIndex:", e);
            return false;
        }
        return true;
    }

    @Override
    public boolean createIndex(Class<?> clazz, String... placeholders) {
        try {
            String indexName = getIndexName(clazz, placeholders);
            if (!this.indexExists(indexName)) {
                // https://www.elastic.co/guide/en/elasticsearch/reference/current/index-modules.html
                HashMap<String, Object> settingMap = new HashMap<>();
                applySettings(settingMap, clazz);
                settingMap.put("number_of_shards", getShardsFromClass(clazz));
                settingMap.put("number_of_replicas", getReplicasFromClass(clazz));

                Map<String, Object> analysisSettings = prepareAnalysisSettings();
                if (analysisSettings != null) {
                    settingMap.put("analysis", analysisSettings);
                }

                Document settings = Document.from(settingMap);
                log.info("Creating new index - {} ", indexName);
                return elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName)).create(settings);
            } else {
                log.info("This index {} already exists", indexName);
            }
        } catch (Exception e) {
            log.error("createIndex:", e);
        }
        return false;
    }

    @Override
    public void applySettings(HashMap<String, Object> settingMap, Class<?> clazz) {
        Map<String, Object> settings = elasticsearchProperties.getIndexSettings();
        if ((settings != null || settingMap != null) && !settings.isEmpty()) {
            settingMap.putAll(settings);
        }

        String className = clazz.getSimpleName();
        Map<String, Object> classSpecificSettings = elasticsearchProperties.getClassSpecificSettings(className);
        if (classSpecificSettings != null && !classSpecificSettings.isEmpty()) {
            settingMap.putAll(classSpecificSettings);
        }
    }

    @Override
    public Map<String, Object> prepareAnalysisSettings() {
        if (!elasticsearchProperties.isAnalyzerEnabled()) {
            return null;
        }
        if (elasticsearchProperties.getAnalyzerPathFile() != null) {
            Map<String, Object> fileSettings = parseAnalysisSettings();
            if (fileSettings != null) {
                return fileSettings;
            } else {
                log.error("Failed to load analysis settings from file, falling back to default settings.");
            }
        }

        Map<String, Object> defaultAnalyzer = new HashMap<>();
        defaultAnalyzer.put("type", "custom");
        defaultAnalyzer.put("tokenizer", "standard");
        defaultAnalyzer.put("filter", elasticsearchProperties.getDefaultFilters());
        defaultAnalyzer.put("char_filter", List.of("html_strip"));

        Map<String, Object> defaultSearchAnalyzer = new HashMap<>(defaultAnalyzer);
        defaultSearchAnalyzer.put("filter", elasticsearchProperties.getDefaultSearchFilters());

        Map<String, Object> analyzers = new HashMap<>();
        analyzers.put("default", defaultAnalyzer);
        analyzers.put("default_search", defaultSearchAnalyzer);

        Map<String, Object> analysisSettings = new HashMap<>();
        analysisSettings.put("analyzer", analyzers);

        return analysisSettings;
    }

    protected Map<String, Object> parseAnalysisSettings() {
        ObjectMapper objectMapper = new ObjectMapper();
        Resource resource = elasticsearchProperties.getAnalyzerPathFile();

        try (InputStream inputStream = resource.getInputStream()) {
            return objectMapper.readValue(inputStream, HashMap.class);
        } catch (Exception e) {
            log.error("Failed to parse settings file", e);
            return null;
        }
    }

    @Override
    public boolean deleteIndex(Class<?> clazz, String... placeholders) {
        try {
            String indexName = getIndexName(clazz, placeholders);
            if (this.indexExists(indexName)) {
                log.warn("Index: " + indexName + " has been deleted!");
                return elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName)).delete();
            } else {
                log.warn("Index: " + indexName + " not found!");
            }
        } catch (Exception e) {
            log.error("deleteIndex: ", e);
        }
        return false;
    }

    @Override
    public boolean openIndex(Class<?> clazz, String... placeholders) {
        try {
            String indexName = getIndexName(clazz, placeholders);
            OpenIndexRequest request = new OpenIndexRequest(indexName);
            OpenIndexResponse execute = elasticsearchTemplate.execute(client -> client.indices().open(request, RequestOptions.DEFAULT));
            boolean acknowledged = execute.isAcknowledged();
            if (acknowledged) {
                log.info("Open index {} success", indexName);
            } else {
                log.info("Open index {} fail", indexName);
            }
            return acknowledged;
        } catch (Exception e) {
            log.error("DeleteIndex:", e);
            return false;
        }
    }

    @Override
    public boolean closeIndex(Class<?> clazz, String... placeholders) {
        try {
            String indexName = getIndexName(clazz, placeholders);
            CloseIndexRequest request = new CloseIndexRequest(indexName);
            CloseIndexResponse execute = elasticsearchTemplate.execute(client -> client.indices().close(request, RequestOptions.DEFAULT));
            boolean acknowledged = execute.isAcknowledged();
            if (acknowledged) {
                log.info("Close index {} success", indexName);
            } else {
                log.info("Close index {} fail", indexName);
            }
            return acknowledged;
        } catch (Exception e) {
            log.error("deleteIndex:", e);
            return false;
        }
    }

    @Override
    public SearchHits<?> search(Query query, Class<?> clazz, String... placeholders) {
        try {
            String indexName = getIndexName(clazz, placeholders);
            return elasticsearchTemplate.search(query, clazz, IndexCoordinates.of(indexName));
        } catch (Exception e) {
            log.error("scrollFirst:", e);
        }
        return null;
    }

    @Override
    public boolean putMapping(Class<?> clazz, String... placeholders) {
        try {
            String indexName = getIndexName(clazz, placeholders);
            Document mapping = operations.indexOps(clazz).createMapping();
            applyMappingSettings(mapping);
            return elasticsearchTemplate.indexOps(IndexCoordinates.of(indexName)).putMapping(mapping);
        } catch (Exception e) {
            log.error("Error in putMapping:", e);
            return false;
        }
    }

    @Override
    public void applyMappingSettings(Document mapping) {
        Map<String, Object> settings = elasticsearchProperties.getMappingSettings();
        if (settings != null) {
            settings.forEach(mapping::put);
        }
    }

    @Override
    public boolean putTemplate(String name, String source) {
        try {
            PutIndexTemplateRequest builder = new PutIndexTemplateRequest(name);
            builder.source(source, XContentType.JSON);
            AcknowledgedResponse execute = elasticsearchTemplate.execute(client -> client.indices().putTemplate(builder, RequestOptions.DEFAULT));
            return execute.isAcknowledged();
        } catch (Exception e) {
            log.error("putTemplate:", e);
            return false;
        }
    }

    @Override
    public SearchScrollHits<?> scrollFirst(Query query, Class<?> clazz, String... placeholders) {
        String indexName = getIndexName(clazz, placeholders);
        try {
            return elasticsearchTemplate.searchScrollStart(60000, query, clazz, IndexCoordinates.of(indexName));
        } catch (Exception e) {
            log.error("scrollFirst: ", e);
        }
        return null;
    }

    @Override
    public SearchScrollHits<?> scroll(String scrollId, Class<?> clazz, String... placeholders) {
        String indexName = getIndexName(clazz, placeholders);
        try {
            return elasticsearchTemplate.searchScrollContinue(scrollId, 60000, clazz, IndexCoordinates.of(indexName));
        } catch (Exception e) {
            log.error("scrollFirst:", e);
        }
        return null;
    }

    private String getIdFromSource(Object source) {
        if (source == null) {
            return null;
        } else {
            Field[] fields = source.getClass().getDeclaredFields();
            Field[] copyFields = fields;
            int fieldLength = fields.length;

            for (int i = 0; i < fieldLength; ++i) {
                Field field = copyFields[i];
                if (field.isAnnotationPresent(Id.class)) {
                    try {
                        field.setAccessible(true);
                        Object name = field.get(source);
                        return name == null ? null : name.toString();
                    } catch (IllegalAccessException e) {
                        log.error(e.toString());
                    }
                }
            }
            return null;
        }
    }

    private String getBeanName(String source) {
        try {
            return context.getBean(source).toString();
        } catch (Exception e) {
            log.error("getBeanName", e);
        }
        return null;
    }

    private String getIndexFromClass(Class<?> source) {
        try {
            String indexName = source.getAnnotation(org.springframework.data.elasticsearch.annotations.Document.class).indexName();
            if (indexName.contains("#{@")) {
                return getBeanName(indexName.substring(3, indexName.length() - 1));
            } else {
                return indexName;
            }
        } catch (Exception e) {
            log.error("getIndexFromClass", e);
        }
        return null;
    }

    private long getShardsFromClass(Class<?> source) {
        long shards = 1;
        if (source.getAnnotation(Setting.class) != null) {
            shards = source.getAnnotation(Setting.class).shards();
        }
        return shards > 1 ? shards : 1;
    }

    private long getReplicasFromClass(Class<?> source) {
        long replicas = 1;
        if (source.getAnnotation(Setting.class) != null) {
            replicas = source.getAnnotation(Setting.class).replicas();
        }
        return replicas > 1 ? replicas : 1;
    }

    private String getIndexName(Class<?> clazz, String... placeholders) {
        String indexName = getIndexFromClass(clazz);
        Assert.notNull(indexName, "indexName must not be null");
        if (indexName.contains(PLACEHOLDERS)) {  // TODO: buducnost
            Assert.notEmpty(placeholders, "placeholders must not be null");
            indexName = String.format(indexName, placeholders);
        }
        return indexName;
    }

}
