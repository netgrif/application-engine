package com.netgrif.workflow.startup

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate
import org.springframework.data.elasticsearch.core.IndexOperations
import org.springframework.data.elasticsearch.core.document.Document
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates
import org.springframework.stereotype.Component

@Component
class ElasticsearchRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ElasticsearchRunner)

    @Value('${spring.data.elasticsearch.drop}')
    private boolean drop

    @Autowired
    private ElasticsearchRestTemplate elasticsearch

    @Value('${spring.data.elasticsearch.index.case}')
    private String CASE_INDEX
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
    private String TASK_INDEX
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
            "users": {
                "type": "long"
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

    @Override
    void run(String... args) throws Exception {
//        if (drop) {
            log.info "Creating Elasticsearch mapping"

            IndexOperations caseOps = elasticsearch.indexOps(IndexCoordinates.of(CASE_INDEX))
            assert caseOps.delete()
            assert caseOps.create()
            assert caseOps.putMapping(Document.parse(CASE_MAPPING))

            IndexOperations taskOps = elasticsearch.indexOps(IndexCoordinates.of(TASK_INDEX))
            assert taskOps.delete()
            assert taskOps.create()
            assert taskOps.putMapping(Document.parse(TASK_MAPPING))
//        } else {
//            log.info "Elasticsearch mapping exists"
//        }
    }
}