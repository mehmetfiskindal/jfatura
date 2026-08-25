package io.jfatura.exception;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * GİB assos-login işlemlerinde (getToken/logout) hata durumunda fırlatılır.
 * Oturum kilidi ("birden fazla giriş" / "Güvenli Çıkış") bu tiptedir.
 */
public class GibAuthException extends GibApiException {

    public GibAuthException(String message) {
        this(message, null);
    }

    public GibAuthException(String message, @Nullable String errorCode) {
        super(message, errorCode);
    }

    public GibAuthException(String message, @Nullable String errorCode, @Nullable List<String> messages) {
        super(message, errorCode, messages);
    }
}
