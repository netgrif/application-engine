package com.netgrif.application.engine.elastic.service;

import java.util.ArrayList;
import java.util.List;

final class FullTextSpecialCharacterTestValues {

    private static final String ASCII_PUNCTUATION = "!\"#$%&'()*+,-./:;<=>?@[\\]^_`{|}~";

    static final List<String> TOKENS = createTokens();

    private FullTextSpecialCharacterTestValues() {
    }

    static String markedValue(int index, String token) {
        return "Special" + index + token + "Marker";
    }

    private static List<String> createTokens() {
        List<String> tokens = new ArrayList<>(ASCII_PUNCTUATION.codePoints()
                .mapToObj(codePoint -> new String(Character.toChars(codePoint)))
                .toList());
        tokens.addAll(List.of(
                "&&", "||", "AND", "OR", "NOT",
                " ", "\t", "\n",
                "–", "—", "„", "“", "”", "€", "§", "°", "✓", "😀"
        ));
        return tokens.stream().distinct().toList();
    }
}
