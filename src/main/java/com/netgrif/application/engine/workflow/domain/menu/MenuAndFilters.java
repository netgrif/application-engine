package com.netgrif.application.engine.workflow.domain.menu;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.netgrif.application.engine.workflow.domain.filter.FilterImportExportList;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

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

    public MenuAndFilters() {
        this.menuList = new MenuList();
        this.filterList = new FilterImportExportList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuAndFilters that = (MenuAndFilters) o;

        if (!Objects.equals(menuList, that.menuList)) return false;
        return Objects.equals(filterList, that.filterList);
    }

    @Override
    public int hashCode() {
        int result = menuList != null ? menuList.hashCode() : 0;
        result = 31 * result + (filterList != null ? filterList.hashCode() : 0);
        return result;
    }
}
