package com.netgrif.application.engine

import com.netgrif.application.engine.petrinet.domain.repositories.PetriNetRepository
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController


import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE

@Slf4j
@RestController
@RequestMapping("/dev/")
@Profile("dev")
class DevConsole {
    // TODO: release/8.0.0 fix old syntax

    @Autowired
    private CaseRepository caseRepository

    @Autowired
    private PetriNetRepository netRepository

    @GetMapping(value = "/dataset/{title}", produces = APPLICATION_JSON_VALUE)
    String dataset(@PathVariable String title) {
        def useCase = caseRepository.findAll().find { it.title == title }
        return "{ ${useCase?.dataSet?.collect { "\"${useCase?.petriNet?.dataSet?.get(it?.key)?.importId}:${useCase?.petriNet?.dataSet?.get(it?.key)?.title?.toString()?.replaceAll("\n[ ]{2}", "")}\":\"${it?.value?.value as String}\"" }?.join(", ")} }"
    }

    @GetMapping(value = "/net/{title}", produces = APPLICATION_XML_VALUE)
    String netSnapshot(@PathVariable String title) {
        // TODO: NAE-1969 fix
//        try {
//            def useCase = caseRepository.findAll().find { it.title == title }
//            def net = useCase.petriNet
//            def xml = new File(net.importXmlPath)
//
//            JAXBContext jaxbContext = JAXBContext.newInstance(Document.class)
//            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller()
//            Document document = (Document) jaxbUnmarshaller.unmarshal(xml)
//
//            document.getImportPlaces().each { importPlace ->
//                importPlace.tokens = useCase.activePlaces.get(net.places.values().find {
//                    it.importId == importPlace.id
//                }.getStringId())
//                if (importPlace.tokens == null)
//                    importPlace.tokens = 0
//            }
//            document.getImportData().each {
//                it.action = null
//                it.values = null
//                it.documentRefs = null
//            }
//            document.getImportTransitions().each {
//                it.dataGroup = null
//            }
//            document.importRoles = null
//            document.importTransactions = null
//            document.importData = null
//
//            Marshaller marshaller = jaxbContext.createMarshaller()
//            StringWriter stringWriter = new StringWriter()
//            marshaller.marshal(document, stringWriter)
//
//            return stringWriter.toString()
//        } catch (Exception ignored) {
//            log.error("Getting snapshot of net failed: ", ignored)
            return null
//        }
    }

}
