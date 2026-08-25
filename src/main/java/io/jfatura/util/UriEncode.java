package io.jfatura.util;

import java.nio.charset.StandardCharsets;

/**
 * JavaScript {@code encodeURIComponent} ile birebir aynı çıktıyı üretir.
 * {@link java.net.URLEncoder} boşluğu {@code +} yaparken bu sınıf {@code %20} üretir.
 */
public final class UriEncode {

    private static final String UNRESERVED = "-_.!~*'()";

    private UriEncode() {}

    public static String encodeURIComponent(String value) {
        StringBuilder out = new StringBuilder();
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            char c = (char) (b & 0xFF);
            if ((c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || UNRESERVED.indexOf(c) >= 0) {
                out.append(c);
            } else {
                out.append('%').append(String.format("%02X", b & 0xFF));
            }
        }
        return out.toString();
    }
}
