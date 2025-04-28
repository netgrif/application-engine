package com.netgrif.application.engine.business.orsr

import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
@SuppressWarnings("GrMethodMayBeStatic")
class OrsrService implements IOrsrService {

    static final Logger log = LoggerFactory.getLogger(OrsrService.class.getName())

    def ORSR_URL_BASE = "http://www.orsr.sk/"

    def ORSR_URL_SEARCH = { ICO -> "hladaj_ico.asp?ICO=$ICO&SID**=0" }

    @Override
    OrsrReference findByIco(String companyId) {
        def companyUrl = parseCompanyUrl(companyId)

        if (companyUrl == null)
            return null

        return parseCompanyInfo(companyUrl, companyId)
    }

    private String parseCompanyUrl(String companyId) {
        try {
            Document doc = Jsoup.connect("${ORSR_URL_BASE}${ORSR_URL_SEARCH(companyId)}" as String).get()
            Element table = doc.select("table[cellpadding=2]").get(0)

            return table.child(0).child(1).child(2).child(0).child(1).attr("href")
        } catch (Exception ignored) {
            log.info("Company [$companyId] couldn't be parsed.")
            return null
        }
    }

    private OrsrReference parseCompanyInfo(String companyUrl, String companyId) {
        OrsrReference info = new OrsrReference(id: companyId)

        try {
            Document doc = Jsoup.connect("${ORSR_URL_BASE}${companyUrl}").get()
            Elements tables = doc.select("table[cellspacing=3]")

            info.name = tables?.get(0)?.select("span[class=ra]")?.get(0)?.text()
            info.created = tables?.get(3)?.select("span[class=ra]")?.get(0)?.text()
            parseStreet(info, tables)
            parseStreetNumber(info, tables)
            parseCity(info, tables)
            parsePostalCode(info, tables)
        } catch (HttpStatusException e) {
            log.error("HTTP error fetching URL $ORSR_URL_BASE$companyUrl", e)
        }

        return info
    }

    private void parseStreet(OrsrReference ref, Elements tables) {
        try {
            Elements streetRow = tables?.get(1)?.select("span[class=ra]")
            if (streetRow.size() > 2)
                ref.street = streetRow?.get(0)?.text()
        } catch (Exception ignored) {
            log.info("Couldn't parse street of [${ref.id}]")
        }
    }

    private void parseStreetNumber(OrsrReference ref, Elements tables) {
        try {
            Elements streetRow = tables?.get(1)?.select("span[class=ra]")
            if (streetRow.size() > 2)
                ref.streetNumber = streetRow?.get(1)?.text()
        } catch (Exception ignored) {
            log.info("Couldn't parse street number of [${ref.id}]")
        }
    }

    private void parseCity(OrsrReference ref, Elements tables) {
        try {
            Elements cityRow = tables?.get(1)?.select("span[class=ra]")
            if (cityRow.size() > 2)
                ref.city = cityRow?.get(2)?.text()
            else
                ref.city = cityRow?.get(0)?.text()
        } catch (Exception ignored) {
            log.info("Couldn't parse city name of [${ref.id}]")
        }
    }

    private void parsePostalCode(OrsrReference ref, Elements tables) {
        try {
            ref.postalCode = tables?.get(1)?.select("span[class=ra]")?.get(3)?.text()
        } catch (Exception ignored) {
            log.info("Couldn't parse postal code of [${ref.id}]")
        }
    }
}