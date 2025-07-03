package com.netgrif.application.engine.export.domain;

import java.util.Locale;
import java.util.Map;

public class ExportedFieldFactory {

    public static final String STRING_ID = "stringId";
    public static final String AUTHOR = "author";
    public static final String CREATION_DATE = "creationDate";
    public static final String TITLE = "title";
    public static final String VISUAL_ID = "visualId";

    public static final Map<Locale, Map<String, ExportedField>> META_FIELDS = Map.of(
            Locale.of("sk"), Map.of(
                    STRING_ID, new ExportedField("meta-stringId", "ID Prípadu", true),
                    AUTHOR, new ExportedField("meta-author", "Autor", true),
                    CREATION_DATE, new ExportedField("meta-creationDate", "Dátum vytvorenia", true),
                    TITLE, new ExportedField("meta-title", "Názov", true),
                    VISUAL_ID, new ExportedField("meta-visualId", "Vizuálne ID", true)
            ),
            Locale.of("en"), Map.of(
                    STRING_ID, new ExportedField("meta-stringId", "Case ID", true),
                    AUTHOR, new ExportedField("meta-author", "Author", true),
                    CREATION_DATE, new ExportedField("meta-creationDate", "Creation Date", true),
                    TITLE, new ExportedField("meta-title", "Title", true),
                    VISUAL_ID, new ExportedField("meta-visualId", "Visual ID", true)
            )
    );
}
