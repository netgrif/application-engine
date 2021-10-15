package com.netgrif.workflow.workflow.domain;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.netgrif.workflow.importer.model.MenuItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "menuList",
})

@XmlRootElement(name = "menus")
public class MenuList {

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "menu")
    protected List<Menu> menuList; //TODO premenovat na menus

    public MenuList () {
        this.menuList = new ArrayList<>();
//        this.menuList.add(new Menu());//TODO netreba pridavat
    }
}
