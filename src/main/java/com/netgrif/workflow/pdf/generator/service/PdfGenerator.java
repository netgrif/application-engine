package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
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

    @Override
    public File convertCaseForm(Case formCase, String transitionId) throws IOException {
        generateData(formCase, transitionId);
        try {
            return transformRequestToPdf(dataConverter);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
        return null;
    }

    @Override
    public void convertCaseForm(Case formCase, String transitionId, OutputStream stream) throws IOException {
        generateData(formCase, transitionId);
        try {
            transformRequestToPdf(dataConverter, stream);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
    }

    private void generateData(Case formCase, String transitionId) throws IOException {
        dataConverter.setPetriNet(formCase.getPetriNet());
        dataConverter.setDataGroups(formCase.getPetriNet().getTransitions().get(transitionId).getDataGroups());
        dataConverter.setDataSet(formCase.getDataSet());
        dataConverter.generateTitleField(formCase.getPetriNet().getTransitions().get(transitionId).getTitle().toString());
        dataConverter.generatePdfFields();
        dataConverter.generatePdfDataGroups();
        dataConverter.correctFieldsPosition();

        constructPdfGenerator();
        pdfDrawer.setupDrawer(pdf);
    }

    protected File transformRequestToPdf(IDataConverter dataHelper) throws IOException {
        File output = new File(((ClassPathResource) pdfResource.getOutputResource()).getPath());
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

    void drawTransitionForm(IDataConverter dataHelper) throws IOException {
        log.info("Drawing form to PDF.");
        List<PdfField> pdfFields = dataHelper.getPdfFields();

        for (PdfField pdfField : pdfFields) {

            if (pdfField.getFieldId().equals("titleField")) {
                pdfDrawer.drawTitleField(pdfField);
            } else if (!pdfField.isDgField()) {
                switch (pdfField.getType()) {
                    case MULTICHOICE:
                    case ENUMERATION:
                        pdfDrawer.drawEnumerationField(pdfField);
                        break;
                    case BOOLEAN:
                        pdfDrawer.drawBooleanField(pdfField);
                        break;
                    default:
                        pdfDrawer.drawTextField(pdfField);
                        break;
                }
            } else {
                pdfDrawer.drawDataGroupField(pdfField);
            }
        }
    }
}
