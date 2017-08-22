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
        String finalPath = (field as FileField).getFilePath(FINAL_FILENAME)
        String draftXml = datasetToDraftXml()
        String offerXml = datasetToOfferXml()

        File draftPdfFile = PdfUtils.fillPdfForm(draftPath, new FileInputStream("$PDF_PATH/$DRAFT_FILENAME"), draftXml)
        File offerPdfFile = PdfUtils.fillPdfForm(offerPath, new FileInputStream("$PDF_PATH/$OFFER_FILENAME"), offerXml)
        File mergedPdf = PdfUtils.mergePdfFiles(finalPath, draftPdfFile, offerPdfFile)

        useCase.dataSet.get(field.stringId).setValue(finalPath)

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

    private String datasetToDraftXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            poisteny(builder)
            poistnik(builder)
            trvaleBydlisko(builder)
            korespondencnaAdresa(builder)
            miestoPoistenia(builder)
            udajeOPoisteni(builder)
            udajeOPoistnom(builder)
            spoluucastPriPoistnomPlneni(builder)
            rekapitulaciaPoistneho(builder)
            uhradaPoistneho(builder)
            vinkulaciaPoistnehoPlnenia(builder)
            indexaciaPoistnejSumy(builder)
            zakladneInformacie(builder)
            poistenieNehnutelnosti(builder)
            poistenieNehnutelnostiPlochy(builder)
            poistenieVedlajsichStaviebPoistneSumy(builder)
            poistenieVedlajsichStaviebPoistne(builder)
            poistneGaraze(builder)
            garazNaInejAdrese(builder)
            doplnkovePoistenieNehnutelnosti(builder)
            poistenieZodpovednostiZaSkoduNehnutelnost(builder)
            poistenieDomacnosti(builder)
            doplnkovePoistenieDomacnostiPoistneSumy(builder)
            doplnkovePoistenieDomacnostiPoistne(builder)
            sposobUhradenia(builder)
            osobitneVyjadrenia(builder)
            suhlasOsobneUdaje(builder)

            field("xfdf:original": "Text Field 143", "${value(309001) ?: ''}")
            field("xfdf:original": "Text Field 153", "${value(309001) ?: ''}")
        }

        return writer.toString()
    }

    private String datasetToOfferXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            field('xfdf:original': "Text Field 143", "${value(309001) ?: ''}")
            field('xfdf:original': "Text Field 152", "${value(309001) ?: ''}")
//            Miesto poistenia
            field('xfdf:original': "21", "${value(109045) ?: ''}")
            field('xfdf:original': "22", "${value(109046) ?: ''}")
            field('xfdf:original': "23", "${value(109047) ?: ''}")
            field('xfdf:original': "24", "${value(109048) ?: ''}")
//            Údaje o poistení
            field('xfdf:original': "106", "${value(309002) ?: ''}")
            field('xfdf:original': "107", "${value(109001) ?: ''}")
            field('xfdf:original': "108", "${(value(109002) as Boolean) ? (value(109003)) : ""}") // todo: check
//            Údaje o poistnom
            field('xfdf:original': "109", "${value(108001) ?: ''}")
//            Spoluúčasť pri poistnom plnení
            field('xfdf:original': "453", "${value(105005) ?: ''}")
            field('xfdf:original': "775", "${value(106001) ?: ''}")
//            Rekapitulácia poistného
            field('xfdf:original': "110", "${value(305002) ?: ''}")
//            field('xfdf:original': "112", 14) // todo
            field('xfdf:original': "114", "${value(306001) ?: ''}")
            field('xfdf:original': "116", "${value(308003) ?: ''}")
//            field('xfdf:original': "118", 20) // todo
            field('xfdf:original': "120", "${value(308004) ?: ''}")
            field('xfdf:original': "89323", "${value(308006) ?: ''}")
            field('xfdf:original': "111", "${value(108003) ?: ''}")
            field('xfdf:original': "113", "${valuePercentageDiscount(208004) ?: ''}")
            field('xfdf:original': "115", "${valuePercentageDiscount(203004) ?: ''}")
            field('xfdf:original': "117", "${valuePercentageDiscount(208008) ?: ''}")
            field('xfdf:original': "119", "${valuePercentageDiscount(208005) ?: ''}")
            field('xfdf:original': "121", "${valuePercentageDiscount(208007) ?: ''}")
