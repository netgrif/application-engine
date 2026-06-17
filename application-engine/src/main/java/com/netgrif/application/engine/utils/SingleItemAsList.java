package com.netgrif.application.engine.utils;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SingleItemAsList<T> {

    private List<T> list;

    public SingleItemAsList() {
        this.list = new ArrayList<>();
    }
}
