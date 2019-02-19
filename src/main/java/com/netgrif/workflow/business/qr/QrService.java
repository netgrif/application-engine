package com.netgrif.workflow.business.qr;

import net.glxn.qrgen.javase.QRCode;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

@Service
public class QrService implements IQrService {

    @Override
    public Optional<InputStream> generateToStream(QrCode code) {
        try {
            return Optional.of(new FileInputStream(generateFile(code)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateToFile(QrCode code) {
        try {
            return Optional.ofNullable(generateFile(code));
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private File generateFile(QrCode code) {
        return QRCode.from(code.getContent())
                .to(code.getImageType())
                .withSize(code.getWidth(), code.getHeight())
                .withColor(code.getOnColor(), code.getOffColor())
                .withErrorCorrection(code.getErrorCorrectionLevel())
                .withCharset(code.getCharset())
                .file(code.getFileName());
    }
}