//            field('xfdf:original': "mjjttzeželljsd", 25) //todo
//            Základné informácie
            field('xfdf:original': "Text Field 226", "${valueBool(101002) ?: ''}")
            field('xfdf:original': "Text Field 238", "${valueBool(101003) ?: ''}")
            field('xfdf:original': "Text Field 239", "${valueBool(101004) ?: ''}")
            field('xfdf:original': "Text Field 240", "${value(101005) ?: ''}")
            field('xfdf:original': "Text Field 241", "${value(101006) ?: ''}")
            field('xfdf:original': "Text Field dadadsasda", "${value(101007) ?: ''}")
            dasdasdda("${valueBool(101008) ?: ''}")
            field('xfdf:original': "Text Field 243", "${valueBool(101009) ?: ''}")
            field('xfdf:original': "Text Field 244", "Nie")
            field('xfdf:original': "Text Field 245", "${valueBool(101011) ?: ''}")
            field('xfdf:original': "Text Field 246", "${valueBool(101012) ?: ''}")
            field('xfdf:original': "Text Field 247", "${value(101013) ?: ''}")
            field('xfdf:original': "Text Field 248", "${valueBool(101014) ?: ''}")
            field('xfdf:original': "Text Field 249", "${value(101016) ?: ''}")
//            Poistenie nehnuteľnosti
            field('xfdf:original': "25", "${value(102001) ?: ''}")
            field('xfdf:original': "26", "${value(102002) ?: ''}")
            field('xfdf:original': "27", "${value(102003) ?: ''}")
            field('xfdf:original': "28", "${valueBool(102005) ?: ''}")
            field('xfdf:original': "29", "${value(102004) ?: ''}")
            field('xfdf:original': "30", "${value(102006) ?: ''}")
            field('xfdf:original': "31", "${value(102007) ?: ''}")
            ZHSNSAKXCAKS("${value(102008) ?: ''}")
//            Poistenie nehnutelnosti - plochy
            field('xfdf:original': "32", "${value(105001) ?: ''}")
            field('xfdf:original': "33", "${value(105002) ?: ''}")
            field('xfdf:original': "34", "${value(105003) ?: ''}")
            field('xfdf:original': "adadafcmmklopíágh", "${value(305001) ?: ''}")
            field('xfdf:original': "adsadadgnnggtujkklôôp", "${value(105006) ?: ''}")
            field('xfdf:original': "adadadvvcvbnžťžťtfg", "${value(105036) ?: ''}")
            field('xfdf:original': "jklopopopoéííikkkjk", "${value(305002) ?: ''}")
//            Poistenie vedľajších stavieb
            field('xfdf:original': "500", "${value(105010) ?: ''}")
            field('xfdf:original': "501", "${value(105012) ?: ''}")
            field('xfdf:original': "502", "${value(105014) ?: ''}")
            field('xfdf:original': "503", "${value(105016) ?: ''}")
            field('xfdf:original': "504", "${value(105018) ?: ''}")
            field('xfdf:original': "505", "${value(105020) ?: ''}")
            field('xfdf:original': "506", "${value(105022) ?: ''}")
            field('xfdf:original': "507", "${value(105024) ?: ''}")
            field('xfdf:original': "508", "${value(105026) ?: ''}")
            field('xfdf:original': "509", "${value(105028) ?: ''}")
            field('xfdf:original': "510", "${value(105030) ?: ''}")
//            Poistenie vs poistne
            field('xfdf:original': "513", "${value(305004) ?: ''}")
            field('xfdf:original': "514", "${value(305005) ?: ''}")
            field('xfdf:original': "515", "${value(305006) ?: ''}")
            field('xfdf:original': "516", "${value(305007) ?: ''}")
            field('xfdf:original': "517", "${value(305008) ?: ''}")
            field('xfdf:original': "518", "${value(305009) ?: ''}")
            field('xfdf:original': "519", "${value(305010) ?: ''}")
            field('xfdf:original': "520", "${value(305011) ?: ''}")
            field('xfdf:original': "521", "${value(305012) ?: ''}")
            field('xfdf:original': "522", "${value(305013) ?: ''}")
            field('xfdf:original': "523", "${value(305014) ?: ''}")
