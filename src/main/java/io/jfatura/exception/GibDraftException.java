package io.jfatura.exception;

import org.jspecify.annotations.Nullable;

/**
 * {@code EARSIV_PORTAL_FATURA_OLUSTUR} komutunun taslağı oluşturmadığı
 * durumlarda fırlatılır; sebep GİB'in {@code data} içindeki serbest metnidir.
 */
public class GibDraftException extends GibApiException {

    public GibDraftException(String message) {
        this(message, null);
    }

    public GibDraftException(String message, @Nullable String errorCode) {
        super(message, errorCode);
    }
}
