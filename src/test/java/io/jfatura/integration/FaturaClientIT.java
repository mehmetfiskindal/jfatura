package io.jfatura.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import io.jfatura.Environment;
import io.jfatura.FaturaClient;
import io.jfatura.exception.GibApiException;
import io.jfatura.model.DraftInvoice;
import io.jfatura.model.InvoiceDetails;
import io.jfatura.model.InvoiceItem;
import io.jfatura.model.UserData;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Timeout;

/**
 * Integration tests — gerçek GİB TEST ortamına karşı çalıştırılır.
 *
 * <p>Çalıştırmak için: {@code mvn failsafe:integration-test}
 *
 * <p>Kendi hesabınla:
 * {@code GIB_TEST_USER=<kullaniciKodu> GIB_TEST_PASS=<sifre> mvn verify -DskipITs=false}
 *
 * <p>GİB'in kamuya açık test hesabı (başka biri kullanıyorsa kilitli olabilir):
 * Kullanıcı {@code 33333301}, şifre {@code 1} —
 * https://earsivportaltest.efatura.gov.tr
 *
 * <p>CI ortamında atlamak için: {@code CI=true}
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("FaturaClient — Integration (GİB TEST env)")
class FaturaClientIT {

    private static final String GIB_USER = System.getenv().getOrDefault("GIB_TEST_USER", "33333301");
    private static final String GIB_PASS = System.getenv().getOrDefault("GIB_TEST_PASS", "1");

    /**
     * GitHub Actions otomatik {@code CI=true} set eder; gece koşusu
     * (integration.yml) bu bayrakla atlamayı bypass eder.
     */
    private static boolean skippedInCi() {
        return "true".equalsIgnoreCase(System.getenv("CI"))
                && !"true".equalsIgnoreCase(System.getenv("JFATURA_IT_RUN"));
    }

    private FaturaClient client;
    private String token;
    private boolean tokenAvailable = true;

    @BeforeAll
    void login() {
        assumeFalse(skippedInCi(), "CI ortamında integration testleri atlanır (JFATURA_IT_RUN=true ile koşulur)");
        client = new FaturaClient(Environment.TEST);
        try {
            token = getTokenWithRetry();
        } catch (Exception err) {
            tokenAvailable = false;
            System.out.println("\n⚠ Integration testleri ATLANDI — GİB TEST girişi başarısız: " + err.getMessage());
            System.out.println(
                    "  Kamu hesabı başka biri tarafından kullanılıyorsa birkaç dakika bekleyip tekrar deneyin.\n");
        }
        assumeTrue(tokenAvailable, "GİB TEST oturumu açılamadı — atlandı");
    }

    @AfterAll
    void logout() {
        if (token != null && !token.isEmpty()) {
            try {
                client.logout(token);
            } catch (Exception ignored) {
                // temizlik hatası test sonucunu etkilemesin
            }
        }
    }

    private String getTokenWithRetry() throws InterruptedException {
        Exception lastError = null;
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                return client.getToken(GIB_USER, GIB_PASS);
            } catch (GibApiException err) {
                lastError = err;
                boolean locked = err.getMessage() != null
                        && (err.getMessage().contains("birden fazla giriş")
                                || err.getMessage().contains("Güvenli Çıkış"));
                if (!locked || attempt == 5) {
                    break;
                }
                Thread.sleep(3000);
            }
        }
        if (lastError instanceof RuntimeException runtimeError) {
            throw runtimeError;
        }
        throw new IllegalStateException(lastError);
    }

    private static String todayGib() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private static String nowTime() {
        return LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT));
    }

    private static InvoiceDetails makeInvoice() {
        return InvoiceDetails.builder(todayGib(), nowTime())
                .taxIDOrTRID("11111111111")
                .name("Test")
                .surname("Alıcı")
                .fullAddress("Test Sokak No:1")
                .country("Türkiye")
                .items(List.of(InvoiceItem.builder()
                        .name("Test Hizmeti")
                        .quantity(1)
                        .unitType("C62")
                        .unitPrice(100.0)
                        .price(100)
                        .vatRate(20.0)
                        .vatAmount(20.0)
                        .vatAmountOfTax(0.0)
                        .build()))
                .grandTotal(100)
                .totalVAT(20)
                .grandTotalInclVAT(120)
                .paymentTotal(120)
                .build();
    }

    // ─── Auth ──────────────────────────────────────────────────────────────────

    @Test
    @Timeout(30)
    @DisplayName("getToken returns a non-empty token string")
    void returnsNonEmptyToken() {
        assertThat(token).isNotBlank();
    }

    // ─── User data ─────────────────────────────────────────────────────────────

    @Test
    @Timeout(15)
    @DisplayName("getUserData returns a taxIDOrTRID string")
    void userDataTaxId() {
        UserData user = client.getUserData(token);
        assertThat(user.taxIDOrTRID()).isNotBlank();
    }

    @Test
    @Timeout(15)
    @DisplayName("getUserData returns a title or name")
    void userDataIdentity() {
        UserData user = client.getUserData(token);
        assertThat(user.title() != null || user.name() != null).isTrue();
    }

    // ─── Invoice queries ───────────────────────────────────────────────────────

    @Test
    @Timeout(30)
    @DisplayName("getAllInvoicesByDateRange returns an array for today's date range")
    void queryToday() {
        List<?> invoices =
                client.getAllInvoicesByDateRange(token, new io.jfatura.model.DateRange(todayGib(), todayGib()));
        assertThat(invoices).isNotNull();
    }

    @Test
    @Timeout(30)
    @DisplayName("getAllInvoicesByDateRange returns an array for a broader range")
    void queryBroadRange() {
        List<?> invoices =
                client.getAllInvoicesByDateRange(token, new io.jfatura.model.DateRange("01/01/2024", todayGib()));
        assertThat(invoices).isNotNull();
    }

    @Test
    @Timeout(30)
    @DisplayName("getAllInvoicesIssuedToMeByDateRange returns an array or throws a GİB permission error")
    void issuedToMe() {
        try {
            List<?> invoices = client.getAllInvoicesIssuedToMeByDateRange(
                    token, new io.jfatura.model.DateRange(todayGib(), todayGib()));
            assertThat(invoices).isNotNull();
        } catch (GibApiException err) {
            // Kamu test hesabının bu endpoint'e yetkisi/verisi olmayabilir.
            // Hesabınızda veri varsa array döner; yoksa hata fırlatır — ikisi de kabul edilir.
            System.out.println(
                    "  ↳ getAllInvoicesIssuedToMeByDateRange: " + err.getMessage() + " (hesap yetersiz — beklenen)");
        }
    }

    // ─── Invoice lifecycle ─────────────────────────────────────────────────────

    @Test
    @Timeout(60)
    @DisplayName("createDraftInvoice + findInvoice creates a draft and finds it by uuid")
    void createAndFindDraft() {
        DraftInvoice draft = client.createDraftInvoice(token, makeInvoice());
        assertThat(draft.uuid()).isNotBlank();
        assertThat(draft.date()).isEqualTo(todayGib());

        var found = client.findInvoice(token, draft);
        assertThat(found).as("taslak bulundu").isPresent();
        assertThat(found.orElseThrow().ettn()).isEqualTo(draft.uuid());
    }

    @Test
    @Timeout(60)
    @DisplayName("getDownloadURL returns a URL containing the invoice uuid and token")
    void downloadUrlContainsIds() {
        DraftInvoice draft = client.createDraftInvoice(token, makeInvoice());
        String url = client.getDownloadURL(token, draft.uuid(), false);

        assertThat(url)
                .startsWith("https://")
                .contains(draft.uuid())
                .contains(token)
                .contains("belgeTip=FATURA");
    }

    @Test
    @Timeout(60)
    @DisplayName("getInvoiceHTML returns a non-empty HTML string for an unsigned invoice")
    void invoiceHtmlNonEmpty() {
        DraftInvoice draft = client.createDraftInvoice(token, makeInvoice());
        String html = client.getInvoiceHTML(token, draft.uuid(), false);
        assertThat(html).isNotEmpty();
    }

    // ─── Recipient lookup ──────────────────────────────────────────────────────

    @Test
    @Timeout(30)
    @DisplayName("getRecipientDataByTaxIDOrTRID returns a defined result for TRID 11111111111")
    void recipientLookup() {
        var result = client.getRecipientDataByTaxIDOrTRID(token, "11111111111");
        assertThat(result).isNotNull();
    }

    // ─── Sign + cancel lifecycle ───────────────────────────────────────────────

    @Test
    @Timeout(90)
    @DisplayName("signDraftInvoice + cancelDraftInvoice signs and cancels when permitted; skips gracefully otherwise")
    void signAndCancelLifecycle() {
        DraftInvoice draft = client.createDraftInvoice(token, makeInvoice());
        var found = client.findInvoice(token, draft);
        assertThat(found).isPresent();

        try {
            // Kamu test hesabının HSM imzalama yetkisi olmayabilir.
            client.signDraftInvoice(token, found.orElseThrow());

            var signedFound = client.findInvoice(token, draft);
            assertThat(signedFound).isPresent();

            var cancelResult =
                    client.cancelDraftInvoice(token, "Integration test — otomatik iptal", signedFound.orElseThrow());
            assertThat(cancelResult).isNotEmpty();
        } catch (GibApiException err) {
            String msg = err.getMessage() == null ? "" : err.getMessage();
            boolean permissionError = msg.contains("yetkiniz yok") || msg.contains("yetki") || msg.contains("HSM");
            if (permissionError) {
                System.out.println("  ↳ signDraftInvoice: \"" + msg + "\" (hesap yetersiz — beklenen)");
            } else {
                throw err; // beklenmedik hata — testi gerçekten başarısız yap
            }
        }
    }

    // ─── Logout ────────────────────────────────────────────────────────────────

    @Test
    @Timeout(15)
    @DisplayName("logout completes without throwing and returns a response")
    void logoutCompletes() {
        var result = client.logout(token);
        // GİB { data: { redirectUrl: "login.html" } } veya düz string döndürür
        assertThat(result.raw()).isNotNull();
        token = null; // afterAll tekrar çağırmasın
    }
}
