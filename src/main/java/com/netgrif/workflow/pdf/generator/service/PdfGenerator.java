package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResources;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.workflow.domain.Case;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generates PDF from the given transition form
 * */
@Service
public class PdfGenerator extends PdfResources implements IPdfGenerator {

    public static Logger log = LoggerFactory.getLogger(PdfGenerator.class);

    private PDDocument pdf;

    @Autowired
    private IDataConverter dataConverter;

    @Autowired
    private IPdfDrawer pdfDrawer;

    /**
     * Creates new PD document, sets resources files for fonts and images
     * @throws IOException I/O exception handling for operations with files
     */
    private void constructPdfGenerator() throws IOException {
        log.info("Setting up PDF generator.");

        this.pdf = new PDDocument();

        File fontTitleFile = fontTitleResource.getFile();
        File fontLabelFile = fontLabelResource.getFile();
        File fontValueFile = fontValueResource.getFile();

        setTitleFont(PDType0Font.load(this.pdf, new FileInputStream(fontTitleFile), true));
        setLabelFont(PDType0Font.load(this.pdf, new FileInputStream(fontLabelFile), true));
        setValueFont(PDType0Font.load(this.pdf, new FileInputStream(fontValueFile), true));

        setCheckboxChecked(PDImageXObject.createFromFileByContent(checkBoxCheckedResource.getFile(), this.pdf));
        setCheckboxUnchecked(PDImageXObject.createFromFileByContent(checkBoxUnCheckedResource.getFile(), this.pdf));
        setRadioChecked(PDImageXObject.createFromFileByContent(radioCheckedResource.getFile(), this.pdf));
        setRadioUnchecked(PDImageXObject.createFromFileByContent(radioUnCheckedResource.getFile(), this.pdf));
    }

    @Override
    public void convertCaseForm(Case formCase, String transitionId) throws IOException {
        dataConverter.setDataGroups(formCase.getPetriNet().getTransitions().get(transitionId).getDataGroups());
        dataConverter.setDataSet(formCase.getDataSet());
        dataConverter.generateTitleField(formCase.getPetriNet().getTransitions().get(transitionId).getTitle().toString());
        dataConverter.generatePdfFields();
        dataConverter.generatePdfDataGroups();
        dataConverter.correctFieldsPosition();

        constructPdfGenerator();
        pdfDrawer.setupDrawer(pdf);

        try {
            File result = transformRequestToPdf(dataConverter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates output files and execute export of elements to PDF
     * @param dataHelper holds the data to be exported
     * @return PDF file generated from form
     * @throws IOException I/O exception handling for operations with files
     */
    private File transformRequestToPdf(IDataConverter dataHelper) throws IOException {
        File out = new File("src/main/resources/out.pdf");
        pdfDrawer.newPage();
        drawTransitionForm(dataHelper);
        pdfDrawer.closeContentStream();
        pdf.save(out);
        pdf.close();

        log.info("PDF is generated from transition.");
        return out;
    }

    /**
     * Parses the input data groups and data field to a PDF file and draws to a PDF file using corresponding
     * functions
     * */
    void drawTransitionForm(IDataConverter dataHelper) throws IOException {
        log.info("Drawing form to PDF.");

        int fieldX, fieldY, fieldWidth, fieldHeight;
        String label;
        FieldType type;
        List<String> values;
        List<String> choices;
        List<PdfField> pdfFields = dataHelper.getPdfFields();

        for(PdfField pdfField : pdfFields){
            fieldX = BASE_X + pdfField.getX();
            fieldY = BASE_Y - pdfField.getBottomY();
            fieldWidth = pdfField.getWidth();
            fieldHeight = pdfField.getHeight();
            label = pdfField.getLabel();
            values = pdfField.getValues();
            choices = pdfField.getChoices();
            type = pdfField.getType();

            if(pdfField.getFieldId().equals("titleField")){
                pdfDrawer.drawTitle(label, pdfField.getX(), pdfField.getBottomY(),fieldWidth);
            }else if(!pdfField.isDgField()) {
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
            }else{
                pdfDrawer.drawLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, titleFont, FONT_GROUP_SIZE);
            }
        }
    }
}
