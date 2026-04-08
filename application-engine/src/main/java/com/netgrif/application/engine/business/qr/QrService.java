package com.netgrif.application.engine.business.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class QrService implements IQrService {

    @Override
    public Optional<InputStream> generateToStream(QrCode code) {
        try {
            return Optional.of(new FileInputStream(generateFile(code)));
        } catch (WriterException | IOException e) {
            log.error("Error creating qr code.", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateToFile(QrCode code) {
        try {
            return Optional.of(generateFile(code));
        } catch (Exception e) {
            log.error("Error creating qr code.", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateWithLogo(QrCode code, InputStream imageStream) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();

            BitMatrix bitMatrix = createBitMatrix(code);
            MatrixToImageConfig config = new MatrixToImageConfig(code.getOnColor(), code.getOffColor());

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);
            BufferedImage overlay = ImageIO.read(imageStream);

            if (overlay == null) {
                log.error("Error creating qr code. Overlay image could not be read.");
                return Optional.empty();
            }

            int maxLogoSize = (int) (Math.min(qrImage.getWidth(), qrImage.getHeight()) * 0.22);
            int logoWidth = overlay.getWidth();
            int logoHeight = overlay.getHeight();

            if (logoWidth > maxLogoSize || logoHeight > maxLogoSize) {
                double scale = Math.min((double) maxLogoSize / logoWidth, (double) maxLogoSize / logoHeight);
                logoWidth = (int) (logoWidth * scale);
                logoHeight = (int) (logoHeight * scale);
            }

            BufferedImage scaledOverlay = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D gScaled = scaledOverlay.createGraphics();
            try {
                gScaled.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gScaled.drawImage(overlay, 0, 0, logoWidth, logoHeight, null);
            } finally {
                gScaled.dispose();
            }

            int x = (qrImage.getWidth() - logoWidth) / 2;
            int y = (qrImage.getHeight() - logoHeight) / 2;

            BufferedImage combined = new BufferedImage(
                    qrImage.getWidth(),
                    qrImage.getHeight(),
                    BufferedImage.TYPE_INT_ARGB
            );

            Graphics2D g = combined.createGraphics();
            try {
                g.drawImage(qrImage, 0, 0, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
                g.drawImage(scaledOverlay, x, y, null);
            } finally {
                g.dispose();
            }

            ImageIO.write(combined, resolveImageFormat(code), os);
            Files.copy(
                    new ByteArrayInputStream(os.toByteArray()),
                    Path.of(code.getFileName()),
                    StandardCopyOption.REPLACE_EXISTING
            );

            return Optional.of(new File(code.getFileName()));
        } catch (WriterException | IOException e) {
            log.error("Error creating qr code.", e);
            return Optional.empty();
        }
    }

    private File generateFile(QrCode code) throws WriterException, IOException {
        BitMatrix bitMatrix = createBitMatrix(code);
        MatrixToImageConfig config = new MatrixToImageConfig(code.getOnColor(), code.getOffColor());

        Path outputPath = Path.of(code.getFileName());

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            MatrixToImageWriter.writeToStream(
                    bitMatrix,
                    resolveImageFormat(code),
                    outputStream,
                    config
            );
        }

        return outputPath.toFile();
    }

    private BitMatrix createBitMatrix(QrCode code) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, code.getCharset());
        hints.put(EncodeHintType.ERROR_CORRECTION, code.getErrorCorrectionLevel());
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        return writer.encode(
                code.getContent(),
                BarcodeFormat.QR_CODE,
                code.getWidth(),
                code.getHeight(),
                hints
        );
    }

    private String resolveImageFormat(QrCode code) {
        if (code.getImageType() == null) {
            return QrImageType.PNG.getFormat();
        }

        return code.getImageType().getFormat();
    }
}