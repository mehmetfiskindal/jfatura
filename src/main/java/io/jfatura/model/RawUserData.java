package io.jfatura.model;

import org.jspecify.annotations.Nullable;

/**
 * GİB {@code EARSIV_PORTAL_KULLANICI_BILGILERI_GETIR} yanıtının Türkçe
 * anahtarlı ham şekli.
 */
public record RawUserData(
        String vknTckn,
        String unvan,
        String ad,
        String soyad,
        @Nullable String sicilNo,
        @Nullable String mersisNo,
        @Nullable String vergiDairesi,
        @Nullable String cadde,
        @Nullable String apartmanAdi,
        @Nullable String apartmanNo,
        @Nullable String kapiNo,
        @Nullable String kasaba,
        @Nullable String ilce,
        @Nullable String il,
        @Nullable String postaKodu,
        @Nullable String ulke,
        @Nullable String telNo,
        @Nullable String faksNo,
        @Nullable String ePostaAdresi,
        @Nullable String webSitesiAdresi,
        @Nullable String isMerkezi) {}
