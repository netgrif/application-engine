package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.pdf.service.PdfUtils
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.workflow.domain.Case
import groovy.xml.MarkupBuilder

class Insurance {

    private final static String PDF_PATH = "src/main/resources/pdf"
    private final static String OFFER_FILENAME = "offer.pdf"
    private final static String DRAFT_FILENAME = "draft.pdf"
    private final static String FINAL_FILENAME = "final.pdf"

    private Case useCase
    private Field field

    Insurance(Case useCase, Field field) {
        this.useCase = useCase
        this.field = field
    }

    File offerPDF() {
        String draftPath = (field as FileField).getFilePath(DRAFT_FILENAME)
        String offerPath = (field as FileField).getFilePath(OFFER_FILENAME)
        String draftXml = datasetToDraftXml()
        String offerXml = datasetToOfferXml()

        File draftPdfFile = PdfUtils.fillPdfForm(draftPath, new FileInputStream("$PDF_PATH/$OFFER_FILENAME"), draftXml)
        File offerPdfFile = PdfUtils.fillPdfForm(offerPath, new FileInputStream("$PDF_PATH/$DRAFT_FILENAME"), offerXml)
        File mergedPdf = PdfUtils.mergePdfFiles(FINAL_FILENAME, draftPdfFile, offerPdfFile)

        useCase.dataSet.get(field.stringId).setValue(FINAL_FILENAME)

        return mergedPdf
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
        useCase.dataSet.get(field.stringId).setValue("${prefix}${base}${postfix}" as String)

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
            field('xfdf:original': "131", "${value(109060) ? 'Áno' : 'Nie'}")
            field('xfdf:original': "Text Field 153", "${value(309001) ?: ''}")
        }

        return writer.toString()
    }

    private String datasetToXmlPremioveByvanie() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            field('xfdf:original': "Text Field 143", "${value(309001) ?: ''}")
            field('xfdf:original': "Text Field 50", "${value(109007) ?: ''}")
            fxtag('xfdf:original': "400", "${value(109021) ?: ''}")
            field('xfdf:original': "Text Field 51", "${value(109008) ?: ''}")
            fxtag('xfdf:original': "401", "${value(109022) ?: ''}")
            fxtag('xfdf:original': "1", "${value(109010) ?: ''} ${value(109011) ?: ''}")
            fxtag('xfdf:original': "402", "${value(109024) ?: ''} ${value(109025) ?: ''}")
            fxtag('xfdf:original': "2", "${value(109009) ?: ''}")
            fxtag('xfdf:original': "403", "${value(109023) ?: ''}")
            fxtag('xfdf:original': "3", "${value(109012) ?: ''}")
            fxtag('xfdf:original': "404", "${value(109026) ?: ''}")
            fxtag('xfdf:original': "4", "${value(109061) ?: ''}")
            fxtag('xfdf:original': "405", "${value(109063) ?: ''}")
            fxtag('xfdf:original': "5", "${value(109062) ?: ''}")
            fxtag('xfdf:original': "406", "${value(109064) ?: ''}")
            fxtag('xfdf:original': "6", "${value(109014) ?: ''}")
            fxtag('xfdf:original': "407", "${value(109025) ?: ''}")
            fxtag('xfdf:original': "7", "${value(109015) ?: (value(109058) ?: '')}")
            fxtag('xfdf:original': "408", "${value(109029) ?: (value(109059) ?: '')}")
            fxtag('xfdf:original': "8", "${value(109013) ?: ''}") // todo: štát 109060
            fxtag('xfdf:original': "409", "${value(109027) ?: ''}")
            fxtag('xfdf:original': "9", "${value(109016) ?: ''}")
            fxtag('xfdf:original': "410", "${value(109030) ?: ''}")
            gbgbgbhhgfvfv("${value(109017) ?: ''}")
            kjzu("${value(109031) ?: ''}")
            ggrrjkcmcbsfaeqrwjel('xfdf:original': "ggrrťjkcmcbsfaeqrwjel", "${value(109018) ?: ''}")
            sddgbnnmzzrdfhhbgbgb("${value(109032) ?: ''}")
            ffffgfgfg("${value(109019) ?: ''}")
            vsvvvggbnmgg('xfdf:original': "vsvvvggbnm,,gg", "${value(109033) ?: ''}")
