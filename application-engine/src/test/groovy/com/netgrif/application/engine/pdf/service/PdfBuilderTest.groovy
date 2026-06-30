package com.netgrif.application.engine.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.nio.file.Path

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class PdfBuilderTest {

    @TempDir
    Path tempDir

    @Test
    void loadMultiple() {
        File draft = createPdf("draft.pdf", 4)
        File offer = createPdf("offer.pdf", 4)
        File test = createPdf("test.pdf", 4)

        PDDocument document = PdfBuilder.builder()
                .load(
                        draft.absolutePath,
                        offer.absolutePath,
                        test.absolutePath
                )
                .build()

        try {
            assert document.numberOfPages == 12
        } finally {
            document.close()
        }
    }

    private File createPdf(String fileName, int numberOfPages) {
        File output = tempDir.resolve(fileName).toFile()

        PDDocument document = new PDDocument()
        try {
            numberOfPages.times {
                document.addPage(new PDPage())
            }

            document.save(output)
        } finally {
            document.close()
        }

        return output
    }
}