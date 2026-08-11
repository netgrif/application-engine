package com.netgrif.application.engine.business.qr;

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.Data;
import net.glxn.qrgen.core.image.ImageType;

@Data
public class QrCode {

    public static final double DEFAULT_LOGO_RATIO = 0.15;
    public static final int DEFAULT_LOGO_BACKGROUND_PADDING = 4;
    public static final int DEFAULT_LOGO_BACKGROUND_ARC = 8;
    public static final QrImageType DEFAULT_IMAGE_TYPE = QrImageType.PNG;

    private String content;

    private String fileName;

    private QrImageType imageType = DEFAULT_IMAGE_TYPE;

    private int width = 250;

    private int height = 250;

    private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.H;

    private int onColor = 0xFF000000;

    private int offColor = 0xFFFFFFFF;

    private String charset = "ISO-8859-1";

    private double logoRatio = DEFAULT_LOGO_RATIO;

    private int logoBackgroundPadding = DEFAULT_LOGO_BACKGROUND_PADDING;

    private int logoBackgroundArc = DEFAULT_LOGO_BACKGROUND_ARC;

    public QrCode(String fileName, String content) {
        this.fileName = fileName;
        this.content = content;
    }
}