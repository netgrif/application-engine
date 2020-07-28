package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jsoup.select.Evaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;

/**
 * Generates PDF from the given transition form
 */
@Slf4j
@Service
public class PdfGenerator implements IPdfGenerator {

    private PDDocument pdf;

    @Autowired
    private IDataConverter dataConverter;

    @Autowired
    private IPdfDrawer pdfDrawer;

    @Autowired
    private PdfResource pdfResource;

    @Value("${nae.pdf.output.path}")
    private String outputPath;

    /**
     * Creates new PD document, sets resources files for fonts and images
     *
     * @throws IOException I/O exception handling for operations with files
     */
    private void constructPdfGenerator() throws IOException {
        log.info("Setting up PDF generator.");

        this.pdf = new PDDocument();

        pdfResource.setTitleFont(PDType0Font.load(this.pdf, new FileInputStream(pdfResource.getFontTitleResource().getFile()), true));
        pdfResource.setLabelFont(PDType0Font.load(this.pdf, new FileInputStream(pdfResource.getFontLabelResource().getFile()), true));
        pdfResource.setValueFont(PDType0Font.load(this.pdf, new FileInputStream(pdfResource.getFontValueResource().getFile()), true));

        pdfResource.setCheckboxChecked(PDImageXObject.createFromFileByContent(pdfResource.getCheckBoxCheckedResource().getFile(), this.pdf));
        pdfResource.setCheckboxUnchecked(PDImageXObject.createFromFileByContent(pdfResource.getCheckBoxUnCheckedResource().getFile(), this.pdf));
        pdfResource.setRadioChecked(PDImageXObject.createFromFileByContent(pdfResource.getRadioCheckedResource().getFile(), this.pdf));
        pdfResource.setRadioUnchecked(PDImageXObject.createFromFileByContent(pdfResource.getRadioUnCheckedResource().getFile(), this.pdf));
    }

    /**
     * Function is called when a PDF needs to be generated from the transition. This generate PDF from transition
     *
     * data by calling corresponding functions.
     * @param formCase case that contains current transition
     * @param transitionId transition that form will be exported from
     * @throws IOException I/O exception handling for operations with files
     * @return output PDF file
     */
    @Override
    public File convertCaseForm(Case formCase, String transitionId) throws IOException {
        dataConverter.setDataGroups(formCase.getPetriNet().getTransitions().get(transitionId).getDataGroups());
        dataConverter.setDataSet(formCase.getDataSet());
        dataConverter.generateTitleField(formCase.getPetriNet().getTransitions().get(transitionId).getTitle().toString());
        dataConverter.generatePdfFields();
        dataConverter.generatePdfDataGroups();
        dataConverter.correctFieldsPosition();

        constructPdfGenerator();
        pdfDrawer.setupDrawer(pdf);

        try {
            return transformRequestToPdf(dataConverter);
        } catch (IOException e) {
            log.error("Error occured while converting form data to PDF", e);
        }
        return null;
    }

    /**
     * Creates output files and execute export of elements to PDF
     *
     * @param dataHelper holds the data to be exported
     * @throws IOException I/O exception handling for operations with files
     * @return PDF file generated from form
     */
    protected File transformRequestToPdf(IDataConverter dataHelper) throws IOException {
        File output = new ClassPathResource(outputPath).getFile();
        transformRequestToPdf(dataHelper, new FileOutputStream(output));
        return output;
    }

    protected void transformRequestToPdf(IDataConverter dataHelper, OutputStream stream) throws IOException {
        pdfDrawer.newPage();
        drawTransitionForm(dataHelper);
        pdfDrawer.closeContentStream();
        pdf.save(stream);
        pdf.close();
        log.info("PDF is generated from transition.");
    }

    /**
     * Parses the input data groups and data field to a PDF file and draws to a PDF file using corresponding
     * functions
     */
    void drawTransitionForm(IDataConverter dataHelper) throws IOException {
        log.info("Drawing form to PDF.");

        int fieldX, fieldY, fieldWidth, fieldHeight;
        String label;
        FieldType type;
        List<String> values;
        List<String> choices;
        List<PdfField> pdfFields = dataHelper.getPdfFields();

        for (PdfField pdfField : pdfFields) {
            fieldX = pdfResource.getBaseX() + pdfField.getX();
            fieldY = pdfResource.getBaseY() - pdfField.getBottomY();
            fieldWidth = pdfField.getWidth();
            fieldHeight = pdfField.getHeight();
            label = pdfField.getLabel();
            values = pdfField.getValues();
            choices = pdfField.getChoices();
            type = pdfField.getType();

            if (pdfField.getFieldId().equals("titleField")) {
                pdfDrawer.drawTitle(label, pdfField.getX(), pdfField.getBottomY(), fieldWidth);
            } else if (!pdfField.isDgField()) {
                switch (pdfField.getType()) {
                    case MULTICHOICE:
                    case ENUMERATION:
                        pdfDrawer.drawSelectionField(label, choices, values, fieldX, fieldY, fieldWidth, fieldHeight, type);
                        break;
                    case BOOLEAN:
                        pdfDrawer.drawBooleanField(label, values, fieldX, fieldY, fieldWidth, fieldHeight);
                        break;
                    default:
                        pdfDrawer.drawTextField(label, values, fieldX, fieldY, fieldWidth, fieldHeight);
                        break;
                }
            } else {
                pdfDrawer.drawLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, pdfResource.getTitleFont(), pdfResource.getFontGroupSize());
            }
        }
    }
}
