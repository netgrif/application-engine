package com.netgrif.application.engine.pdf.service;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationWidget;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PdfUtilsTest {

    private static final String TEST_XML = """
            <?xml version="1.0" encoding="UTF-8"?>
            <fields xmlns:xfdf="http://ns.adobe.com/xfdf-transition/">
                <TextField143 xfdf:original="Text Field 143">Test value 1</TextField143>
                <TextField50 xfdf:original="Text Field 50">+ľščťžýáíéúäňô§</TextField50>
                <fxtag xfdf:original="1">Test value 2</fxtag>
                <fxtag xfdf:original="21">Test address</fxtag>
                <TextField153 xfdf:original="Text Field 153">Test footer value</TextField153>
            </fields>
            """;

    @TempDir
    Path tempDir;

    @Test
    public void fillPdfForm() throws Exception {
        File input = createTestPdfForm(
                tempDir.resolve("test-form.pdf"),
                List.of(
                        "Text Field 143",
                        "Text Field 50",
                        "1",
                        "21",
                        "Text Field 153"
                )
        );

        File output;
        try (FileInputStream inputStream = new FileInputStream(input)) {
            output = PdfUtils.fillPdfForm(
                    tempDir.resolve("test-out.pdf").toString(),
                    inputStream,
                    TEST_XML
            );
        }

        assertNotNull(output);
        assertTrue(output.exists());
        assertTrue(output.length() > 0);
        assertPdfContainsText(output, "Test value 1");
        assertPdfContainsText(output, "Test address");
    }

    @Test
    public void fillPdfFormWithMultipleFields() throws Exception {
        File input = createTestPdfForm(
                tempDir.resolve("test-form-multiple-fields.pdf"),
                List.of(
                        "Text Field 143",
                        "Text Field 50",
                        "1",
                        "21",
                        "22",
                        "23",
                        "24",
                        "Text Field 153"
                )
        );

        File output;
        try (FileInputStream inputStream = new FileInputStream(input)) {
            output = PdfUtils.fillPdfForm(
                    tempDir.resolve("test-out-multiple-fields.pdf").toString(),
                    inputStream,
                    TEST_XML
            );
        }

        assertNotNull(output);
        assertTrue(output.exists());
        assertTrue(output.length() > 0);
        assertPdfContainsText(output, "Test value 1");
        assertPdfContainsText(output, "Test footer value");
    }

    @Test
    public void mergePdf() throws Exception {
        File f1 = createSimplePdf(tempDir.resolve("test-1.pdf"), "Test PDF 1");
        File f2 = createSimplePdf(tempDir.resolve("test-2.pdf"), "Test PDF 2");
        File f3 = createSimplePdf(tempDir.resolve("test-3.pdf"), "Test PDF 3");

        File output = PdfUtils.mergePdfFiles(
                tempDir.resolve("test-merged.pdf").toString(),
                f1,
                f2,
                f3
        );

        assertNotNull(output);
        assertTrue(output.exists());
        assertTrue(output.length() > 0);

        try (PDDocument document = PDDocument.load(output)) {
            assertEquals(3, document.getNumberOfPages());
        }
    }

    @Test
    public void encryptPdf() throws Exception {
        File input = createSimplePdf(tempDir.resolve("test-encrypt-input.pdf"), "PDF to encrypt");

        File output = PdfUtils.encryptPdfFile(
                tempDir.resolve("test-encrypted.pdf").toString(),
                input,
                "owner",
                "user"
        );

        assertNotNull(output);
        assertTrue(output.exists());
        assertTrue(output.length() > 0);
        assertThrows(InvalidPasswordException.class, () -> PDDocument.load(output));

        try (PDDocument document = PDDocument.load(output, "user")) {
            assertTrue(document.isEncrypted());
            assertFalse(document.getCurrentAccessPermission().canModify());
        }
    }

    private void assertPdfContainsText(File pdf, String expectedText) throws Exception {
        try (PDDocument document = PDDocument.load(pdf)) {
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains(expectedText), "Expected PDF to contain text [" + expectedText + "]");
        }
    }

    private File createTestPdfForm(Path output, List<String> fieldNames) throws Exception {
        File fontFile = new File("src/main/resources/pdfGenerator/fonts/Roboto-Light.ttf");

        try (PDDocument document = new PDDocument();
             FileInputStream fontInput = new FileInputStream(fontFile)) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, fontInput, true);

            PDResources resources = new PDResources();
            resources.put(COSName.getPDFName("Roboto"), font);

            PDAcroForm acroForm = new PDAcroForm(document);
            acroForm.setDefaultResources(resources);
            acroForm.setDefaultAppearance("/Roboto 10 Tf 0 g");
            acroForm.setNeedAppearances(true);

            document.getDocumentCatalog().setAcroForm(acroForm);

            float y = 750;
            int index = 1;

            for (String fieldName : fieldNames) {
                addTextField(document, page, acroForm, fieldName, "Field " + index, 50, y);
                y -= 35;
                index++;
            }

            document.save(output.toFile());
        }

        return output.toFile();
    }

    private void addTextField(
            PDDocument document,
            PDPage page,
            PDAcroForm acroForm,
            String fieldName,
            String label,
            float x,
            float y
    ) throws Exception {
        PDResources resources = acroForm.getDefaultResources();
        PDType0Font font = (PDType0Font) resources.getFont(COSName.getPDFName("Roboto"));

        try (PDPageContentStream contentStream = new PDPageContentStream(
                document,
                page,
                PDPageContentStream.AppendMode.APPEND,
                true
        )) {
            contentStream.beginText();
            contentStream.setFont(font, 10);
            contentStream.newLineAtOffset(x, y + 5);
            contentStream.showText(label);
            contentStream.endText();
        }

        PDTextField field = new PDTextField(acroForm);
        field.setPartialName(fieldName);
        field.setDefaultAppearance("/Roboto 10 Tf 0 g");

        PDAnnotationWidget widget = field.getWidgets().get(0);
        widget.setRectangle(new PDRectangle(x + 100, y, 250, 20));
        widget.setPage(page);

        page.getAnnotations().add(widget);
        acroForm.getFields().add(field);
    }

    private File createSimplePdf(Path output, String text) throws Exception {
        File fontFile = new File("src/main/resources/pdfGenerator/fonts/Roboto-Light.ttf");

        try (PDDocument document = new PDDocument();
             FileInputStream fontInput = new FileInputStream(fontFile)) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font = PDType0Font.load(document, fontInput, true);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(font, 12);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText(text);
                contentStream.endText();
            }

            document.save(output.toFile());
        }

        return output.toFile();
    }
}
