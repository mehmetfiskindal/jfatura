package io.jfatura;

import static org.assertj.core.api.Assertions.assertThat;

import io.jfatura.util.TurkishNumberConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("utils/number")
class TurkishNumberConverterTest {

    private static String convertNumber(long n) {
        return TurkishNumberConverter.convertNumber(n);
    }

    @Test
    @DisplayName("0 → SIFIR")
    void zero() {
        assertThat(convertNumber(0)).isEqualTo("SIFIR");
    }

    // ─── birler (1-9) ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("1 → BİR")
    void one() {
        assertThat(convertNumber(1)).isEqualTo("BİR");
    }

    @Test
    @DisplayName("5 → BEŞ")
    void five() {
        assertThat(convertNumber(5)).isEqualTo("BEŞ");
    }

    @Test
    @DisplayName("9 → DOKUZ")
    void nine() {
        assertThat(convertNumber(9)).isEqualTo("DOKUZ");
    }

    // ─── onlar (10-99) ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("10 → ON")
    void ten() {
        assertThat(convertNumber(10)).isEqualTo("ON");
    }

    @Test
    @DisplayName("11 → ON BİR")
    void eleven() {
        assertThat(convertNumber(11)).isEqualTo("ON BİR");
    }

    @Test
    @DisplayName("19 → ON DOKUZ")
    void nineteen() {
        assertThat(convertNumber(19)).isEqualTo("ON DOKUZ");
    }

    @Test
    @DisplayName("20 → YİRMİ")
    void twenty() {
        assertThat(convertNumber(20)).isEqualTo("YİRMİ");
    }

    @Test
    @DisplayName("40 → KIRK")
    void forty() {
        assertThat(convertNumber(40)).isEqualTo("KIRK");
    }

    @Test
    @DisplayName("42 → KIRK İKİ")
    void fortyTwo() {
        assertThat(convertNumber(42)).isEqualTo("KIRK İKİ");
    }

    @Test
    @DisplayName("99 → DOKSAN DOKUZ")
    void ninetyNine() {
        assertThat(convertNumber(99)).isEqualTo("DOKSAN DOKUZ");
    }

    // ─── yüzler (100-999) ──────────────────────────────────────────────────────

    @Test
    @DisplayName("100 → YÜZ (BİR YÜZ değil)")
    void hundred() {
        assertThat(convertNumber(100)).isEqualTo("YÜZ");
    }

    @Test
    @DisplayName("101 → YÜZ BİR")
    void hundredOne() {
        assertThat(convertNumber(101)).isEqualTo("YÜZ BİR");
    }

    @Test
    @DisplayName("110 → YÜZ ON")
    void hundredTen() {
        assertThat(convertNumber(110)).isEqualTo("YÜZ ON");
    }

    @Test
    @DisplayName("111 → YÜZ ON BİR")
    void hundredEleven() {
        assertThat(convertNumber(111)).isEqualTo("YÜZ ON BİR");
    }

    @Test
    @DisplayName("200 → İKİ YÜZ")
    void twoHundred() {
        assertThat(convertNumber(200)).isEqualTo("İKİ YÜZ");
    }

    @Test
    @DisplayName("500 → BEŞ YÜZ")
    void fiveHundred() {
        assertThat(convertNumber(500)).isEqualTo("BEŞ YÜZ");
    }

    @Test
    @DisplayName("999 → DOKUZ YÜZ DOKSAN DOKUZ")
    void nineHundredNinetyNine() {
        assertThat(convertNumber(999)).isEqualTo("DOKUZ YÜZ DOKSAN DOKUZ");
    }

    // ─── binler (1000-999999) ──────────────────────────────────────────────────

    @Test
    @DisplayName("1000 → BİN (BİR BİN değil)")
    void thousand() {
        assertThat(convertNumber(1000)).isEqualTo("BİN");
    }

    @Test
    @DisplayName("1001 → BİN BİR")
    void thousandOne() {
        assertThat(convertNumber(1001)).isEqualTo("BİN BİR");
    }

    @Test
    @DisplayName("1100 → BİN YÜZ")
    void thousandHundred() {
        assertThat(convertNumber(1100)).isEqualTo("BİN YÜZ");
    }

    @Test
    @DisplayName("2000 → İKİ BİN")
    void twoThousand() {
        assertThat(convertNumber(2000)).isEqualTo("İKİ BİN");
    }

    @Test
    @DisplayName("10000 → ON BİN")
    void tenThousand() {
        assertThat(convertNumber(10_000)).isEqualTo("ON BİN");
    }

    @Test
    @DisplayName("100000 → YÜZ BİN")
    void hundredThousand() {
        assertThat(convertNumber(100_000)).isEqualTo("YÜZ BİN");
    }

    @Test
    @DisplayName("999999 → DOKUZ YÜZ DOKSAN DOKUZ BİN DOKUZ YÜZ DOKSAN DOKUZ")
    void nineHundredNinetyNineThousand() {
        assertThat(convertNumber(999_999)).isEqualTo("DOKUZ YÜZ DOKSAN DOKUZ BİN DOKUZ YÜZ DOKSAN DOKUZ");
    }

    // ─── milyon / milyar ───────────────────────────────────────────────────────

    @Test
    @DisplayName("1000000 → BİR MİLYON")
    void oneMillion() {
        assertThat(convertNumber(1_000_000)).isEqualTo("BİR MİLYON");
    }

