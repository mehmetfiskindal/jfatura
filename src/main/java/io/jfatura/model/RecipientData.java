package io.jfatura.model;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * VKN/TCKN alıcı sorgusu sonucu. GİB yanıtı serbest şekilli olduğundan
 * bilinen üç alan ayrıca, diğer tüm anahtarlar {@link #all()} içinde sunulur.
 */
public record RecipientData(
        @Nullable String unvan, @Nullable String vknTckn, @Nullable String vergiDairesi, Map<String, Object> all) {

    public static RecipientData fromJson(ObjectMapper mapper, JsonNode node) {
        Map<String, Object> all = mapper.convertValue(
                node == null || node.isNull() ? mapper.createObjectNode() : node,
                new TypeReference<LinkedHashMap<String, Object>>() {});
        return new RecipientData(
                asString(all.get("unvan")), asString(all.get("vknTckn")), asString(all.get("vergiDairesi")), all);
    }

    private static String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
