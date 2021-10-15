package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.workflow.importer.model.MenuItemRole;
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
        "entry_name",
        "iconIdentifier",
        "menuItemRoleList",
})
public class MenuEntry {

    @XmlElement(required = true)
    protected String entry_name;
    @XmlElement
    protected String iconIdentifier;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entryRole")
    protected List<MenuItemRole> menuItemRoleList;
    @JacksonXmlProperty(isAttribute = true)
    protected Boolean useIcon;
}