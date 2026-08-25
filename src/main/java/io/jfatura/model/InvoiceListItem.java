package io.jfatura.model;

import org.jspecify.annotations.Nullable;

/**
 * {@code EARSIV_PORTAL_TASLAKLARI_GETIR} liste kaydı.
 *
 * <p>Alan adları GİB portalının döndürdüğü gerçek anahtarlardır; tarih
 * {@code GG-AA-YYYY} biçiminde gelir (sorgu parametreleri ise {@code GG/AA/YYYY}).
 * Bilinen alanlar dışındaki tüm anahtarlar korunur; imzalama/iptal isteklerinde
 * satır olduğu gibi geri gönderilir.
 */
public final class InvoiceListItem {

    private final java.util.Map<String, Object> values;

    @com.fasterxml.jackson.annotation.JsonCreator
    public InvoiceListItem(java.util.Map<String, Object> values) {
        this.values = new java.util.LinkedHashMap<>(values);
    }

    public static InvoiceListItem of(String ettn) {
        java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("ettn", ettn);
        return new InvoiceListItem(map);
    }

    /** Bir anahtarı değiştirilmiş kopya döner ({@code {...item, key: value}} karşılığı). */
    public InvoiceListItem with(String key, Object value) {
        java.util.Map<String, Object> copy = new java.util.LinkedHashMap<>(this.values);
        copy.put(key, value);
        return new InvoiceListItem(copy);
    }

    @com.fasterxml.jackson.annotation.JsonValue
    public java.util.Map<String, Object> values() {
        return values;
    }

    public java.util.Map<String, Object> raw() {
        return java.util.Collections.unmodifiableMap(values);
    }

    public String ettn() {
        return String.valueOf(values.get("ettn"));
    }

    public @Nullable String belgeNumarasi() {
        return asString("belgeNumarasi");
    }

    public @Nullable String belgeTarihi() {
        return asString("belgeTarihi");
    }

    public @Nullable String aliciVknTckn() {
        return asString("aliciVknTckn");
    }

    public @Nullable String aliciUnvanAdSoyad() {
        return asString("aliciUnvanAdSoyad");
    }

    public @Nullable String belgeTuru() {
        return asString("belgeTuru");
    }

    public @Nullable String onayDurumu() {
        return asString("onayDurumu");
    }

    private @Nullable String asString(String key) {
        Object value = values.get(key);
        return value == null ? null : String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof InvoiceListItem other && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return values.toString();
    }
}