//          Adresa trvalého bydliska / Sídlo
            fxtag('xfdf:original': "11", "${value(109039) ?: ''}")
            fxtag('xfdf:original': "12", "${value(109036) ?: ''}")
            fxtag('xfdf:original': "13", "${value(109037) ?: ''}")
            fxtag('xfdf:original': "14", "${value(109038) ?: ''}")
//          Korešpondenčná adresa
            fxtag('xfdf:original': "16", "${value(109045) ?: ''}")
            fxtag('xfdf:original': "17", "${value(109042) ?: ''}")
            fxtag('xfdf:original': "18", "${value(109043) ?: ''}")
            fxtag('xfdf:original': "19", "${value(109044) ?: ''}")
//          Miesto poistenia
            fxtag('xfdf:original': "21", "${value(109048) ?: ''}")
            fxtag('xfdf:original': "22", "${value(109045) ?: ''}")
            fxtag('xfdf:original': "23", "${value(109046) ?: ''}")
            fxtag('xfdf:original': "24", "${value(109047) ?: ''}")
//          Údaje o poistení
            fxtag('xfdf:original': "106", "${value(309002) ?: ''}")
            fxtag('xfdf:original': "107", "${value(109001) ?: ''}")
            fxtag('xfdf:original': "108", "${value(109002) ?: ''}")
//          Údaje o poistnom
            fxtag('xfdf:original': "109", "${value(108001) ?: ''}")
//          Spoluúčasť pri poistnom plnení
            fxtag('xfdf:original': "453", "${value(105005) ?: ''}")
            fxtag('xfdf:original': "775", "${value(106001) ?: ''}")
//          Rekapitulácia poistného | Zľavy
            fxtag('xfdf:original': "110", "${value(305002) ?: ''}")
            fxtag('xfdf:original': "111", "${value(108003) ?: ''}")
            fxtag('xfdf:original': "112", 51) // todo: vedlajsie stavby/garaz
            fxtag('xfdf:original': "113", 52)
            fxtag('xfdf:original': "114", 53)
            fxtag('xfdf:original': "115", 54)
            fxtag('xfdf:original': "116", 55)
            fxtag('xfdf:original': "117", 56)
            fxtag('xfdf:original': "118", 57)
            fxtag('xfdf:original': "119", 58)
            fxtag('xfdf:original': "120", "${value(308004) ?: ''}")
            fxtag('xfdf:original': "121", "${value(108004) ?: ''}")
            fxtag('xfdf:original': "89323", "${value(308006) ?: ''}")
            mjjttzeelljsd('xfdf:original': "mjjttzeželljsd", 66)
//          Splatnosť poistného a informácie na úhradu poistného
            fxtag('xfdf:original': "126", "${value(308007) ?: ''}")
//          Vinkulácia poistného plnenia
            fxtag('xfdf:original': "129", "${value(109004) ?: ''}")
            fxtag('xfdf:original': "130", "${value(109005) ?: ''}")
//          Indexácia poistnej sumy
            fxtag('xfdf:original': "131", "${value(109060) ?: ''}")
//          Základné informácie
            field('xfdf:original': "Text Field 226", "${value(101002) ?: ''}")
            field('xfdf:original': "Text Field 238", "${value(101003) ?: ''}")
            field('xfdf:original': "Text Field 239", "${value(101004) ?: ''}")
            field('xfdf:original': "Text Field 240", "${value(101005) ?: ''}")
            field('xfdf:original': "Text Field 241", "${value(101006) ?: ''}")
            field('xfdf:original': "Text Field dadadsasda", "${value(101007) ?: ''}")
            field('xfdf:original': "Text Field 243", "${value(101009) ?: ''}")
            field('xfdf:original': "Text Field 244", "${value(101010) ?: ''}")
            field('xfdf:original': "Text Field 245", "${value(101011) ?: ''}")
            field('xfdf:original': "Text Field 246", "${value(101012) ?: ''}")
            field('xfdf:original': "Text Field 247", "${value(101013) ?: ''}")
            field('xfdf:original': "Text Field 248", "${value(101014) ?: ''}")
            field('xfdf:original': "Text Field 249", "${value(101016) ?: ''}")
            dasdasdda("${value(101008) ?: ''}")
