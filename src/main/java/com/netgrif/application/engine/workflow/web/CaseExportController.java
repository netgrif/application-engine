package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.configuration.properties.CaseExportProperties;
import com.netgrif.application.engine.workflow.service.interfaces.ICaseImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

@Slf4j
@RestController()
@RequestMapping("/api/case/export")
@AllArgsConstructor
public class CaseExportController {

    private final ICaseImportExportService caseExportImportService;
    private final CaseExportProperties properties;

    @Operation(summary = "Download xml file containing data of requested cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> exportCases(@RequestParam("caseIds") Set<String> caseIds, @RequestParam boolean withFiles) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        exportCases(caseIds, outputStream, withFiles);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        String fileName = withFiles ? "case_export.zip" : properties.getFileName();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    private void exportCases(Set<String> caseIds, ByteArrayOutputStream outputStream, boolean withFiles) {
        try {
            if (withFiles) {
                caseExportImportService.findAndExportCasesWithFiles(caseIds, outputStream);
            } else {
                caseExportImportService.findAndExportCases(caseIds, outputStream);
            }
        } catch (IOException e) {
            log.error("Error occurred during exporting of cases", e);
            throw new RuntimeException(e);
        }
    }
}
