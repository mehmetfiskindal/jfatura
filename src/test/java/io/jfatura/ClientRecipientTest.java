package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.support.Fixtures;
import io.jfatura.support.GibHttpMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — recipient")
class ClientRecipientTest {

    private FaturaClient client;
    private GibHttpMock gib;

    private static final String TAX_ID = "9876543210";
    private static final String TRID = "12345678901";

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        client = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
    }

    @Nested
    @DisplayName("getRecipientDataByTaxIDOrTRID")
    class GetRecipientData {

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":{\"unvan\":\"Alıcı Şirketi\"}}");
            client.getRecipientDataByTaxIDOrTRID(Fixtures.TOKEN, TAX_ID);
            assertThat(gib.call(0).cmd()).isEqualTo("SICIL_VEYA_MERNISTEN_BILGILERI_GETIR");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":{}}");
            client.getRecipientDataByTaxIDOrTRID(Fixtures.TOKEN, TAX_ID);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_BASITFATURA");
        }

        @Test
        @DisplayName("sends the tax ID as vknTcknn (double-n)")
        void doubleNParam() {
            gib.once("{\"data\":{}}");
            client.getRecipientDataByTaxIDOrTRID(Fixtures.TOKEN, TAX_ID);
            assertThat(gib.call(0).jp().get("vknTcknn")).isEqualTo(TAX_ID);
        }

        @Test
        @DisplayName("accepts a Turkish Republic ID number (TRID)")
        void acceptsTrid() {
            gib.once("{\"data\":{}}");
            client.getRecipientDataByTaxIDOrTRID(Fixtures.TOKEN, TRID);
            assertThat(gib.call(0).jp().get("vknTcknn")).isEqualTo(TRID);
        }

        @Test
        @DisplayName("returns result.data")
        void returnsData() {
            gib.once("{\"data\":{\"unvan\":\"Alıcı Şirketi\",\"vknTckn\":\"" + TAX_ID + "\"}}");
            var result = client.getRecipientDataByTaxIDOrTRID(Fixtures.TOKEN, TAX_ID);
            assertThat(result.get("unvan").asText()).isEqualTo("Alıcı Şirketi");
            assertThat(result.get("vknTckn").asText()).isEqualTo(TAX_ID);
        }

        @Test
        @DisplayName("includes the token in the body")
        void includesToken() {
            gib.once("{\"data\":{}}");
            client.getRecipientDataByTaxIDOrTRID(Fixtures.TOKEN, TAX_ID);
            assertThat(gib.call(0).token()).isEqualTo(Fixtures.TOKEN);
        }

        @Test
        @DisplayName("[BUG FIX] is defined exactly once (not duplicate)")
        void methodDefinedOnce() {
            long count = java.util.Arrays.stream(FaturaClient.class.getMethods())
                    .filter(m -> m.getName().equals("getRecipientDataByTaxIDOrTRID"))
                    .count();
            assertThat(count).isEqualTo(1);
        }
    }
}
