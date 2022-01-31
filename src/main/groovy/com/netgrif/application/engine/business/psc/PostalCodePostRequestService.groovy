package com.netgrif.application.engine.business.psc

import com.netgrif.application.engine.business.PostalCode
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory

//@Service
class PostalCodePostRequestService {

    private static final Logger log = LoggerFactory.getLogger(PostalCodePostRequestService.class.name)

    List<PostalCode> getByCode(String code) {
        def json = makeRequest(code)
        if (json.offices.isEmpty()) return new ArrayList<PostalCode>()
        List<PostalCode> postalCodes = new ArrayList<>()
        json.ofmojefices.each {
            postalCodes.add(new PostalCode(code, it.city, "other", "L1"))
        }
        return postalCodes
    }

    List<PostalCode> getByLocality() {
        return new ArrayList<PostalCode>()
    }

    private static Object makeRequest(String param) {
        def cookies = []
        def httpBuilder = new HTTPBuilder()
        httpBuilder.handler.success = { HttpResponseDecorator resp, reader ->
            resp.getHeaders('Set-Cookie').each {
                String cookie = it.value.split(';')[0]
                cookies.add(cookie)
            }
            return reader
        }

        def response = httpBuilder.request(Method.GET, ContentType.ANY) { request ->
            uri.path = "http://api.posta.sk/private/search"
            uri.query = [q: param, m: "zip"]
            headers['Cookie'] = "TS012f851e=01a27f45ea53a1d0587d0dd26752ac5b6f9ae78ff4cb68b8985f14717934a71bc85eeafbf7986ce78ffa9420c08ec96ebb2653176d; Path=/; Domain=.api.posta.sk"
        }

        //def response = new URL("http://api.posta.sk/private/search?q=${param}&m=zip").getText()
        log.info("POST RESPONSE: " + response)
//        def slurper = new JsonSlurper()
//        return slurper.parseText(response)
        return response
    }
}