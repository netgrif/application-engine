package com.netgrif.workflow.workflow.domain.menu;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "menus",
})

@XmlRootElement(name = "menus")
public class MenuList {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menu")
    protected List<Menu> menus;

    public MenuList () {
        this.menus = new ArrayList<>();
    }
}
