package io.jfatura.util;

import java.util.Set;

/**
 * Log çıktılarında GİB form gövdesindeki hassas değerleri maskeler.
 */
public final class LogMasking {

    private static final Set<String> SENSITIVE_KEYS = Set.of("sifre", "sifre2", "userid", "token", "parola");

    private LogMasking() {}

    /** {@code a=1&sifre=gizli&b=2} → {@code a=1&sifre=***&b=2} */
    public static String maskFormBody(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }
        String[] pairs = body.split("&");
        StringBuilder out = new StringBuilder(body.length());
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            int eq = pair.indexOf('=');
            if (i > 0) {
                out.append('&');
            }
            if (eq > 0 && SENSITIVE_KEYS.contains(pair.substring(0, eq))) {
                out.append(pair, 0, eq + 1).append("***");
            } else {
                out.append(pair);
            }
        }
        return out.toString();
    }

    /** {@code abcd1234efgh} → {@code abcd***efgh}; kısa/değersiz girdide {@code ***}. */
    public static String maskToken(String token) {
        if (token == null || token.length() < 10) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }
}
