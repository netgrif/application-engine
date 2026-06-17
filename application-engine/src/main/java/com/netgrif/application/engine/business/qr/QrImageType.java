package com.netgrif.application.engine.business.qr;

public enum QrImageType {

    PNG("png"),
    JPG("jpg"),
    JPEG("jpeg"),
    GIF("gif"),
    BMP("bmp");

    private final String format;

    QrImageType(String format) {
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}