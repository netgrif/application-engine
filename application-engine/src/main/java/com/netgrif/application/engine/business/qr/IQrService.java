package com.netgrif.application.engine.business.qr;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

public interface IQrService {

    Optional<InputStream> generateToStream(QrCode content);

    Optional<File> generateToFile(QrCode content);

    Optional<File> generateWithLogo(QrCode code, InputStream imageStream);
}