//          Poistenie nehnuteľnosti
            fxtag('xfdf:original': "25", "${value(102001) ?: ''}")
            fxtag('xfdf:original': "26", "${value(102002) ?: ''}")
            fxtag('xfdf:original': "27", "${value(102003) ?: ''}")
            fxtag('xfdf:original': "28", "${value(102005) ?: ''}")
            fxtag('xfdf:original': "29", "${value(102004) ?: ''}")
            fxtag('xfdf:original': "30", "${value(102006) ?: ''}")
            fxtag('xfdf:original': "31", "${value(102007) ?: ''}")
            ZHSNSAKXCAKS("${value(102008) ?: ''}")
//
            fxtag('xfdf:original': "32", "${value(105001) ?: ''}")
            fxtag('xfdf:original': "33", "${value(105002) ?: ''}")
            fxtag('xfdf:original': "34", "${value(105003) ?: ''}")
            fxtag('xfdf:original': "35", "${value(305001) ?: ''}")
            fxtag('xfdf:original': "36", "${value() ?: ''}")// todo Poistná suma
            fxtag('xfdf:original': "37", "${value() ?: ''}")// todo poistne
//          Poistenie vedľajších stavieb
            fxtag('xfdf:original': "500", "${value(105010) ?: ''}")
            fxtag('xfdf:original': "513", "${value(305004) ?: ''}")
            fxtag('xfdf:original': "501", "${value(105012) ?: ''}")
            fxtag('xfdf:original': "514", "${value(305005) ?: ''}")
            fxtag('xfdf:original': "502", "${value(105014) ?: ''}")
            fxtag('xfdf:original': "515", "${value(305006) ?: ''}")
            fxtag('xfdf:original': "503", "${value(105016) ?: ''}")
            fxtag('xfdf:original': "516", "${value(305007) ?: ''}")
            fxtag('xfdf:original': "504", "${value(105018) ?: ''}")
            fxtag('xfdf:original': "517", "${value(305008) ?: ''}")
            fxtag('xfdf:original': "505", "${value(105020) ?: ''}")
            fxtag('xfdf:original': "518", "${value(305009) ?: ''}")
            fxtag('xfdf:original': "506", "${value(105022) ?: ''}")
            fxtag('xfdf:original': "519", "${value(305010) ?: ''}")
            fxtag('xfdf:original': "507", "${value(105024) ?: ''}")
            fxtag('xfdf:original': "520", "${value(305011) ?: ''}")
            fxtag('xfdf:original': "508", "${value(105026) ?: ''}")
            fxtag('xfdf:original': "521", "${value(305012) ?: ''}")
            fxtag('xfdf:original': "509", "${value(105028) ?: ''}")
            fxtag('xfdf:original': "522", "${value(305013) ?: ''}")
            fxtag('xfdf:original': "510", "${value(105030) ?: ''}")
            fxtag('xfdf:original': "523", "${value(305014) ?: ''}")
//
            fxtag('xfdf:original': "527", "${value(105004) ?: ''}")
            fxtag('xfdf:original': "525", "${value(105007) ?: ''}")
            fxtag('xfdf:original': "526", "${value(305003) ?: ''}")
//
            fxtag('xfdf:original': "38", "${value(109057) ?: ''}")
            fxtag('xfdf:original': "39", "${value(109054) ?: ''}")
            fxtag('xfdf:original': "40", "${value(109055) ?: ''}")
            fxtag('xfdf:original': "41", "${value(109056) ?: ''}")
