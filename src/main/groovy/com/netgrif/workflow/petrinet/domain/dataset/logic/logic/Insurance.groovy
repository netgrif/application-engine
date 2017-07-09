package com.netgrif.workflow.petrinet.domain.dataset.logic.logic

import com.netgrif.workflow.pdf.service.PdfFormFiller
import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.workflow.domain.Case
import groovy.xml.MarkupBuilder


class Insurance {

    private Case useCase
    private FileField field

    Insurance(Case useCase, FileField field) {
        this.useCase = useCase
        this.field = field
    }

    File offerPDF(){
        String name = "offer.pdf"
        File input = new File("src/main/resources/pdf/zmluva_editovatelna.pdf")
        File xml = new File("src/main/resources/pdf/zmluva_editovatelna.xml")

        File pdf = PdfFormFiller.fillPdfForm(name, new FileInputStream(input), new FileInputStream(xml))
        useCase.dataSet.get(field.objectId).value = name

        return pdf
    }
}