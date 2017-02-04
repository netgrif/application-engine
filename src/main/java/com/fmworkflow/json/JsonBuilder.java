package com.fmworkflow.json;

import org.codehaus.jettison.json.JSONObject;

public class JsonBuilder {
    JSONObject json;

    private JsonBuilder() {
        json = new JSONObject();
    }

    public static JsonBuilder init() {
        return new JsonBuilder();
    }

    private void addString(Key key, String string) {
        try {
            json.put(String.valueOf(key), string);
        } catch (Exception ignore) {}
    }

    public JsonBuilder addSuccessMessage(String message) {
        addString(Key.success, message);
        return this;
    }

    public JsonBuilder addErrorMessage(String message) {
        addString(Key.error, message);
        return this;
    }

    public String build() {
        return json.toString();
    }

    public static String errorMessage(String message) {
        return init()
                .addErrorMessage(message)
                .build();
    }

    public static String successMessage(String message) {
        return init()
                .addSuccessMessage(message)
                .build();
    }

    enum Key {
        success,
        error
    }
}
