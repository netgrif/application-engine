package com.netgrif.workflow.petrinet.domain.dataset.logic.action

import com.netgrif.workflow.context.ApplicationContextProvider
import com.netgrif.workflow.pdf.service.PdfUtils
import com.netgrif.workflow.petrinet.domain.dataset.Field
import com.netgrif.workflow.petrinet.domain.dataset.FileField
import com.netgrif.workflow.premiuminsurance.IdGenerator
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

        File draftPdfFile = generateDraftPdf(draftPath)
        File offerPdfFile = generateOfferPdf(offerPath)
        File mergedPdf = PdfUtils.mergePdfFiles(finalPath, draftPdfFile, offerPdfFile)

        useCase.dataSet.get(field.stringId).setValue(FINAL_FILENAME)

        return mergedPdf
    }

    String offerId() {
        def generator = ApplicationContextProvider.getBean("idGenerator") as IdGenerator
        def id = generator.getId() as String

        def prefix = "311"
        def base = id.padLeft(6, "0")
        def postfix = "$prefix$base".collect { it as int }.sum() % 10

        useCase.dataSet.get(field.stringId).setValue("${prefix}${base}${postfix}" as String)

        return "${prefix}${base}${postfix}"
    }

    private File generateDraftPdf(String draftPath) {
        String draftXml = datasetToDraftXml()
        File pdf = PdfUtils.fillPdfForm(draftPath, new FileInputStream("$PDF_PATH/$DRAFT_FILENAME"), draftXml)

        switch (value(410001)) {
            case "Nehnutelnost":
                pdf = PdfUtils.removePages(pdf, 4)
                break
            case "Domacnost":
                pdf = PdfUtils.removePages(pdf, 3)
                break
        }

        return pdf
    }

    private File generateOfferPdf(String offerPath) {
        String offerXml = datasetToOfferXml()
        File pdf = PdfUtils.fillPdfForm(offerPath, new FileInputStream("$PDF_PATH/$OFFER_FILENAME"), offerXml)
        return pdf
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
            zodpovednostZaSkoduDomacnost(builder)

            field("xfdf:original": "Text Field 143", "${value(309001) ?: ''}")
            field("xfdf:original": "Text Field 153", "${value(309001) ?: ''}")
        }

        return writer.toString()
    }

    private String datasetToOfferXml() {
        def writer = new StringWriter()
        def builder = new MarkupBuilder(writer)

        builder.fields('xmlns:xfdf': '"http://ns.adobe.com/xfdf-transition/"') {
            miestoPoistenia(builder)
            udajeOPoisteni(builder)
            udajeOPoistnom(builder)
            spoluucastPriPoistnomPlneni(builder)
            rekapitulaciaPoistneho(builder)
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
            zodpovednostZaSkoduDomacnost(builder)

            field('xfdf:original': "Text Field 153", "${value(309001) ?: ''}")
            field('xfdf:original': "Text Field 143", "${value(309001) ?: ''}")
            field('xfdf:original': "Text Field 152", "${value(309001) ?: ''}")
        }
        return writer.toString()
    }

    Closure<MarkupBuilder> poistnik = { MarkupBuilder builder ->
        builder.field("xfdf:original": "Text Field 50", "${value(109007) ?: ''}")
        builder.field("xfdf:original": "Text Field 51", "${value(109008) ?: ''}")
        builder.field("xfdf:original": "1", "${value(109010) ?: ''} ${value(109011) ?: ''}")
        builder.field("xfdf:original": "2", "${value(109012) ?: ''}")
        builder.field("xfdf:original": "3", "${value(109009) ?: ''}")
        builder.field("xfdf:original": "4", "${valueDate(109014) ?: ''}")
        builder.field("xfdf:original": "5", "${(valueRC(109015) ?: value(109058)) ?: ''}")
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
        builder.field("xfdf:original": "405", "${valueDate(109028) ?: ''}")
        builder.field("xfdf:original": "406", "${(valueRC(109029) ?: value(109059)) ?: ''}")
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
        builder.field("xfdf:original": "106", "${valueDate(309002) ?: ''}")
        builder.field("xfdf:original": "107", "${valueDate(109001) ?: ''}")
        builder.field("xfdf:original": "108", "${(Boolean.parseBoolean(value(109002))) ? (valueDate(109003) ?: '') : 'poistenie na dobu neurčitú'}")

        return builder
    }

    Closure<MarkupBuilder> udajeOPoistnom = { MarkupBuilder builder ->
        builder.field("xfdf:original": "109", "${value(108001) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> spoluucastPriPoistnomPlneni = { MarkupBuilder builder ->
        builder.field("xfdf:original": "453", "${value(105005).replace('€', '') ?: ''}")
        builder.field("xfdf:original": "775", "${value(106001).replace('€', '') ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> rekapitulaciaPoistneho = { MarkupBuilder builder ->
        builder.field("xfdf:original": "110", "${valueRound(305002) ?: ''}")
        builder.field("xfdf:original": "112", "${valueSum(305003, 305004, 305005, 305006, 305007, 305008, 305009, 305010, 305011, 305012, 305013, 305014) ?: ''}")
        builder.field("xfdf:original": "114", "${valueRound(306001) ?: ''}")
        builder.field("xfdf:original": "116", "${valueRound(308003) ?: ''}")
        builder.field("xfdf:original": "118", "${valueSum(305015, 305016, 306002, 306003, 306004, 306005, 306006, 306007, 306008, 306009, 306010) ?: ''}")
        builder.field("xfdf:original": "120", "${valueRound(308004) ?: ''}")
        builder.field("xfdf:original": "89323", "${valueRound(308006) ?: ''}")
        builder.field("xfdf:original": "111", "${value(108003)?.replace('%', '.0') ?: ''}")
        builder.field("xfdf:original": "113", "${valuePercentageDiscount(208004) ?: ''}")
        builder.field("xfdf:original": "115", "${valuePercentageDiscount(203004) ?: ''}")
        builder.field("xfdf:original": "117", "${valuePercentageDiscount(208008) ?: ''}")
        builder.field("xfdf:original": "119", "${valuePercentageDiscount(208005) ?: ''}")
        builder.field("xfdf:original": "121", "${valuePercentageDiscount(208007) ?: ''}")

        def periodicity = ((value(108001) == "štvrťročná") ? 4 : ((value(108001) == "polročná") ? 2 : 1)) as double
        def payment = Math.floor((((value(308006) as Double) / periodicity) * 100) as Double) / 100
        builder.field("xfdf:original": "mjjttzeželljsd", "$payment")

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
        builder.field("xfdf:original": "531", "${value(107001).replace('€', '') ?: ''}")
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
        builder.field("xfdf:original": "ôôlll", "${value(107003).replace('€', '') ?: ''}")
        builder.ldkdd("${valueRound(308009) ?: ''}")
        builder.field("xfdf:original": "pooríííčáčôfúääňňsss", "${value(104003) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> sposobUhradenia = { MarkupBuilder builder ->
        builder.x(value(109006) == "prevodom" ? 'X' : '')
        builder.X2(value(109006) == "prevodom" ? '' : 'X')
        builder.mn("${value(109065) ?: ''}")

        return builder
    }

    Closure<MarkupBuilder> osobitneVyjadrenia = { MarkupBuilder builder ->
        def MAX_LENGTH = 125
        def multiLine = value(109066)

        if (multiLine?.length() > MAX_LENGTH) {
            int index = multiLine?.take(MAX_LENGTH)?.toString()?.lastIndexOf(' ')
            builder.osobitne("${multiLine?.take(index) ?: ''}")
            builder.field("xfdf:original": ",ketuýg", "${value(109066)?.substring(index) ?: ''}")
        } else {
            builder.osobitne("${multiLine ?: ''}")
        }

        return builder
    }

    Closure<MarkupBuilder> suhlasOsobneUdaje = { MarkupBuilder builder ->
        builder.X3(Boolean.parseBoolean(value(109067)) ? 'X' : '')
        builder.X4(Boolean.parseBoolean(value(109067)) ? '' : 'X')

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

        try {
            return Date.parseToStringDate(value).format("dd.MM.YYYY")
        } catch (Exception e) {
            e.printStackTrace()
            return null
        }
    }

    /**
     *
     * @param id
     * @param precision
     * @return
     */
    private String valueRound(Long id, int precision = 2, boolean returnZero = false) {
        def value = value(id)
        if (value == null)
            return null

        if (value as Double == 0.0 && !returnZero)
            return null

//        TODO: format to separate thousands e.g. '1 000 000'
        return (value as Double).round(precision) as String
    }

    private String valueSum(Long... ids) {
        Double sum = 0.0
        ids.each {
            def value = value(it)
            if (value != null)
                sum += value as Double
        }

        return sum.round(2)
    }

    private String valueRC(Long id) {
        def rc = value(id)
        if (rc == null)
            return null

        if (rc ==~ /^[0-9]{6}\/[0-9]{3,4}$/)
            return rc

        return "${rc.substring(0, 6)}/${rc.substring(6, rc.length())}"
    }
}