package com.netgrif.application.engine.pdf.service

import com.netgrif.application.engine.business.qr.IQrService
import com.netgrif.application.engine.business.qr.QrCode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(SpringExtension.class)
@ActiveProfiles(["test"])
@SpringBootTest
class QrCodeTest {

    @Autowired
    private IQrService service

    @Test
    void qrPdfTest() {
        def qr = new QrCode("qrcode.jpg", '{"caseId":"5c6680281efd8c101c4be344"}')
        def fileOpt = service.generateToFile(qr)

        PdfBuilder builder = PdfBuilder.builder()
        builder.load("src/test/resources/pdf/qr_form.pdf")
        builder.includeImage(fileOpt.get().path, 0, 50, 0, 400, 400)
        builder.save("storage/qr_form.pdf")

        assert Files.exists(Paths.get("storage/qr_form.pdf"))
    }

    @Test
    void logoQrCodeTest() {
        def qr = new QrCode("qrcode_logo.png", '{"caseId":"5c6680281efd8c101c4be344"}')
        qr.setWidth(153 * 4)
        qr.setHeight(153 * 4)
        def fileOpt = service.generateWithLogo(qr, new FileInputStream("src/test/resources/img/icon-512x512.png"))

        assert fileOpt.isPresent() && Files.exists(Paths.get("qrcode_logo.png"))
    }
}