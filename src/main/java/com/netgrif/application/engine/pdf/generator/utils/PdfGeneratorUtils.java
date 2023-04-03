package com.netgrif.application.engine.pdf.generator.utils;

import com.netgrif.application.engine.pdf.generator.config.PdfResource;
import com.netgrif.application.engine.petrinet.domain.DataRef;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class PdfGeneratorUtils {

    public static String getCombinedTypeComponent(DataRef dataRef) {
        return dataRef.getComponent() != null ? dataRef.getField().getType().value() + "_" + dataRef.getComponent() .getName() : dataRef.getField().getType().value();
    }

    public static int getMaxLineSize(int fieldWidth, int fontSize, int padding, float sizeMultiplier) {
        return (int) ((fieldWidth - padding) * sizeMultiplier / fontSize);
    }

    public static void breakLongWordToMultipleLine(StringBuilder output, String longWord, int lineLength, int maxLineLength) {
        if (maxLineLength - lineLength <= 0) {
            lineLength = 0;
        }
        while (longWord.length() > maxLineLength - lineLength) {
            output.append(longWord, 0, maxLineLength - lineLength - 4);
            output.append("\n");
            longWord = longWord.substring(maxLineLength - lineLength - 3);
            lineLength = 0;
        }
    }

    public static List<String> generateMultiLineText(List<String> values, float maxLineLength) {
        StringTokenizer tokenizer;
        StringBuilder output;
        List<String> result = new ArrayList<>();
        int lineLen = 0;

        for (String value : values) {
            tokenizer = new StringTokenizer(value.trim(), " ");
            output = new StringBuilder(value.length());
            while (tokenizer.hasMoreTokens()) {
                String word = tokenizer.nextToken();

                if (word.length() > maxLineLength - lineLen && word.length() > maxLineLength) {
                    breakLongWordToMultipleLine(output, word, lineLen, (int) maxLineLength);
                    lineLen = 0;
                } else if (lineLen + word.length() > maxLineLength) {
                    output.append("\n");
                    lineLen = 0;
                    output.append(word).append(" ");
                    lineLen += word.length() + 1;
                } else {
                    output.append(word).append(" ");
                    lineLen += word.length() + 1;
                }
            }
            lineLen = 0;
            result.addAll(Arrays.asList(output.toString().split("\n")));
        }
        return result;
    }

    public static String removeUnsupportedChars(String input, PdfResource resource) {
        String value = Jsoup.parse(input.replaceAll("\\s{1,}", " ")).text();
        value = Normalizer.normalize(value, Normalizer.Form.NFC);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            if (isCharEncodable(value.charAt(i), resource.getValueFont())) {
                b.append(value.charAt(i));
            } else if (isCharEncodable(value.charAt(i), resource.getLabelFont())) {
                b.append(value.charAt(i));
            } else if (isCharEncodable(value.charAt(i), resource.getTitleFont())) {
                b.append(value.charAt(i));
            }
        }
        return b.toString();
    }

    public static boolean isCharEncodable(char character, PDType0Font font) {
        try {
            font.encode(Character.toString(character));
            return true;
        } catch (IllegalArgumentException | IOException iae) {
            return false;
        }
    }

    public static int getTextWidth(List<String> values, PDType0Font font, int fontSize, PdfResource resource) throws IOException {
        int result = 0;
        for (String value : values) {
            String formattedValue = removeUnsupportedChars(value, resource);
            if (result < font.getStringWidth(formattedValue) / 1000 * fontSize)
                result = (int) (font.getStringWidth(formattedValue) / 1000 * fontSize);
        }
        return result;
    }
}
