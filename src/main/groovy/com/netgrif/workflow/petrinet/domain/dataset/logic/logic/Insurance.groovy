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
            field('xfdf:original': "Text Field 143", "${value(163)}")
            field('xfdf:original': "Text Field 50", "${value(17)}")
            field('xfdf:original': "Text Field 51", "${value(119)}")
            field('xfdf:original': "Text Field 52", "${value(121)} ${value(122)}")
//            field('xfdf:original': "Text Field 55", "${value()}") 7
            field('xfdf:original': "Text Field 57", "${value(135)}")
            field('xfdf:original': "Text Field 58", "${value(120)}")
            field('xfdf:original': "Text Field 59", "${value(124)}")
            field('xfdf:original': "Text Field 60", "${value(125)}")
            field('xfdf:original': "Text Field 61", "${value(134)}")
            field('xfdf:original': "Text Field 62", "${value(137)}")
            field('xfdf:original': "Text Field 63", "${value(138)}")
//            field('xfdf:original': "Text Field 64", "${value()}") 16
//            field('xfdf:original': "Text Field 65", "${value()}") 17
//            field('xfdf:original': "Text Field 66", "${value()}") 18
//            field('xfdf:original': "Text Field 67", "${value()}") 19
//            field('xfdf:original': "Text Field 68", "${value()}") 20
//            field('xfdf:original': "Text Field 69", "${value()}") 21
//            field('xfdf:original': "Text Field 70", "${value()}") 22
//            field('xfdf:original': "Text Field 71", "${value()}") 23
//            field('xfdf:original': "Text Field 72", "${value()}") 24
//            field('xfdf:original': "Text Field 73", "${value()}") 25
//            field('xfdf:original': "Text Field 74", "${value()}") 26
//            field('xfdf:original': "Text Field 75", "${value()}") 27
            field('xfdf:original': "Text Field 76", "${value(147)}")
            field('xfdf:original': "Text Field 77", "${value(148)}")
            field('xfdf:original': "Text Field 78", "${value(149)}")
            field('xfdf:original': "Text Field 79", "${value(150)}")
            field('xfdf:original': "Text Field 150", "${value(163)}")
            field('xfdf:original': "Text Field 4", "${value(19)}")
            field('xfdf:original': "Text Field 5", "${value(20)}")
            field('xfdf:original': "Text Field 6", "${value(23)}")
            field('xfdf:original': "Text Field 8", "${value(22)}")
            field('xfdf:original': "Text Field 10", "${value(24)}")
            field('xfdf:original': "Text Field 11", "${value(25)}")
            field('xfdf:original': "Text Field 12", "${value(26)}")
//            field('xfdf:original': "Text Field 13", "${value()}") 40
//            field('xfdf:original': "Text Field 14", "${value()}") 41
//            field('xfdf:original': "Text Field 15", "${value()}") 42
//            field('xfdf:original': "Text Field 16", "${value()}") 43
//            field('xfdf:original': "Text Field 17", "${value()}") 44
//            field('xfdf:original': "Text Field 18", "${value()}") 45
            field('xfdf:original': "Text Field 19", "${value(39)}")
            field('xfdf:original': "Text Field 20", "${value(40)}")
            field('xfdf:original': "Text Field 21", "${value(41)}")
            field('xfdf:original': "Text Field 22", "${value(42)}")
            field('xfdf:original': "Text Field 23", "${value(43)}")
            field('xfdf:original': "Text Field 24", "${value(44)}")
            field('xfdf:original': "Text Field 25", "${value(45)}")
            field('xfdf:original': "Text Field 26", "${value(46)}")
            field('xfdf:original': "Text Field 27", "${value(47)}")
            field('xfdf:original': "Text Field 28", "${value(48)}")
            field('xfdf:original': "Text Field 29", "${value(49)}")
            field('xfdf:original': "Text Field 30", "${value(175)}")
            field('xfdf:original': "Text Field 31", "${value(176)}")
            field('xfdf:original': "Text Field 32", "${value(177)}")
            field('xfdf:original': "Text Field 33", "${value(178)}")
            field('xfdf:original': "Text Field 34", "${value(179)}")
            field('xfdf:original': "Text Field 35", "${value(180)}")
            field('xfdf:original': "Text Field 36", "${value(181)}")
            field('xfdf:original': "Text Field 37", "${value(182)}")
            field('xfdf:original': "Text Field 38", "${value(183)}")
            field('xfdf:original': "Text Field 39", "${value(184)}")
            field('xfdf:original': "Text Field 40", "${value(185)}")
//            field('xfdf:original': "Text Field 41", "${value()}") 68
            field('xfdf:original': "Text Field 42", "${value(37)}")
            field('xfdf:original': "Text Field 43", "${value(170)}")
            field('xfdf:original': "Text Field 44", "${value(167)}")
            field('xfdf:original': "Text Field 45", "${value(168)}")
            field('xfdf:original': "Text Field 46", "${value(169)}")
//            field('xfdf:original': "Text Field 47", "${value()}") 74
//            field('xfdf:original': "Text Field 48", "${value()}") 75
            field('xfdf:original': "Text Field 49", "${value(32)}")
            field('xfdf:original': "Text Field 151", "${value(163)}")
            field('xfdf:original': "Text Field 80", "${value(27)}")
            field('xfdf:original': "Text Field 81", "${value(28)}")
            field('xfdf:original': "Text Field 82", "${value(29)}")
            field('xfdf:original': "Text Field 83", "${value(30)}")
            field('xfdf:original': "Text Field 84", "${value(31)}")
