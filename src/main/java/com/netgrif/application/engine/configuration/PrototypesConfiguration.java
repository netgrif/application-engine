package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.auth.web.responsebodies.UserResourceAssembler;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.pdf.generator.service.PdfDataHelper;
import com.netgrif.application.engine.pdf.generator.service.PdfDrawer;
import com.netgrif.application.engine.pdf.generator.service.PdfGenerator;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.application.engine.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.application.engine.workflow.domain.FileStorageConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

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
    public IPdfDataHelper pdfDataHelper() {
        return new PdfDataHelper();
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfGenerator pdfGenerator() {
        return new PdfGenerator();
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