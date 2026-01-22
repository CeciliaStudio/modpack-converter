package org.ceciliastudio.modpackconverter.util;

public class Logger {
    public static void info(String key) {
        System.out.println(I18n.localized(key));
    }

    public static void error(String key) {
        System.err.println(I18n.localized(key));
    }

    private Logger() {
    }
}
