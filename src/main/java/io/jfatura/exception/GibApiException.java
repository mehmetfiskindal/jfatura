package io.jfatura.exception;

import java.util.List;
import org.jspecify.annotations.Nullable;

/**
 * GİB API bir hata bildirdiğinde ({@code error != "0"}) ya da beklenen
 * yanıt doğrulanamadığında (ör. taslak oluşturma serbest metni) fırlatılan
 * taban istisna.
 *
 * <p>Alt tipler: {@link GibAuthException} (assos-login),
 * {@link GibDraftException} (taslak oluşturma reddi).
 */
public class GibApiException extends RuntimeException {

    private final @Nullable String errorCode;
    private final List<String> messages;

    public GibApiException(String message) {
        this(message, null, List.of());
    }

    public GibApiException(String message, @Nullable String errorCode) {
        this(message, errorCode, List.of());
    }

    public GibApiException(String message, @Nullable String errorCode, List<String> messages) {
        super(message);
        this.errorCode = errorCode;
        this.messages = messages == null ? List.of() : List.copyOf(messages);
    }

    /** GİB'in döndürdüğü ham {@code error} kodu ("1" vb.); yoksa {@code null}. */
    public @Nullable String getErrorCode() {
        return errorCode;
    }

    /** GİB'in {@code messages} dizisindeki metinler. */
    public List<String> getMessages() {
        return messages;
    }
}
