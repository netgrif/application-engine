package com.netgrif.application.engine.pdf.service

import com.netgrif.application.engine.business.qr.IQrService
import com.netgrif.application.engine.business.qr.QrCode
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import javax.imageio.ImageIO
import java.awt.Color
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class QrCodeTest {

    @Autowired
    private IQrService service

    @TempDir
    Path tempDir

    @Test
    void qrPdfTest() {
        File inputPdf = createCleanPdf("qr-input.pdf")
        File outputPdf = tempDir.resolve("qr-output.pdf").toFile()

        def qr = new QrCode(
                tempDir.resolve("qrcode.jpg").toString(),
                '{"caseId":"test-case-id"}'
        )

        def fileOpt = service.generateToFile(qr)

        assert fileOpt.isPresent()
        assert fileOpt.get().exists()

        PdfBuilder builder = PdfBuilder.builder()
        builder.load(inputPdf.absolutePath)
        builder.includeImage(fileOpt.get().absolutePath, 0, 50, 50, 200, 200)
        builder.save(outputPdf.absolutePath)

        assert outputPdf.exists()
        assert outputPdf.length() > 0
    }

    @Test
    void logoQrCodeTest() {
        File logo = createCleanLogo("test-logo.png")
        File outputQr = tempDir.resolve("qrcode-logo.png").toFile()

        def qr = new QrCode(
                outputQr.absolutePath,
                '{"caseId":"test-case-id"}'
        )
        qr.setWidth(153 * 4)
        qr.setHeight(153 * 4)

        def fileOpt
        logo.withInputStream { inputStream ->
            fileOpt = service.generateWithLogo(qr, inputStream)
        }

        assert fileOpt.isPresent()
        assert fileOpt.get().exists()
        assert outputQr.exists()
        assert outputQr.length() > 0
    }

    private File createCleanPdf(String fileName) {
        File output = tempDir.resolve(fileName).toFile()

        PDDocument document = new PDDocument()
        try {
            document.addPage(new PDPage())
            document.save(output)
        } finally {
            document.close()
        }

        return output
    }

    private File createCleanLogo(String fileName) {
        File output = tempDir.resolve(fileName).toFile()

        BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB)

        def graphics = image.createGraphics()
        try {
            graphics.setColor(Color.WHITE)
            graphics.fillRect(0, 0, 128, 128)
            graphics.setColor(Color.BLACK)
            graphics.fillRect(32, 32, 64, 64)
        } finally {
            graphics.dispose()
        }

        ImageIO.write(image, "png", output)

        return output
    }
}