package com.netgrif.application.engine.business.qr;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;
import net.glxn.qrgen.core.image.ImageType;

@Data
public class QrCode {

    private String content;

    private String fileName;

    private ImageType imageType = ImageType.JPG;

    private int width = 250;

    private int height = 250;

    private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.L;

    private int onColor = 0xFF000000;

    private int offColor = 0xFFFFFFFF;

    private String charset = "ISO-8859-1";

    public QrCode(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }
}