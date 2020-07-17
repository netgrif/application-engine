package com.netgrif.workflow.pdf.generator.service;

import com.netgrif.workflow.pdf.generator.config.PdfResources;
import com.netgrif.workflow.pdf.generator.domain.PdfField;
import com.netgrif.workflow.pdf.generator.service.interfaces.IDataConverter;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.workflow.domain.Case;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Generates PDF from the given transition form
 * */
@Service
public class PdfGenerator extends PdfResources implements IPdfGenerator {

    private PDDocument pdf;

    @Autowired
    private IDataConverter dataConverter;

    @Autowired
    private IPdfDrawer pdfDrawer;


    private void constructPdfGenerator() throws IOException {
        this.pdf = new PDDocument();

        File fontTitleFile = fontTitleResource.getFile();
        File fontLabelFile = fontLabelResource.getFile();
        File fontValueFile = fontValueResource.getFile();

        setTitleFont(PDType0Font.load(this.pdf, new FileInputStream(fontTitleFile), true));
        setLabelFont(PDType0Font.load(this.pdf, new FileInputStream(fontLabelFile), true));
        setValueFont(PDType0Font.load(this.pdf, new FileInputStream(fontValueFile), true));

//        this.setCheckboxChecked(PDImageXObject.createFromFile(checkBoxCheckedPath, this.getPdf()))
//        this.setCheckboxUnchecked(PDImageXObject.createFromFile(checkBoxUnCheckedPath, this.getPdf()))
//        this.setRadioChecked(PDImageXObject.createFromFile(radioCheckedPath, this.getPdf()))
//        this.setRadioUnchecked(PDImageXObject.createFromFile(radioUnCheckedPath, this.getPdf()))
    }

    @Override
    public void convertCaseForm(Case formCase, String transitionId) throws IOException {
        dataConverter.setDataGroups(formCase.getPetriNet().getTransitions().get(transitionId).getDataGroups());
        dataConverter.setDataSet(formCase.getDataSet());
        dataConverter.generateTitleField(formCase.getPetriNet().getTransitions().get(transitionId).getTitle().toString());
        dataConverter.generatePdfFields();
        dataConverter.generatePdfDataGroups();
        dataConverter.correctCoveringFields();

        constructPdfGenerator();
        pdfDrawer.setupDrawer(pdf);

        try {
            File result = transformRequestToPdf(dataConverter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File transformRequestToPdf(IDataConverter dataHelper) throws IOException {
        File out = new File("src/main/resources/out.pdf");
        pdfDrawer.newPage();
        drawTransitionForm(dataHelper);
        pdfDrawer.closeContentStream();
        if (pdf == null)
            throw new IllegalArgumentException("[$pdf] is null  ");
        pdf.save(out);
        pdf.close();
        return out;
    }

    /**
     * Parses the input data groups and data field to a PDF file and draws to a PDF file using corresponding
     * functions
     * */
    void drawTransitionForm(IDataConverter dataHelper) throws IOException {
        int fieldX, fieldY, fieldWidth, fieldHeight;
        String label, value;
        List<PdfField> pdfFields = dataHelper.getPdfFields();

        for(PdfField pdfField : pdfFields){
            fieldX = pdfField.getX();
            fieldY = pdfField.getBottomY();
            fieldWidth = pdfField.getWidth();
            fieldHeight = pdfField.getHeight();
            label = pdfField.getLabel();
            value = pdfField.getValue();

            if(pdfField.getFieldId().equals("titleField")){
                pdfDrawer.drawTitle(label, fieldWidth);
            }else if(!pdfField.isDgField()) {
                switch (pdfField.getType()) {
                    case TEXT:
                        pdfDrawer.drawTextField(label, value, fieldX, fieldY, fieldWidth, fieldHeight);
                        break;
                    case NUMBER:
                        pdfDrawer.drawTextField(label, value, fieldX, fieldY, fieldWidth, fieldHeight);
                        break;
                }
            }else{
                pdfDrawer.drawFieldLabel(label, fieldX, fieldY, fieldWidth, fieldHeight, titleFont, FONT_GROUP_SIZE);
            }
        }
    }
}
