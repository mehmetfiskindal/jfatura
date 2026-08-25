package io.jfatura.model;

/** {@code createInvoice} kompozit metodunun sonucu. */
public record CreateInvoiceResult(String token, String uuid, boolean signed) {
}
