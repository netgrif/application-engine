package com.netgrif.workflow.configuration;

import com.netgrif.workflow.importer.service.Importer;
import com.netgrif.workflow.pdf.generator.service.PdfDataHelper;
import com.netgrif.workflow.pdf.generator.service.PdfDrawer;
import com.netgrif.workflow.pdf.generator.service.PdfGenerator;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDataHelper;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfDrawer;
import com.netgrif.workflow.pdf.generator.service.interfaces.IPdfGenerator;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.ActionDelegate;
import com.netgrif.workflow.workflow.domain.FileStorageConfiguration;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;

@Configuration
public class PrototypesConfiguration {

    @Bean("importer")
    @Primary
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

    @Bean("dataConverter")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfDataHelper dataConverter(){return new PdfDataHelper();}

    @Bean("pdfGenerator")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfGenerator pdfGenerator(){return new PdfGenerator();}

    @Bean("pdfDrawer")
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public IPdfDrawer pdfDrawer(){return new PdfDrawer();}
}