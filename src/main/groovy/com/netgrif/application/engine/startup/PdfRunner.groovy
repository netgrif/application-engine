package com.netgrif.application.engine.startup

import com.netgrif.application.engine.pdf.generator.config.PdfResource
import com.netgrif.application.engine.pdf.generator.service.PdfDrawer
import com.netgrif.application.engine.pdf.generator.service.PdfGenerator
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
@Slf4j
class PdfRunner extends AbstractOrderedCommandLineRunner{

    @Autowired
    private PdfResource resource

    @Override
    void run(String... args) throws Exception {
        assert resource.fontTitleResource.exists()
        assert resource.fontLabelResource.exists()
        assert resource.fontValueResource.exists()

        assert resource.checkBoxCheckedResource.exists()
        assert resource.checkBoxUnCheckedResource.exists()
        assert resource.radioCheckedResource.exists()
        assert resource.radioUnCheckedResource.exists()
        assert resource.booleanCheckedResource.exists()
        assert resource.booleanUncheckedResource.exists()
    }
}
