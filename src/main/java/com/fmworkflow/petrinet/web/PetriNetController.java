package com.fmworkflow.petrinet.web;

import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.service.IPetriNetService;
import com.fmworkflow.petrinet.web.requestbodies.ImportBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/petrinet")
public class PetriNetController {
    @Autowired
    private IPetriNetService service;

    @RequestMapping(value = "/import", method = RequestMethod.POST)
    public String importPetriNet(@RequestBody ImportBody body) {
        try {
            service.importPetriNet(body.xmlFile, URLDecoder.decode(body.title, StandardCharsets.UTF_8.name()));
            return JsonBuilder.successMessage("Petri net imported successfully");
        } catch (SAXException e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Invalid xml file");
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Invalid parser configuration");
        } catch (IOException e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("IO error");
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<PetriNet> getAll() {
        return service.loadAll();
    }
}
