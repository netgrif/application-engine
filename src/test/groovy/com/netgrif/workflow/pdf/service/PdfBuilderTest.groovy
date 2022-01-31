package com.netgrif.workflow.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PdfBuilderTest {

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