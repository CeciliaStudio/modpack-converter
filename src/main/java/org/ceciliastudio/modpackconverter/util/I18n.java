package org.ceciliastudio.modpackconverter.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class I18n {
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("I18n", Locale.getDefault());

    public static String localized(String key) {
        return BUNDLE.getString(key);
    }
}
