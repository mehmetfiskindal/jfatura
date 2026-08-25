package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.model.InvoiceListItem;
import io.jfatura.support.Fixtures;
import io.jfatura.support.GibHttpMock;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — SMS verification")
class ClientSmsTest {

    private FaturaClient client;
    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        client = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
    }

    // ─── getSignPhoneNumber ────────────────────────────────────────────────────

    @Nested
    @DisplayName("getSignPhoneNumber")
    class GetSignPhoneNumber {

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":{\"telefon\":\"5301234567\"}}");
            client.getSignPhoneNumber(Fixtures.TOKEN);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_TELEFONNO_SORGULA");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":{\"telefon\":\"5301234567\"}}");
            client.getSignPhoneNumber(Fixtures.TOKEN);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_SMSONAY");
        }

        @Test
        @DisplayName("sends an empty payload")
        void emptyPayload() {
            gib.once("{\"data\":{\"telefon\":\"5301234567\"}}");
            client.getSignPhoneNumber(Fixtures.TOKEN);
            assertThat(gib.call(0).jp()).isEmpty();
        }

        @Test
        @DisplayName("returns the registered phone number")
        void returnsPhone() {
            gib.once("{\"data\":{\"telefon\":\"5301234567\"}}");
            assertThat(client.getSignPhoneNumber(Fixtures.TOKEN)).isEqualTo("5301234567");
        }

        @Test
        @DisplayName("returns null when no phone is registered")
        void returnsNullWithoutPhone() {
            gib.once("{\"data\":{}}");
            assertThat(client.getSignPhoneNumber(Fixtures.TOKEN)).isNull();
        }
    }

    // ─── sendSignSMSCode ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("sendSignSMSCode")
    class SendSignSmsCode {

        private final String PHONE = "05321234567";

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"oid\":\"op-id-123\"}");
            client.sendSignSMSCode(Fixtures.TOKEN, PHONE);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_SMSSIFRE_GONDER");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"oid\":\"op-id-123\"}");
            client.sendSignSMSCode(Fixtures.TOKEN, PHONE);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_SMSONAY");
        }

        @Test
        @DisplayName("sends the phone number as CEPTEL")
        void cepTel() {
            gib.once("{\"oid\":\"op-id-123\"}");
            client.sendSignSMSCode(Fixtures.TOKEN, PHONE);
            assertThat(gib.call(0).jp().get("CEPTEL")).isEqualTo(PHONE);
        }

        @Test
        @DisplayName("sends KCEPTEL as false")
        void kccepTelFalse() {
            gib.once("{\"oid\":\"op-id-123\"}");
            client.sendSignSMSCode(Fixtures.TOKEN, PHONE);
            assertThat(gib.call(0).jp().get("KCEPTEL")).isEqualTo(Boolean.FALSE);
        }

        @Test
        @DisplayName("sends TIP as empty string")
        void tipEmpty() {
            gib.once("{\"oid\":\"op-id-123\"}");
            client.sendSignSMSCode(Fixtures.TOKEN, PHONE);
            assertThat(gib.call(0).jp().get("TIP")).isEqualTo("");
        }

        @Test
        @DisplayName("returns the operation ID (oid) from the response")
        void returnsOid() {
            gib.once("{\"oid\":\"operation-id-xyz\"}");
            assertThat(client.sendSignSMSCode(Fixtures.TOKEN, PHONE)).isEqualTo("operation-id-xyz");
        }

        @Test
        @DisplayName("returns null when oid is not in the response")
        void returnsNullWithoutOid() {
            gib.once("{}");
            assertThat(client.sendSignSMSCode(Fixtures.TOKEN, PHONE)).isNull();
        }

        @Test
        @DisplayName("includes the token in the body")
        void includesToken() {
            gib.once("{\"oid\":\"x\"}");
            client.sendSignSMSCode(Fixtures.TOKEN, PHONE);
            assertThat(gib.call(0).token()).isEqualTo(Fixtures.TOKEN);
        }

        @Test
        @DisplayName("[BUG FIX] reads oid from response.data.oid")
        void readsOidFromData() {
            gib.once("{\"data\":{\"oid\":\"data-icindeki-oid\"}}");
            assertThat(client.sendSignSMSCode(Fixtures.TOKEN, PHONE)).isEqualTo("data-icindeki-oid");
        }
    }

    // ─── verifySignSMSCode ─────────────────────────────────────────────────────

    @Nested
    @DisplayName("verifySignSMSCode")
    class VerifySignSmsCode {

        private final String SMS_CODE = "123456";
        private final String OPERATION_ID = "operation-id-xyz";

        @Test
        @DisplayName("[BUG FIX] uses the opaque signing command id, not EARSIV_PORTAL_SMSSIFRE_DOGRULA")
        void opaqueCommandId() {
            // Canlı portal EARSIV_PORTAL_SMSSIFRE_DOGRULA için "Service Not Found" döner.
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat(gib.call(0).cmd()).isEqualTo("0lhozfib5410mp");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_SMSONAY");
        }

        @Test
        @DisplayName("sends the SMS code as SIFRE")
        void sifreField() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat(gib.call(0).jp().get("SIFRE")).isEqualTo(SMS_CODE);
        }

        @Test
        @DisplayName("sends the operation ID as OID")
        void oidField() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat(gib.call(0).jp().get("OID")).isEqualTo(OPERATION_ID);
        }

        @Test
        @DisplayName("[BUG FIX] returns true when GİB reports sonuc=1")
        void trueOnSonucOne() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            assertThat(client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID)).isTrue();
        }

        @Test
        @DisplayName("[BUG FIX] returns false when GİB does not report sonuc=1")
        void falseOnOtherSonuc() {
            gib.once("{\"data\":{\"sonuc\":\"0\"}}");
            assertThat(client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID)).isFalse();
        }

        @Test
        @DisplayName("returns false when the response has no sonuc field")
        void falseWithoutSonuc() {
            gib.once("{}");
            assertThat(client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID)).isFalse();
        }

        @Test
        @DisplayName("[BUG FIX] sends OPR=1 — without it GİB verifies the code but signs nothing")
        void oprOne() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat(gib.call(0).jp().get("OPR")).isEqualTo(1);
        }

        @Test
        @DisplayName("[BUG FIX] sends the invoices to be signed as DATA")
        void dataInvoices() {
            List<InvoiceListItem> invoices = List.of(
                    InvoiceListItem.of("ettn-1"), InvoiceListItem.of("ettn-2"));
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID, invoices);
            assertThat(gib.call(0).jp().get("DATA"))
                    .isEqualTo(List.of(Map.of("ettn", "ettn-1"), Map.of("ettn", "ettn-2")));
        }

        @Test
        @DisplayName("sends DATA as an empty array when no invoice is given")
        void emptyData() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat((List<?>) gib.call(0).jp().get("DATA")).isEmpty();
        }

        @Test
        @DisplayName("uses a different command than sendSignSMSCode")
        void differentCommand() {
            gib.once("{\"data\":{\"sonuc\":\"1\"}}");
            client.verifySignSMSCode(Fixtures.TOKEN, SMS_CODE, OPERATION_ID);
            assertThat(gib.call(0).cmd()).isNotEqualTo("EARSIV_PORTAL_SMSSIFRE_GONDER");
        }
    }
}
