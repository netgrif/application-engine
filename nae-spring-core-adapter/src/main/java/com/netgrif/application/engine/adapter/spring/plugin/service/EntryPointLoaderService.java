package com.netgrif.application.engine.adapter.spring.plugin.service;

import com.netgrif.application.engine.objects.plugin.domain.EntryPoint;

import java.util.List;

public interface EntryPointLoaderService {
    List<EntryPoint> getAll();
}
