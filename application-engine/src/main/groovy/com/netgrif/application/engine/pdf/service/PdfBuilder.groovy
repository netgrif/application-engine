package com.netgrif.application.engine.pdf.service

import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static com.netgrif.application.engine.pdf.service.PdfUtils.mmToPoint

class PdfBuilder {

    public static final Logger log = LoggerFactory.getLogger(PdfBuilder)

    protected PDDocument document

    protected PdfBuilder() {

    }

    static PdfBuilder builder() {
        return new PdfBuilder()
    }

    PdfBuilder load(String path) {
        document = PDDocument.load(new File(path))
        return this
    }

    PdfBuilder load(String... paths) {
        PDFMergerUtility pdfMerger = new PDFMergerUtility()

        document = new PDDocument()
        paths.each { path ->
            PDDocument toMerge = PDDocument.load(new File(path))
            pdfMerger.appendDocument(this.document, toMerge)
        }

        return this
    }

    File save(String path) {
        if (document == null || path == null)
            throw new IllegalArgumentException("Document=[$document] and path=[$path]")
        document.save(path)
        document.close()
        return new File(path)
    }

    PDDocument build() {
        return document
    }

    PdfBuilder includeImage(String filePath, int pageNumber, float x, float y, float width, float height) {
        return includeImage(new File(filePath), pageNumber, x, y, width, height)
    }

    PdfBuilder includeImage(File file, int pageNumber, float x, float y, float width, float height) {
        PDPage page = document.getPage(pageNumber)
        PDImageXObject pdImage = PDImageXObject.createFromFileByContent(file, document)

        PDPageContentStream contentStream = new PDPageContentStream(document, page, true, true)
        contentStream.drawImage(pdImage, x, y, width, height)
        contentStream.close()

        return this
    }

    PdfBuilder fill(String xml, Map<String, String> fonts) {
        try {
            PDDocumentCatalog docCatalog = document.getDocumentCatalog()
            PDAcroForm acroForm = docCatalog.getAcroForm()

            PDResources res = acroForm.getDefaultResources()
            if (res == null)
                res = new PDResources()

            def loadedFonts = [:]
            fonts.each { fontId, fontPath ->
                File fontFile = new File(fontPath)
                PDType0Font font = PDType0Font.load(document, new FileInputStream(fontFile), true)

                String fontName = res.add(font).name
                if (fontName == null)
                    throw new IllegalArgumentException("Could not add font to Pdf document")

                acroForm.setDefaultResources(res)

                loadedFonts[fontId] = fontName
            }

            PdfUtils.addFieldValues(acroForm, xml, loadedFonts)

            return this
        } catch (IOException e) {
            log.error("Filling PDF failed: ", e)
            throw new IllegalArgumentException(e);
        }
    }

    PdfBuilder merge(PDDocument... documents) {
        PDFMergerUtility pdfMerger = new PDFMergerUtility()

        documents.each { toMerge ->
            pdfMerger.appendDocument(this.document, toMerge)
        }

        return this
    }

    PdfBuilder encrypt(String ownerPassword = "", String userPassword = "") {
        AccessPermission ap = new AccessPermission(canFillInForm: false, canModify: false)
        StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap)
        spp.setEncryptionKeyLength(PdfUtils.KEY_LENGTH)
        document.protect(spp)

        return this
    }

    PdfBuilder removePages(int ... pages) {
        pages.each {
            document.removePage(it)
        }

        return this
    }

    PdfBuilder resize(float left, float right, float up, float down) {
        PDPageTree pages = document.getDocumentCatalog().getPages()
        PDDocument outputDoc = new PDDocument()

        for (PDPage page : pages) {
            PDRectangle rectangle = new PDRectangle()
            rectangle.setLowerLeftX(-mmToPoint(left))
            rectangle.setLowerLeftY(-mmToPoint(down))
            rectangle.setUpperRightX(PDRectangle.A4.width + mmToPoint(right) as float)
            rectangle.setUpperRightY(PDRectangle.A4.height + mmToPoint(up) as float)
            page.setMediaBox(rectangle)
            page.setCropBox(rectangle)
            outputDoc.addPage(page)
        }

        return this
    }
}