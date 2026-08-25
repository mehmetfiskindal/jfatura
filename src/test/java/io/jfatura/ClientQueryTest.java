package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.model.DateRange;
import io.jfatura.model.InvoiceListItem;
import io.jfatura.support.Fixtures;
import io.jfatura.support.GibHttpMock;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

@DisplayName("FaturaClient — query operations")
class ClientQueryTest {

    private FaturaClient client;
    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        client = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
    }

    // ─── getAllInvoicesByDateRange ─────────────────────────────────────────────

    @Nested
    @DisplayName("getAllInvoicesByDateRange")
    class GetAllInvoicesByDateRange {

        private final DateRange range = DateRange.of("01/01/2024", "01/31/2024");

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_TASLAKLARI_GETIR");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_BASITTASLAKLAR");
        }

        @Test
        @DisplayName("sends startDate as baslangic")
        void baslangic() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).jp().get("baslangic")).isEqualTo(range.startDate());
        }

        @Test
        @DisplayName("sends endDate as bitis")
        void bitis() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).jp().get("bitis")).isEqualTo(range.endDate());
        }

        @Test
        @DisplayName("sends hangiTip as '5000/30000'")
        void hangiTip() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).jp().get("hangiTip")).isEqualTo("5000/30000");
        }

        @Test
        @DisplayName("sends an empty table array")
        void emptyTable() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat((List<?>) gib.call(0).jp().get("table")).isEmpty();
        }

        @Test
        @DisplayName("returns result.data array")
        void returnsData() {
            gib.once("{\"data\":[" + jsonOf(Fixtures.invoiceListItem()) + "]}");
            List<InvoiceListItem> result = client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(result).containsExactly(Fixtures.invoiceListItem());
        }

        @Test
        @DisplayName("returns an empty array when no invoices found")
        void emptyResult() {
            gib.once("{\"data\":[]}");
            List<InvoiceListItem> result = client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns multiple invoices")
        void multipleInvoices() {
            InvoiceListItem second = Fixtures.invoiceListItem()
                    .with("ettn", "aaaaaaaa-0000-1111-2222-333333333333");
            gib.once("{\"data\":[" + jsonOf(Fixtures.invoiceListItem()) + "," + jsonOf(second) + "]}");
            List<InvoiceListItem> result = client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("includes the token in the body")
        void includesToken() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).token()).isEqualTo(Fixtures.TOKEN);
        }
    }

    // ─── getAllInvoicesIssuedToMeByDateRange ───────────────────────────────────

    @Nested
    @DisplayName("getAllInvoicesIssuedToMeByDateRange")
    class GetAllInvoicesIssuedToMeByDateRange {

        private final DateRange range = DateRange.of("02/01/2024", "02/29/2024");

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_ADIMA_KESILEN_BELGELERI_GETIR");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).pageName()).isEqualTo("RG_ALICI_TASLAKLAR");
        }

        @Test
        @DisplayName("sends startDate as baslangic")
        void baslangic() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).jp().get("baslangic")).isEqualTo(range.startDate());
        }

        @Test
        @DisplayName("sends endDate as bitis")
        void bitis() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).jp().get("bitis")).isEqualTo(range.endDate());
        }

        @Test
        @DisplayName("sends hangiTip as '5000/30000'")
        void hangiTip() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).jp().get("hangiTip")).isEqualTo("5000/30000");
        }

        @Test
        @DisplayName("returns result.data array")
        void returnsData() {
            gib.once("{\"data\":[" + jsonOf(Fixtures.invoiceListItem()) + "]}");
            List<InvoiceListItem> result =
                    client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(result).containsExactly(Fixtures.invoiceListItem());
        }

        @Test
        @DisplayName("uses a different command than getAllInvoicesByDateRange")
        void differentCommand() {
            gib.once("{\"data\":[]}");
            client.getAllInvoicesIssuedToMeByDateRange(Fixtures.TOKEN, range);
            assertThat(gib.call(0).cmd()).isNotEqualTo("EARSIV_PORTAL_TASLAKLARI_GETIR");
        }
    }

    private static String jsonOf(Object value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
