package com.netgrif.application.engine.adapter.spring.workflow.service;

import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.IllegalFilterFileException;
import com.netgrif.application.engine.objects.workflow.domain.filter.FilterImportExportList;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface FilterImportExportService {

    FileFieldValue exportFiltersToFile(Collection<String> filtersToExport) throws IOException;

    FilterImportExportList exportFilters(Collection<String> filtersToExport);

    List<String> importFilters() throws IOException, IllegalFilterFileException, TransitionNotExecutableException;

    Map<String, String> importFilters(FilterImportExportList filters) throws IOException, TransitionNotExecutableException;

    void createFilterImport(IUser author);

    void createFilterExport(IUser author);

    void changeFilterField(Collection<String> filterFields);
}
