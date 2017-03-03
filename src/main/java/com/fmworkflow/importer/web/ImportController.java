package com.fmworkflow.importer.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@RestController
@RequestMapping("/importer/")
public class ImportController {
    @RequestMapping(value = "/xml", method = RequestMethod.GET)
    public @ResponseBody String showXml() throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = Files.newBufferedReader(Paths.get("src/test/resources/prikladFM.xml"));
        br.lines().forEach(sb::append);
        return sb.toString();
    }
}