package io.jfatura.api;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * GİB API mesaj alanı iki formatta gelebilir:
 * <ul>
 *   <li>Nesne: {@code {type: string, text: string}} (çoğu endpoint)</li>
 *   <li>String: {@code "Genel Sistem Hatası: ..."} (bazı dispatch hataları)</li>
 * </ul>
 */
@JsonDeserialize(using = GibApiMessage.Deserializer.class)
public sealed interface GibApiMessage permits GibApiMessage.Plain, GibApiMessage.Structured {

    String text();

    default String type() {
        return "";
    }

    record Plain(String text) implements GibApiMessage {
    }

    record Structured(String type, String text) implements GibApiMessage {

        @Override
        public String type() {
            return type;
        }
    }

    final class Deserializer extends com.fasterxml.jackson.databind.JsonDeserializer<GibApiMessage> {

        @Override
        public GibApiMessage deserialize(com.fasterxml.jackson.core.JsonParser parser,
                com.fasterxml.jackson.databind.DeserializationContext context) throws java.io.IOException {
            com.fasterxml.jackson.databind.JsonNode node = parser.readValueAsTree();
            if (node.isTextual()) {
                return new Plain(node.textValue());
            }
            if (node.isObject()) {
                return new Structured(node.path("type").asText(""), node.path("text").asText(""));
            }
            return new Plain(node.asText(""));
        }
    }
}