//
            asdasdasdadd("${value(105032) ?: ''}")
            fxtag('xfdf:original': "529", "${value(305015) ?: ''}")
            fxtag('xfdf:original': "528", "${value(105034) ?: ''}")
            fxtag('xfdf:original': "530", "${value(305016) ?: ''}")
//          Poistenie zodpovednosti za škodu - nehnuteľnosť
            fxtag('xfdf:original': "531", "${value(107001) ?: ''}")
            fxtag('xfdf:original': "532", "${value(308008) ?: ''}")
//          Poistenie domácnosti
            fxtag('xfdf:original': "67", "${value(103001) ?: ''}")
            fxtag('xfdf:original': "68", "${value(103002) ?: ''}")
            fxtag('xfdf:original': "69", "${value(103003) ?: ''}")
            fxtag('xfdf:original': "70", "${value(103004) ?: ''}")
            fxtag('xfdf:original': "71", "${value(103005) ?: ''}")
            fxtag('xfdf:original': "72", "${value(106003) ?: ''}")
            fxtag('xfdf:original': "73", 139) // todo Poistna suma
            fxtag('xfdf:original': "74", "${value(308002) ?: ''}")
//          Doplnkové poistenie domácnosti
            fxtag('xfdf:original': "91", "${value(106005) ?: ''}")
            fxtag('xfdf:original': "92", "${value(306002) ?: ''}")
            fxtag('xfdf:original': "93", "${value(106007) ?: ''}")
            fxtag('xfdf:original': "94", "${value(306003) ?: ''}")
            fxtag('xfdf:original': "95", "${value(106009) ?: ''}")
            fxtag('xfdf:original': "96", "${value(306004) ?: ''}")
            fxtag('xfdf:original': "97", "${value(106011) ?: ''}")
            fxtag('xfdf:original': "98", "${value(306005) ?: ''}")
            fxtag('xfdf:original': "99", "${value(106013) ?: ''}")
            fxtag('xfdf:original': "100", "${value(306006) ?: ''}")
            fxtag('xfdf:original': "101", "${value(106015) ?: ''}")
            fxtag('xfdf:original': "102", "${value(306007) ?: ''}")
            fxtag('xfdf:original': "103", "${value(106017) ?: ''}")
            fxtag('xfdf:original': "104", "${value(306008) ?: ''}")
            fxtag('xfdf:original': "105", "${value(106019) ?: ''}")
            sdasdavkphout('xfdf:original': "sdasdavkphouíáít", "${value(306009) ?: ''}")
            fvbgggztzhjg("${value(106021) ?: ''}")
            fdsfsfsdtttttttgdsfs('xfdf:original': "fdsfsfsdtttttttgdsfsôôôôô", "${value(306010) ?: ''}")
