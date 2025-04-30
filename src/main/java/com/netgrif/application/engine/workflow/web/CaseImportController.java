package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.exceptions.ImportXmlFileMissingException;
import com.netgrif.application.engine.workflow.service.interfaces.ICaseImportExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController()
@RequestMapping("/api/case/import")
@AllArgsConstructor
public class CaseImportController {

    private final ICaseImportExportService caseExportImportService;

    @Operation(summary = "Import cases from xml file", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importCases(@RequestPart(value = "file") MultipartFile multipartFile) {
        List<Case> importedCases;
        try {
            importedCases = caseExportImportService.importCases(multipartFile.getInputStream());
        } catch (IOException e) {
            log.error("Error occurred during importing of cases", e);
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
    @PostMapping(value = "/withFiles", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> importCasesWithFiles(@RequestPart(value = "zipFile") MultipartFile multipartZipFile) {
        List<Case> importedCases;
        try {
            importedCases = caseExportImportService.importCasesWithFiles(multipartZipFile.getInputStream());
        } catch (IOException | StorageException e) {
            log.error("Error occurred during importing of cases", e);
            throw new RuntimeException(e);
        } catch (ImportXmlFileMissingException e) {
            log.error("Xml file with cases to import missing from archive file", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(importedCases.stream().map(Case::getStringId).collect(Collectors.joining(",")));
    }
}
