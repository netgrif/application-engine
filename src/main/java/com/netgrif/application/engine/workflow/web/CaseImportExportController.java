package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.workflow.service.interfaces.ICaseExportImportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Slf4j
@RestController()
@RequestMapping("/api/case")
public class CaseImportExportController {

    @Autowired
    private ICaseExportImportService caseExportImportService;

    @Operation(summary = "Download xml file containing exported case data", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> exportCase(@RequestParam("caseId") String caseId) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        caseExportImportService.findAndExportCases(Set.of(caseId), outputStream);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + caseId + ".xml\"");
//todo delete file after after returning
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    @Operation(summary = "Import case from xml file file", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/import", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importCase(@RequestPart(value = "file") MultipartFile multipartFile) {
        try {
            caseExportImportService.importCases(multipartFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body("Import successful");
    }
}
