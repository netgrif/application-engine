package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.FileFieldValue;
import com.netgrif.workflow.workflow.domain.IllegalFilterFileException;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface IFilterImportExportService {

    FileFieldValue exportFilters(Set<String> filtersToExport) throws IOException;

    List<String> importFilters() throws IOException, IllegalFilterFileException;

    void createFilterImport(User author);

    void createFilterExport(User author);

    void changeFilterField(List<String> filterFields);
}
