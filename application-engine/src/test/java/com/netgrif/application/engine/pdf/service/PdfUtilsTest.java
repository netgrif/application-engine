package com.netgrif.application.engine.pdf.service;

import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PdfUtilsTest {

    private static final Pattern KLAVIKA_FONT_PATTERN = Pattern.compile("/(KlavikaBasic-[^\\s]+)");

    @Test
    public void fillPdfForm() throws Exception {
        File input = new File("src/test/resources/pdf/test.pdf");
        File xml = new File("src/test/resources/pdf/test.xml");

        File out = PdfUtils.fillPdfForm("target/test_out.pdf", new FileInputStream(preparePdfFormFonts(input)), Files.readString(xml.toPath(), StandardCharsets.UTF_8));

        assert out != null;
    }

    @Test
    public void fillPdfFormPoisteniePremioveByvanie() throws Exception {
        File input = new File("src/test/resources/pdf/draft.pdf");
        File xml = new File("src/test/resources/pdf/draft.xml");

        File out = PdfUtils.fillPdfForm("target/test_out_premiovebyvanie.pdf", new FileInputStream(preparePdfFormFonts(input)), Files.readString(xml.toPath(), StandardCharsets.UTF_8));

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

    private File preparePdfFormFonts(File input) throws Exception {
        File output = File.createTempFile("pdf-utils-test-", ".pdf", new File("target"));
        File fontFile = new File("src/main/resources/pdfGenerator/fonts/Roboto-Light.ttf");
        try (PDDocument document = PDDocument.load(input);
             FileInputStream fontInput = new FileInputStream(fontFile)) {
            PDAcroForm acroForm = document.getDocumentCatalog().getAcroForm();
            PDResources resources = acroForm.getDefaultResources();
            if (resources == null) {
                resources = new PDResources();
            }
            PDType0Font fallbackFont = PDType0Font.load(document, fontInput, true);
            for (String fontName : findKlavikaFontNames(acroForm)) {
                resources.put(COSName.getPDFName(fontName), fallbackFont);
            }
            acroForm.setDefaultResources(resources);
            document.save(output);
        }
        return output;
    }

    private Set<String> findKlavikaFontNames(PDAcroForm acroForm) {
        Set<String> names = new HashSet<>();
        collectKlavikaFontNames(acroForm.getCOSObject().getString(COSName.DA), names);
        acroForm.getFieldTree().forEach(field -> collectKlavikaFontNames(field.getCOSObject().getString(COSName.DA), names));
        return names;
    }

    private void collectKlavikaFontNames(String defaultAppearance, Set<String> names) {
        if (defaultAppearance == null) {
            return;
        }
        Matcher matcher = KLAVIKA_FONT_PATTERN.matcher(defaultAppearance);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
    }
}
