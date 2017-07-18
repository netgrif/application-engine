package com.netgrif.workflow.pdf.service

import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.PDResources
import org.apache.pdfbox.pdmodel.font.PDTrueTypeFont
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PdfFormFiller {

    private static final Logger log = LoggerFactory.getLogger(PdfFormFiller.class)

    static File fillPdfForm(String outPdfName, InputStream pdfFile, InputStream xmlFile) throws IllegalArgumentException {
        try {
            PDDocument document = PDDocument.load(pdfFile)
            PDDocumentCatalog docCatalog = document.getDocumentCatalog()
            PDAcroForm acroForm = docCatalog.getAcroForm()

            Map<String, String> fonts = new HashMap<>()
            fonts.put("/KlavikaBasic-Regular", addFont(document, acroForm, "src/main/resources/fonts/Klavika Regular.ttf"))
            fonts.put("/KlavikaBasic-Bold", addFont(document, acroForm, "src/main/resources/fonts/Klavika Bold.ttf"))
            fonts.put("/KlavikaBasic-Medium", addFont(document, acroForm, "src/main/resources/fonts/Klavika Medium.ttf"))
            addFieldValues(acroForm, xmlFile.getText(), fonts)
            return saveToFile(document, outPdfName)
        } catch (IOException e) {
            e.printStackTrace()
            throw new IllegalArgumentException(e)
        }
    }

    private static String addFont(PDDocument document, PDAcroForm acroForm, String fontPath) {
        PDResources res = acroForm.getDefaultResources()
        if (res == null)
            res = new PDResources()

        InputStream fontStream = new FileInputStream(fontPath)
        PDTrueTypeFont font = PDTrueTypeFont.loadTTF(document, fontStream)

        String fontName = res.add(font).name
        if (fontName == null)
            log.error("Could not add font to pdf resource")

        acroForm.setDefaultResources(res)

        return fontName
    }

    private static void addFieldValues(PDAcroForm acroForm, String xmlText, Map<String, String> fonts) {
        def fieldValues = new XmlSlurper().parseText(xmlText)

        fieldValues.children().each {
            String DA = acroForm.getField(it["@xfdf:original"] as String).getCOSObject().getString(COSName.DA)
            fonts.each { font ->
                if (DA.contains(font.key))
                    acroForm.getField(it["@xfdf:original"] as String).getCOSObject().setString(COSName.DA, DA.replaceAll(font.key, "/${font.value}"))
            }
            acroForm.getField(it["@xfdf:original"] as String).setValue(it as String)
        }

        acroForm.flatten()
    }

    private static File saveToFile(PDDocument document, String outPdfName) {
        File file = new File(outPdfName)
        document.save(file)
        document.close()
        return file
    }

    static File mergePdfFiles(String outPdfName, File... files) {
        PDFMergerUtility pdfMerger = new PDFMergerUtility()
        pdfMerger.setDestinationFileName(outPdfName)

        files.each {
            pdfMerger.addSource(it)
        }

        pdfMerger.mergeDocuments(MemoryUsageSetting.setupMixed(100_000_000L, 500_000_000L))

        return new File(pdfMerger.getDestinationFileName())
    }
}