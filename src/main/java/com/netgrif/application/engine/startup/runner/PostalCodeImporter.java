package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.business.IPostalCodeService;
import com.netgrif.application.engine.business.PostalCode;
import com.netgrif.application.engine.business.PostalCodeRepository;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;


@Slf4j
@Component
@RunnerOrder(18)
@Profile("!update")
@RequiredArgsConstructor
public class PostalCodeImporter extends AbstractOrderedApplicationRunner {

    @Getter
    @Setter
    @Value("${nae.postal.codes.import}")
    private Boolean importPostalCode;

    @Getter
    @Setter
    @Value("${nae.postal.codes.csv}")
    private String postalCodesPath;

    private final IPostalCodeService service;
    private final PostalCodeRepository repository;

    public void run(ApplicationArguments strings) throws IOException {
        if (!importPostalCode) {
            log.info("Skip import postal codes");
            return;
        }

        log.info("Importing postal codes from file {}", postalCodesPath);
        AtomicInteger lineCounter = new AtomicInteger(0);
        try (Stream<String> lines = Files.lines(new ClassPathResource(postalCodesPath).getFile().toPath())) {
            lines.parallel().forEach(line -> lineCounter.incrementAndGet());
        } catch (Exception ex) {
            log.error("Failed to import postal codes", ex);
        }

        if (repository.count() == lineCounter.get()) {
            log.info("All {} postal codes have been already imported", lineCounter.get());
            return;
        }

        repository.deleteAll();
        List<PostalCode> codes = new ArrayList<>();
        try (Stream<String> lines = Files.lines(new ClassPathResource(postalCodesPath).getFile().toPath())) {
            codes = lines.map(line -> {
                        String[] parts = line.split(",");
                        if (parts.length < 2) return null;
                        return new PostalCode(parts[0].replaceAll("\\s", "").trim(), parts[1].trim());
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception ex) {
            log.error("Failed to import postal codes", ex);
        }
        service.savePostalCodes(codes);
        log.info("Postal codes imported");
    }

}
