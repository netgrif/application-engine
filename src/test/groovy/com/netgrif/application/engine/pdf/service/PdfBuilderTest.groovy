package com.netgrif.application.engine.pdf.service

import com.netgrif.application.engine.EngineTest
import groovy.transform.CompileStatic
import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
@CompileStatic
class PdfBuilderTest extends EngineTest {

    @Test
    void loadMultiple() {
        PDDocument document = PdfBuilder.builder()
                .load("src/test/resources/pdf/draft.pdf",
                        "src/test/resources/pdf/offer.pdf",
                        "src/test/resources/pdf/test.pdf")
                .build()

        assert document.numberOfPages > 10
    }
}