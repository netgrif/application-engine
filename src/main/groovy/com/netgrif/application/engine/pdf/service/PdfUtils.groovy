package com.netgrif.application.engine.pdf.service

import org.apache.pdfbox.cos.COSName
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import org.apache.pdfbox.pdmodel.*
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.encryption.AccessPermission
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import org.apache.pdfbox.pdmodel.font.PDType0Font
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm
import org.apache.pdfbox.pdmodel.interactive.form.PDField
import org.apache.pdfbox.pdmodel.interactive.form.PDNonTerminalField
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PdfUtils {

    private static final Logger log = LoggerFactory.getLogger(PdfUtils.class)

    static final int KEY_LENGTH = 128

    /**
     * Creates and returns new PDF file, created by shrinking input PDF file. To enlarge pdf, provide negative values.
     * @param inputFile input pdf
     * @param outputFileName output pdf file name
     * @param left left space in mm
     * @param right right space in mm
     * @param up upper space in mm
     * @param down lower space in mm
     * @return resized PDF file
     */
    static File resize(File inputFile, String outputFileName, float left, float right, float up, float down) {
        File outputFile = new File(outputFileName)
        PDDocument inputDoc = PDDocument.load(inputFile)
        PDDocument outputDoc = new PDDocument()

        PDPageTree pages = inputDoc.getDocumentCatalog().getPages()
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
        outputDoc.save(outputFile)
        inputDoc.close()
        outputDoc.close()

        return outputFile
    }

    static float mmToPoint(float mm) {
        return (mm * 72) / 25.4F
    }

    static File removePages(File pdfFile, int ... pages) {
        PDDocument document = PDDocument.load(pdfFile)

        pages.each {
            document.removePage(it)
        }
        document.save(pdfFile)
        document.close()

        return pdfFile
    }

    static File encryptPdfFile(String outPdfPath, File input, String ownerPassword = "", String userPassword = "") {
        PDDocument doc = PDDocument.load(input)
        AccessPermission ap = new AccessPermission(canFillInForm: false, canModify: false)
        StandardProtectionPolicy spp = new StandardProtectionPolicy(ownerPassword, userPassword, ap)
        File encrypted = new File(outPdfPath)

        spp.setEncryptionKeyLength(KEY_LENGTH)
        doc.protect(spp)
        doc.save(encrypted)
        doc.close()

        return encrypted
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

    static File fillPdfForm(String outPdfName, InputStream pdfFile, InputStream xmlFile) throws IllegalArgumentException {
        fillPdfForm(outPdfName, pdfFile, xmlFile.getText())
        xmlFile.close()
    }

    static File fillPdfForm(String outPdfName, InputStream pdfFile, String xml) throws IllegalArgumentException {
        try {
            PDDocument document = PDDocument.load(pdfFile)
            PDDocumentCatalog docCatalog = document.getDocumentCatalog()
            PDAcroForm acroForm = docCatalog.getAcroForm()

            addFieldValues(acroForm, xml, [:])
            return saveToFile(document, outPdfName)
        } catch (IOException e) {
            log.error("Filling PDF form failed: ", e)
            throw new IllegalArgumentException(e)
        } finally {
            pdfFile.close()
        }
    }

    static String addFont(PDDocument document, PDAcroForm acroForm, String fontPath) {
        PDResources res = acroForm.getDefaultResources()
        if (res == null)
            res = new PDResources()

//        File fontFile = ResourceFileLoader.loadResourceFile(fontPath)
        File fontFile = new File(fontPath)
        PDType0Font font = PDType0Font.load(document, new FileInputStream(fontFile), true)

        String fontName = res.add(font).name
        if (fontName == null)
            log.error("Could not add font to pdf resource")

        acroForm.setDefaultResources(res)

        return fontName
    }

    static void addFieldValues(PDAcroForm acroForm, String xmlText, Map<String, String> fonts) {
        def fieldValues = new XmlSlurper().parseText(xmlText)

        fieldValues.children().each {
            setFieldValueAndFont(acroForm, it, fonts)
        }

        acroForm.flatten()
    }

    static setFieldValueAndFont(PDAcroForm acroForm, def xmlNode, Map<String, String> fonts) {
        def id = ((xmlNode["@xfdf:original"] as String) ?: xmlNode.name()) as String
        def field = acroForm.fieldIterator.find { it.partialName.equalsIgnoreCase(id) }
        if (field == null) {
            log.warn("Cannot find field [$id]")
            return
        }

        try {
            String DA = field.getCOSObject().getString(COSName.DA)

            fonts.each { font ->
                if (DA.contains(font.key))
                    field.getCOSObject().setString(COSName.DA, DA.replaceAll(font.key, "/${font.value}"))
            }
            field.setValue(xmlNode as String)
        } catch (NullPointerException e) {
            log.warn("Cannot find field $id", e)
        }
    }

    static File saveToFile(PDDocument document, String outPdfName) {
        File file = new File(outPdfName)
        document.save(file)
        document.close()
        return file
    }

    static Map<String, String> readPdfForm(InputStream inputStream) {

        Map<String, String> result = new HashMap<>()

        PDDocument document = PDDocument.load(inputStream)
        PDDocumentCatalog docCatalog = document.getDocumentCatalog()
        PDAcroForm acroForm = docCatalog.getAcroForm()

        List<PDField> fields = acroForm.getFields()

        fields.forEach({
            addAllFieldsAndChildFields(it, result)
        })
        document.close()
        inputStream.close()

        return result
    }

    static addAllFieldsAndChildFields(PDField field, Map<String, String> result) {

        if (field instanceof PDNonTerminalField) {
            PDNonTerminalField nonTerminalField = (PDNonTerminalField) field
            nonTerminalField.getChildren().forEach({
                //Non terminal fields are not written to map at the moment
                //If needed, add it here
                addAllFieldsAndChildFields(it, result)
            })
        } else {
            result.put(field.getFullyQualifiedName(), field.getValueAsString())
        }
    }
}