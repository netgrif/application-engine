package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.workflow.importer.model.MenuItem;
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
        "menuItemList",
})

@XmlRootElement(name = "menu")
public class Menu {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menuItem")
    protected List<MenuEntry> menuItemList;
}
