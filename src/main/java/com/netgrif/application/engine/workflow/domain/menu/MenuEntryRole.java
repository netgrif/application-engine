package com.netgrif.application.engine.workflow.domain.menu;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.application.engine.workflow.domain.AuthorizationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.Objects;

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
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    protected AuthorizationType authorizationType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MenuEntryRole that = (MenuEntryRole) o;

        if (!Objects.equals(roleImportId, that.roleImportId)) return false;
        if (!Objects.equals(netImportId, that.netImportId)) return false;
        return authorizationType == that.authorizationType;
    }

    @Override
    public int hashCode() {
        int result = roleImportId != null ? roleImportId.hashCode() : 0;
        result = 31 * result + (netImportId != null ? netImportId.hashCode() : 0);
        result = 31 * result + (authorizationType != null ? authorizationType.hashCode() : 0);
        return result;
    }
}