package io.jfatura.support;

import com.fasterxml.jackson.databind.node.MissingNode;
import io.jfatura.api.ApiResponse;
import io.jfatura.model.DraftInvoice;
import io.jfatura.model.InvoiceDetails;
import io.jfatura.model.InvoiceItem;
import io.jfatura.model.InvoiceListItem;
import io.jfatura.model.RawUserData;
import io.jfatura.model.UserData;
import java.util.List;
import java.util.Map;

/**
 * {@code tests/fixtures/*.ts} karşılığı.
 */
public final class Fixtures {

    public static final String TOKEN = "test-session-token-abc123";

    /** createDraftInvoice'ın üç çağrısından hangisi fatura oluşturma çağrısıdır. */
    public static final int CREATE_CALL = 1;

    /** GİB'in taslak oluşturma başarı mesajı. */
    public static final String GIB_DRAFT_CREATED =
            "Faturanız başarıyla oluşturulmuştur. Düzenlenen Belgeler menüsünden faturanıza ulaşabilirsiniz.";

    private Fixtures() {}

    /** Minimum geçerli InvoiceDetails — yalnızca zorunlu alanlar dolu. */
    public static InvoiceDetails minimalInvoice() {
        return InvoiceDetails.builder("01/15/2024", "12:00:00")
                .items(List.of(InvoiceItem.builder()
                        .name("Test Hizmeti")
                        .price(100)
                        .unitPrice(100.0)
                        .quantity(1)
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

    /** Tüm opsiyonel alanları dolu InvoiceDetails — alan eşleme testleri için. */
    public static InvoiceDetails fullInvoice() {
        return InvoiceDetails.builder("01/15/2024", "14:30:00")
                .uuid("11111111-2222-1333-a444-555555555555")
                .documentNumber("DOC-2024-001")
                .currency("EUR")
                .currencyRate("32.50")
                .invoiceType("5000/30000")
                .hangiTip("Buyuk")
                .orderNumber("ORD-001")
                .orderDate("01/10/2024")
                .dispatchNumber("DIS-001")
                .dispatchDate("01/12/2024")
                .slipNumber("SLIP-001")
                .slipDate("01/13/2024")
                .slipTime("10:00:00")
                .slipType("type1")
                .zReportNumber("Z001")
                .okcSerialNumber("OKC001")
                .taxIDOrTRID("1234567890")
                .title("Test Şirketi A.Ş.")
                .name("Ahmet")
                .surname("Yılmaz")
                .fullAddress("Atatürk Cad. No:1")
                .buildingName("İş Merkezi")
                .buildingNumber("1A")
                .doorNumber("5")
                .town("Beşiktaş")
                .district("Beşiktaş")
                .city("İstanbul")
                .country("Türkiye")
                .zipCode("34349")
                .phoneNumber("02121234567")
                .faxNumber("02121234568")
                .email("test@example.com")
                .webSite("https://example.com")
                .taxOffice("Beşiktaş VD")
                .taxType("gelirVergisi")
                .commissionRate(5.0)
                .freightRate(2.0)
                .hammaliyeOrani(1.0)
                .nakliyeOrani(3.0)
                .halRusumuOrani(0.5)
                .halRusumuTutari("25.00")
                .ticaretBorsasiOrani(0.3)
                .ticaretBorsasiTutari("15.00")
                .milliSavunmaFonuOrani(0.2)
                .milliSavunmaFonuTutari("10.00")
                .digerOrani(0.1)
                .digerTutari("5.00")
                .gelirVergisiOrani(15.0)
                .bagkurTevkifatiOrani(4.0)
                .gelirVergisiTevkifatiTutari("75.00")
                .bagkurTevkifatiTutari("20.00")
                .specialTaxBaseAmount("500.00")
                .specialTaxBaseRate(10.0)
                .specialTaxBaseTaxAmount(50.0)
                .totalDiscount(10.0)
                .toplamMasraflar("5.00")
                .items(List.of(InvoiceItem.builder()
                        .name("Yazılım Hizmeti")
                        .quantity(2)
                        .unitType("HUR")
                        .unitPrice(50.0)
                        .price(100)
                        .vatRate(20.0)
                        .vatAmount(20.0)
                        .vatAmountOfTax(0.0)
                        .discountRate(5.0)
                        .discountAmount(5.0)
                        .discountReason("Promosyon")
                        .discount("İskonto")
                        .taxRate(0.0)
                        .build()))
                .grandTotal(100)
                .totalVAT(20)
                .grandTotalInclVAT(120)
                .paymentTotal(120)
                .build();
    }

    /** createDraftInvoice'ın döndürdüğü tipik taslak nesnesi (kararlı örnek). */
    public static synchronized DraftInvoice draftInvoiceResponse() {
        if (DRAFT_INVOICE_RESPONSE == null) {
            ApiResponse response = new ApiResponse();
            response.setData(MissingNode.getInstance());
            DRAFT_INVOICE_RESPONSE =
                    new DraftInvoice("01/15/2024", "11111111-2222-1333-a444-555555555555", null, null, response);
        }
        return DRAFT_INVOICE_RESPONSE;
    }

    private static volatile DraftInvoice DRAFT_INVOICE_RESPONSE;

    /** GİB API'sinin döndürdüğü tipik liste satırı. */
    public static synchronized InvoiceListItem invoiceListItem() {
        if (INVOICE_LIST_ITEM == null) {
            INVOICE_LIST_ITEM = new InvoiceListItem(Map.ofEntries(
                    Map.entry("ettn", "11111111-2222-1333-a444-555555555555"),
                    Map.entry("faturaTarihi", "01/15/2024"),
                    Map.entry("aliciUnvan", "Test Şirketi A.Ş."),
                    Map.entry("aliciAdi", "Ahmet"),
                    Map.entry("aliciSoyadi", "Yılmaz"),
                    Map.entry("belgeTuru", "FATURA"),
                    Map.entry("onayDurumu", "Onaylandı")));
        }
        return INVOICE_LIST_ITEM;
    }

    private static volatile InvoiceListItem INVOICE_LIST_ITEM;

    /** İngilizce anahtarlı UserData (public API şekli). */
    public static UserData userData() {
        return UserData.builder("1234567890", "Test Şirketi A.Ş.", "Ahmet", "Yılmaz")
                .registryNo("12345")
                .mersisNo("0123456789000011")
                .taxOffice("Beşiktaş VD")
                .fullAddress("Atatürk Cad. No:1")
                .buildingName("İş Merkezi")
                .buildingNumber("1A")
                .doorNumber("5")
                .town("Beşiktaş")
                .district("Beşiktaş")
                .city("İstanbul")
                .zipCode("34349")
                .country("Türkiye")
                .phoneNumber("02121234567")
                .faxNumber("02121234568")
                .email("test@example.com")
                .webSite("https://example.com")
                .businessCenter("Levent İş Merkezi")
                .build();
    }

    /** Türkçe anahtarlı RawUserData (GİB API yanıt şekli). */
    public static RawUserData rawUserData() {
        return new RawUserData(
                "1234567890",
                "Test Şirketi A.Ş.",
                "Ahmet",
                "Yılmaz",
                "12345",
                "0123456789000011",
                "Beşiktaş VD",
                "Atatürk Cad. No:1",
                "İş Merkezi",
                "1A",
                "5",
                "Beşiktaş",
                "Beşiktaş",
                "İstanbul",
                "34349",
                "Türkiye",
                "02121234567",
                "02121234568",
                "test@example.com",
                "https://example.com",
                "Levent İş Merkezi");
    }

    /**
     * {@code mock-fetch.ts#mockDraftCreation} karşılığı — createDraftInvoice
     * üç HTTP çağrısı yapar:
     *
     * <pre>
     * 0 → taslak listesi (oluşturmadan önce)
     * 1 → EARSIV_PORTAL_FATURA_OLUSTUR
     * 2 → taslak listesi (oluşturmadan sonra, yeni ETTN'i bulmak için)
     * </pre>
     */
    public static void mockDraftCreation(GibHttpMock gib) {
        mockDraftCreation(gib, Map.of("ettn", "gib-atanan-ettn"), List.of());
    }

    public static void mockDraftCreation(GibHttpMock gib, Object created, List<Object> existing) {
        gib.once("{\"data\":" + toJson(existing) + "}");
        gib.once("{\"data\":\"" + GIB_DRAFT_CREATED + "\"}");
        List<Object> after = new java.util.ArrayList<>(existing);
        after.add(created);
        gib.once("{\"data\":" + toJson(after) + "}");
    }

    private static String toJson(Object value) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
