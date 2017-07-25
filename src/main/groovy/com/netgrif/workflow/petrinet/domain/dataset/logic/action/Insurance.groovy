package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.pdf.service.PdfUtils
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.utils.DateUtils
import com.netgrif.workflow.workflow.domain.Case
import groovy.xml.MarkupBuilder
import org.apache.tomcat.jni.Local

import java.time.LocalDate

class Insurance {

    private Case useCase
    private Field field

    Insurance(Case useCase, Field field) {
        this.useCase = useCase
        this.field = field
    }

    File offerPDF() {
        String name = "offer.pdf"
        String pdfPath = (field as FileField).getFilePath(name)
        File input = new File("src/main/resources/pdf/test.pdf")
        String xml = datasetToXml()

        File pdf = PdfUtils.fillPdfForm(pdfPath, new FileInputStream(input), xml)
        useCase.dataSet.get(field.objectId).setValue(name)

        return pdf
    }

    String offerId() {
        def id = new File("src/test/resources/counter.txt").getText()

        def prefix = "311"
        def base = id.padLeft(6, "0")
        def postfix = 0
        prefix.each {
            postfix += it as Long
        }
        base.each {
            postfix += it as Long
        }
        postfix %= 10

        new File("src/test/resources/counter.txt").setText(((id as Long) + 1) as String)
        useCase.dataSet.get(field.objectId).setValue("${prefix}${base}${postfix}" as String)

        return "${prefix}${base}${postfix}"
    }

    private String datasetToXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            field('xfdf:original': "Text Field 143", "${value(309001) ?: ''}")
//            Poistník
            field('xfdf:original': "Text Field 50", "${value(109007) ?: ''}")
            field('xfdf:original': "Text Field 51", "${value(109008) ?: ''}")
            field('xfdf:original': "1", "${value(109010) ?: ''} ${value(109011) ?: ''}")
            field('xfdf:original': "2", "${value(109012) ?: ''}")
            field('xfdf:original': "3", "${value(109009) ?: ''}")
            field('xfdf:original': "4", "${value(109014) ?: ''}")
            field('xfdf:original': "5", "${value(109015) ?: ''}") // todo: IČO
            field('xfdf:original': "6", "${value(109013) ?: ''}")
            field('xfdf:original': "7", "${value(109016) ?: ''}")
            field('xfdf:original': "8", "${value(109017) ?: ''}")
            field('xfdf:original': "9", "${value(109018) ?: ''}")
            field('xfdf:original': "10", "${value(109019) ?: ''}")
//            Poistený
            field('xfdf:original': "400", "${value(109021) ?: ''}")
            field('xfdf:original': "401", "${value(109022) ?: ''}")
            field('xfdf:original': "402", "${value(109024) ?: ''} ${value(109025) ?: ''}")
            field('xfdf:original': "403", "${value(109026) ?: ''}")
            field('xfdf:original': "404", "${value(109023) ?: ''}")
            field('xfdf:original': "405", "${value(109028) ?: ''}")
            field('xfdf:original': "406", "${value(109029) ?: ''}") // todo: IČO
            field('xfdf:original': "407", "${value(109027) ?: ''}")
            field('xfdf:original': "408", "${value(109030) ?: ''}")
            field('xfdf:original': "409", "${value(109031) ?: ''}")
            field('xfdf:original': "410", "${value(109032) ?: ''}")
            field('xfdf:original': "411", "${value(109033) ?: ''}")
//            Adresa trvalého bydliska / Sídlo
            field('xfdf:original': "11", "${value(109036) ?: ''}")
            field('xfdf:original': "12", "${value(109037) ?: ''}")
            field('xfdf:original': "13", "${value(109038) ?: ''}")
            field('xfdf:original': "14", "${value(109039) ?: ''}")
//            Korešpondenčná adresa
            field('xfdf:original': "16", "${value(109041) ?: ''}")
            field('xfdf:original': "17", "${value(109042) ?: ''}")
            field('xfdf:original': "18", "${value(109043) ?: ''}")
            field('xfdf:original': "19", "${value(109044) ?: ''}")
//            Miesto poistenia
            field('xfdf:original': "21", "${value(109045) ?: ''}")
            field('xfdf:original': "22", "${value(109046) ?: ''}")
            field('xfdf:original': "23", "${value(109047) ?: ''}")
            field('xfdf:original': "24", "${value(109048) ?: ''}")
//           Údaje o poistení
            field('xfdf:original': "106", "${value(309002) ?: ''}")
            field('xfdf:original': "107", "${value(109001) ?: ''}")
            field('xfdf:original': "108", "${value(109003) ?: ''}")
//            Údaje o poistnom
            field('xfdf:original': "109", "${value(108001) ?: ''}")
//            Spoluúčasť pri poistnom plnení
            field('xfdf:original': "453", "${value(105005) ?: ''}")
            field('xfdf:original': "775", "${value(106001) ?: ''}")
//            Rekapitulácia poistného
            field('xfdf:original': "110", "${(value(308001) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "111", "${(value(208006) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "112", "${(value(305003) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "113", "${(value(208004) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "114", "${(value(308002) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "115", "${(value(203004) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "116", "${(value(308003) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "117", "${(value(208008) as Double).round(2) as String ?: ''}")
//            field('xfdf:original': "118", "${value()}") // todo: Doplnkové poistenie
            field('xfdf:original': "119", "${(value(308006) as Double).round(2) as String ?: ''}")
            field('xfdf:original': "120", "${(value(308004) as Double).round(2) as String ?: ''}")
//            field('xfdf:original': "121", "${value()}") // todo: Výška splátky poistného
//            Informácie na úhradu poistného
            field('xfdf:original': "126", "${value(308007) ?: ''}")
//            Vinkulácia poistného plnenia
            field('xfdf:original': "129", "${value(109004) ?: ''}")
            field('xfdf:original': "130", "${value(109005) ?: ''}")
//            field('xfdf:original': "131", "${value()}") // todo: Zriaďuje sa indexácia poistnej sumy?
            field('xfdf:original': "Text Field 153", "${value(309001) ?: ''}")
        }

        return writer.toString()
    }

    private String value(Long id) {
        return useCase.dataSet[useCase.petriNet.dataSet.find { it.value.importId == id }?.key]?.value?.toString()
    }
}