package com.netgrif.workflow

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

import java.util.concurrent.ThreadLocalRandom

@Component
@Profile("!test")
class StartRunner  implements CommandLineRunner{

    @Autowired
    private MongoTemplate mongoTemplate
    @Autowired
    private InsuranceImporter insuranceImporter
    @Autowired
    private XlsImporter xlsImporter
    @Autowired
    private SuperCreator superCreator

    @Override
    void run(String... strings) throws Exception {
        mongoTemplate.getDb().dropDatabase()

        insuranceImporter.run(strings)

        //xlsImporter.run(strings)

        superCreator.run(strings)
    }

    static String randomColor() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 7)
        switch (randomNum) {
            case 0:
                return "color-fg-primary-500"
            case 1:
                return "color-fg-indigo-500"
            case 2:
                return "color-fg-deep-purple-500"
            case 3:
                return "color-fg-lime-500"
            case 4:
                return "color-fg-amber-500"
            case 5:
                return "color-fg-deep-orange-500"
            case 6:
                return "color-fg-blue-grey-500"
            case 7:
                return "color-fg-brown-500"
            default:
                return "color-fg-primary-500"
        }
    }
}
