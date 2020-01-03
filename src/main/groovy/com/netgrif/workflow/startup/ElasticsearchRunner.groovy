package com.netgrif.workflow.startup

import com.netgrif.workflow.elastic.domain.ElasticTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate
import org.springframework.stereotype.Component

@Component
class ElasticsearchRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchRunner)

    @Value('${spring.data.elasticsearch.drop}')
    private boolean drop

    @Value('${spring.data.elasticsearch.cluster-name}')
    private String clusterName

    @Value('${spring.data.elasticsearch.url}')
    private String url

    @Value('${spring.data.elasticsearch.port}')
    private int port

    @Value('${spring.data.elasticsearch.index.case}')
    private String caseIndex
    private static final String CASE_TYPE = "case"
    private static final String CASE_MAPPING = """
    {
       "properties":{
          "id":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "version": {
            "type":"long"
          },
          "lastModified": {
            "type":"long"
          },
          "stringId":{
             "type":"keyword"
          },
          "visualId":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "processIdentifier":{
             "type":"keyword"
          },
          "processId":{
             "type":"keyword"
          },
          "title":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "creationDate":{
             "type":"date"
          },
          "author":{
             "type":"long"
          },
          "authorName":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "authorEmail":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "dataSet":{
            "type":"join",
            "relations": {
                "${CASE_TYPE}": "${DATA_TYPE}"
            }
          },
          "taskIds":{
             "type":"keyword"
          },
          "taskMongoIds":{
             "type":"keyword"
          },
          "enabledRoles":{
             "type":"keyword"
          }
       }
    }
    """

    @Value('${spring.data.elasticsearch.index.task}')
    private String taskIndex

    @Value('${spring.data.elasticsearch.index.data.prefix}')
    private String dataIndexPrefix
    private static final String DATA_TYPE = "data"

    @Value('${spring.data.elasticsearch.index.data.text-suffix}')
    private String textDataIndexSuffix
    private static final String TEXT_DATA_MAPPING = """
    {
        "properties": {
            "id":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "version": {
            "type":"long"
          },
          "fulltext": {
            "type":"text"
          },
          "searchable": {
            "type":"text"
          },
          "sortable": {
            "type":"keyword"
          },
          "dataSet":{
            "type":"join",
            "relations": {
                "${CASE_TYPE}": "${DATA_TYPE}"
            }
          }
        }
    }
    """

    @Value('${spring.data.elasticsearch.index.data.number-suffix}')
    private String numberDataIndexSuffix
    private static final String NUMBER_DATA_MAPPING = """
    {
        "properties": {
            "id":{
             "type":"text",
             "fields":{
                "keyword":{
                   "type":"keyword",
                   "ignore_above":256
                }
             }
          },
          "version": {
            "type":"long"
          },
          "fulltext": {
            "type":"text"
          },
          "searchable": {
            "type":"double"
          },
          "sortable": {
            "type":"double"
          },
          "dataSet":{
            "type":"join",
            "relations": {
                "${CASE_TYPE}": "${DATA_TYPE}"
            }
          }
        }
    }
    """

    @Autowired
    private ElasticsearchTemplate template

    @Override
    void run(String... args) throws Exception {
        Map<String, String> suffixes = ["${textDataIndexSuffix}":TEXT_DATA_MAPPING, "${numberDataIndexSuffix}":NUMBER_DATA_MAPPING]

        if (drop) {
            log.info("Dropping Elasticsearch database [${url}:${port}/${clusterName}]")
            template.deleteIndex(caseIndex)
            template.createIndex(caseIndex)
            for(String suffix in suffixes.keySet()) {
                template.deleteIndex(dataIndexPrefix+suffix)
                template.createIndex(dataIndexPrefix+suffix)
            }
            template.deleteIndex(ElasticTask.class)
            template.createIndex(ElasticTask.class)
        }
        log.info("Updating Elasticsearch case mapping [${caseIndex}]")
        template.putMapping(caseIndex, CASE_TYPE, CASE_MAPPING)
        log.info("Updating Elasticsearch task mapping [${taskIndex}]")
        template.putMapping(ElasticTask.class)
        for(Map.Entry<String, String> entry in suffixes.entrySet()) {
            log.info("Updating Elasticsearch data mapping [${dataIndexPrefix+entry.getKey()}]")
            template.putMapping(dataIndexPrefix+entry.getKey(), DATA_TYPE, entry.getValue())
        }
    }
}