package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.pdf.generator.service.PdfDataHelper;
import com.netgrif.application.engine.pdf.generator.service.PdfDrawer;
import com.netgrif.application.engine.pdf.generator.service.PdfGenerator;
import com.netgrif.application.engine.pdf.generator.service.fieldbuilders.PdfFieldBuilder;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.application.engine.pdf.generator.service.renderer.PdfFieldRenderer;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;

@Configuration
public class PrototypesConfiguration {

    @Bean("importer")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public Importer importer() {
        return new Importer();
    }

    @Bean("actionDelegate")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public ActionDelegate actionDelegate() {
        return new ActionDelegate();
    }

    @Bean("fileStorageConfiguration")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public FileStorageConfiguration fileStorageConfiguration() {
        return new FileStorageConfiguration();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfDataHelper pdfDataHelper(List<PdfFieldBuilder<?>> builders) {
        return new PdfDataHelper(builders);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfGenerator pdfGenerator(IPdfDataHelper pdfDataHelper, List<PdfFieldRenderer<?>> renderers, IPdfDrawer pdfDrawer) {
        return new PdfGenerator(pdfDataHelper, renderers, pdfDrawer);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfDrawer pdfDrawer() {
        return new PdfDrawer();
    }

    @Bean("userResourceAssembler")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public UserResourceAssembler userResourceAssembler() {
        return new UserResourceAssembler();
    }
}