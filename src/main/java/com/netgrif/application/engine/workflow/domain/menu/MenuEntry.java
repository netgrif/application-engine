package com.netgrif.application.engine.workflow.domain.menu;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;
import java.util.Objects;

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
    @EqualsAndHashCode.Exclude
    @XmlElement(required = true)
    protected String filterCaseId;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "entryRole")
    protected List<MenuEntryRole> menuEntryRoleList;
    @JacksonXmlProperty(isAttribute = true)
    protected Boolean useIcon;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuEntry menuEntry = (MenuEntry) o;

        if (!Objects.equals(entryName, menuEntry.entryName)) return false;
        if (!Objects.equals(menuEntryRoleList, menuEntry.menuEntryRoleList))
            return false;
        return Objects.equals(useIcon, menuEntry.useIcon);
    }

    @Override
    public int hashCode() {
        int result = entryName != null ? entryName.hashCode() : 0;
        result = 31 * result + (menuEntryRoleList != null ? menuEntryRoleList.hashCode() : 0);
        result = 31 * result + (useIcon != null ? useIcon.hashCode() : 0);
        return result;
    }
}