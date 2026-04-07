package com.netgrif.application.engine.export.service.interfaces;


import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.export.domain.ExportedField;
import com.netgrif.application.engine.export.web.requestbodies.FilteredCasesRequest;

import java.io.File;
import java.util.List;
import java.util.Locale;

public interface IXlsExportService {

    File getExportFilteredCasesFile(FilteredCasesRequest request, LoggedUser user, Locale locale) throws Exception;

    File getExportFilteredCasesFile(List<CaseSearchRequest> requests, Boolean isIntersection, List<ExportedField> selectedField, LoggedUser user, Locale locale) throws Exception;
}