//            field('xfdf:original': "Text Field 85", "${value()}") 83
            field('xfdf:original': "Text Field 86", "${value(53)}")
            field('xfdf:original': "Text Field 87", "${value(191)}")
//            field('xfdf:original': "Text Field 88", "${value()}") 86
//            field('xfdf:original': "Text Field 89", "${value()}") 87
//            field('xfdf:original': "Text Field 90", "${value()}") 88
//            field('xfdf:original': "Text Field 91", "${value()}") 89
//            field('xfdf:original': "Text Field 92", "${value()}") 90
//            field('xfdf:original': "Text Field 93", "${value()}") 91
//            field('xfdf:original': "Text Field 94", "${value()}") 92
//            field('xfdf:original': "Text Field 95", "${value()}") 93
//            field('xfdf:original': "Text Field 96", "${value()}") 94
//            field('xfdf:original': "Text Field 97", "${value()}") 95
//            field('xfdf:original': "Text Field 98", "${value()}") 96
//            field('xfdf:original': "Text Field 99", "${value()}") 97
//            field('xfdf:original': "Text Field 100", "${value()}") 98
//            field('xfdf:original': "Text Field 101", "${value()}") 99
//            field('xfdf:original': "Text Field 102", "${value()}") 100
//            field('xfdf:original': "Text Field 103", "${value()}") 101
            field('xfdf:original': "Text Field 105", "${value(54)}")
            field('xfdf:original': "Text Field 106", "${value(192)}")
            field('xfdf:original': "Text Field 107", "${value(55)}")
            field('xfdf:original': "Text Field 108", "${value(193)}")
            field('xfdf:original': "Text Field 109", "${value(56)}")
            field('xfdf:original': "Text Field 110", "${value(194)}")
            field('xfdf:original': "Text Field 111", "${value(57)}")
            field('xfdf:original': "Text Field 112", "${value(195)}")
            field('xfdf:original': "Text Field 113", "${value(58)}")
            field('xfdf:original': "Text Field 114", "${value(196)}")
            field('xfdf:original': "Text Field 115", "${value(59)}")
            field('xfdf:original': "Text Field 116", "${value(197)}")
            field('xfdf:original': "Text Field 117", "${value(60)}")
            field('xfdf:original': "Text Field 118", "${value(198)}")
            field('xfdf:original': "Text Field 104", "${value(32)}")
            field('xfdf:original': "Text Field 152", "${value(163)}")
            field('xfdf:original': "Text Field 119", "${value(162)}")
            field('xfdf:original': "Text Field 120", "${value(112)}")
            field('xfdf:original': "Text Field 121", "${value(114)}")
            field('xfdf:original': "Text Field 122", "${value(70)}")
            field('xfdf:original': "Text Field 123", "${value(36)}")
//            field('xfdf:original': "Text Field 124", "${value()}") 123
            field('xfdf:original': "Text Field 125", "${value(53)}")
            field('xfdf:original': "Text Field 126", "${value(68)}")
//            field('xfdf:original': "Text Field 127", "${value()}") 126
            field('xfdf:original': "Text Field 128", "${value(69)}")
            field('xfdf:original': "Text Field 129", "${value(109)}")
            field('xfdf:original': "Text Field 130", "${value(106)}")
//            field('xfdf:original': "Text Field 131", "${value()}") 130
            field('xfdf:original': "Text Field 132", "${value(107)}")
//            field('xfdf:original': "Text Field 133", "${value()}") 132
//            field('xfdf:original': "Text Field 134", "${value()}") 133
//            field('xfdf:original': "Text Field 135", "${value()}") 134
            field('xfdf:original': "Text Field 136", "${value(145)}")
//            field('xfdf:original': "Text Field 137", "${value()}") 136
            field('xfdf:original': "Text Field 138", "${value(161)}")
//            field('xfdf:original': "Text Field 139", "${value()}") 138
            field('xfdf:original': "Text Field 140", "${value(115)}")
            field('xfdf:original': "Text Field 141", "${value(116)}")
//            field('xfdf:original': "Text Field 142", "${value()}") 141
            field('xfdf:original': "Text Field 153", "${value(163)}")
//            field('xfdf:original': "Text Field 157", "${value()}") 143
//            field('xfdf:original': "Text Field 159", "${value()}") 144
//            field('xfdf:original': "Text Field 160", "${value()}") 145
//            field('xfdf:original': "Text Field 144", "${value()}") 146
//            field('xfdf:original': "Text Field 145", "${value()}") 147
//            field('xfdf:original': "Text Field 147", "${value()}") 148
//            field('xfdf:original': "Text Field 148", "${value()}") 149
//            field('xfdf:original': "Text Field 149", "${value()}") 150
            field('xfdf:original': "Text Field 154", "${value(163)}")
//            field('xfdf:original': "Text Field 156", "${value()}") 152
//            field('xfdf:original': "field 157", "${value()}") 153
            field('xfdf:original': "Text Field 155", "${value(163)}")
        }

        return writer.toString()
    }

    private String value(Long id) {
        return useCase.dataSet[useCase.petriNet.dataSet.find {it.value.importId == id}?.key]?.value?.toString()
    }
}