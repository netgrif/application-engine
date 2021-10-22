package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.netgrif.workflow.filters.FilterImportExportList;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@Data
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "menuList",
        "filterList"
})

@JacksonXmlRootElement(localName = "menusWithFilters")
public class MenuAndFilters {
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menus")
    protected MenuList menuList;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "filters")
    protected FilterImportExportList filterList;

    public MenuAndFilters () {
        this.menuList = new MenuList();
        this.filterList = new FilterImportExportList();
    }
}
