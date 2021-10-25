package com.netgrif.workflow.importer


import com.netgrif.workflow.startup.ImportHelper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class ImporterTestGroovy {

    @Autowired
    private ImportHelper importHelper

    public static final String FILE_NAME = "importer_upsert.xml"
    public static final String IDENTIFIER = "importer_upsert"

    @Test
    void upsertTest() {
        def net = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert net.present

        def upserted = importHelper.upsertNet(FILE_NAME, IDENTIFIER)
        assert upserted.present

        assert upserted.get().creationDate == net.get().creationDate
    }
}
