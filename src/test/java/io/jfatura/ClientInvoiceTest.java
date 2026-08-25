package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jfatura.exception.GibApiException;
import io.jfatura.model.DraftInvoice;
import io.jfatura.model.InvoiceDetails;
import io.jfatura.model.InvoiceItem;
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

@DisplayName("FaturaClient — invoice operations")
class ClientInvoiceTest {

    private FaturaClient client;
    private GibHttpMock gib;

    @BeforeEach
    void setUp() {
        gib = new GibHttpMock();
        client = new FaturaClient(Environment.PROD, RestClient.builder().requestFactory(gib));
    }

    // ─── createDraftInvoice ────────────────────────────────────────────────────

    @Nested
    @DisplayName("createDraftInvoice")
    class CreateDraftInvoiceTest {

        private Map<String, Object> jpOfCreateCall() {
            return gib.call(Fixtures.CREATE_CALL).jp();
        }

        @SuppressWarnings("unchecked")
        private Map<String, Object> firstMalHizmetRow() {
            List<Map<String, Object>> table = (List<Map<String, Object>>) jpOfCreateCall().get("malHizmetTable");
            return table.get(0);
        }

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(gib.call(Fixtures.CREATE_CALL).cmd()).isEqualTo("EARSIV_PORTAL_FATURA_OLUSTUR");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(gib.call(Fixtures.CREATE_CALL).pageName()).isEqualTo("RG_BASITFATURA");
        }

