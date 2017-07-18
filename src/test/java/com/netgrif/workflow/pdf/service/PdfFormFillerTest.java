package com.netgrif.workflow.pdf.service;

import org.junit.Test;
import org.junit.runner.RunWith;
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