    @Test
    @DisplayName("2000000 → İKİ MİLYON")
    void twoMillion() {
        assertThat(convertNumber(2_000_000)).isEqualTo("İKİ MİLYON");
    }

    @Test
    @DisplayName("1500000 → BİR MİLYON BEŞ YÜZ BİN")
    void onePointFiveMillion() {
        assertThat(convertNumber(1_500_000)).isEqualTo("BİR MİLYON BEŞ YÜZ BİN");
    }

    @Test
    @DisplayName("1000000000 → BİR MİLYAR")
    void oneBillion() {
        assertThat(convertNumber(1_000_000_000L)).isEqualTo("BİR MİLYAR");
    }

    @Test
    @DisplayName("2500000000 → İKİ MİLYAR BEŞ YÜZ MİLYON")
    void twoPointFiveBillion() {
        assertThat(convertNumber(2_500_000_000L)).isEqualTo("İKİ MİLYAR BEŞ YÜZ MİLYON");
    }

    // ─── string girdi ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("string '50' ile number 50 aynı sonucu verir")
    void stringEqualsNumber() {
        assertThat(TurkishNumberConverter.convertNumber("50")).isEqualTo(TurkishNumberConverter.convertNumber(50));
    }

    @Test
    @DisplayName("string '1000' → BİN")
    void stringThousand() {
        assertThat(TurkishNumberConverter.convertNumber("1000")).isEqualTo("BİN");
    }

    @Test
    @DisplayName("string '0' → SIFIR")
    void stringZero() {
        assertThat(TurkishNumberConverter.convertNumber("0")).isEqualTo("SIFIR");
    }

    // ─── çıktı her zaman büyük harf ────────────────────────────────────────────

    @Test
    @DisplayName("42 sonucu uppercase")
    void uppercase42() {
        String result = convertNumber(42);
        assertThat(result).isEqualTo(result.toUpperCase(java.util.Locale.ROOT));
    }

    @Test
    @DisplayName("999999 sonucu uppercase")
    void uppercase999999() {
        String result = convertNumber(999_999);
        assertThat(result).isEqualTo(result.toUpperCase(java.util.Locale.ROOT));
    }

    // ─── convertPriceToText ────────────────────────────────────────────────────

    @Test
    @DisplayName("0 → SIFIR LIRA SIFIR KURUS")
    void priceZero() {
        assertThat(TurkishNumberConverter.convertPriceToText(0)).isEqualTo("SIFIR LIRA SIFIR KURUS");
    }

    @Test
    @DisplayName("1 → BİR LIRA SIFIR KURUS")
    void priceOne() {
        assertThat(TurkishNumberConverter.convertPriceToText(1)).isEqualTo("BİR LIRA SIFIR KURUS");
    }

    @Test
    @DisplayName("100 → YÜZ LIRA SIFIR KURUS")
    void priceHundred() {
        assertThat(TurkishNumberConverter.convertPriceToText(100)).isEqualTo("YÜZ LIRA SIFIR KURUS");
    }

    @Test
    @DisplayName("100.50 → YÜZ LIRA ELLİ KURUS")
    void priceHundredFifty() {
        assertThat(TurkishNumberConverter.convertPriceToText(100.5)).isEqualTo("YÜZ LIRA ELLİ KURUS");
    }

    @Test
    @DisplayName("100.05 → YÜZ LIRA BEŞ KURUS")
    void priceHundredFive() {
        assertThat(TurkishNumberConverter.convertPriceToText(100.05)).isEqualTo("YÜZ LIRA BEŞ KURUS");
    }

    @Test
    @DisplayName("0.01 → SIFIR LIRA BİR KURUS")
    void priceOneCent() {
        assertThat(TurkishNumberConverter.convertPriceToText(0.01)).isEqualTo("SIFIR LIRA BİR KURUS");
    }

    @Test
    @DisplayName("0.99 → SIFIR LIRA DOKSAN DOKUZ KURUS")
    void priceNinetyNineCents() {
        assertThat(TurkishNumberConverter.convertPriceToText(0.99)).isEqualTo("SIFIR LIRA DOKSAN DOKUZ KURUS");
    }

    @Test
    @DisplayName("1250.75 → BİN İKİ YÜZ ELLİ LIRA YETMİŞ BEŞ KURUS")
    void priceThousand() {
        assertThat(TurkishNumberConverter.convertPriceToText(1250.75))
                .isEqualTo("BİN İKİ YÜZ ELLİ LIRA YETMİŞ BEŞ KURUS");
    }

    @Test
    @DisplayName("120 ile 120.00 aynı sonucu verir")
    void priceIntVsDouble() {
        assertThat(TurkishNumberConverter.convertPriceToText(120))
                .isEqualTo(TurkishNumberConverter.convertPriceToText(120.0));
    }

    @Test
    @DisplayName("format: 'X LIRA Y KURUS' şeklinde bölünür")
    void priceFormat() {
        String result = TurkishNumberConverter.convertPriceToText(5.25);
        String[] parts = result.split(" LIRA ");
        assertThat(parts).hasSize(2);
        assertThat(parts[0]).isEqualTo("BEŞ");
        assertThat(parts[1]).isEqualTo("YİRMİ BEŞ KURUS");
    }
}