//          Poistenie zodpovednosti za škodu
            lll('xfdf:original': "ôôlll", "${value(107003) ?: ''}")
            ldkdd("${value(308009) ?: ''}")
            poorfsss('xfdf:original': "pooríííčáčôfúääňňsss", "${value(104003) ?: ''}")

            field('xfdf:original': "Text Field 153", "${value(309001) ?: ''}")
        }
        return writer.toString()
    }

    private String datasetToOfferXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            field("xfdf:original": " Text Field 143 ", "${value(309001) ?: ''}")
            field("xfdf:original": " Text Field 50 ", 2)
            field("xfdf:original": " Text Field 51 ", 3)
            field("xfdf:original": "1", 4)
            field("xfdf:original": "2", 5)
            field("xfdf:original": "3", 6)
            field("xfdf:original": "4", 7)
            field("xfdf:original": "5", 8)
            field("xfdf:original": "6", 9)
            field("xfdf:original": "7", 10)
            field("xfdf:original": "8", 11)
            field("xfdf:original": "9", 12)
            field("xfdf:original": "10", 13)
            field("xfdf:original": "400", 14)
            field("xfdf:original": "401", 15)
            field("xfdf:original": "402", 16)
            field("xfdf:original": "403", 17)
            field("xfdf:original": "404", 18)
            field("xfdf:original": "405", 19)
            field("xfdf:original": "406", 20)
            field("xfdf:original": "407", 21)
            field("xfdf:original": "408", 22)
            field("xfdf:original": "409", 23)
            field("xfdf:original": "410", 24)
            field("xfdf:original": "411", 25)
            field("xfdf:original": "11", 26)
            field("xfdf:original": "12", 27)
            field("xfdf:original": "13", 28)
            field("xfdf:original": "14", 29)
            field("xfdf:original": "16", 30)
            field("xfdf:original": "17", 31)
            field("xfdf:original": "18", 32)
            field("xfdf:original": "19", 33)
            field("xfdf:original": "21", 35)
            field("xfdf:original": "22", 36)
            field("xfdf:original": "23", 37)
            field("xfdf:original": "24", 38)
            field("xfdf:original": "106", 39)
            field("xfdf:original": "107", 40)
            field("xfdf:original": "108", 41)
            field("xfdf:original": "109", 42)
            field("xfdf:original": "453", 43)
            field("xfdf:original": "775", 44)
            field("xfdf:original": "110", 45)
            field("xfdf:original": "111", 52)
            field("xfdf:original": "112", 46)
            field("xfdf:original": "113", 53)
            field("xfdf:original": "114", 47)
            field("xfdf:original": "115", 54)
            field("xfdf:original": "116", 48)
            field("xfdf:original": "117", 55)
            field("xfdf:original": "118", 49)
            field("xfdf:original": "119", 56)
            field("xfdf:original": "120", 50)
            field("xfdf:original": "89323", 51)
            field("xfdf:original": "121", 57)
            field("xfdf:original": "mjjttzeželljsd", 58)
            field("xfdf:original": "126", 59)
            field("xfdf:original": "129", 60)
            field("xfdf:original": "130", 61)
            field("xfdf:original": "131", 62)
            field("xfdf:original": " Text Field 226 ", 64)
            field("xfdf:original": " Text Field 238 ", 65)
            field("xfdf:original": " Text Field 239 ", 66)
            field("xfdf:original": " Text Field 240 ", 67)
            field("xfdf:original": " Text Field 241 ", 68)
            field("xfdf:original": "Text Field dadadsasda", 69)
            dasdasdda(70)
            field("xfdf:original": " Text Field 243 ", 71)
            field("xfdf:original": " Text Field 244 ", 72)
            field("xfdf:original": " Text Field 245 ", 73)
            field("xfdf:original": " Text Field 246 ", 74)
            field("xfdf:original": " Text Field 247 ", 75)
            field("xfdf:original": " Text Field 248 ", 76)
            field("xfdf:original": " Text Field 249 ", 77)
            field("xfdf:original": "25", 80)
            field("xfdf:original": "26", 81)
            field("xfdf:original": "27", 82)
            field("xfdf:original": "28", 83)
            field("xfdf:original": "29", 84)
            field("xfdf:original": "30", 85)
            field("xfdf:original": "31", 86)
            ZHSNSAKXCAKS(87)
            field("xfdf:original": "32", 88)
            field("xfdf:original": "33", 89)
            field("xfdf:original": "34", 90)
            field("xfdf:original": "adadafcmmklopíágh", 91)
            field("xfdf:original": "adsadadgnnggtujkklôôp", 92)
            field("xfdf:original": "adadadvvcvbnžťžťtfg", 93)
            field("xfdf:original": "jklopopopoéííikkkjk", 94)
            field("xfdf:original": "500", 95)
            field("xfdf:original": "513", 96)
            field("xfdf:original": "501", 97)
            field("xfdf:original": "514", 98)
            field("xfdf:original": "502", 99)
            field("xfdf:original": "515", 100)
            field("xfdf:original": "503", 101)
            field("xfdf:original": "516", 102)
            field("xfdf:original": "504", 103)
            field("xfdf:original": "517", 104)
            field("xfdf:original": "505", 105)
            field("xfdf:original": "518", 106)
            field("xfdf:original": "506", 107)
            field("xfdf:original": "519", 108)
            field("xfdf:original": "507", 109)
            field("xfdf:original": "520", 110)
            field("xfdf:original": "508", 111)
            field("xfdf:original": "521", 112)
            field("xfdf:original": "509", 113)
            field("xfdf:original": "522", 114)
            field("xfdf:original": "510", 115)
            field("xfdf:original": "523", 116)
            field("xfdf:original": "525", 118)
            field("xfdf:original": "527", 117)
            field("xfdf:original": "526", 119)
            field("xfdf:original": "38", 120)
            field("xfdf:original": "39", 121)
            field("xfdf:original": "40", 122)
            field("xfdf:original": "41", 123)
            asdasdasdadd(124)
            field("xfdf:original": "529", 125)
            field("xfdf:original": "528", 126)
            field("xfdf:original": "530", 127)
            field("xfdf:original": "531", 128)
            field("xfdf:original": "532", 129)
            field("xfdf:original": "67", 131)
            field("xfdf:original": "68", 132)
            field("xfdf:original": "69", 133)
            field("xfdf:original": "70", 134)
            field("xfdf:original": "71", 135)
            field("xfdf:original": "72", 136)
            field("xfdf:original": "73", 137)
            field("xfdf:original": "74", 138)
            field("xfdf:original": "vvbcbcvcvcvnmjkl,,,", 139)
            field("xfdf:original": "91", 140)
            field("xfdf:original": "92", 141)
            field("xfdf:original": "93", 142)
            field("xfdf:original": "94", 143)
            field("xfdf:original": "95", 144)
            field("xfdf:original": "96", 145)
            field("xfdf:original": "97", 146)
            field("xfdf:original": "98", 147)
            field("xfdf:original": "99", 148)
            field("xfdf:original": "100", 149)
            field("xfdf:original": "101", 150)
            field("xfdf:original": "102", 151)
            field("xfdf:original": "103", 152)
            field("xfdf:original": "104", 153)
            field("xfdf:original": "105", 154)
            field("xfdf:original": "sdasdavkphouíáít", 155)
            fvbgggztzhjg(156)
            field("xfdf:original": "fdsfsfsdtttttttgdsfsôôôôô", 157)
            field("xfdf:original": "ôôlll", 158)
            ldkdd(159)
            field("xfdf:original": "pooríííčáčôfúääňňsss", 160)
            x(163)
            X2(164)
            mn(166)
            field("xfdf:original": ",ketuýg", 167)
            X3(168)
            X4(169)
            field("xfdf:original": " Text Field 153 ", 170)
        }

        return writer.toString()
    }

    private String datasetToDraftXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            field('xfdf:original': "Text Field 143", "${value(309001) ?: ''}")
            field('xfdf:original': "21", "${value(109045) ?: ''}")
            field('xfdf:original': "22", "${value(109046) ?: ''}")
            field('xfdf:original': "23", "${value(109047) ?: ''}")
            field('xfdf:original': "24", "${value(109048) ?: ''}")
            field('xfdf:original': "106", "${value(309002) ?: ''}")
            field('xfdf:original': "107", "${value(109001) ?: ''}")
            field('xfdf:original': "108", "${(value(109002) as Boolean)?(value(109003)):"" }") // todo: check
            field('xfdf:original': "109", "${value(108001) ?: ''}")
            field('xfdf:original': "453", "${value(105005) ?: ''}")
            field('xfdf:original': "775", "${value(106001) ?: ''}")
            field('xfdf:original': "110", "${value(105036) ?: ''}")
            field('xfdf:original': "111", "${valuePercentage(108003) ?: ''}")
