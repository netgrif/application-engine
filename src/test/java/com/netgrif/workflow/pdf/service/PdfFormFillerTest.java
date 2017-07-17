package com.netgrif.workflow.pdf.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;

@SpringBootTest
@ActiveProfiles({"test"})
@RunWith(SpringRunner.class)
public class PdfFormFillerTest {

    @Test
    public void fillPdfForm() throws Exception {
        File input = new File("src/test/resources/pdf/test.pdf");
        File xml = new File("src/test/resources/pdf/test.xml");

        File out = PdfFormFiller.fillPdfForm("test_out.pdf", new FileInputStream(input), new FileInputStream(xml));

        assert out != null;
    }
}