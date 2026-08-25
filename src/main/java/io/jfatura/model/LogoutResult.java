package io.jfatura.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import org.jspecify.annotations.Nullable;

/**
 * {@code logout} sonucu. GİB yönlendirme adresini iki farklı biçimde döndürebilir:
 * düz string ({@code data: "login.jsp"}) veya nesne ({@code data: {redirectUrl: "login.html"}}).
 * İki biçim de {@link #redirectUrl()} alanına çözümlenir; ham yanıt {@link #raw()} içindedir.
 */
public record LogoutResult(@Nullable String redirectUrl, JsonNode raw) {

    public static LogoutResult fromJson(JsonNode data) {
        if (data == null || data.isNull() || data.isMissingNode()) {
            return new LogoutResult(null, MissingNode.getInstance());
        }
        if (data.isTextual()) {
            return new LogoutResult(data.textValue(), data);
        }
        if (data.isObject() && data.hasNonNull("redirectUrl")) {
            return new LogoutResult(data.get("redirectUrl").asText(), data);
        }
        return new LogoutResult(null, data);
    }
}
