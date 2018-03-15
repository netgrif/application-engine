package com.netgrif.workflow.startup

import com.netgrif.workflow.orgstructure.domain.GroupRepository
import com.netgrif.workflow.orgstructure.domain.MemberRepository
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class Neo4jRunner extends AbstractOrderedCommandLineRunner {

    private static final Logger log = Logger.getLogger(MongoDbRunner)

    @Autowired
    private MemberRepository memberRepository

    @Autowired
    private GroupRepository groupRepository

    @Value('${spring.data.neo4j.uri}')
    private String URL

    @Value('${spring.data.neo4j.drop}')
    private boolean dropDatabase

    @Override
    void run(String... strings) throws Exception {
        if (dropDatabase) {
            log.info("Deleting all nodes from Neo4j on $URL")
            memberRepository.deleteAll()
            groupRepository.deleteAll()
        }
    }
}