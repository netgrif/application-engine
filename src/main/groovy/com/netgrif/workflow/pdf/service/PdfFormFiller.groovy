package com.netgrif.workflow.pdf.service

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDDocumentCatalog
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.springframework.stereotype.Component


@Component
class PdfFormFiller {

    File fillPdfForm(String outPdfName, InputStream pdfFile, InputStream xmlFile) throws IllegalArgumentException {
        try {
            PDDocument document = PDDocument.load(pdfFile)
            OutputStream outputStream = new FileOutputStream(outPdfName)
            PDDocumentCatalog docCatalog = document.getDocumentCatalog()
            PDAcroForm acroForm = docCatalog.getAcroForm()
            def fieldValues = new XmlSlurper().parseText(xmlFile.getText())

            fieldValues.children().each {
                acroForm.getField(it["@xfdf:original"] as String).setValue(it as String)
            }

            acroForm.flatten()
            document.save(outputStream)
            return outputStream
        } catch (IOException e) {
            e.printStackTrace()
            throw new IllegalArgumentException(e)
        }
    }
}