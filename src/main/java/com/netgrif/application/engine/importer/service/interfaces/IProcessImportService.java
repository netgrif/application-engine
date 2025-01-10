package com.netgrif.application.engine.importer.service.interfaces;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.workflow.domain.throwable.MissingProcessMetaDataException;
import com.netgrif.application.engine.workflow.domain.VersionType;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.petrinetoutcomes.ImportProcessEventOutcome;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface IProcessImportService {
    ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser user) throws IOException, MissingProcessMetaDataException, MissingIconKeyException;

    ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser user, String uriNodeId) throws IOException, MissingProcessMetaDataException, MissingIconKeyException;

    ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser user, Map<String, String> params) throws IOException, MissingProcessMetaDataException, MissingIconKeyException;

    ImportProcessEventOutcome importProcess(InputStream xmlFile, VersionType releaseType, LoggedUser user, String uriNodeId, Map<String, String> params) throws IOException, MissingProcessMetaDataException, MissingIconKeyException;


}
