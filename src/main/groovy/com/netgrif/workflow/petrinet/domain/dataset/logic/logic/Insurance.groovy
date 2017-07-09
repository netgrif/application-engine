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

    File offerPDF() {
        String name = "offer.pdf"
        File input = new File("src/main/resources/pdf/zmluva_editovatelna.pdf")
        String xml = datasetToXml()

        File pdf = PdfFormFiller.fillPdfForm(name, input, xml)
        useCase.dataSet.get(field.objectId).value = name

        return pdf
    }

    private String datasetToXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields() {
            TextField50('xfdf:original' : "Text Field 50", "${value('Právna subjektivita poisteného?')}")
            TextField70('xfdf:original' : "Text Field 70", "${value('Obec')}")
        }

        return writer.toString()
    }

    private String value(String name) {
        return useCase.dataSet[useCase.petriNet.dataSet.find {it.value.name == name}?.key]?.value?.toString()
    }
}