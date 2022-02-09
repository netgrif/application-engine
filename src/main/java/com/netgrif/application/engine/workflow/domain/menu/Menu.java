package com.netgrif.application.engine.workflow.domain.menu;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "menuEntries",
})

@XmlRootElement(name = "menu")
public class Menu {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menuEntry")
    protected List<MenuEntry> menuEntries;

    @JacksonXmlProperty(isAttribute = true, localName = "name")
    protected String menuIdentifier;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Menu menu = (Menu) o;

        if (!Objects.equals(menuEntries, menu.menuEntries)) return false;
        return Objects.equals(menuIdentifier, menu.menuIdentifier);
    }

    @Override
    public int hashCode() {
        int result = menuEntries != null ? menuEntries.hashCode() : 0;
        result = 31 * result + (menuIdentifier != null ? menuIdentifier.hashCode() : 0);
        return result;
    }
}
