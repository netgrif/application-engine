package com.netgrif.application.engine.pdf.service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileInputStream;

@SpringBootTest
@ActiveProfiles({"test"})
@ExtendWith(SpringExtension.class)
public class PdfUtilsTest {

    @Test
    @Disabled("TODO: 4/6/18 font fix")
    public void fillPdfForm() throws Exception {
        File input = new File("src/test/resources/pdf/test.pdf");
        File xml = new File("src/test/resources/pdf/test.xml");

        File out = PdfUtils.fillPdfForm("target/test_out.pdf", new FileInputStream(input), new FileInputStream(xml));

        assert out != null;
    }

    @Test
    @Disabled("// TODO: 4/6/18 font fix")
    public void fillPdfFormPoisteniePremioveByvanie() throws Exception {
        File input = new File("src/test/resources/pdf/draft.pdf");
        File xml = new File("src/test/resources/pdf/draft.xml");

        File out = PdfUtils.fillPdfForm("target/test_out_premiovebyvanie.pdf", new FileInputStream(input), new FileInputStream(xml));

        assert out != null;
    }

    @Test
    public void mergePdf() {
        File f1 = new File("src/test/resources/pdf/test.pdf");
        File f2 = new File("src/test/resources/pdf/test.pdf");
        File f3 = new File("src/test/resources/pdf/test.pdf");

        File out = PdfUtils.mergePdfFiles("target/test_out_2.pdf", f1, f2, f3);

        assert out != null;
    }

    @Test
    public void encryptPdf() {
        File input = new File("src/test/resources/pdf/test.pdf");

        File output = PdfUtils.encryptPdfFile("target/test_encrypt.pdf", input, "owner", "user");

        assert output != null;
    }
}