        @Test
        @DisplayName("includes the token in the body")
        void includesToken() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(gib.call(Fixtures.CREATE_CALL).token()).isEqualTo(Fixtures.TOKEN);
        }

        @Test
        @DisplayName("[BUG FIX] sends faturaUuid empty — GİB assigns the ETTN itself")
        void emptyFaturaUuid() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("faturaUuid")).isEqualTo("");
        }

        @Test
        @DisplayName("[BUG FIX] ignores invoiceDetails.uuid instead of sending it to GİB")
        void ignoresClientUuid() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(gib.call(Fixtures.CREATE_CALL).rawBody())
                    .doesNotContain("11111111-2222-1333-a444-555555555555");
        }

        @Test
        @DisplayName("[BUG FIX] returns the ETTN assigned by GİB")
        void returnsGibAssignedEttn() {
            Fixtures.mockDraftCreation(gib,
                    Map.of("ettn", "0f5926b2-862b-4a31-a4a1-235118b7fc11", "belgeNumarasi", "GIB2026000001644"),
                    List.of());
            DraftInvoice result = client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(result.uuid()).isEqualTo("0f5926b2-862b-4a31-a4a1-235118b7fc11");
            assertThat(result.documentNumber()).isEqualTo("GIB2026000001644");
        }

        @Test
        @DisplayName("[BUG FIX] finds the new ETTN by diffing the draft list, not by taking the first row")
        void diffsDraftList() {
            Fixtures.mockDraftCreation(gib,
                    Map.of("ettn", "yeni-ettn"),
                    List.of(Map.of("ettn", "onceden-var-olan")));
            DraftInvoice result = client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(result.uuid()).isEqualTo("yeni-ettn");
        }

        @Test
        @DisplayName("[BUG FIX] throws when GİB rejects the draft (error is only in the data text)")
        void throwsWhenRejected() {
            gib.once("{\"data\":[]}");
            gib.once("{\"data\":\"Ettn ya eksik ya boş ya da 36 uzunluk sınırına uymuyor.\"}");
            assertThatThrownBy(() -> client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice()))
                    .isInstanceOf(GibApiException.class)
                    .hasMessageContaining("Ettn ya eksik");
        }

        @Test
        @DisplayName("sets result.date to the invoice date")
        void resultDate() {
            Fixtures.mockDraftCreation(gib);
            DraftInvoice result = client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(result.date()).isEqualTo(Fixtures.minimalInvoice().date());
        }

        @Test
        @DisplayName("spreads the API response into the result")
        void spreadsApiResponse() {
            gib.once("{\"data\":[]}");
            gib.once("{\"data\":\"" + Fixtures.GIB_DRAFT_CREATED + "\",\"someField\":\"someValue\"}");
            gib.once("{\"data\":[{\"ettn\":\"gib-atanan-ettn\"}]}");
            DraftInvoice result = client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(result.data().asText()).isEqualTo(Fixtures.GIB_DRAFT_CREATED);
            assertThat(result.response().extra().get("someField")).isEqualTo("someValue");
        }

        @Test
        @DisplayName("maps date to faturaTarihi")
        void mapsDate() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("faturaTarihi")).isEqualTo(Fixtures.minimalInvoice().date());
        }

        @Test
        @DisplayName("maps time to saat")
        void mapsTime() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("saat")).isEqualTo(Fixtures.minimalInvoice().time());
        }

        @Test
        @DisplayName("maps taxIDOrTRID to vknTckn")
        void mapsTaxId() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("vknTckn")).isEqualTo(Fixtures.fullInvoice().taxIDOrTRID());
        }

        @Test
        @DisplayName("defaults vknTckn to 11111111111 when taxIDOrTRID is omitted")
        void defaultTaxId() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("vknTckn")).isEqualTo("11111111111");
        }

        @Test
        @DisplayName("maps title to aliciUnvan")
        void mapsTitle() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("aliciUnvan")).isEqualTo(Fixtures.fullInvoice().title());
        }

        @Test
        @DisplayName("maps name to aliciAdi")
        void mapsName() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("aliciAdi")).isEqualTo(Fixtures.fullInvoice().name());
        }

        @Test
        @DisplayName("maps surname to aliciSoyadi")
        void mapsSurname() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("aliciSoyadi")).isEqualTo(Fixtures.fullInvoice().surname());
        }

        @Test
        @DisplayName("maps fullAddress to bulvarcaddesokak")
        void mapsFullAddress() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("bulvarcaddesokak")).isEqualTo(Fixtures.fullInvoice().fullAddress());
        }

        @Test
        @DisplayName("maps city to sehir")
        void mapsCity() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("sehir")).isEqualTo(Fixtures.fullInvoice().city());
        }

        @Test
        @DisplayName("maps country to ulke")
        void mapsCountry() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("ulke")).isEqualTo(Fixtures.fullInvoice().country());
        }

        @Test
        @DisplayName("maps taxOffice to vergiDairesi")
        void mapsTaxOffice() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("vergiDairesi")).isEqualTo(Fixtures.fullInvoice().taxOffice());
        }

        // ── BUG FIX: dispatchDate → irsaliyeTarihi ─────────────────────────────

        @Test
        @DisplayName("[BUG FIX] maps dispatchDate → irsaliyeTarihi")
        void mapsDispatchDate() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("irsaliyeTarihi")).isEqualTo(Fixtures.fullInvoice().dispatchDate());
        }

        @Test
        @DisplayName("[BUG FIX] irsaliyeTarihi is not taken from a 'discountDate' field")
        void dispatchDateNotFromOtherField() {
            InvoiceDetails invoice = InvoiceDetails.builder("01/15/2024", "12:00:00")
                    .dispatchDate("02/20/2024")
                    .items(List.of(InvoiceItem.builder().name("Test Hizmeti").price(100).build()))
                    .grandTotal(100).totalVAT(20).grandTotalInclVAT(120).paymentTotal(120)
                    .build();
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, invoice);
            assertThat(jpOfCreateCall().get("irsaliyeTarihi")).isEqualTo("02/20/2024");
        }

        // ── BUG FIX: halRusumuTutari kendi alanından okunur ─────────────────────

        @Test
        @DisplayName("[BUG FIX] maps halRusumuTutari from its own field (not hammaliyeTutari)")
        void halRusumuFromOwnField() {
            InvoiceDetails invoice = InvoiceDetails.builder("01/15/2024", "12:00:00")
                    .halRusumuTutari("99.00")
                    .hammaliyeTutari("10.00")
                    .items(List.of(InvoiceItem.builder().name("Test Hizmeti").price(100).build()))
                    .grandTotal(100).totalVAT(20).grandTotalInclVAT(120).paymentTotal(120)
                    .build();
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, invoice);
            assertThat(jpOfCreateCall().get("halRusumuTutari")).isEqualTo("99.00");
            assertThat(jpOfCreateCall().get("hammaliyeTutari")).isEqualTo("10.00");
        }

        // ── Kalemler (malHizmetTable) ───────────────────────────────────────────

        @Test
        @DisplayName("maps items to malHizmetTable")
        void itemsToMalHizmetTable() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat((List<?>) jpOfCreateCall().get("malHizmetTable")).hasSize(1);
        }

        @Test
        @DisplayName("maps item.name to malHizmet")
        void itemNameMapping() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(firstMalHizmetRow().get("malHizmet"))
                    .isEqualTo(Fixtures.minimalInvoice().items().get(0).name());
        }

        @Test
        @DisplayName("maps item.VATRate to kdvOrani as string")
        void vatRateAsString() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(firstMalHizmetRow().get("kdvOrani")).isEqualTo("20");
        }

        @Test
        @DisplayName("uses 'C62' as default unit type when unitType is omitted")
        void defaultUnitType() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(firstMalHizmetRow().get("birim")).isEqualTo("C62");
        }

        @Test
        @DisplayName("uses item.unitType when provided")
        void providedUnitType() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(firstMalHizmetRow().get("birim")).isEqualTo("HUR");
        }

        @Test
        @DisplayName("formats item.price as 2-decimal string")
        void priceFormatted() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(firstMalHizmetRow().get("fiyat")).isEqualTo("100.00");
        }

        // ── Finansal toplamlar ─────────────────────────────────────────────────

        @Test
        @DisplayName("formats grandTotal as 2-decimal string in matrah")
        void matrahFormatted() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("matrah")).isEqualTo("100.00");
        }

        @Test
        @DisplayName("formats totalVAT as 2-decimal string in hesaplanankdv")
        void kdvFormatted() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("hesaplanankdv")).isEqualTo("20.00");
        }

        @Test
        @DisplayName("formats grandTotalInclVAT in vergilerDahilToplamTutar")
        void grandTotalInclVatFormatted() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("vergilerDahilToplamTutar")).isEqualTo("120.00");
        }

        @Test
        @DisplayName("formats paymentTotal in odenecekTutar")
        void paymentTotalFormatted() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("odenecekTutar")).isEqualTo("120.00");
        }

        @Test
        @DisplayName("puts Turkish price text in 'not' field")
        void turkishPriceTextInNot() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            String not = (String) jpOfCreateCall().get("not");
            assertThat(not).contains("LIRA").contains("KURUS");
        }

        @Test
        @DisplayName("defaults item numeric fields to 0 when omitted")
        void defaultsItemNumericsToZero() {
            InvoiceDetails invoice = InvoiceDetails.builder("01/15/2024", "12:00:00")
                    .items(List.of(InvoiceItem.builder().name("Minimal Item").price(50).build()))
                    .grandTotal(100).totalVAT(20).grandTotalInclVAT(120).paymentTotal(120)
                    .build();
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, invoice);
            assertThat(firstMalHizmetRow().get("malHizmetTutari")).isEqualTo("0.00"); // 0 * 0
            assertThat(firstMalHizmetRow().get("kdvOrani")).isEqualTo("0");
            assertThat(firstMalHizmetRow().get("kdvTutari")).isEqualTo("0.00");
            assertThat(firstMalHizmetRow().get("vergininKdvTutari")).isEqualTo("0.00");
        }

        @Test
        @DisplayName("maps returnItems to iadeTable as empty objects")
        void returnItemsToIadeTable() {
            InvoiceDetails invoice = InvoiceDetails.builder("01/15/2024", "12:00:00")
                    .returnItems(List.of("item1", "item2"))
                    .items(List.of(InvoiceItem.builder().name("Test Hizmeti").price(100).build()))
                    .grandTotal(100).totalVAT(20).grandTotalInclVAT(120).paymentTotal(120)
                    .build();
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, invoice);
            Object iadeTableObj = jpOfCreateCall().get("iadeTable");
            assertThat(iadeTableObj).isInstanceOf(List.class);
            List<?> iadeTable = (List<?>) iadeTableObj;
            assertThat(iadeTable).hasSize(2);
            assertThat(iadeTable.get(0)).isEqualTo(Map.of());
        }

        @Test
        @DisplayName("defaults currency to TRY")
        void defaultCurrency() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.minimalInvoice());
            assertThat(jpOfCreateCall().get("paraBirimi")).isEqualTo("TRY");
        }

        @Test
        @DisplayName("uses specified currency")
        void specifiedCurrency() {
            Fixtures.mockDraftCreation(gib);
            client.createDraftInvoice(Fixtures.TOKEN, Fixtures.fullInvoice());
            assertThat(jpOfCreateCall().get("paraBirimi")).isEqualTo("EUR");
        }
    }

    // ─── findInvoice ───────────────────────────────────────────────────────────

    @Nested
    @DisplayName("findInvoice")
    class FindInvoiceTest {

        private final DraftInvoice draft = Fixtures.draftInvoiceResponse();

        @Test
        @DisplayName("queries getAllInvoicesByDateRange with the same start and end date")
        void queriesSameDay() {
            gib.once("{\"data\":[" + jsonOf(Fixtures.invoiceListItem()) + "]}");
            client.findInvoice(Fixtures.TOKEN, draft);
            Map<String, Object> jp = gib.call(0).jp();
            assertThat(jp.get("baslangic")).isEqualTo(draft.date());
            assertThat(jp.get("bitis")).isEqualTo(draft.date());
        }

        @Test
        @DisplayName("returns the invoice whose ettn matches the uuid")
        void matchesEttn() {
            gib.once("{\"data\":[" + jsonOf(Fixtures.invoiceListItem()) + "]}");
            var result = client.findInvoice(Fixtures.TOKEN, draft);
            assertThat(result).contains(Fixtures.invoiceListItem());
        }

        @Test
        @DisplayName("returns empty when no invoice matches the uuid")
        void emptyWhenNoMatch() {
            InvoiceListItem other = Fixtures.invoiceListItem().with("ettn", "different-uuid");
            gib.once("{\"data\":[" + jsonOf(other) + "]}");
            var result = client.findInvoice(Fixtures.TOKEN, draft);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when the list is empty")
        void emptyList() {
            gib.once("{\"data\":[]}");
            var result = client.findInvoice(Fixtures.TOKEN, draft);
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("handles multiple invoices and picks the correct one")
        void picksCorrectOne() {
            InvoiceListItem other = Fixtures.invoiceListItem()
                    .with("ettn", "aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
            gib.once("{\"data\":[" + jsonOf(other) + "," + jsonOf(Fixtures.invoiceListItem()) + "]}");
            var result = client.findInvoice(Fixtures.TOKEN, draft);
            assertThat(result.orElseThrow().ettn()).isEqualTo(draft.uuid());
        }
    }

    // ─── signDraftInvoice ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("signDraftInvoice")
    class SignDraftInvoiceTest {

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":\"ok\"}");
            client.signDraftInvoice(Fixtures.TOKEN, Fixtures.invoiceListItem());
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_FATURA_HSM_CIHAZI_ILE_IMZALA");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":\"ok\"}");
            client.signDraftInvoice(Fixtures.TOKEN, Fixtures.invoiceListItem());
            assertThat(gib.call(0).pageName()).isEqualTo("RG_BASITTASLAKLAR");
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("wraps the invoice in imzalanacaklar array")
        void wrapsInImzalanacaklar() {
            gib.once("{\"data\":\"ok\"}");
            client.signDraftInvoice(Fixtures.TOKEN, Fixtures.invoiceListItem());
            List<Map<String, Object>> imzalanacaklar =
                    (List<Map<String, Object>>) gib.call(0).jp().get("imzalanacaklar");
            assertThat(imzalanacaklar).hasSize(1);
            assertThat(imzalanacaklar.get(0)).containsEntry("ettn", Fixtures.invoiceListItem().ettn());
        }
    }

    // ─── cancelDraftInvoice ────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancelDraftInvoice")
    class CancelDraftInvoiceTest {

        private final String reason = "Yanlış kesildi";

        @Test
        @DisplayName("sends the correct GIB command")
        void command() {
            gib.once("{\"data\":\"cancelled\"}");
            client.cancelDraftInvoice(Fixtures.TOKEN, reason, Fixtures.invoiceListItem());
            assertThat(gib.call(0).cmd()).isEqualTo("EARSIV_PORTAL_FATURA_SIL");
        }

        @Test
        @DisplayName("sends the correct pageName")
        void pageName() {
            gib.once("{\"data\":\"cancelled\"}");
            client.cancelDraftInvoice(Fixtures.TOKEN, reason, Fixtures.invoiceListItem());
            assertThat(gib.call(0).pageName()).isEqualTo("RG_BASITTASLAKLAR");
        }

        @Test
        @DisplayName("sends the cancellation reason in aciklama")
        void aciklamaField() {
            gib.once("{\"data\":\"cancelled\"}");
            client.cancelDraftInvoice(Fixtures.TOKEN, reason, Fixtures.invoiceListItem());
            assertThat(gib.call(0).jp().get("aciklama")).isEqualTo(reason);
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("wraps the invoice in silinecekler array")
        void wrapsInSilinecekler() {
            gib.once("{\"data\":\"cancelled\"}");
            client.cancelDraftInvoice(Fixtures.TOKEN, reason, Fixtures.invoiceListItem());
            List<Map<String, Object>> silinecekler =
                    (List<Map<String, Object>>) gib.call(0).jp().get("silinecekler");
            assertThat(silinecekler).hasSize(1);
        }

        @Test
        @DisplayName("returns result.data")
        void returnsData() {
            gib.once("{\"data\":\"cancel-confirmation\"}");
            var result = client.cancelDraftInvoice(Fixtures.TOKEN, reason, Fixtures.invoiceListItem());
            assertThat(result.asText()).isEqualTo("cancel-confirmation");
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
