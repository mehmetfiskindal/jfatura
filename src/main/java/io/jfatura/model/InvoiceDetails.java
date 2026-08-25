package io.jfatura.model;

import java.util.List;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/**
 * e-Arşiv fatura detayları. Zorunlu alanlar: {@code date}, {@code time},
 * {@code items}, {@code grandTotal}, {@code totalVAT}, {@code grandTotalInclVAT},
 * {@code paymentTotal}. Diğer tüm alanlar isteğe bağlıdır.
 */
public record InvoiceDetails(
        // Identity
        @Nullable String uuid,
        @Nullable String documentNumber,
        String date,
        String time,
        @Nullable String invoiceType,
        @Nullable String hangiTip,

        // Currency
        @Nullable String currency,
        @Nullable String currencyRate,

        // Order / dispatch / slip
        @Nullable String orderNumber,
        @Nullable String orderDate,
        @Nullable String dispatchNumber,
        @Nullable String dispatchDate,
        @Nullable String slipNumber,
        @Nullable String slipDate,
        @Nullable String slipTime,
        @Nullable String slipType,
        @Nullable String zReportNumber,
        @Nullable String okcSerialNumber,

        // Recipient
        @Nullable String taxIDOrTRID,
        @Nullable String title,
        @Nullable String name,
        @Nullable String surname,
        @Nullable String fullAddress,
        @Nullable String buildingName,
        @Nullable String buildingNumber,
        @Nullable String doorNumber,
        @Nullable String town,
        @Nullable String district,
        @Nullable String city,
        @Nullable String country,
        @Nullable String zipCode,
        @Nullable String phoneNumber,
        @Nullable String faxNumber,
        @Nullable String email,
        @Nullable String webSite,
        @Nullable String taxOffice,
        @Nullable String taxType,

        // Commission / freight charges
        @Nullable Double commissionRate,
        @Nullable Double freightRate,
        @Nullable Double hammaliyeOrani,
        @Nullable Double nakliyeOrani,
        @Nullable String komisyonTutari,
        @Nullable String navlunTutari,
        @Nullable String hammaliyeTutari,
        @Nullable String nakliyeTutari,
        @Nullable Double komisyonKDVOrani,
        @Nullable Double navlunKDVOrani,
        @Nullable Double hammaliyeKDVOrani,
        @Nullable Double nakliyeKDVOrani,
        @Nullable String komisyonKDVTutari,
        @Nullable String navlunKDVTutari,
        @Nullable String hammaliyeKDVTutari,
        @Nullable String nakliyeKDVTutari,

        // Income / social security withholdings
        @Nullable Double gelirVergisiOrani,
        @Nullable Double bagkurTevkifatiOrani,
        @Nullable String gelirVergisiTevkifatiTutari,
        @Nullable String bagkurTevkifatiTutari,

        // Market / hall / defense fund fees
        @Nullable Double halRusumuOrani,
        @Nullable String halRusumuTutari,
        @Nullable Double halRusumuKDVOrani,
        @Nullable String halRusumuKDVTutari,
        @Nullable Double ticaretBorsasiOrani,
        @Nullable String ticaretBorsasiTutari,
        @Nullable Double ticaretBorsasiKDVOrani,
        @Nullable String ticaretBorsasiKDVTutari,
        @Nullable Double milliSavunmaFonuOrani,
        @Nullable String milliSavunmaFonuTutari,
        @Nullable Double milliSavunmaFonuKDVOrani,
        @Nullable String milliSavunmaFonuKDVTutari,
        @Nullable Double digerOrani,
        @Nullable String digerTutari,
        @Nullable Double digerKDVOrani,
        @Nullable String digerKDVTutari,

        // Special tax base
        @Nullable String specialTaxBaseAmount,
        @Nullable Double specialTaxBaseRate,
        @Nullable Double specialTaxBaseTaxAmount,

        // Totals
        @Nullable String toplamMasraflar,
        double grandTotal,
        @Nullable Double totalDiscount,
        double totalVAT,
        double grandTotalInclVAT,
        double paymentTotal,

        // Items
        List<InvoiceItem> items,
        List<Object> returnItems) {

    public InvoiceDetails {
        Objects.requireNonNull(date, "date zorunludur");
        Objects.requireNonNull(time, "time zorunludur");
        items = items == null ? List.of() : List.copyOf(items);
        returnItems = returnItems == null ? List.of() : List.copyOf(returnItems);
    }

    public static Builder builder(String date, String time) {
        return new Builder(date, time);
    }

    public static final class Builder {

        private final String date;
        private final String time;
        private boolean grandTotalSet;
        private boolean totalVatSet;
        private boolean grandTotalInclVatSet;
        private boolean paymentTotalSet;

        private String uuid;
        private String documentNumber;
        private String invoiceType;
        private String hangiTip;
        private String currency;
        private String currencyRate;
        private String orderNumber;
        private String orderDate;
        private String dispatchNumber;
        private String dispatchDate;
        private String slipNumber;
        private String slipDate;
        private String slipTime;
        private String slipType;
        private String zReportNumber;
        private String okcSerialNumber;
        private String taxIDOrTRID;
        private String title;
        private String name;
        private String surname;
        private String fullAddress;
        private String buildingName;
        private String buildingNumber;
        private String doorNumber;
        private String town;
        private String district;
        private String city;
        private String country;
        private String zipCode;
        private String phoneNumber;
        private String faxNumber;
        private String email;
        private String webSite;
        private String taxOffice;
        private String taxType;
        private Double commissionRate;
        private Double freightRate;
        private Double hammaliyeOrani;
        private Double nakliyeOrani;
        private String komisyonTutari;
        private String navlunTutari;
        private String hammaliyeTutari;
        private String nakliyeTutari;
        private Double komisyonKDVOrani;
        private Double navlunKDVOrani;
        private Double hammaliyeKDVOrani;
        private Double nakliyeKDVOrani;
        private String komisyonKDVTutari;
        private String navlunKDVTutari;
        private String hammaliyeKDVTutari;
        private String nakliyeKDVTutari;
        private Double gelirVergisiOrani;
        private Double bagkurTevkifatiOrani;
        private String gelirVergisiTevkifatiTutari;
        private String bagkurTevkifatiTutari;
        private Double halRusumuOrani;
        private String halRusumuTutari;
        private Double halRusumuKDVOrani;
        private String halRusumuKDVTutari;
        private Double ticaretBorsasiOrani;
        private String ticaretBorsasiTutari;
        private Double ticaretBorsasiKDVOrani;
        private String ticaretBorsasiKDVTutari;
        private Double milliSavunmaFonuOrani;
        private String milliSavunmaFonuTutari;
        private Double milliSavunmaFonuKDVOrani;
        private String milliSavunmaFonuKDVTutari;
        private Double digerOrani;
        private String digerTutari;
        private Double digerKDVOrani;
        private String digerKDVTutari;
        private String specialTaxBaseAmount;
        private Double specialTaxBaseRate;
        private Double specialTaxBaseTaxAmount;
        private String toplamMasraflar;
        private double grandTotal;
        private Double totalDiscount;
        private double totalVAT;
        private double grandTotalInclVAT;
        private double paymentTotal;
        private List<InvoiceItem> items = List.of();
        private List<Object> returnItems = List.of();

        private Builder(String date, String time) {
            this.date = date;
            this.time = time;
        }

        public Builder uuid(String v) { this.uuid = v; return this; }
        public Builder documentNumber(String v) { this.documentNumber = v; return this; }
        public Builder invoiceType(String v) { this.invoiceType = v; return this; }
        public Builder hangiTip(String v) { this.hangiTip = v; return this; }
        public Builder currency(String v) { this.currency = v; return this; }
        public Builder currencyRate(String v) { this.currencyRate = v; return this; }
        public Builder orderNumber(String v) { this.orderNumber = v; return this; }
        public Builder orderDate(String v) { this.orderDate = v; return this; }
        public Builder dispatchNumber(String v) { this.dispatchNumber = v; return this; }
        public Builder dispatchDate(String v) { this.dispatchDate = v; return this; }
        public Builder slipNumber(String v) { this.slipNumber = v; return this; }
        public Builder slipDate(String v) { this.slipDate = v; return this; }
        public Builder slipTime(String v) { this.slipTime = v; return this; }
        public Builder slipType(String v) { this.slipType = v; return this; }
        public Builder zReportNumber(String v) { this.zReportNumber = v; return this; }
        public Builder okcSerialNumber(String v) { this.okcSerialNumber = v; return this; }
        public Builder taxIDOrTRID(String v) { this.taxIDOrTRID = v; return this; }
        public Builder title(String v) { this.title = v; return this; }
        public Builder name(String v) { this.name = v; return this; }
        public Builder surname(String v) { this.surname = v; return this; }
        public Builder fullAddress(String v) { this.fullAddress = v; return this; }
        public Builder buildingName(String v) { this.buildingName = v; return this; }
        public Builder buildingNumber(String v) { this.buildingNumber = v; return this; }
        public Builder doorNumber(String v) { this.doorNumber = v; return this; }
        public Builder town(String v) { this.town = v; return this; }
        public Builder district(String v) { this.district = v; return this; }
        public Builder city(String v) { this.city = v; return this; }
        public Builder country(String v) { this.country = v; return this; }
        public Builder zipCode(String v) { this.zipCode = v; return this; }
        public Builder phoneNumber(String v) { this.phoneNumber = v; return this; }
        public Builder faxNumber(String v) { this.faxNumber = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder webSite(String v) { this.webSite = v; return this; }
        public Builder taxOffice(String v) { this.taxOffice = v; return this; }
        public Builder taxType(String v) { this.taxType = v; return this; }
        public Builder commissionRate(Double v) { this.commissionRate = v; return this; }
        public Builder freightRate(Double v) { this.freightRate = v; return this; }
        public Builder hammaliyeOrani(Double v) { this.hammaliyeOrani = v; return this; }
        public Builder nakliyeOrani(Double v) { this.nakliyeOrani = v; return this; }
        public Builder komisyonTutari(String v) { this.komisyonTutari = v; return this; }
        public Builder navlunTutari(String v) { this.navlunTutari = v; return this; }
        public Builder hammaliyeTutari(String v) { this.hammaliyeTutari = v; return this; }
        public Builder nakliyeTutari(String v) { this.nakliyeTutari = v; return this; }
        public Builder komisyonKDVOrani(Double v) { this.komisyonKDVOrani = v; return this; }
        public Builder navlunKDVOrani(Double v) { this.navlunKDVOrani = v; return this; }
        public Builder hammaliyeKDVOrani(Double v) { this.hammaliyeKDVOrani = v; return this; }
        public Builder nakliyeKDVOrani(Double v) { this.nakliyeKDVOrani = v; return this; }
        public Builder komisyonKDVTutari(String v) { this.komisyonKDVTutari = v; return this; }
        public Builder navlunKDVTutari(String v) { this.navlunKDVTutari = v; return this; }
        public Builder hammaliyeKDVTutari(String v) { this.hammaliyeKDVTutari = v; return this; }
        public Builder nakliyeKDVTutari(String v) { this.nakliyeKDVTutari = v; return this; }
        public Builder gelirVergisiOrani(Double v) { this.gelirVergisiOrani = v; return this; }
        public Builder bagkurTevkifatiOrani(Double v) { this.bagkurTevkifatiOrani = v; return this; }
        public Builder gelirVergisiTevkifatiTutari(String v) { this.gelirVergisiTevkifatiTutari = v; return this; }
        public Builder bagkurTevkifatiTutari(String v) { this.bagkurTevkifatiTutari = v; return this; }
        public Builder halRusumuOrani(Double v) { this.halRusumuOrani = v; return this; }
        public Builder halRusumuTutari(String v) { this.halRusumuTutari = v; return this; }
        public Builder halRusumuKDVOrani(Double v) { this.halRusumuKDVOrani = v; return this; }
        public Builder halRusumuKDVTutari(String v) { this.halRusumuKDVTutari = v; return this; }
        public Builder ticaretBorsasiOrani(Double v) { this.ticaretBorsasiOrani = v; return this; }
        public Builder ticaretBorsasiTutari(String v) { this.ticaretBorsasiTutari = v; return this; }
        public Builder ticaretBorsasiKDVOrani(Double v) { this.ticaretBorsasiKDVOrani = v; return this; }
        public Builder ticaretBorsasiKDVTutari(String v) { this.ticaretBorsasiKDVTutari = v; return this; }
        public Builder milliSavunmaFonuOrani(Double v) { this.milliSavunmaFonuOrani = v; return this; }
        public Builder milliSavunmaFonuTutari(String v) { this.milliSavunmaFonuTutari = v; return this; }
        public Builder milliSavunmaFonuKDVOrani(Double v) { this.milliSavunmaFonuKDVOrani = v; return this; }
        public Builder milliSavunmaFonuKDVTutari(String v) { this.milliSavunmaFonuKDVTutari = v; return this; }
        public Builder digerOrani(Double v) { this.digerOrani = v; return this; }
        public Builder digerTutari(String v) { this.digerTutari = v; return this; }
        public Builder digerKDVOrani(Double v) { this.digerKDVOrani = v; return this; }
        public Builder digerKDVTutari(String v) { this.digerKDVTutari = v; return this; }
        public Builder specialTaxBaseAmount(String v) { this.specialTaxBaseAmount = v; return this; }
        public Builder specialTaxBaseRate(Double v) { this.specialTaxBaseRate = v; return this; }
        public Builder specialTaxBaseTaxAmount(Double v) { this.specialTaxBaseTaxAmount = v; return this; }
        public Builder toplamMasraflar(String v) { this.toplamMasraflar = v; return this; }
        public Builder totalDiscount(Double v) { this.totalDiscount = v; return this; }

        public Builder grandTotal(double v) { this.grandTotal = v; this.grandTotalSet = true; return this; }
        public Builder totalVAT(double v) { this.totalVAT = v; this.totalVatSet = true; return this; }
        public Builder grandTotalInclVAT(double v) { this.grandTotalInclVAT = v; this.grandTotalInclVatSet = true; return this; }
        public Builder paymentTotal(double v) { this.paymentTotal = v; this.paymentTotalSet = true; return this; }

        public Builder items(List<InvoiceItem> v) { this.items = v; return this; }
        public Builder returnItems(List<Object> v) { this.returnItems = v; return this; }

        public InvoiceDetails build() {
            if (!grandTotalSet || !totalVatSet || !grandTotalInclVatSet || !paymentTotalSet) {
                throw new IllegalStateException(
                        "grandTotal, totalVAT, grandTotalInclVAT ve paymentTotal zorunludur");
            }
            if (items.isEmpty()) {
                throw new IllegalStateException("items en az bir kalem içermelidir");
            }
            return new InvoiceDetails(uuid, documentNumber, date, time, invoiceType, hangiTip,
                    currency, currencyRate, orderNumber, orderDate, dispatchNumber, dispatchDate,
                    slipNumber, slipDate, slipTime, slipType, zReportNumber, okcSerialNumber,
                    taxIDOrTRID, title, name, surname, fullAddress, buildingName, buildingNumber,
                    doorNumber, town, district, city, country, zipCode, phoneNumber, faxNumber,
                    email, webSite, taxOffice, taxType,
                    commissionRate, freightRate, hammaliyeOrani, nakliyeOrani,
                    komisyonTutari, navlunTutari, hammaliyeTutari, nakliyeTutari,
                    komisyonKDVOrani, navlunKDVOrani, hammaliyeKDVOrani, nakliyeKDVOrani,
                    komisyonKDVTutari, navlunKDVTutari, hammaliyeKDVTutari, nakliyeKDVTutari,
                    gelirVergisiOrani, bagkurTevkifatiOrani, gelirVergisiTevkifatiTutari, bagkurTevkifatiTutari,
                    halRusumuOrani, halRusumuTutari, halRusumuKDVOrani, halRusumuKDVTutari,
                    ticaretBorsasiOrani, ticaretBorsasiTutari, ticaretBorsasiKDVOrani, ticaretBorsasiKDVTutari,
                    milliSavunmaFonuOrani, milliSavunmaFonuTutari, milliSavunmaFonuKDVOrani, milliSavunmaFonuKDVTutari,
                    digerOrani, digerTutari, digerKDVOrani, digerKDVTutari,
                    specialTaxBaseAmount, specialTaxBaseRate, specialTaxBaseTaxAmount,
                    toplamMasraflar, grandTotal, totalDiscount, totalVAT, grandTotalInclVAT, paymentTotal,
                    items, returnItems);
        }
    }
}
