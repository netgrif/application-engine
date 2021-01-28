package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResource;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
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
    private IPdfDataHelper pdfDataHelper;

    @Autowired
    private IPdfDrawer pdfDrawer;

    @Override
    public void setupPdfGenerator(PdfResource pdfResource) throws IOException{
        setupPdfGenerator(pdfResource, 1.4f);
    }

    @Override
    public void setupPdfGenerator(PdfResource pdfResource, float version) throws IOException {
        log.info("Setting up PDF generator.");

        this.pdf = new PDDocument();
        this.pdf.setVersion(version);
        pdfDataHelper.setupDataHelper(pdfResource);
        pdfDrawer.setupDrawer(pdf, pdfResource);

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
    public void addCustomField(PdfField field, PdfResource pdfResource) {
        generateData(field, pdfResource);
    }

    @Override
    public File generatePdf(Case formCase, String transitionId, PdfResource pdfResource, List<String> excludedFields){
        generateData(formCase.getPetriNet(), formCase, formCase.getPetriNet().getTransition(transitionId), pdfResource, excludedFields);
        return generatePdf(pdfResource);
    }

    @Override
    public File generatePdf(Case formCase, String transitionId, PdfResource pdfResource) {
        generateData(formCase.getPetriNet(), formCase, formCase.getPetriNet().getTransition(transitionId), pdfResource);
        return generatePdf(pdfResource);
    }

    @Override
    public File generatePdf(PdfResource pdfResource) {
        try{
            return transformRequestToPdf(pdfDataHelper.getPdfFields(), pdfResource);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
        return null;
    }

    @Override
    public void generatePdf(Case formCase, String transitionId, PdfResource pdfResource, OutputStream stream) {
        Transition transition = formCase.getPetriNet().getTransition(transitionId);
        generatePdf(formCase, transition, pdfResource, stream);
    }

    @Override
    public void generatePdf(Case formCase, Transition transition, PdfResource pdfResource, OutputStream stream) {
        generateData(formCase.getPetriNet(), formCase, transition, pdfResource);
        try {
            transformRequestToPdf(pdfDataHelper.getPdfFields(), pdfResource, stream);
        } catch (IOException e) {
            log.error("Error occurred while converting form data to PDF", e);
        }
    }

    @Override
    public void generateData(PetriNet petriNet, Case useCase, Transition transition, PdfResource pdfResource, List<String> excludedFields) {
        pdfDataHelper.setExcludedFields(excludedFields);
        generateData(useCase.getPetriNet(), useCase, transition, pdfResource);
    }

    @Override
    public void generateData(PetriNet petriNet, Case useCase, Transition transition, PdfResource pdfResource) {
        pdfDataHelper.setPetriNet(petriNet);
        pdfDataHelper.setTaskId(useCase, transition);
        pdfDataHelper.generateTitleField();
        pdfDataHelper.generatePdfFields();
        pdfDataHelper.correctFieldsPosition();
        pdfDrawer.setupDrawer(pdf, pdfResource);
    }

    @Override
    public void generateData(PdfField pdfField, PdfResource pdfResource) {
        pdfDataHelper.getPdfFields().add(pdfField);
        pdfDataHelper.correctFieldsPosition();
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
        pdfDrawer.closeTemplate();
        pdf.close();
        stream.close();
        log.info("PDF is generated from transition.");
    }

    void drawTransitionForm(List<PdfField> pdfFields) throws IOException {
        log.info("Drawing form to PDF.");

        for (PdfField pdfField : pdfFields) {

            if (pdfField.getFieldId().equals("titleField")) {
                pdfDrawer.drawTitleField(pdfField);
            } else if (!pdfField.isDgField()) {
                switch (pdfField.getType()) {
                    case MULTICHOICE_MAP:
                    case MULTICHOICE:
                        pdfDrawer.drawMultiChoiceField(pdfField);
                        break;
                    case ENUMERATION_MAP:
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
