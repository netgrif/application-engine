package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.workflow.petrinet.domain.I18nString;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "roleImportId",
        "netImportId"
})
public class MenuEntryRole {

    @XmlElement(required = true)
    protected String roleImportId;
    @XmlElement(required = true)
    protected String netImportId;
    @XmlAttribute(name = "authorizationType", required = true)
    protected AuthorizationType authorizationType;

}