//            Poistne garaze
            field('xfdf:original': "527", "${value(105004) ?: ''}")
            field('xfdf:original': "525", "${value(105007) ?: ''}")
            field('xfdf:original': "526", "${value(305003) ?: ''}")
//            Garaz na inom mieste
            field('xfdf:original': "38", "${value(109057) ?: ''}")
            field('xfdf:original': "39", "${value(109054) ?: ''}")
            field('xfdf:original': "40", "${value(109055) ?: ''}")
            field('xfdf:original': "41", "${value(109056) ?: ''}")
//            Doplnkove nehnutelnosti
            asdasdasdadd("${value(105032) ?: ''}")
            field('xfdf:original': "529", "${value(305015) ?: ''}")
            field('xfdf:original': "528", "${value(105034) ?: ''}")
            field('xfdf:original': "530", "${value(305016) ?: ''}")
//            Poistenie zodp. za skodu - nehnutelnost
            field('xfdf:original': "531", "${value(107001) ?: ''}")
            field('xfdf:original': "532", "${value(308008) ?: ''}")
//            Poistenie domácnosti
            field('xfdf:original': "67", "${value(103001) ?: ''}")
            field('xfdf:original': "68", "${value(103002) ?: ''}")
            field('xfdf:original': "69", "${valueBool(103003) ?: ''}")
            field('xfdf:original': "70", "${valueBool(103004) ?: ''}")
            field('xfdf:original': "71", "${valueBool(103005) ?: ''}")
            field('xfdf:original': "72", "${value(106003) ?: ''}")
            field('xfdf:original': "73", "${value(106002) ?: ''}")
            field('xfdf:original': "74", "${value(106022) ?: ''}")
            field('xfdf:original': "vvbcbcvcvcvnmjkl,,,", "${value(306001) ?: ''}")
//            Doplnkové poistenie domácnosti - poistna suma
            field('xfdf:original': "91", "${value(106005) ?: ''}")
            field('xfdf:original': "93", "${value(106007) ?: ''}")
            field('xfdf:original': "95", "${value(106009) ?: ''}")
            field('xfdf:original': "97", "${value(106011) ?: ''}")
            field('xfdf:original': "99", "${value(106013) ?: ''}")
            field('xfdf:original': "101", "${value(106015) ?: ''}")
            field('xfdf:original': "103", "${value(106017) ?: ''}")
            field('xfdf:original': "105", "${value(106019) ?: ''}")
            fvbgggztzhjg("${value(106021) ?: ''}")
//            Doplnkové poistenie domácnosti - poistne
            field('xfdf:original': "92", "${value(306002) ?: ''}")
            field('xfdf:original': "94", "${value(306003) ?: ''}")
            field('xfdf:original': "96", "${value(306004) ?: ''}")
            field('xfdf:original': "98", "${value(306005) ?: ''}")
            field('xfdf:original': "100", "${value(306006) ?: ''}")
            field('xfdf:original': "102", "${value(306007) ?: ''}")
            field('xfdf:original': "104", "${value(306008) ?: ''}")
            field('xfdf:original': "sdasdavkphouíáít", "${value(306009) ?: ''}")
            field('xfdf:original': "fdsfsfsdtttttttgdsfsôôôôô", "${value(306010) ?: ''}")