//            field('xfdf:original': "112", 14)
            field('xfdf:original': "113", "${valuePercentage(108001) ?: ''}")
            field('xfdf:original': "114", "${value(306001) ?: ''}")
//            field('xfdf:original': "115", 17)
            field('xfdf:original': "116", "${value(308003) ?: ''}")
            field('xfdf:original': "117", "${valuePercentage(308005) ?: ''}")
//            field('xfdf:original': "118", 20)
            field('xfdf:original': "119", "${valuePercentage(108002) ?: ''}")
            field('xfdf:original': "120", "${value(308004) ?: ''}")
//            field('xfdf:original': "121", 23)
            field('xfdf:original': "89323", "${value(308006) ?: ''}")
//            field('xfdf:original': "mjjttzeželljsd", 25)

            field('xfdf:original': "Text Field 152", "${value(309001) ?: ''}")

            field('xfdf:original': "Text Field 226", "${valueBool(101002) ?: ''}")
            field('xfdf:original': "Text Field 238", "${valueBool(101003) ?: ''}")
            field('xfdf:original': "Text Field 239", "${valueBool(101004) ?: ''}")
            field('xfdf:original': "Text Field 240", "${value(101005) ?: ''}")
            field('xfdf:original': "Text Field 241", "${value(101006) ?: ''}")
            field('xfdf:original': "Text Field dadadsasda", "${value(101007) ?: ''}")
            dasdasdda("${valueBool(101009) ?: ''}")
            field('xfdf:original': "Text Field 243", "Nie")
            field('xfdf:original': "Text Field 244", "${valueBool(101011) ?: ''}")
            field('xfdf:original': "Text Field 245", "${value(101012) ?: ''}")
            field('xfdf:original': "Text Field 246", "${value(101013) ?: ''}")
            field('xfdf:original': "Text Field 247", "${value(101014) ?: ''}")
            field('xfdf:original': "Text Field 248", "${value(101016) ?: ''}")
            field('xfdf:original': "Text Field 249", "${value(101008) ?: ''}")

            field('xfdf:original': "25", "${value(102001) ?: ''}")
            field('xfdf:original': "26", "${value(102002) ?: ''}")
            field('xfdf:original': "27", "${value(102003) ?: ''}")
            field('xfdf:original': "28", "${value(102005) ?: ''}")
            field('xfdf:original': "29", "${value(102004) ?: ''}")
            field('xfdf:original': "30", "${value(102006) ?: ''}")
            field('xfdf:original': "31", "${value(102007) ?: ''}")
            ZHSNSAKXCAKS("${value(102008) ?: ''}")

            field('xfdf:original': "32", "${value(105001) ?: ''}")
            field('xfdf:original': "33", "${value(105002) ?: ''}")
            field('xfdf:original': "34", "${value(105003) ?: ''}")
            field('xfdf:original': "adadafcmmklopíágh", "${value(305001) ?: ''}")
            field('xfdf:original': "adsadadgnnggtujkklôôp", "${value(105006) ?: ''}")
            field('xfdf:original': "adadadvvcvbnžťžťtfg", "${value(305002) ?: ''}")
            field('xfdf:original': "jklopopopoéííikkkjk", "${value(105036) ?: ''}")

            field('xfdf:original': "500", "${value(105010) ?: ''}")
            field('xfdf:original': "513", "${value(305004) ?: ''}")
            field('xfdf:original': "501", "${value(105012) ?: ''}")
            field('xfdf:original': "514", "${value(305005) ?: ''}")
            field('xfdf:original': "502", "${value(105014) ?: ''}")
            field('xfdf:original': "515", "${value(305006) ?: ''}")
            field('xfdf:original': "503", "${value(105016) ?: ''}")
            field('xfdf:original': "516", "${value(305007) ?: ''}")
            field('xfdf:original': "504", "${value(105018) ?: ''}")
            field('xfdf:original': "517", "${value(305008) ?: ''}")
            field('xfdf:original': "505", "${value(105020) ?: ''}")
            field('xfdf:original': "518", "${value(305009) ?: ''}")
            field('xfdf:original': "506", "${value(105022) ?: ''}")
            field('xfdf:original': "519", "${value(305010) ?: ''}")
            field('xfdf:original': "507", "${value(105024) ?: ''}")
            field('xfdf:original': "520", "${value(305011) ?: ''}")
            field('xfdf:original': "508", "${value(105026) ?: ''}")
            field('xfdf:original': "521", "${value(305012) ?: ''}")
            field('xfdf:original': "509", "${value(105028) ?: ''}")
            field('xfdf:original': "522", "${value(305013) ?: ''}")
            field('xfdf:original': "510", "${value(105030) ?: ''}")
            field('xfdf:original': "523", "${value(305014) ?: ''}")

            field('xfdf:original': "527", "${value(105004) ?: ''}")
            field('xfdf:original': "525", "${value(105007) ?: ''}")
            field('xfdf:original': "526", "${value(305003) ?: ''}")

            field('xfdf:original': "38", "${value(109057) ?: ''}")
            field('xfdf:original': "39", "${value(109054) ?: ''}")
            field('xfdf:original': "40", "${value(109055) ?: ''}")
            field('xfdf:original': "41", "${value(109056) ?: ''}")

            asdasdasdadd("${value(105032) ?: ''}")
            field('xfdf:original': "529", "${value(305015) ?: ''}")
            field('xfdf:original': "528", "${value(105034) ?: ''}")
            field('xfdf:original': "530", "${value(305016) ?: ''}")

            field('xfdf:original': "531", "${value(107001) ?: ''}")
            field('xfdf:original': "532", "${value(308008) ?: ''}")

            field('xfdf:original': "67", "${value(103001) ?: ''}")
            field('xfdf:original': "68", "${value(103002) ?: ''}")
            field('xfdf:original': "69", "${value(103003) ?: ''}")
            field('xfdf:original': "70", "${value(103004) ?: ''}")
            field('xfdf:original': "71", "${value(103005) ?: ''}")
            field('xfdf:original': "72", "${value(106003) ?: ''}")
            field('xfdf:original': "73", "${value(106002) ?: ''}")
            field('xfdf:original': "74", "${value(106022) ?: ''}")
            field('xfdf:original': "vvbcbcvcvcvnmjkl,,,", "${value(306001) ?: ''}")

            field('xfdf:original': "91", "${value(106005) ?: ''}")
            field('xfdf:original': "92", "${value(306002) ?: ''}")
            field('xfdf:original': "93", "${value(106007) ?: ''}")
            field('xfdf:original': "94", "${value(306003) ?: ''}")
            field('xfdf:original': "95", "${value(106009) ?: ''}")
            field('xfdf:original': "96", "${value(306004) ?: ''}")
            field('xfdf:original': "97", "${value(106011) ?: ''}")
            field('xfdf:original': "98", "${value(306005) ?: ''}")
            field('xfdf:original': "99", "${value(106013) ?: ''}")
            field('xfdf:original': "100", "${value(306006) ?: ''}")
            field('xfdf:original': "101", "${value(106015) ?: ''}")
            field('xfdf:original': "102", "${value(306007) ?: ''}")
            field('xfdf:original': "103", "${value(106017) ?: ''}")
            field('xfdf:original': "104", "${value(306008) ?: ''}")
            field('xfdf:original': "105", "${value(106019) ?: ''}")
            field('xfdf:original': "sdasdavkphouíáít", "${value(306009) ?: ''}")
            fvbgggztzhjg("${value(106021) ?: ''}")
            field('xfdf:original': "fdsfsfsdtttttttgdsfsôôôôô", "${value(306010) ?: ''}")

            field('xfdf:original': "ôôlll", "${value(107003) ?: ''}")
            ldkdd("${value(308009) ?: ''}")
            field('xfdf:original': "pooríííčáčôfúääňňsss", "${value(104003) ?: ''}")

            field('xfdf:original': "Text Field 153", "${value(309001) ?: ''}")
        }
        return writer.toString()
    }

    private String value(Long id) {
        return useCase.dataSet[useCase.petriNet.dataSet.find { it.value.importId == id }?.key]?.value?.toString()
    }

    private Boolean valueBool(Long id) {
        return value(id) == 'true' ? 'Áno' : 'Nie'
    }

    private String valuePercentage(Long id) {
        Double value = value(id) as Double
        value *= 100
        return "${value.round(2)}"
    }
}