package com.netgrif.application.engine.business.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import net.glxn.qrgen.javase.QRCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
public class QrService implements IQrService {

    private static Logger log = LoggerFactory.getLogger(QrService.class);

    @Override
    public Optional<InputStream> generateToStream(QrCode code) {
        try {
            return Optional.of(new FileInputStream(generateFile(code)));
        } catch (FileNotFoundException e) {
            log.error("Error creating qr code.", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateToFile(QrCode code) {
        try {
            return Optional.ofNullable(generateFile(code));
        } catch (Exception e) {
            log.error("Error creating qr code.", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateWithLogo(QrCode code, InputStream imageStream) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            BitMatrix bitMatrix = QRCode.from(code.getContent())
                    .to(code.getImageType())
                    .withSize(code.getWidth(), code.getHeight())
                    .withColor(code.getOnColor(), code.getOffColor())
                    .withErrorCorrection(code.getErrorCorrectionLevel())
                    .withCharset(code.getCharset())
                    .getQrWriter().encode(code.getContent(), BarcodeFormat.QR_CODE, code.getWidth(), code.getHeight());
            MatrixToImageConfig config = new MatrixToImageConfig(code.getOnColor(), code.getOffColor());

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
            BufferedImage overly = ImageIO.read(imageStream);

            int deltaHeight = qrImage.getHeight() - overly.getHeight();
            int deltaWidth = qrImage.getWidth() - overly.getWidth();

            BufferedImage combined = new BufferedImage(qrImage.getHeight(), qrImage.getWidth(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = (Graphics2D) combined.getGraphics();

            g.drawImage(qrImage, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.drawImage(overly, Math.round(deltaWidth / 2), Math.round(deltaHeight / 2), null);

            ImageIO.write(combined, "png", os);
            Files.copy(new ByteArrayInputStream(os.toByteArray()), Paths.get(code.getFileName()), StandardCopyOption.REPLACE_EXISTING);

            return Optional.of(new File(code.getFileName()));
        } catch (WriterException | IOException e) {
            log.error("Error creating qr code.", e);
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