//            Zodp. za skodu domacnost
            field('xfdf:original': "ôôlll", "${value(107003) ?: ''}")
            ldkdd("${value(308009) ?: ''}")
            field('xfdf:original': "pooríííčáčôfúääňňsss", "${value(104003) ?: ''}")

            field('xfdf:original': "Text Field 153", "${value(309001) ?: ''}")
        }
        return writer.toString()
    }

    Closure<MarkupBuilder> poistnik = { MarkupBuilder builder ->
        builder.field("xfdf:original": "Text Field 50", "${value(109007) ?: ''}")
        builder.field("xfdf:original": "Text Field 51", "${value(109008) ?: ''}")
        builder.field("xfdf:original": "1", "${value(109010) ?: ''} ${value(109011) ?: ''}")
        builder.field("xfdf:original": "2", "${value(109012) ?: ''}")
        builder.field("xfdf:original": "3", "${value(109009) ?: ''}")
        builder.field("xfdf:original": "4", "${value(109014) ?: ''}")
        builder.field("xfdf:original": "5", "${(value(109015) ?: value(109058)) ?: ''}")
        builder.field("xfdf:original": "6", "${value(109013) ?: ''}")
        builder.field("xfdf:original": "7", "${value(109016) ?: ''}")
        builder.field("xfdf:original": "8", "${value(109017) ?: ''}")
        builder.field("xfdf:original": "9", "${value(109018) ?: ''}")
        builder.field("xfdf:original": "10", "${value(109019) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poisteny = { MarkupBuilder builder ->
        builder.field("xfdf:original": "400", "${value(109021) ?: ''}")
        builder.field("xfdf:original": "401", "${value(109022) ?: ''}")
        builder.field("xfdf:original": "402", "${value(109024) ?: ''} ${value(109025) ?: ''}")
        builder.field("xfdf:original": "403", "${value(109026) ?: ''}")
        builder.field("xfdf:original": "404", "${value(109023) ?: ''}")
        builder.field("xfdf:original": "405", "${value(109028) ?: ''}")
        builder.field("xfdf:original": "406", "${(value(109029) ?: value(109059)) ?: ''}")
        builder.field("xfdf:original": "407", "${value(109027) ?: ''}")
        builder.field("xfdf:original": "408", "${value(109030) ?: ''}")
        builder.field("xfdf:original": "409", "${value(109031) ?: ''}")
        builder.field("xfdf:original": "410", "${value(109032) ?: ''}")
        builder.field("xfdf:original": "411", "${value(109033) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> trvaleBydlisko = { MarkupBuilder builder ->
        builder.field("xfdf:original": "11", "${value(109036) ?: ''}")
        builder.field("xfdf:original": "12", "${value(109037) ?: ''}")
        builder.field("xfdf:original": "13", "${value(109038) ?: ''}")
        builder.field("xfdf:original": "14", "${value(109039) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> korespondencnaAdresa = { MarkupBuilder builder ->
        builder.field("xfdf:original": "16", "${value(109041) ?: ''}")
        builder.field("xfdf:original": "17", "${value(109042) ?: ''}")
        builder.field("xfdf:original": "18", "${value(109043) ?: ''}")
        builder.field("xfdf:original": "19", "${value(109044) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> miestoPoistenia = { MarkupBuilder builder ->
        builder.field("xfdf:original": "21", "${value(109045) ?: ''}")
        builder.field("xfdf:original": "22", "${value(109046) ?: ''}")
        builder.field("xfdf:original": "23", "${value(109047) ?: ''}")
        builder.field("xfdf:original": "24", "${value(109048) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> udajeOPoisteni = { MarkupBuilder builder ->
        builder.field("xfdf:original": "106", "${value(309002) ?: ''}")
        builder.field("xfdf:original": "107", "${value(109001) ?: ''}")
        builder.field("xfdf:original": "108", "${valueBool(109002) ? value(109003) : ''}") //todo date format

        return builder
    }

    Closure<MarkupBuilder> udajeOPoistnom = { MarkupBuilder builder ->
        builder.field("xfdf:original": "109", "${value(108001) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> spoluucastPriPoistnomPlneni = { MarkupBuilder builder ->
        builder.field("xfdf:original": "453", "${value(105005) ?: ''}")
        builder.field("xfdf:original": "775", "${value(106001) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> rekapitulaciaPoistneho = { MarkupBuilder builder ->
        builder.field("xfdf:original": "110", "${valueRound(305002) ?: ''}")
//        builder.field("xfdf:original": "112", "${valueRound() ?: ''}") //todo
        builder.field("xfdf:original": "114", "${valueRound(306001) ?: ''}")
        builder.field("xfdf:original": "116", "${valueRound(308003) ?: ''}")
//        builder.field("xfdf:original": "118", "${valueRound() ?: ''}") //todo
        builder.field("xfdf:original": "120", "${valueRound(308004) ?: ''}")
        builder.field("xfdf:original": "89323", "${valueRound(308006) ?: ''}")
        builder.field("xfdf:original": "111", "${value(108003) ?: ''}")
        builder.field("xfdf:original": "113", "${valuePercentageDiscount(208004) ?: ''}")
        builder.field("xfdf:original": "115", "${valuePercentageDiscount(203004) ?: ''}")
        builder.field("xfdf:original": "117", "${valuePercentageDiscount(208008) ?: ''}")
        builder.field("xfdf:original": "119", "${valuePercentageDiscount(208005) ?: ''}")
        builder.field("xfdf:original": "121", "${valuePercentageDiscount(208007) ?: ''}")
//        builder.field("xfdf:original": "mjjttzeželljsd", "${value() ?: ''}") // todo

        return builder
    }
    Closure<MarkupBuilder> uhradaPoistneho = { MarkupBuilder builder ->
        builder.field("xfdf:original": "126", "${value(309001) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> vinkulaciaPoistnehoPlnenia = { MarkupBuilder builder ->
        builder.field("xfdf:original": "129", "${value(109004) ?: ''}")
        builder.field("xfdf:original": "130", "${value(109005) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> indexaciaPoistnejSumy = { MarkupBuilder builder ->
        builder.field("xfdf:original": "131", "${valueBool(109060) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> zakladneInformacie = { MarkupBuilder builder ->
        builder.field("xfdf:original": "Text Field 226", "${valueBool(101002) ?: ''}")
        builder.field("xfdf:original": "Text Field 238", "${valueBool(101003) ?: ''}")
        builder.field("xfdf:original": "Text Field 239", "${valueBool(101004) ?: ''}")
        builder.field("xfdf:original": "Text Field 240", "${value(101005) ?: ''}")
        builder.field("xfdf:original": "Text Field 241", "${value(101006) ?: ''}")
        builder.field("xfdf:original": "Text Field dadadsasda", "${value(101007) ?: ''}")
        builder.dasdasdda("${value(101008) ?: ''}")
        builder.field("xfdf:original": "Text Field 243", "${valueBool(101009) ?: ''}")
        builder.field("xfdf:original": "Text Field 244", "Nie")
        builder.field("xfdf:original": "Text Field 245", "${valueBool(101011) ?: ''}")
        builder.field("xfdf:original": "Text Field 246", "${valueBool(101012) ?: ''}")
        builder.field("xfdf:original": "Text Field 247", "${value(101013) ?: ''}")
        builder.field("xfdf:original": "Text Field 248", "${valueBool(101014) ?: ''}")
        builder.field("xfdf:original": "Text Field 249", "${value(101016) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistenieNehnutelnosti = { MarkupBuilder builder ->
        builder.field("xfdf:original": "25", "${value(102001) ?: ''}")
        builder.field("xfdf:original": "26", "${value(102002) ?: ''}")
        builder.field("xfdf:original": "27", "${value(102003) ?: ''}")
        builder.field("xfdf:original": "28", "${valueBool(102005) ?: ''}")
        builder.field("xfdf:original": "29", "${value(102004) ?: ''}")
        builder.field("xfdf:original": "30", "${value(102006) ?: ''}")
        builder.field("xfdf:original": "31", "${value(102007) ?: ''}")
        builder.ZHSNSAKXCAKS("${value(102008) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistenieNehnutelnostiPlochy = { MarkupBuilder builder ->
        builder.field("xfdf:original": "32", "${valueRound(105001) ?: ''}")
        builder.field("xfdf:original": "33", "${valueRound(105002) ?: ''}")
        builder.field("xfdf:original": "34", "${valueRound(105003) ?: ''}")
        builder.field("xfdf:original": "adadafcmmklopíágh", "${valueRound(305001) ?: ''}")
        builder.field("xfdf:original": "adsadadgnnggtujkklôôp", "${valueRound(105006) ?: ''}")
        builder.field("xfdf:original": "adadadvvcvbnžťžťtfg", "${valueRound(105036) ?: ''}")
        builder.field("xfdf:original": "jklopopopoéííikkkjk", "${valueRound(305002) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistenieVedlajsichStaviebPoistneSumy = { MarkupBuilder builder ->
        builder.field("xfdf:original": "500", "${valueRound(105010) ?: ''}")
        builder.field("xfdf:original": "501", "${valueRound(105012) ?: ''}")
        builder.field("xfdf:original": "502", "${valueRound(105014) ?: ''}")
        builder.field("xfdf:original": "503", "${valueRound(105016) ?: ''}")
        builder.field("xfdf:original": "504", "${valueRound(105018) ?: ''}")
        builder.field("xfdf:original": "505", "${valueRound(105020) ?: ''}")
        builder.field("xfdf:original": "506", "${valueRound(105022) ?: ''}")
        builder.field("xfdf:original": "507", "${valueRound(105024) ?: ''}")
        builder.field("xfdf:original": "508", "${valueRound(105026) ?: ''}")
        builder.field("xfdf:original": "509", "${valueRound(105028) ?: ''}")
        builder.field("xfdf:original": "510", "${valueRound(105030) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistenieVedlajsichStaviebPoistne = { MarkupBuilder builder ->
        builder.field("xfdf:original": "513", "${valueRound(305004) ?: ''}")
        builder.field("xfdf:original": "514", "${valueRound(305005) ?: ''}")
        builder.field("xfdf:original": "515", "${valueRound(305006) ?: ''}")
        builder.field("xfdf:original": "516", "${valueRound(305007) ?: ''}")
        builder.field("xfdf:original": "517", "${valueRound(305008) ?: ''}")
        builder.field("xfdf:original": "518", "${valueRound(305009) ?: ''}")
        builder.field("xfdf:original": "519", "${valueRound(305010) ?: ''}")
        builder.field("xfdf:original": "520", "${valueRound(305011) ?: ''}")
        builder.field("xfdf:original": "521", "${valueRound(305012) ?: ''}")
        builder.field("xfdf:original": "522", "${valueRound(305013) ?: ''}")
        builder.field("xfdf:original": "523", "${valueRound(305014) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistneGaraze = { MarkupBuilder builder ->
        builder.field("xfdf:original": "527", "${valueRound(105004) ?: ''}")
        builder.field("xfdf:original": "525", "${valueRound(105007) ?: ''}")
        builder.field("xfdf:original": "526", "${valueRound(305003) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> garazNaInejAdrese = { MarkupBuilder builder ->
        builder.field("xfdf:original": "38", "${value(109057) ?: ''}")
        builder.field("xfdf:original": "39", "${value(109054) ?: ''}")
        builder.field("xfdf:original": "40", "${value(109055) ?: ''}")
        builder.field("xfdf:original": "41", "${value(109056) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> doplnkovePoistenieNehnutelnosti = { MarkupBuilder builder ->
        builder.asdasdasdadd("${valueRound(105032) ?: ''}")
        builder.field("xfdf:original": "529", "${valueRound(305015) ?: ''}")
        builder.field("xfdf:original": "528", "${valueRound(105034) ?: ''}")
        builder.field("xfdf:original": "530", "${valueRound(305016) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistenieZodpovednostiZaSkoduNehnutelnost = { MarkupBuilder builder ->
        builder.field("xfdf:original": "531", "${value(107001) ?: ''}")
        builder.field("xfdf:original": "532", "${valueRound(308008) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> poistenieDomacnosti = { MarkupBuilder builder ->
        builder.field("xfdf:original": "67", "${value(103001) ?: ''}")
        builder.field("xfdf:original": "68", "${value(103002) ?: ''}")
        builder.field("xfdf:original": "69", "${valueBool(103003) ?: ''}")
        builder.field("xfdf:original": "70", "${valueBool(103004) ?: ''}")
        builder.field("xfdf:original": "71", "${valueBool(103005) ?: ''}")
        builder.field("xfdf:original": "72", "${valueRound(106003) ?: ''}")
        builder.field("xfdf:original": "73", "${valueRound(106002) ?: ''}")
        builder.field("xfdf:original": "74", "${valueRound(106022) ?: ''}")
        builder.field("xfdf:original": "vvbcbcvcvcvnmjkl,,,", "${valueRound(306001) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> doplnkovePoistenieDomacnostiPoistneSumy = { MarkupBuilder builder ->
        builder.field("xfdf:original": "91", "${valueRound(106005) ?: ''}")
        builder.field("xfdf:original": "93", "${valueRound(106007) ?: ''}")
        builder.field("xfdf:original": "95", "${valueRound(106009) ?: ''}")
        builder.field("xfdf:original": "97", "${valueRound(106011) ?: ''}")
        builder.field("xfdf:original": "99", "${valueRound(106013) ?: ''}")
        builder.field("xfdf:original": "101", "${valueRound(106015) ?: ''}")
        builder.field("xfdf:original": "103", "${valueRound(106017) ?: ''}")
        builder.field("xfdf:original": "105", "${valueRound(106019) ?: ''}")
        builder.fvbgggztzhjg("${value(106021) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> doplnkovePoistenieDomacnostiPoistne = { MarkupBuilder builder ->
        builder.field("xfdf:original": "92", "${valueRound(306002) ?: ''}")
        builder.field("xfdf:original": "94", "${valueRound(306003) ?: ''}")
        builder.field("xfdf:original": "96", "${valueRound(306004) ?: ''}")
        builder.field("xfdf:original": "98", "${valueRound(306005) ?: ''}")
        builder.field("xfdf:original": "100", "${valueRound(306006) ?: ''}")
        builder.field("xfdf:original": "102", "${valueRound(306007) ?: ''}")
        builder.field("xfdf:original": "104", "${valueRound(306008) ?: ''}")
        builder.field("xfdf:original": "sdasdavkphouíáít", "${valueRound(306009) ?: ''}")
        builder.field("xfdf:original": "fdsfsfsdtttttttgdsfsôôôôô", "${valueRound(306010) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> zodpovednostZaSkoduDomacnost = { MarkupBuilder builder ->
        builder.field("xfdf:original": "ôôlll", "${value(107003) ?: ''}")
        builder.ldkdd("${valueRound(308009) ?: ''}")
        builder.field("xfdf:original": "pooríííčáčôfúääňňsss", "${value(104003) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> sposobUhradenia = { MarkupBuilder builder ->
//            x("${value() ?: ''}") //todo
//            X2("${value() ?: ''}") //todo

        return builder
    }

    Closure<MarkupBuilder> osobitneVyjadrenia = { MarkupBuilder builder ->
//            mn("${value() ?: ''}") //todo
//            field("xfdf:original": ",ketuýg", "${value() ?: ''}") //todo

        return builder
    }

    Closure<MarkupBuilder> suhlasOsobneUdaje = { MarkupBuilder builder ->
//            X3("${value() ?: ''}") //todo
//            X4("${value() ?: ''}") //todo

        return builder
    }

    private String value(Long id) {
        return useCase.dataSet[useCase.petriNet.dataSet.find { it.value.importId == id }?.key]?.value?.toString()
    }

    /**
     * Transforms boolean data field value into 'Áno' / 'Nie'.
     * @param id data field import id
     * @return <b>'Áno'</b> if value is true, <b>'Nie'</b> otherwise <br><b>null</b> if value is null
     */
    private String valueBool(Long id) {
        def value = value(id)
        if (value == null)
            return null

        return value == 'true' ? 'Áno' : 'Nie'
    }

    /**
     * Calculates discount percentage as (1 - value)%
     * @param id data field import id
     * @param returnZero if false, method will return null when value is 0
     * @return values discount percentage<br> <b>null</b> if value is null
     */
    private String valuePercentageDiscount(Long id, boolean returnZero = false) {
        def value = value(id)
        if (value == null)
            return null

        value = ((1.0 - (value as Double)) * 100.0).round(2)
        if (value == 0.0 && !returnZero)
            return null
        return value as String
    }

    private String valueDate(Long id) {
        def value = value(id)
        if (value == null)
            return null
//        todo
        return value
    }

    /**
     *
     * @param id
     * @param precision
     * @return
     */
    private String valueRound(Long id, int precision = 2) {
        def value = value(id)
        if (value == null)
            return null

        return (value as Double).round(precision) as String
    }
}