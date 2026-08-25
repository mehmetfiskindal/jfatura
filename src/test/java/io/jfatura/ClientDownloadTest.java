package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.support.Fixtures;
import io.jfatura.support.GibHttpMock;
import io.jfatura.util.UriEncode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — download & HTML")
class ClientDownloadTest {

    private static final String INVOICE_UUID = "11111111-2222-1333-a444-555555555555";

    private FaturaClient prodClient;
    private FaturaClient testClient;
    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        prodClient = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
        testClient = new FaturaClient(Environment.TEST, RestClient.builder().requestFactory(gib));
    }

    // ─── getInvoiceHTML ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getInvoiceHTML")
    class GetInvoiceHtml {

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":\"<html>...</html>\"}");
            prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_FATURA_GOSTER");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":\"<html>...</html>\"}");
            prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_BASITTASLAKLAR");
        }

        @Test
        @DisplayName("sends the invoice uuid as ettn")
        void ettnField() {
            gib.once("{\"data\":\"<html>...</html>\"}");
            prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(gib.call(0).jp().get("ettn")).isEqualTo(INVOICE_UUID);
        }

        @Test
        @DisplayName("signed: true → onayDurumu is 'Onaylandı'")
        void signedApproval() {
            gib.once("{\"data\":\"<html>...</html>\"}");
            prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(gib.call(0).jp().get("onayDurumu")).isEqualTo("Onaylandı");
        }

        @Test
        @DisplayName("signed: false → onayDurumu is 'Onaylanmadı'")
        void unsignedApproval() {
            gib.once("{\"data\":\"<html>...</html>\"}");
            prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, false);
            assertThat(gib.call(0).jp().get("onayDurumu")).isEqualTo("Onaylanmadı");
        }

        @Test
        @DisplayName("returns result.data (the HTML string)")
        void returnsHtml() {
            String html = "<html><body>Fatura içeriği</body></html>";
            gib.once("{\"data\":\"" + html.replace("\"", "\\\"") + "\"}");
            String result = prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(result).isEqualTo(html);
        }

        @Test
        @DisplayName("includes the token in the body")
        void includesToken() {
            gib.once("{\"data\":\"\"}");
            prodClient.getInvoiceHTML(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(gib.call(0).token()).isEqualTo(Fixtures.TOKEN);
        }
    }

    // ─── getDownloadURL ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("getDownloadURL")
    class GetDownloadUrl {

        @Test
        @DisplayName("is a synchronous function (no await needed)")
        void synchronousStringReturn() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).isInstanceOf(String.class);
        }

        @Test
        @DisplayName("uses the PROD base URL")
        void prodBaseUrl() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains(Environment.PROD.baseUrl());
        }

        @Test
        @DisplayName("uses the TEST base URL for test client")
        void testBaseUrl() {
            String url = testClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains(Environment.TEST.baseUrl());
        }

        @Test
        @DisplayName("contains the /earsiv-services/download path")
        void downloadPath() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains("/earsiv-services/download");
        }

        @Test
        @DisplayName("includes the token as a query parameter")
        void tokenParam() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains("token=" + Fixtures.TOKEN);
        }

        @Test
        @DisplayName("includes the invoice UUID as ettn")
        void ettnParam() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains("ettn=" + INVOICE_UUID);
        }

        @Test
        @DisplayName("includes belgeTip=FATURA")
        void belgeTipParam() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains("belgeTip=FATURA");
        }

        @Test
        @DisplayName("signed: true → onayDurumu contains 'Onaylandı' (percent-encoded)")
        void signedEncoded() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains(UriEncode.encodeURIComponent("Onaylandı"));
        }

        @Test
        @DisplayName("signed: false → onayDurumu contains 'Onaylanmadı' (percent-encoded)")
        void unsignedEncoded() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, false);
            assertThat(url).contains(UriEncode.encodeURIComponent("Onaylanmadı"));
        }

        @Test
        @DisplayName("includes cmd=downloadResource")
        void cmdParam() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            assertThat(url).contains("cmd=downloadResource");
        }

        @Test
        @DisplayName("Turkish characters in onayDurumu are properly percent-encoded")
        void turkishCharsEncoded() {
            String url = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            // Ham Türkçe karakterler URL'de kodlanmamış görünmemeli
            assertThat(url).doesNotContain("Onaylandı");
            assertThat(url).contains("%C4%B1"); // 'ı' encoded
        }

        @Test
        @DisplayName("signed and unsigned URLs are different")
        void signedDiffersFromUnsigned() {
            String signed = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, true);
            String unsigned = prodClient.getDownloadURL(Fixtures.TOKEN, INVOICE_UUID, false);
            assertThat(signed).isNotEqualTo(unsigned);
        }
    }
}
