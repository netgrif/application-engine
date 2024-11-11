package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(200)
@RequiredArgsConstructor
public class PdfRunner implements ApplicationEngineStartupRunner {

    private final PdfResource resource;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        assert resource.getFontTitleResource().exists();
        assert resource.getFontLabelResource().exists();
        assert resource.getFontValueResource().exists();

        assert resource.getCheckBoxCheckedResource().exists();
        assert resource.getCheckBoxUnCheckedResource().exists();
        assert resource.getRadioCheckedResource().exists();
        assert resource.getRadioUnCheckedResource().exists();
        assert resource.getBooleanCheckedResource().exists();
        assert resource.getBooleanUncheckedResource().exists();
    }

}
