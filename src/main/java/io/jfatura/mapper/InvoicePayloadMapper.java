package io.jfatura.mapper;

import io.jfatura.model.InvoiceDetails;
import io.jfatura.model.InvoiceItem;
import io.jfatura.util.TurkishNumberConverter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * İngilizce anahtarlı {@link InvoiceDetails} modelini GİB'in Türkçe
 * anahtarlı ~70 alanlı fatura payload'ına çevirir.
 *
 * <p>Sayısal alanlar JS {@code JSON.stringify} semantiğiyle yazılır:
 * matematiksel olarak tam sayı olan değerler {@code 5} biçiminde,
 * kesirli değerler {@code 0.5} biçiminde serileştirilir.
 */
public final class InvoicePayloadMapper {

    private InvoicePayloadMapper() {
    }

    public static Map<String, Object> toPayload(InvoiceDetails d) {
        Map<String, Object> p = new LinkedHashMap<>();
        // GİB ETTN'i sunucu tarafında üretir; istemciden gelen bir UUID
        // "Ettn ya eksik ya boş ya da 36 uzunluk sınırına uymuyor." hatası verir.
        p.put("faturaUuid", "");
        p.put("belgeNumarasi", or(d.documentNumber(), ""));
        p.put("faturaTarihi", d.date());
        p.put("saat", d.time());
        p.put("paraBirimi", or(d.currency(), "TRY"));
        p.put("dovzTLkur", or(d.currencyRate(), "0"));
        p.put("faturaTipi", or(d.invoiceType(), "5000/30000"));
        p.put("hangiTip", or(d.hangiTip(), "Buyuk"));
        p.put("siparisNumarasi", or(d.orderNumber(), ""));
        p.put("siparisTarihi", or(d.orderDate(), ""));
        p.put("irsaliyeNumarasi", or(d.dispatchNumber(), ""));
        p.put("irsaliyeTarihi", or(d.dispatchDate(), ""));
        p.put("fisNo", or(d.slipNumber(), ""));
        p.put("fisTarihi", or(d.slipDate(), ""));
        p.put("fisSaati", or(d.slipTime(), " "));
        p.put("fisTipi", or(d.slipType(), " "));
        p.put("zRaporNo", or(d.zReportNumber(), ""));
        p.put("okcSeriNo", or(d.okcSerialNumber(), ""));
        p.put("vknTckn", or(d.taxIDOrTRID(), "11111111111"));
        p.put("aliciUnvan", or(d.title(), ""));
        p.put("aliciAdi", or(d.name(), ""));
        p.put("aliciSoyadi", or(d.surname(), ""));
        p.put("bulvarcaddesokak", or(d.fullAddress(), ""));
        p.put("binaAdi", or(d.buildingName(), ""));
        p.put("binaNo", or(d.buildingNumber(), ""));
        p.put("kapiNo", or(d.doorNumber(), ""));
        p.put("kasabaKoy", or(d.town(), ""));
        p.put("mahalleSemtIlce", or(d.district(), ""));
        p.put("sehir", or(d.city(), " "));
        p.put("ulke", or(d.country(), ""));
        p.put("postaKodu", or(d.zipCode(), ""));
        p.put("tel", or(d.phoneNumber(), ""));
        p.put("fax", or(d.faxNumber(), ""));
        p.put("eposta", or(d.email(), ""));
        p.put("websitesi", or(d.webSite(), ""));
        p.put("vergiDairesi", or(d.taxOffice(), ""));
        p.put("komisyonOrani", num(orZero(d.commissionRate())));
        p.put("navlunOrani", num(orZero(d.freightRate())));
        p.put("hammaliyeOrani", num(orZero(d.hammaliyeOrani())));
        p.put("nakliyeOrani", num(orZero(d.nakliyeOrani())));
        p.put("komisyonTutari", or(d.komisyonTutari(), "0"));
        p.put("navlunTutari", or(d.navlunTutari(), "0"));
        p.put("hammaliyeTutari", or(d.hammaliyeTutari(), "0"));
        p.put("nakliyeTutari", or(d.nakliyeTutari(), "0"));
        p.put("komisyonKDVOrani", num(orZero(d.komisyonKDVOrani())));
        p.put("navlunKDVOrani", num(orZero(d.navlunKDVOrani())));
        p.put("hammaliyeKDVOrani", num(orZero(d.hammaliyeKDVOrani())));
        p.put("nakliyeKDVOrani", num(orZero(d.nakliyeKDVOrani())));
        p.put("komisyonKDVTutari", or(d.komisyonKDVTutari(), "0"));
        p.put("navlunKDVTutari", or(d.navlunKDVTutari(), "0"));
        p.put("hammaliyeKDVTutari", or(d.hammaliyeKDVTutari(), "0"));
        p.put("nakliyeKDVTutari", or(d.nakliyeKDVTutari(), "0"));
        p.put("gelirVergisiOrani", num(orZero(d.gelirVergisiOrani())));
        p.put("bagkurTevkifatiOrani", num(orZero(d.bagkurTevkifatiOrani())));
        p.put("gelirVergisiTevkifatiTutari", or(d.gelirVergisiTevkifatiTutari(), "0"));
        p.put("bagkurTevkifatiTutari", or(d.bagkurTevkifatiTutari(), "0"));
        p.put("halRusumuOrani", num(orZero(d.halRusumuOrani())));
        p.put("ticaretBorsasiOrani", num(orZero(d.ticaretBorsasiOrani())));
        p.put("milliSavunmaFonuOrani", num(orZero(d.milliSavunmaFonuOrani())));
        p.put("digerOrani", num(orZero(d.digerOrani())));
        p.put("halRusumuTutari", or(d.halRusumuTutari(), "0"));
        p.put("ticaretBorsasiTutari", or(d.ticaretBorsasiTutari(), "0"));
        p.put("milliSavunmaFonuTutari", or(d.milliSavunmaFonuTutari(), "0"));
        p.put("digerTutari", or(d.digerTutari(), "0"));
        p.put("halRusumuKDVOrani", num(orZero(d.halRusumuKDVOrani())));
        p.put("ticaretBorsasiKDVOrani", num(orZero(d.ticaretBorsasiKDVOrani())));
        p.put("milliSavunmaFonuKDVOrani", num(orZero(d.milliSavunmaFonuKDVOrani())));
        p.put("digerKDVOrani", num(orZero(d.digerKDVOrani())));
        p.put("halRusumuKDVTutari", or(d.halRusumuKDVTutari(), "0"));
        p.put("ticaretBorsasiKDVTutari", or(d.ticaretBorsasiKDVTutari(), "0"));
        p.put("milliSavunmaFonuKDVTutari", or(d.milliSavunmaFonuKDVTutari(), "0"));
        p.put("digerKDVTutari", or(d.digerKDVTutari(), "0"));

        List<Map<Object, Object>> iadeTable = new ArrayList<>();
        for (Object ignored : d.returnItems()) {
            iadeTable.add(new LinkedHashMap<>());
        }
        p.put("iadeTable", iadeTable);

        p.put("ozelMatrahTutari", or(d.specialTaxBaseAmount(), "0"));
        p.put("ozelMatrahOrani", num(orZero(d.specialTaxBaseRate())));
        p.put("ozelMatrahVergiTutari", fixed2(orZero(d.specialTaxBaseTaxAmount())));
        p.put("vergiCesidi", or(d.taxType(), " "));
        p.put("malHizmetTable", malHizmetTable(d.items()));
        p.put("tip", "İskonto");
        p.put("matrah", fixed2(d.grandTotal()));
        p.put("malhizmetToplamTutari", fixed2(d.grandTotal()));
        p.put("toplamIskonto", fixed2(orZero(d.totalDiscount())));
        p.put("hesaplanankdv", fixed2(d.totalVAT()));
        p.put("vergilerToplami", fixed2(d.totalVAT()));
        p.put("vergilerDahilToplamTutar", fixed2(d.grandTotalInclVAT()));
        p.put("toplamMasraflar", or(d.toplamMasraflar(), "0"));
        p.put("odenecekTutar", fixed2(d.paymentTotal()));
        p.put("not", TurkishNumberConverter.convertPriceToText(d.paymentTotal()));
        return p;
    }

