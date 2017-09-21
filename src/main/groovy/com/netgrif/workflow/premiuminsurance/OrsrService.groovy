package com.netgrif.workflow.premiuminsurance

import org.apache.log4j.Logger
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import org.springframework.stereotype.Service

@Service
class OrsrService implements IOrsrService {

    private static final Logger log = Logger.getLogger(OrsrService.class.name)

    def ORSR_URL_BASE = "http://www.orsr.sk/"

    def ORSR_URL_SEARCH = {ICO -> "hladaj_ico.asp?ICO=$ICO&SID**=0"}

    @Override
    OrsrReference findByIco(String ico) {
        def companyUrl = parseCompanyUrl(ico)
        return parseCompanyInfo(companyUrl)
    }

    private String parseCompanyUrl(String companyIco) {
        try {
            Document doc = Jsoup.connect("${ORSR_URL_BASE}${ORSR_URL_SEARCH(companyIco)}" as String).get()
            Element table = doc.select("table[cellpadding=2]").get(0)

            return table.child(0).child(1).child(2).child(0).child(1).attr("href")
        } catch (Exception ignored) {
            return null
        }
    }

    private OrsrReference parseCompanyInfo(String companyUrl) {
        OrsrReference info = new OrsrReference()

        try {
            Document doc = Jsoup.connect("${ORSR_URL_BASE}${companyUrl}").get()
            Elements tables = doc.select("table[cellspacing=3]")

            info.name = tables?.get(0)?.select("span[class=ra]")?.get(0)?.text()
            info.created = tables?.get(3)?.select("span[class=ra]")?.get(0)?.text()
            info.street = tables?.get(1)?.select("span[class=ra]")?.get(0)?.text()
            info.streetNumber = tables?.get(1)?.select("span[class=ra]")?.get(1)?.text()
            info.city = tables?.get(1)?.select("span[class=ra]")?.get(2)?.text()
            info.zipCode = tables?.get(1)?.select("span[class=ra]")?.get(3)?.text()
        } catch (HttpStatusException e) {
            log.error("HTTP error fetching URL $ORSR_URL_BASE$companyUrl", e)
        }

        return info
    }
}