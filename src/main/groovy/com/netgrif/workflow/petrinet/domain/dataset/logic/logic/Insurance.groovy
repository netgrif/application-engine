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
        File input = new File("src/main/resources/pdf/test.pdf")
        String xml = datasetToXml()

        File pdf = PdfFormFiller.fillPdfForm(name, input, xml)
        useCase.dataSet.get(field.objectId).value = name

        return pdf
    }

    private String datasetToXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields() {
            field('xfdf:original': "Text Field 143", "${value(163)}")
//            Poistník
            field('xfdf:original': "Text Field 50", "${value(118)}")
            field('xfdf:original': "Text Field 51", "${value(119)}")
            field('xfdf:original': "1", "${value(121)} ${value(122)}")
//            field('xfdf:original': "2", "${value()}") // todo: obchodné meno
            field('xfdf:original': "3", "${value(120)}")
            field('xfdf:original': "4", "${value(124)}")
            field('xfdf:original': "5", "${value(125)}") // todo: IČO
            field('xfdf:original': "6", "${value(123)}")
            field('xfdf:original': "7", "${value(126)}")
            field('xfdf:original': "8", "${value(127)}")
//            field('xfdf:original': "9", "${value()}") // todo: tel. číslo
//            field('xfdf:original': "10", "${value()}") // todo: email
//            Poistený
            field('xfdf:original': "400", "${value(129)}")
            field('xfdf:original': "401", "${value(130)}")
            field('xfdf:original': "402", "${value(132)} ${value(133)}")
//            field('xfdf:original': "403", "${value()}") // todo: obchodné meno
            field('xfdf:original': "404", "${value(131)}")
            field('xfdf:original': "405", "${value(135)}")
            field('xfdf:original': "406", "${value(136)}") // todo: IČO
            field('xfdf:original': "407", "${value(134)}")
            field('xfdf:original': "408", "${value(137)}")
            field('xfdf:original': "409", "${value(138)}")
//            field('xfdf:original': "410", "${value()}") // todo: tel. číslo
//            field('xfdf:original': "411", "${value()}") // todo: email
//            Adresa trvalého bydliska / Sídlo // todo: ?
            field('xfdf:original': "11", "${value()}") 27
            field('xfdf:original': "12", "${value()}") 28
            field('xfdf:original': "13", "${value()}") 29
            field('xfdf:original': "14", "${value()}") 30
//            Korešpondenčná adresa // todo: ?
            field('xfdf:original': "16", "${value()}") 31
            field('xfdf:original': "17", "${value()}") 32
            field('xfdf:original': "18", "${value()}") 33
            field('xfdf:original': "19", "${value()}") 34
//            Miesto poistenia
            field('xfdf:original': "21", "${value(147)}")
            field('xfdf:original': "22", "${value(148)}")
            field('xfdf:original': "23", "${value(149)}")
            field('xfdf:original': "24", "${value(150)}")
//           Údaje o poistení
            field('xfdf:original': "106", "${value(162)}")
            field('xfdf:original': "107", "${value(112)}")
            field('xfdf:original': "108", "${value(114)}")
//            Údaje o poistnom
            field('xfdf:original': "109", "${value(70)}")
//            Spoluúčasť pri poistnom plnení
            field('xfdf:original': "453", "${value(35)}")
            field('xfdf:original': "775", "${value(52)}")
//            Rekapitulácia poistného
            field('xfdf:original': "110", "${value(66)}")
            field('xfdf:original': "111", "${value(109)}")
            field('xfdf:original': "112", "${value(174)}")
            field('xfdf:original': "113", "${value(106)}")
            field('xfdf:original': "114", "${value(67)}")
            field('xfdf:original': "115", "${value(100)}")
            field('xfdf:original': "116", "${value(68)}")
            field('xfdf:original': "117", "${value(107)}")
//            field('xfdf:original': "118", "${value()}") // todo: Doplnkové poistenie
            field('xfdf:original': "119", "${value(75)}")
            field('xfdf:original': "120", "${value(69)}")
//            field('xfdf:original': "121", "${value()}") // todo: Výška splátky poistného
//            Informácie na úhradu poistného
            field('xfdf:original': "126", "${value(161)}")
//            Vinkulácia poistného plnenia
            field('xfdf:original': "129", "${value(115)}")
            field('xfdf:original': "130", "${value(116)}")
//            field('xfdf:original': "131", "${value()}") // todo: Zriaďuje sa indexácia poistnej sumy?
            field('xfdf:original': "Text Field 153", "${value(163)}")
        }

        return writer.toString()
    }

    private String value(Long id) {
        return useCase.dataSet[useCase.petriNet.dataSet.find { it.value.importId == id }?.key]?.value?.toString()
    }
}