    static List<Map<String, Object>> malHizmetTable(List<InvoiceItem> items) {
        List<Map<String, Object>> table = new ArrayList<>();
        for (InvoiceItem item : items) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("iskontoArttm", or(item.discount(), "İskonto"));
            row.put("malHizmet", item.name());
            row.put("miktar", item.quantity() == null ? 1 : item.quantity());
            row.put("birim", or(item.unitType(), "C62"));
            row.put("birimFiyat", fixed2(orZero(item.unitPrice())));
            row.put("fiyat", fixed2(item.price()));
            row.put("iskontoOrani", num(orZero(item.discountRate())));
            row.put("iskontoTutari", fixed2(orZero(item.discountAmount())));
            row.put("iskontoNedeni", or(item.discountReason(), ""));
            double miktar = item.quantity() == null ? 0 : item.quantity();
            row.put("malHizmetTutari", fixed2(miktar * orZero(item.unitPrice())));
            row.put("kdvOrani", fixed0(orZero(item.vatRate())));
            row.put("vergiOrani", num(orZero(item.taxRate())));
            row.put("kdvTutari", fixed2(orZero(item.vatAmount())));
            row.put("vergininKdvTutari", fixed2(orZero(item.vatAmountOfTax())));
            table.add(row);
        }
        return table;
    }

    /** JS sayı semantiği: integral değeri Integer, kesirlisi Double olarak yazar. */
    public static Object num(double value) {
        if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE && value == Math.rint(value)) {
            return (int) value;
        }
        return value;
    }

    private static <T> @Nullable T or(@Nullable T value, @Nullable T fallback) {
        return value != null ? value : fallback;
    }

    private static double orZero(@Nullable Double value) {
        return value != null ? value : 0d;
    }

    static String fixed2(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    /** JS {@code toFixed(0)} karşılığı: ondalıksız metin. */
    static String fixed0(double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }
}
