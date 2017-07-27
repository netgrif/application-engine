package com.netgrif.workflow

import com.netgrif.workflow.workflow.domain.repositories.CaseRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import static org.springframework.web.bind.annotation.RequestMethod.GET

@RestController
@RequestMapping("/dev/")
@Profile("dev")
class DevConsole {

    @Autowired
    private CaseRepository caseRepository

    @RequestMapping(value = "/dataset/{title}", method = GET, produces = APPLICATION_JSON_VALUE)
    String dataset(@PathVariable String title) {
        def useCase = caseRepository.findAll().find {it.title == title}
        return "{ ${useCase?.dataSet?.collect {"\"${useCase?.petriNet?.dataSet?.get(it?.key)?.importId}:${useCase?.petriNet?.dataSet?.get(it?.key)?.name?.replaceAll("\n[ ]{2}", "")}\":\"${it?.value?.value as String}\""}?.join(", ")} }"
    }

//    @RequestMapping(value = "/login", method = GET)
//    def login() {
//        def remote = new HTTPBuilder("http://localhost:8080/user")
//        remote.auth.basic('agent@company.com', 'password')
//        remote.request(Method.GET) {}
//        return new RedirectView("/offers")
//    }
}