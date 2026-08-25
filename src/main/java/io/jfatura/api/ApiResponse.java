package io.jfatura.api;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * GİB dispatch / assos-login yanıtlarının ham karşılığı.
 *
 * <p>Bilinmeyen üst seviye alanlar {@link #extra()} içinde saklanır.
 */
@JsonIgnoreProperties(ignoreUnknown = false)
public final class ApiResponse {

    private JsonNode data;
    private @Nullable String oid;
    private @Nullable String token;
    /** GİB API hata kodu — "0" veya yoksa başarılı, "1" hata */
    private @Nullable String error;

    private List<GibApiMessage> messages = List.of();
    private final Map<String, Object> extra = new LinkedHashMap<>();

    public JsonNode data() {
        return data;
    }

    public void setData(JsonNode data) {
        this.data = data;
    }

    public @Nullable String oid() {
        return oid;
    }

    public void setOid(@Nullable String oid) {
        this.oid = oid;
    }

    public @Nullable String token() {
        return token;
    }

    public void setToken(@Nullable String token) {
        this.token = token;
    }

    public @Nullable String error() {
        return error;
    }

    @JsonProperty("error")
    public void setError(@Nullable String error) {
        this.error = error;
    }

    public List<GibApiMessage> messages() {
        return messages;
    }

    @JsonDeserialize(contentUsing = GibApiMessage.Deserializer.class)
    public void setMessages(List<GibApiMessage> messages) {
        this.messages = messages == null ? List.of() : messages;
    }

    public Map<String, Object> extra() {
        return extra;
    }

    @JsonAnySetter
    void setExtra(String key, Object value) {
        extra.put(key, value);
    }

    /** GİB hata bildirdi mi? ({@code error} alanı var ve "0" değil) */
    public boolean hasError() {
        return error != null && !"0".equals(error);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ApiResponse other)) {
            return false;
        }
        return java.util.Objects.equals(stringOf(data), stringOf(other.data))
                && java.util.Objects.equals(oid, other.oid)
                && java.util.Objects.equals(token, other.token)
                && java.util.Objects.equals(error, other.error)
                && extra.equals(other.extra);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(stringOf(data), oid, token, error);
    }

    private static String stringOf(JsonNode node) {
        return node == null ? null : node.toString();
    }

    @Override
    public String toString() {
        return "ApiResponse{data=" + data + ", oid=" + oid + ", token=" + token + ", error=" + error + ", extra="
                + extra.keySet() + "}";
    }
}
