package com.netgrif.application.engine.business.qr;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class QrService implements IQrService {


    @Override
    public Optional<InputStream> generateToStream(QrCode code) {
        log.debug("Generating QR code to stream [file={}, size={}x{}, errorCorrection={}]", code.getFileName(), code.getWidth(), code.getHeight(), code.getErrorCorrectionLevel());
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            BitMatrix bitMatrix = createBitMatrix(code);
            MatrixToImageConfig config = new MatrixToImageConfig(code.getOnColor(), code.getOffColor());
            String format = resolveImageFormat(code);

            MatrixToImageWriter.writeToStream(bitMatrix, format, os, config);

            log.trace("QR code stream generated successfully [file={}, bytes={}]", code.getFileName(), os.size());
            return Optional.of(new ByteArrayInputStream(os.toByteArray()));
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code to stream [file={}]", code.getFileName(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateToFile(QrCode code) {
        log.debug("Generating QR code to file [file={}, size={}x{}, errorCorrection={}]", code.getFileName(), code.getWidth(), code.getHeight(), code.getErrorCorrectionLevel());
        try {
            File result = generateFile(code);
            log.trace("QR code file generated successfully [file={}, sizeBytes={}]", result.getAbsolutePath(), result.length());
            return Optional.of(result);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code to file [file={}]", code.getFileName(), e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<File> generateWithLogo(QrCode code, InputStream imageStream) {
        log.debug("Generating QR code with logo [file={}, size={}x{}, errorCorrection={}, logoRatio={}]", code.getFileName(), code.getWidth(), code.getHeight(), code.getErrorCorrectionLevel(), code.getLogoRatio());

        validateLogoErrorCorrection(code);

        BufferedImage overlay = readLogoImage(imageStream, code.getFileName());
        if (overlay == null) {
            return Optional.empty();
        }

        try {
            BitMatrix bitMatrix = createBitMatrix(code);
            MatrixToImageConfig config = new MatrixToImageConfig(code.getOnColor(), code.getOffColor());
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, config);

            log.trace("QR BitMatrix created [file={}, qrSize={}x{}]", code.getFileName(), qrImage.getWidth(), qrImage.getHeight());

            BufferedImage scaledLogo = scaleLogo(overlay, qrImage.getWidth(), qrImage.getHeight(), code.getLogoRatio());
            BufferedImage combined = compositeLogoOnQr(qrImage, scaledLogo, code);
            writeImageToFile(combined, code);

            File result = new File(code.getFileName());
            log.trace("QR code with logo written to disk [file={}, sizeBytes={}]", result.getAbsolutePath(), result.length());
            return Optional.of(result);
        } catch (WriterException | IOException e) {
            log.error("Failed to generate QR code with logo [file={}]", code.getFileName(), e);
            return Optional.empty();
        }
    }

    private File generateFile(QrCode code) throws WriterException, IOException {
        BitMatrix bitMatrix = createBitMatrix(code);
        MatrixToImageConfig config = new MatrixToImageConfig(code.getOnColor(), code.getOffColor());
        Path outputPath = Path.of(code.getFileName());

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            MatrixToImageWriter.writeToStream(bitMatrix, resolveImageFormat(code), outputStream, config);
        } catch (IOException e) {
            log.error("Failed to write QR code to file [file={}, path={}]", code.getFileName(), outputPath.toAbsolutePath(), e);
            throw e;
        }

        return outputPath.toFile();
    }

    private BitMatrix createBitMatrix(QrCode code) throws WriterException {
        Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
        hints.put(EncodeHintType.CHARACTER_SET, code.getCharset());
        hints.put(EncodeHintType.ERROR_CORRECTION, code.getErrorCorrectionLevel());
        hints.put(EncodeHintType.MARGIN, 1);

        log.trace("Encoding BitMatrix [file={}, charset={}, errorCorrection={}, margin=1]", code.getFileName(), code.getCharset(), code.getErrorCorrectionLevel());

        return new QRCodeWriter().encode(
                code.getContent(),
                BarcodeFormat.QR_CODE,
                code.getWidth(),
                code.getHeight(),
                hints
        );
    }

    private BufferedImage readLogoImage(InputStream imageStream, String fileName) {
        try {
            BufferedImage image = ImageIO.read(imageStream);
            if (image == null) {
                log.error("Logo image stream produced a null image — unsupported format or empty stream [file={}]", fileName);
                return null;
            }
            log.trace("Logo image read successfully [file={}, logoSize={}x{}]",
                    fileName, image.getWidth(), image.getHeight());
            return image;
        } catch (IOException e) {
            log.error("Failed to read logo image stream [file={}]", fileName, e);
            return null;
        }
    }

    private BufferedImage scaleLogo(BufferedImage logo, int qrWidth, int qrHeight, double logoRatio) {
        int maxSize = (int) (Math.min(qrWidth, qrHeight) * logoRatio);
        int logoWidth = logo.getWidth();
        int logoHeight = logo.getHeight();

        log.trace("Logo scale check [original={}x{}, maxAllowed={}px, ratio={}]", logoWidth, logoHeight, maxSize, logoRatio);

        if (logoWidth <= maxSize && logoHeight <= maxSize) {
            log.debug("Logo fits within ratio, no scaling needed [logo={}x{}, max={}px]", logoWidth, logoHeight, maxSize);
            return logo;
        }

        double scale = Math.min((double) maxSize / logoWidth, (double) maxSize / logoHeight);
        int scaledWidth = Math.max(1, (int) (logoWidth * scale));
        int scaledHeight = Math.max(1, (int) (logoHeight * scale));

        log.debug("Scaling logo [from={}x{} to={}x{}, maxAllowed={}px, scale={}]",
                logoWidth, logoHeight, scaledWidth, scaledHeight, maxSize, String.format("%.4f", scale));

        BufferedImage scaled = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scaled.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(logo, 0, 0, scaledWidth, scaledHeight, null);
        } finally {
            g.dispose();
        }

        return scaled;
    }

    private BufferedImage compositeLogoOnQr(BufferedImage qrImage, BufferedImage logo, QrCode code) {
        int x = (qrImage.getWidth() - logo.getWidth()) / 2;
        int y = (qrImage.getHeight() - logo.getHeight()) / 2;
        int padding = code.getLogoBackgroundPadding();
        int arc = code.getLogoBackgroundArc();

        log.trace("Compositing logo onto QR [qr={}x{}, logo={}x{}, offset=({},{}), padding={}, arc={}]", qrImage.getWidth(), qrImage.getHeight(), logo.getWidth(), logo.getHeight(), x, y, padding, arc);

        BufferedImage combined = new BufferedImage(
                qrImage.getWidth(),
                qrImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g = combined.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(qrImage, 0, 0, null);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g.setColor(Color.WHITE);
            g.fillRoundRect(
                    x - padding,
                    y - padding,
                    logo.getWidth() + padding * 2,
                    logo.getHeight() + padding * 2,
                    arc,
                    arc
            );

            g.drawImage(logo, x, y, null);
        } finally {
            g.dispose();
        }

        return combined;
    }

    private void writeImageToFile(BufferedImage image, QrCode code) throws IOException {
        String format = resolveImageFormat(code);
        Path outputPath = Path.of(code.getFileName());

        log.trace("Writing combined image to disk [file={}, format={}]", code.getFileName(), format);

        try (OutputStream outputStream = Files.newOutputStream(outputPath)) {
            boolean written = ImageIO.write(image, format, outputStream);
            if (!written) {
                log.error("Failed to write image to disk [file={}, format={}, path={}]", code.getFileName(), format, outputPath.toAbsolutePath());
                throw new IOException("No ImageIO writer found for format: " + format);
            }
        }
    }


    private void validateLogoErrorCorrection(QrCode code) {
        if (code.getErrorCorrectionLevel() != ErrorCorrectionLevel.H) {
            log.warn("QR code '{}' uses ErrorCorrectionLevel.{} — ErrorCorrectionLevel.H is strongly recommended when embedding a logo to ensure reliable scanning.", code.getFileName(), code.getErrorCorrectionLevel());
        }
    }

    private String resolveImageFormat(QrCode code) {
        if (code.getImageType() == null) {
            log.trace("No image type set, defaulting to PNG [file={}]", code.getFileName());
            return QrImageType.PNG.getFormat();
        }
        return code.getImageType().getFormat();
    }
}