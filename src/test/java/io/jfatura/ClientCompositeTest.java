package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import io.jfatura.model.CreateInvoiceResult;
import io.jfatura.model.InvoiceListItem;
import io.jfatura.support.Fixtures;
import io.jfatura.support.GibHttpMock;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — composite (high-level) methods")
class ClientCompositeTest {

    private static final String USER_ID = "gib-user";
    private static final String PASSWORD = "gib-pass";
    private static final String TOKEN = "session-token-abc";
    private static final String DRAFT_UUID = "11111111-2222-1333-a444-555555555555";

    private FaturaClient client;
    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        client = Mockito.spy(
                new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib)));
    }

    private void setupHappyPath() {
        doReturn(TOKEN).when(client).getToken(USER_ID, PASSWORD);
        doReturn(Fixtures.draftInvoiceResponse()).when(client).createDraftInvoice(TOKEN, Fixtures.minimalInvoice());
        doReturn(java.util.Optional.of(Fixtures.invoiceListItem()))
                .when(client)
                .findInvoice(TOKEN, Fixtures.draftInvoiceResponse());
        doReturn(new io.jfatura.api.ApiResponse()).when(client).signDraftInvoice(TOKEN, Fixtures.invoiceListItem());
    }

    // ─── createInvoice ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("createInvoice")
    class CreateInvoiceTest {

        @Test
        @DisplayName("calls getToken with userId and password")
        void callsGetToken() {
            setupHappyPath();
            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client).getToken(USER_ID, PASSWORD);
        }

        @Test
        @DisplayName("calls createDraftInvoice with the received token")
        void callsCreateDraft() {
            setupHappyPath();
            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client).createDraftInvoice(TOKEN, Fixtures.minimalInvoice());
        }

        @Test
        @DisplayName("calls findInvoice with the draft returned by createDraftInvoice")
        void callsFindInvoice() {
            setupHappyPath();
            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client).findInvoice(TOKEN, Fixtures.draftInvoiceResponse());
        }

        @Test
        @DisplayName("calls signDraftInvoice when sign defaults to true")
        void signsByDefault() {
            setupHappyPath();
            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client).signDraftInvoice(TOKEN, Fixtures.invoiceListItem());
        }

        @Test
        @DisplayName("does NOT call signDraftInvoice when sign: false")
        void noSignWhenFalse() {
            setupHappyPath();
            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            verify(client, never()).signDraftInvoice(TOKEN, Fixtures.invoiceListItem());
        }

        @Test
        @DisplayName("does NOT call signDraftInvoice when findInvoice returns empty")
        void noSignWhenNotFound() {
            doReturn(TOKEN).when(client).getToken(USER_ID, PASSWORD);
            doReturn(Fixtures.draftInvoiceResponse()).when(client).createDraftInvoice(TOKEN, Fixtures.minimalInvoice());
            doReturn(java.util.Optional.<InvoiceListItem>empty())
                    .when(client)
                    .findInvoice(TOKEN, Fixtures.draftInvoiceResponse());

            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client, never()).signDraftInvoice(TOKEN, Fixtures.invoiceListItem());
        }

        @Test
        @DisplayName("returns token, uuid, and signed: true by default")
        void returnsTripleDefault() {
            setupHappyPath();
            CreateInvoiceResult result = client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            assertThat(result).isEqualTo(new CreateInvoiceResult(TOKEN, DRAFT_UUID, true));
        }

        @Test
        @DisplayName("returns signed: false when sign option is false")
        void returnsSignedFalse() {
            setupHappyPath();
            CreateInvoiceResult result = client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            assertThat(result).isEqualTo(new CreateInvoiceResult(TOKEN, DRAFT_UUID, false));
        }

        @Test
        @DisplayName("calls methods in the correct order (getToken → createDraft → find → sign)")
        void callOrder() {
            List<String> order = new ArrayList<>();
            doAnswer(inv -> {
                        order.add("getToken");
                        return TOKEN;
                    })
                    .when(client)
                    .getToken(USER_ID, PASSWORD);
            doAnswer(inv -> {
                        order.add("createDraftInvoice");
                        return Fixtures.draftInvoiceResponse();
                    })
                    .when(client)
                    .createDraftInvoice(TOKEN, Fixtures.minimalInvoice());
            doAnswer(inv -> {
                        order.add("findInvoice");
                        return java.util.Optional.of(Fixtures.invoiceListItem());
                    })
                    .when(client)
                    .findInvoice(TOKEN, Fixtures.draftInvoiceResponse());
            doAnswer(inv -> {
                        order.add("signDraftInvoice");
                        return new io.jfatura.api.ApiResponse();
                    })
                    .when(client)
                    .signDraftInvoice(TOKEN, Fixtures.invoiceListItem());

            client.createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            assertThat(order).containsExactly("getToken", "createDraftInvoice", "findInvoice", "signDraftInvoice");
        }
    }

    // ─── createInvoiceAndGetDownloadURL ────────────────────────────────────────

    @Nested
    @DisplayName("createInvoiceAndGetDownloadURL")
    class CreateInvoiceAndGetDownloadUrlTest {

        @Test
        @DisplayName("returns a URL string")
        void returnsUrlString() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, true))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), true);

            String result = client.createInvoiceAndGetDownloadURL(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            assertThat(result).isInstanceOf(String.class).contains("download");
        }

        @Test
        @DisplayName("passes options through to createInvoice")
        void passesOptionsThrough() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, false))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);

            client.createInvoiceAndGetDownloadURL(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            verify(client).createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
        }

        @Test
        @DisplayName("calls getDownloadURL with the token, uuid and signed flag from createInvoice")
        void callsGetDownloadUrl() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, true))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), true);

            String url = client.createInvoiceAndGetDownloadURL(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client).getDownloadURL(TOKEN, DRAFT_UUID, true);
            assertThat(url).contains("token=" + TOKEN).contains(DRAFT_UUID);
        }

        @Test
        @DisplayName("[BUG FIX] passes the unsigned flag to getDownloadURL, not the default signed=true")
        void passesUnsignedFlagThrough() {
            // Orijinal JS'te imzasız sonuç istendiğinde bile URL imzalı üretiliyordu;
            // burada signed bayrağının birebir aktarıldığı doğrulanır.
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, false))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);

            client.createInvoiceAndGetDownloadURL(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            verify(client).getDownloadURL(TOKEN, DRAFT_UUID, false);
            assertThat(client.getDownloadURL(TOKEN, DRAFT_UUID, false))
                    .contains(io.jfatura.util.UriEncode.encodeURIComponent("Onaylanmadı"));
        }
    }

    // ─── createInvoiceAndGetHTML ───────────────────────────────────────────────

    @Nested
    @DisplayName("createInvoiceAndGetHTML")
    class CreateInvoiceAndGetHtmlTest {

        @Test
        @DisplayName("returns an HTML string")
        void returnsHtmlString() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, true))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), true);
            gib.once("{\"data\":\"<html>Fatura</html>\"}");

            String result = client.createInvoiceAndGetHTML(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            assertThat(result).isEqualTo("<html>Fatura</html>");
        }

        @Test
        @DisplayName("passes options through to createInvoice")
        void passesOptionsThrough() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, false))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            gib.once("{\"data\":\"\"}");

            client.createInvoiceAndGetHTML(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            verify(client).createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
        }

        @Test
        @DisplayName("calls getInvoiceHTML with token, uuid and signed flag from createInvoice")
        void callsGetInvoiceHtml() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, true))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), true);
            gib.once("{\"data\":\"<html></html>\"}");

            String result = client.createInvoiceAndGetHTML(USER_ID, PASSWORD, Fixtures.minimalInvoice());
            verify(client).getInvoiceHTML(TOKEN, DRAFT_UUID, true);
            assertThat(result).isEqualTo("<html></html>");
        }

        @Test
        @DisplayName("[BUG FIX] passes the unsigned flag to getInvoiceHTML, not the default signed=true")
        void passesUnsignedFlagThrough() {
            doReturn(new CreateInvoiceResult(TOKEN, DRAFT_UUID, false))
                    .when(client)
                    .createInvoice(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            gib.once("{\"data\":\"\"}");

            client.createInvoiceAndGetHTML(USER_ID, PASSWORD, Fixtures.minimalInvoice(), false);
            verify(client).getInvoiceHTML(TOKEN, DRAFT_UUID, false);
        }
    }
}
