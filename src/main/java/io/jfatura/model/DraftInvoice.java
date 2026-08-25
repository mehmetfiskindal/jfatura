package io.jfatura.model;

import com.fasterxml.jackson.databind.JsonNode;
import org.jspecify.annotations.Nullable;

/**
 * {@code createDraftInvoice} sonucu: taslak tarihi, GİB'in atadığı ETTN
 * (uuid), diff ile bulunan liste satırı ve ham API yanıtı.
 */
public record DraftInvoice(
        String date,
        String uuid,
        @Nullable String documentNumber,
        @Nullable InvoiceListItem listItem,
        io.jfatura.api.ApiResponse response) {

    public JsonNode data() {
        return response.data();
    }
}
