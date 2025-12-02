package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.AcknowledgedResponse;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.ErrorCause;
import co.elastic.clients.elasticsearch._types.ErrorResponse;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netgrif.application.engine.configuration.properties.DataConfigurationProperties;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseMappingService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskMappingService;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.elastic.domain.ElasticTask;
import com.netgrif.application.engine.objects.elastic.serializer.LocalDateTimeJsonDeserializer;
import com.netgrif.application.engine.objects.elastic.serializer.LocalDateTimeJsonSerializer;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@Service
public class ElasticIndexService implements IElasticIndexService {

    private static final String PLACEHOLDERS = "petriNetIndex, caseIndex, taskIndex";
    private final ApplicationContext context;
    private final ElasticsearchTemplate elasticsearchTemplate;
    private final ElasticsearchClient elasticsearchClient;
    private final MongoTemplate mongoTemplate;
    private final IElasticCaseMappingService caseMappingService;
    private final IElasticTaskMappingService taskMappingService;
    private final IPetriNetService petriNetService;
    private final DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties;
    private final ObjectMapper objectMapper;

    public ElasticIndexService(ApplicationContext context,
                               ElasticsearchTemplate elasticsearchTemplate,
                               ElasticsearchClient elasticsearchClient,
                               MongoTemplate mongoTemplate,
                               IElasticCaseMappingService caseMappingService,
                               IElasticTaskMappingService taskMappingService,
                               IPetriNetService petriNetService,
                               DataConfigurationProperties.ElasticsearchProperties elasticsearchProperties) {
        this.context = context;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.elasticsearchClient = elasticsearchClient;
        this.mongoTemplate = mongoTemplate;
        this.caseMappingService = caseMappingService;
        this.taskMappingService = taskMappingService;
        this.petriNetService = petriNetService;
        this.elasticsearchProperties = elasticsearchProperties;
        this.objectMapper = new ObjectMapper();
        configureObjectMapper();
    }

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
            OpenRequest request = OpenRequest.of((builder -> builder.index(indexName)));
            OpenResponse execute = elasticsearchTemplate.execute(client -> client.indices().open(request));
            boolean acknowledged = execute.acknowledged();
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
            CloseIndexRequest request = new CloseIndexRequest.Builder().index(indexName).build();
            CloseIndexResponse execute = elasticsearchTemplate.execute(client -> client.indices().close(request));
            boolean acknowledged = execute.acknowledged();
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
            Document mapping = elasticsearchTemplate.indexOps(clazz).createMapping();
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
            PutTemplateRequest request = PutTemplateRequest.of((builder) -> builder
                    .name(name)
            );
            AcknowledgedResponse response = elasticsearchTemplate.execute(client -> client.indices().putTemplate(request));
            return response.acknowledged();
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
            log.error("scroll:", e);
        }
        return null;
    }

    @Override
    public void clearScrollHits(List<String> scrollIds) {
        try {
            elasticsearchTemplate.searchScrollClear(scrollIds);
        } catch (Exception e) {
            log.error("clearScrollHits:", e);
        }
    }

    /**
     * Performs bulk indexing of cases and tasks into Elasticsearch.
     *
     * @param indexAll      if true, indexes all cases and tasks, regardless of modification time
     * @param after         the time after which cases and tasks should be considered for reindexing
     * @param caseBatchSize number of cases to process per batch. If null, defaults from Elasticsearch properties
     * @param taskBatchSize number of tasks to process per batch. If null, defaults from Elasticsearch properties
     */
    @Override
    public void bulkIndex(boolean indexAll, LocalDateTime after, Integer caseBatchSize, Integer taskBatchSize) {
        log.info("Reindexing stale cases: started reindexing after {}", after);
        LocalDateTime now = LocalDateTime.now();

        if (caseBatchSize == null) {
            caseBatchSize = elasticsearchProperties.getBatch().getCaseBatchSize();
        }
        if (taskBatchSize == null) {
            taskBatchSize = elasticsearchProperties.getBatch().getTaskBatchSize();
        }

        org.springframework.data.mongodb.core.query.Query query;
        if (indexAll || after == null) {
            query = org.springframework.data.mongodb.core.query.Query.query(Criteria.where("lastModified").lt(now));
            log.info("Reindexing stale cases: force all");
        } else {
            query = org.springframework.data.mongodb.core.query.Query.query(Criteria.where("lastModified").lt(now).gt(after.minusMinutes(2)));
        }

        long count = mongoTemplate.count(query, Case.class);
        if (count > 0) {
            reindexQueried(query, count, caseBatchSize, taskBatchSize);
        }
        log.info("Reindexing stale cases: end");
    }

    /**
     * Reindexes queried cases and tasks into Elasticsearch in batches.
     *
     * @param count         total number of cases to reindex
     * @param caseBatchSize batch size for cases
     * @param taskBatchSize batch size for tasks
     */
    private void reindexQueried(org.springframework.data.mongodb.core.query.Query query, long count, int caseBatchSize, int taskBatchSize) {
        long numOfPages = Math.max(1, Math.ceilDiv(count, (long) caseBatchSize));
        log.info("Reindexing {} pages", numOfPages);

        query.cursorBatchSize(caseBatchSize);
        long page = 1, currentBatchSize = 0;
        List<BulkOperation> caseOperations = new ArrayList<>();
        List<String> caseIds = new ArrayList<>();

        try (Stream<Case> cursorStream = mongoTemplate.stream(query, Case.class)) {
            Iterator<Case> cursor = cursorStream.iterator();
            while (cursor.hasNext()) {
                Case aCase = cursor.next();
                prepareCase(aCase);
                ElasticCase doc = caseMappingService.transform(aCase);
                prepareCaseBulkOperation(doc, caseOperations);
                caseIds.add(aCase.getStringId());

                if (++currentBatchSize == caseBatchSize || !cursor.hasNext()) {
                    log.info("Reindexing case page {} / {}", page, numOfPages);
                    executeAndValidate(caseOperations);
                    bulkIndexTasks(caseIds, taskBatchSize);
                    caseOperations.clear();
                    caseIds.clear();
                    currentBatchSize = 0;
                    page++;
                }
            }
        }

    }

    /**
     * Reindexes tasks into Elasticsearch in batches corresponding to the provided case IDs.
     *
     * @param caseIds       list of case IDs whose tasks need to be reindexed
     * @param taskBatchSize size of the batch for tasks
     */
    private void bulkIndexTasks(List<String> caseIds, int taskBatchSize) {
        if (caseIds == null || caseIds.isEmpty()) {
            return;
        }
        org.springframework.data.mongodb.core.query.Query query = org.springframework.data.mongodb.core.query.Query.query(Criteria.where("caseId").in(caseIds)).cursorBatchSize(taskBatchSize);
        long totalSize = mongoTemplate.count(query, Task.class);
        long numOfPages = Math.max(1, Math.ceilDiv(totalSize, (long) taskBatchSize));

        long page = 1, currentBatchSize = 0;
        List<BulkOperation> taskOperations = new ArrayList<>();

        try (Stream<Task> cursorStream = mongoTemplate.stream(query, Task.class)) {
            Iterator<Task> cursor = cursorStream.iterator();
            while (cursor.hasNext()) {
                Task task = cursor.next();
                ElasticTask elasticTask = taskMappingService.transform(task);
                prepareTaskBulkOperation(elasticTask, taskOperations);

                if (++currentBatchSize == taskBatchSize || !cursor.hasNext()) {
                    log.info("Reindexing task page {} / {}", page, numOfPages);
                    executeAndValidate(taskOperations);
                    taskOperations.clear();
                    currentBatchSize = 0;
                    page++;
                }
            }
        }
    }

    /**
     * Prepares the case object by ensuring necessary dependencies and last modified timestamp are set.
     *
     * @param useCase case object to prepare
     */
    private void prepareCase(Case useCase) {
        if (useCase.getPetriNet() == null) {
            useCase.setPetriNet(petriNetService.get(useCase.getPetriNetObjectId()));
        }
        if (useCase.getLastModified() == null) {
            useCase.setLastModified(LocalDateTime.now());
        }
    }

    /**
     * Prepares a bulk operation for indexing or updating a case in Elasticsearch.
     *
     * @param doc        transformed ElasticCase object
     * @param operations collection of BulkOperations to add this operation to
     */
    private void prepareCaseBulkOperation(ElasticCase doc, List<BulkOperation> operations) {
        try {
            operations.add(BulkOperation.of(op -> op
                    .update(u -> u
                            .index(elasticsearchProperties.getIndex().get("case"))
                            .id(doc.getId())
                            .action(a -> a
                                    .doc(elasticsearchTemplate.getElasticsearchConverter().mapObject(doc))
                                    .docAsUpsert(true)
                            )
                    )));
        } catch (Exception e) {
            log.error("Failed to prepare bulk operation for case [{}]: {}", doc.getId(), e.getMessage());
        }
    }

    /**
     * Prepares a bulk operation for indexing or updating a task in Elasticsearch.
     *
     * @param doc        transformed ElasticTask object
     * @param operations collection of BulkOperations to add this operation to
     */
    private void prepareTaskBulkOperation(ElasticTask doc, List<BulkOperation> operations) {
        try {
            operations.add(BulkOperation.of(op -> op
                    .update(u -> u
                            .index(elasticsearchProperties.getIndex().get("task"))
                            .id(doc.getId())
                            .action(a -> a
                                    .doc(elasticsearchTemplate.getElasticsearchConverter().mapObject(doc))
                                    .docAsUpsert(true)
                            )
                    ))
            );
        } catch (Exception e) {
            log.error("Failed to prepare bulk operation for task [{}]: {}", doc.getId(), e.getMessage());
        }
    }

    /**
     * Executes the bulk operations and validates the results, retrying on partial failures.
     *
     * @param operations list of bulk operations to execute
     */
    private void executeAndValidate(List<BulkOperation> operations) {
        if (operations.isEmpty()) {
            return;
        }

        BulkRequest.Builder builder = new BulkRequest.Builder();
        builder.operations(operations);

        try {
            BulkResponse response = elasticsearchClient.bulk(builder.build());
            checkForBulkUpdateFailure(response);
            log.info("Batch indexed successfully with {} ops", operations.size());
        } catch (ElasticsearchException e) {
            log.warn("Failed for {} ops to index bulk {}", operations.size(), e.getMessage(), e);

            if (operations.size() == 1) {
                log.error("Single operation failed. Skipping. {}", operations.get(0), e);
                return;
            }

            log.warn("Dividing the requirement.");

            int mid = operations.size() / 2;
            List<BulkOperation> left = operations.subList(0, mid);
            List<BulkOperation> right = operations.subList(mid, operations.size());

            executeAndValidate(new ArrayList<>(left));
            executeAndValidate(new ArrayList<>(right));
        } catch (Exception e) {
            log.error("Failed to index bulk: {}", e.getMessage(), e);
        }
    }

    /**
     * Checks the results of a bulk indexing operation for failures.
     *
     * @param response the BulkResponse from Elasticsearch
     * @throws ElasticsearchException if there are failures in the bulk response
     */
    private void checkForBulkUpdateFailure(BulkResponse response) {
        Map<String, String> failedDocuments = new HashMap<>();
        response.items().forEach(item -> {
            if (item.error() != null) {
                failedDocuments.put(item.id(), item.error().reason());
            }
        });

        if (!failedDocuments.isEmpty()) {
            String message = "Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for details [" + failedDocuments.values() + "]";
            throw new ElasticsearchException(message,
                    ErrorResponse.of(builder -> builder
                            .error(ErrorCause.of(errorCauseBuilder -> errorCauseBuilder.reason(message)))
                            .status(response.items().getFirst().status())));
        }
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

    private void configureObjectMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

}
