package com.netgrif.application.engine.elastic.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.netgrif.application.engine.configuration.properties.ElasticsearchProperties;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.domain.ElasticCaseRepository;
import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.elastic.domain.ElasticTaskRepository;
import com.netgrif.application.engine.elastic.serializer.LocalDateTimeJsonDeserializer;
import com.netgrif.application.engine.elastic.serializer.LocalDateTimeJsonSerializer;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticIndexService;
import com.netgrif.application.engine.petrinet.service.PetriNetService;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CloseIndexRequest;
import org.elasticsearch.client.indices.CloseIndexResponse;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.SearchScrollHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.util.CloseableIterator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class ElasticIndexService implements IElasticIndexService {

    private static final String PLACEHOLDERS = "petriNetIndex, caseIndex, taskIndex";

    private final ApplicationContext context;

    private final ElasticsearchRestTemplate elasticsearchTemplate;

    private final RestHighLevelClient elasticsearchClient;

    private final ElasticsearchOperations operations;

    private final ElasticsearchProperties elasticsearchProperties;

    private final ElasticCaseRepository elasticCaseRepository;

    private final ElasticTaskRepository elasticTaskRepository;

    private final PetriNetService petriNetService;

    private final MongoTemplate mongoTemplate;

    private final ElasticCaseMappingService caseMappingService;

    private final ElasticTaskMappingService taskMappingService;

    private final ObjectMapper objectMapper;

    public ElasticIndexService(ApplicationContext context,
                               ElasticsearchRestTemplate elasticsearchTemplate,
                               RestHighLevelClient elasticsearchClient,
                               ElasticsearchOperations operations,
                               ElasticsearchProperties elasticsearchProperties,
                               ElasticCaseRepository elasticCaseRepository,
                               ElasticTaskRepository elasticTaskRepository,
                               PetriNetService petriNetService,
                               MongoTemplate mongoTemplate,
                               ElasticCaseMappingService caseMappingService,
                               ElasticTaskMappingService taskMappingService) {
        this.context = context;
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.elasticsearchClient = elasticsearchClient;
        this.operations = operations;
        this.elasticsearchProperties = elasticsearchProperties;
        this.elasticCaseRepository = elasticCaseRepository;
        this.elasticTaskRepository = elasticTaskRepository;
        this.petriNetService = petriNetService;
        this.mongoTemplate = mongoTemplate;
        this.caseMappingService = caseMappingService;
        this.taskMappingService = taskMappingService;
        this.objectMapper = new ObjectMapper();
        configureMapper();
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
        long numOfPages = ((count / caseBatchSize) + 1);
        log.info("Reindexing {} pages", numOfPages);

        query.cursorBatchSize(caseBatchSize);
        long page = 1, currentBatchSize = 0;
        List<UpdateRequest> caseOperations = new ArrayList<>();
        List<String> caseIds = new ArrayList<>();

        try (CloseableIterator<Case> cursor = mongoTemplate.stream(query, Case.class)) {
            while (cursor.hasNext()) {
                Case aCase = cursor.next();
                prepareCase(aCase);
                ElasticCase elasticCase = caseMappingService.transform(aCase);
                ElasticCase existingCase = null;
                try {
                    existingCase = elasticCaseRepository.findByStringId(aCase.getStringId());
                } catch (InvalidDataAccessApiUsageException ignored) {
                    log.debug("[{}]: Case \"{}\" has duplicates, will reindex.", aCase.getStringId(), aCase.getTitle());
                    elasticCaseRepository.deleteAllByStringId(aCase.getStringId());
                }
                if (existingCase == null) {
                    existingCase = elasticCase;
                } else {
                    existingCase.update(elasticCase);
                }
                prepareCaseBulkOperation(existingCase, caseOperations);
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
        long numOfPages = ((totalSize / taskBatchSize) + 1);

        long page = 1, currentBatchSize = 0;
        List<UpdateRequest> taskOperations = new ArrayList<>();

        try (CloseableIterator<Task> cursor = mongoTemplate.stream(query, Task.class)) {
            while (cursor.hasNext()) {
                Task task = cursor.next();
                ElasticTask elasticTask = taskMappingService.transform(task);
                ElasticTask existingTask = null;
                try {
                    existingTask = elasticTaskRepository.findByStringId(task.getStringId());
                } catch (InvalidDataAccessApiUsageException ignored) {
                    log.debug("[{}]: Task \"{}\" has duplicates, will reindex.", task.getStringId(), task.getTitle());
                    elasticTaskRepository.deleteAllByStringId(task.getStringId());
                }
                if (existingTask == null) {
                    existingTask = elasticTask;
                } else {
                    existingTask.update(elasticTask);
                }
                prepareTaskBulkOperation(existingTask, taskOperations);

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
    private void prepareCaseBulkOperation(ElasticCase doc, List<UpdateRequest> operations) {
        try {
            String json = objectMapper.writeValueAsString(doc);
            UpdateRequest updateRequest = new UpdateRequest()
                    .id(doc.getId() == null ? doc.getStringId() : doc.getId())
                    .doc(json, XContentType.JSON)
                    .upsert(json, XContentType.JSON)
                    .index(elasticsearchProperties.getIndex().get("case"));
            operations.add(updateRequest);
        } catch (Exception e) {
            log.error("Failed to prepare bulk operation for case [{}]: {}", doc.getStringId(), e.getMessage());
        }
    }

    /**
     * Prepares a bulk operation for indexing or updating a task in Elasticsearch.
     *
     * @param doc        transformed ElasticTask object
     * @param operations collection of BulkOperations to add this operation to
     */
    private void prepareTaskBulkOperation(ElasticTask doc, List<UpdateRequest> operations) {
        try {
            String json = objectMapper.writeValueAsString(doc);
            UpdateRequest updateRequest = new UpdateRequest()
                    .id(doc.getId() == null ? doc.getStringId() : doc.getId())
                    .doc(json, XContentType.JSON)
                    .upsert(json, XContentType.JSON)
                    .index(elasticsearchProperties.getIndex().get("task"));
            operations.add(updateRequest);
        } catch (Exception e) {
            log.error("Failed to prepare bulk operation for task [{}]: {}", doc.getStringId(), e.getMessage());
        }
    }

    /**
     * Executes the bulk operations and validates the results, retrying on partial failures.
     *
     * @param operations list of bulk operations to execute
     */
    private void executeAndValidate(List<UpdateRequest> operations) {
        if (operations.isEmpty()) {
            return;
        }

        BulkRequest request = new BulkRequest();
        operations.forEach(request::add);

        try {
            BulkResponse response = elasticsearchClient.bulk(request, RequestOptions.DEFAULT);
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
            List<UpdateRequest> left = operations.subList(0, mid);
            List<UpdateRequest> right = operations.subList(mid, operations.size());

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
        Arrays.stream(response.getItems()).forEach(item -> {
            if (item.getFailure() != null) {
                failedDocuments.put(item.getId(), item.getFailure().getMessage());
            }
        });

        if (!failedDocuments.isEmpty()) {
            throw new ElasticsearchException("Bulk indexing has failures. Use ElasticsearchException.getFailedDocuments() for details [{}]", failedDocuments);
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

    private void configureMapper() {
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeJsonSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeJsonDeserializer());
        objectMapper.registerModule(javaTimeModule);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
