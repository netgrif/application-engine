package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.ICaseExportImportService;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@RequestMapping("/api/case")
@AllArgsConstructor
public class CaseImportExportController {

    private final ICaseExportImportService caseExportImportService;

    @Operation(summary = "Download xml file containing data of requested cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> exportCases(@RequestParam("caseIds") Set<String> caseIds) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        todo exception handling
        try {
            caseExportImportService.findAndExportCases(caseIds, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"case_export.xml\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    @Operation(summary = "Download xml file containing data of requested cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/exportWithFiles", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> exportCasesWithFiles(@RequestParam("caseIds") Set<String> caseIds) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        todo exception handling
        try {
            caseExportImportService.findAndExportCasesWithFiles(caseIds, outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"case_export.zip\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(new ByteArrayInputStream(outputStream.toByteArray())));
    }

    @Operation(summary = "Import cases from xml file", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/import", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importCases(@RequestPart(value = "file") MultipartFile multipartFile) {
        List<Case> importedCases;
        try {
            importedCases = caseExportImportService.importCases(multipartFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(importedCases.stream().map(Case::getStringId).collect(Collectors.joining(",")));
    }

    @Operation(summary = "Import cases from zip archive", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/importWithFiles", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importCasesWithFiles(@RequestPart(value = "zipFile") MultipartFile multipartZipFile) {
        List<Case> importedCases;
        try {
            importedCases = caseExportImportService.importCasesWithFiles(multipartZipFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(importedCases.stream().map(Case::getStringId).collect(Collectors.joining(",")));
    }
}
