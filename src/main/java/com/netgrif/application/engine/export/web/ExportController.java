package com.netgrif.application.engine.export.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.export.configuration.XlsExportProperties;
import com.netgrif.application.engine.export.service.interfaces.IXlsExportService;
import com.netgrif.application.engine.export.web.requestbodies.FilteredCasesRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/export")
public class ExportController {

    private final IXlsExportService exportService;
    private final XlsExportProperties exportProperties;

    @PostMapping(value = "/filteredCases", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> getStatisticsFile(@RequestBody FilteredCasesRequest requestBody, Authentication auth, Locale locale) throws Exception {

        LoggedUser user = (LoggedUser) auth.getPrincipal();
        File excel = exportService.getExportFilteredCasesFile(requestBody, user, locale);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=" +
                (LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH_mm_ss")) + "-" + exportProperties.getExportFileName() + ".xlsx"));
        headers.setContentLength(Files.size(excel.toPath()));

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new FileSystemResource(excel) {
                    @Override
                    public InputStream getInputStream() throws IOException {
                        return new FileInputStream(excel) {
                            @Override
                            public void close() throws IOException {
                                super.close();
                                Files.delete(excel.toPath());
                            }
                        };
                    }
                });
    }

}
