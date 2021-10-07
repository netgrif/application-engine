package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.workflow.domain.IllegalFilterFileException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;


/**
 * Interface which provides methods for filter import and export.
 */

public interface IFilterImportExportService {

    FileFieldValue exportFilters(Collection<String> filtersToExport) throws IOException;

    List<String> importFilters() throws IOException, IllegalFilterFileException;

    void createFilterImport(User author);

    void createFilterExport(User author);

    void changeFilterField(List<String> filterFields);
}
