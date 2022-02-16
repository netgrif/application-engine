package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.filter.FilterImportExportList;
import com.netgrif.application.engine.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.application.engine.workflow.domain.IllegalFilterFileException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;


/**
 * Interface which provides methods for filter import and export.
 */

public interface IFilterImportExportService {

    FileFieldValue exportFiltersToFile(Collection<String> filtersToExport) throws IOException;

    FilterImportExportList exportFilters(Collection<String> filtersToExport);

    List<String> importFilters() throws IOException, IllegalFilterFileException, TransitionNotExecutableException;

    Map<String, String> importFilters(FilterImportExportList filters) throws IOException, TransitionNotExecutableException;

    void createFilterImport(IUser author);

    void createFilterExport(IUser author);

    void changeFilterField(Collection<String> filterFields);
}
