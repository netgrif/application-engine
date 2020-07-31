package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.DataField;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.List;
import java.util.Map;

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


    private void constructPdfGenerator(PdfResource pdfResource) throws IOException {
        log.info("Setting up PDF generator.");

        this.pdf = new PDDocument();

        pdfResource.setTitleFont(PDType0Font.load(this.pdf, new FileInputStream(pdfResource.getFontTitleResource().getFile()), true));
        pdfResource.setLabelFont(PDType0Font.load(this.pdf, new FileInputStream(pdfResource.getFontLabelResource().getFile()), true));
        pdfResource.setValueFont(PDType0Font.load(this.pdf, new FileInputStream(pdfResource.getFontValueResource().getFile()), true));

        pdfResource.setCheckboxChecked(PDImageXObject.createFromFileByContent(pdfResource.getCheckBoxCheckedResource().getFile(), this.pdf));
        pdfResource.setCheckboxUnchecked(PDImageXObject.createFromFileByContent(pdfResource.getCheckBoxUnCheckedResource().getFile(), this.pdf));
        pdfResource.setRadioChecked(PDImageXObject.createFromFileByContent(pdfResource.getRadioCheckedResource().getFile(), this.pdf));
        pdfResource.setRadioUnchecked(PDImageXObject.createFromFileByContent(pdfResource.getRadioUnCheckedResource().getFile(), this.pdf));
        pdfResource.setBooleanChecked(PDImageXObject.createFromFileByContent(pdfResource.getBooleanCheckedResource().getFile(), this.pdf));
        pdfResource.setBooleanUnchecked(PDImageXObject.createFromFileByContent(pdfResource.getBooleanUncheckedResource().getFile(), this.pdf));
    }

    @Override
    public File convertCustomField(List<PdfField> pdfFields, PdfResource pdfResource){
        try {
            generateData(pdfFields, pdfResource);
            return transformRequestToPdf(dataConverter.getPdfFields(), pdfResource);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
        return null;
    }

    @Override
    public File convertCaseForm(Case formCase, String transitionId, PdfResource pdfResource) throws IOException {
        return convertCaseForm(formCase, formCase.getPetriNet().getTransition(transitionId).getDataGroups(), pdfResource);
    }

    @Override
    public File convertCaseForm(Case formCase, Map<String, DataGroup> dataGroupMap, PdfResource pdfResource) throws IOException {
        generateData(formCase.getPetriNet(), dataGroupMap, formCase.getDataSet(), pdfResource);
        try {
            return transformRequestToPdf(dataConverter.getPdfFields(), pdfResource);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
        return null;
    }

    @Override
    public void convertCaseForm(Case formCase, String transitionId, PdfResource pdfResource, OutputStream stream) throws IOException {
        convertCaseForm(formCase, formCase.getPetriNet().getTransition(transitionId).getDataGroups(), pdfResource, stream);
    }

    @Override
    public void convertCaseForm(Case formCase, Map<String, DataGroup> dataGroupMap, PdfResource pdfResource, OutputStream stream) throws IOException {
        generateData(formCase.getPetriNet(), dataGroupMap, formCase.getDataSet(), pdfResource);
        try {
            transformRequestToPdf(dataConverter.getPdfFields(), pdfResource, stream);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
    }

    @Override
    public void generateData(PetriNet petriNet, Map<String, DataGroup> dataGroupMap, Map<String, DataField> dataSet, PdfResource pdfResource) throws IOException {
        dataConverter.setupDataConverter(pdfResource);
        dataConverter.setPetriNet(petriNet);
        dataConverter.setDataGroups(dataGroupMap);
        dataConverter.setDataSet(dataSet);
        dataConverter.generateTitleField();
        dataConverter.generatePdfFields();
        dataConverter.generatePdfDataGroups();
        dataConverter.correctFieldsPosition();

        constructPdfGenerator(pdfResource);
        pdfDrawer.setupDrawer(pdf, pdfResource);
    }

    @Override
    public void generateData(List<PdfField> pdfFields, PdfResource pdfResource) throws IOException {
        dataConverter.setPdfFields(pdfFields);
        dataConverter.correctFieldsPosition();

        constructPdfGenerator(pdfResource);
        pdfDrawer.setupDrawer(pdf, pdfResource);
    }

    protected File transformRequestToPdf(List<PdfField> pdfFields, PdfResource pdfResource) throws IOException {
        File output = new File(((ClassPathResource) pdfResource.getOutputResource()).getPath());
        transformRequestToPdf(pdfFields, pdfResource, new FileOutputStream(output));
        return output;
    }

    protected void transformRequestToPdf(List<PdfField> pdfFields, PdfResource pdfResource, OutputStream stream) throws IOException {
        File template = new File(((ClassPathResource) pdfResource.getTemplateResource()).getPath());
        if(template.exists()){
            pdfDrawer.setTemplatePdf(PDDocument.load(template));
        }
        pdfDrawer.newPage();
        drawTransitionForm(pdfFields);
        pdfDrawer.closeContentStream();
        pdf.save(stream);
        pdf.close();
        log.info("PDF is generated from transition.");
    }

    void drawTransitionForm(List<PdfField> pdfFields) throws IOException {
        log.info("Drawing form to PDF.");

        for (PdfField pdfField : pdfFields) {

            if (pdfField.getFieldId().equals("titleField")) {
                pdfDrawer.drawTitleField(pdfField);
            } else if (!pdfField.isDgField()) {
                switch (pdfField.getType()) {
                    case MULTICHOICE:
                        pdfDrawer.drawMultiChoiceField(pdfField);
                        break;
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
        pdfDrawer.drawPageNumber();
    }
}
