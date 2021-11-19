package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.workflow.domain.filter.FilterImportExportList;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.workflow.domain.IllegalFilterFileException;

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

    List<String> importFilters() throws IOException, IllegalFilterFileException;

    Map<String, String> importFilters(FilterImportExportList filters) throws IOException;

    void createFilterImport(User author);

    void createFilterExport(User author);

    void changeFilterField(Collection<String> filterFields);
}
