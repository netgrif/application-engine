package com.netgrif.workflow.pdf.service

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

            addFont(document, acroForm, "src/main/resources/fonts/Klavika Regular.ttf")
            addFieldValues(acroForm, xmlFile.getText())
            return saveToFile(document, outPdfName)
        } catch (IOException e) {
            e.printStackTrace()
            throw new IllegalArgumentException(e)
        }
    }

    private static void addFont(PDDocument document, PDAcroForm acroForm, String fontPath) {
        PDResources res = acroForm.getDefaultResources()
        if (res == null)
            res = new PDResources()

        InputStream fontStream = new FileInputStream(fontPath)
        PDTrueTypeFont font = PDTrueTypeFont.loadTTF(document, fontStream)

        String fontName = res.add(font)
        if (fontName == null)
            log.error("Could not add font to pdf resource")

        acroForm.setDefaultResources(res)
    }

    private static void addFieldValues(PDAcroForm acroForm, String xmlText) {
        def fieldValues = new XmlSlurper().parseText(xmlText)

        fieldValues.children().each {
            String DA = acroForm.getField(it["@xfdf:original"] as String).getCOSObject().getString(COSName.DA)
            acroForm.getField(it["@xfdf:original"] as String).getCOSObject().setString(COSName.DA, DA.replaceAll("/KlavikaBasic-[a-zA-Z]*","/Helv"))
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
}