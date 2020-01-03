package com.netgrif.workflow.startup


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
    private static final String CASE_MAPPING = '''
    {
       "properties":{
          "author":{
             "type":"long"
          },
          "authorEmail":{
             "type":"keyword"
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
          "creationDate":{
             "type":"long"
          },
          "creationDateSortable": {
              "type": "long"
          },
          "enabledRoles":{
             "type":"keyword"
          },
          "id":{
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
          "stringId":{
             "type":"keyword"
          },
          "taskIds":{
             "type":"keyword"
          },
          "taskMongoIds":{
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
          "titleSortable":{
             "type":"keyword"
          },
          "visualId":{
             "type":"keyword"
          }
       }
    }
    '''

    @Value('${spring.data.elasticsearch.index.task}')
    private String taskIndex
    private static final String TASK_TYPE = "task"
    private static final String TASK_MAPPING = '''
    {
        "properties": {
            "assignPolicy": {
                "type": "keyword"
            },
            "caseColor": {
                "type": "keyword"
            },
            "caseId": {
                "type": "keyword"
            },
            "caseTitle": {
                "type": "text",
                "fields": {
                    "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                    }
                }
            },
            "caseTitleSortable": {
                "type": "keyword"
            },
            "dataFocusPolicy": {
                "type": "keyword"
            },
            "finishPolicy": {
                "type": "keyword"
            },
            "icon": {
                "type": "keyword"
            },
            "priority": {
                "type": "long"
            },
            "processId": {
                "type": "keyword"
            },
            "roles": {
                "type": "keyword"
            },
            "stringId": {
                "type": "keyword"
            },
            "title": {
                "type": "text",
                "fields": {
                    "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                    }
                }
            },
            "titleSortable": {
                "type": "keyword"
            },
            "transactionId": {
                "type": "keyword"
            },
            "transitionId": {
                "type": "keyword"
            }
        }
    }
    '''

    @Autowired
    private ElasticsearchTemplate template

    @Override
    void run(String... args) throws Exception {
        if (drop) {
            log.info("Dropping Elasticsearch database [${url}:${port}/${clusterName}]")
            template.deleteIndex(caseIndex)
            template.createIndex(caseIndex)
            template.deleteIndex(taskIndex)
            template.createIndex(taskIndex)
        }
        log.info("Updating Elasticsearch case mapping [${caseIndex}]")
        template.putMapping(caseIndex, CASE_TYPE, CASE_MAPPING)
        log.info("Updating Elasticsearch task mapping [${taskIndex}]")
        template.putMapping(taskIndex, TASK_TYPE, TASK_MAPPING)
    }
}