package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.xml.bind.annotation.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "menuEntry", propOrder = {
        "entryName",
        "menuEntryRoleList",
})
public class MenuEntry {

    @XmlElement(required = true)
    protected String entryName;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entryRole")
    protected List<MenuEntryRole> menuEntryRoleList;
    @JacksonXmlProperty(isAttribute = true)
    protected Boolean useIcon;
}