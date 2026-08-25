package io.jfatura.exception;

/**
 * GİB API bir hata bildirdiğinde ({@code error != "0"}) ya da beklenen
 * yanıt doğrulanamadığında (ör. taslak oluşturma serbest metni) fırlatılır.
 */
public class GibApiException extends RuntimeException {

    public GibApiException(String message) {
        super(message);
    }
}
