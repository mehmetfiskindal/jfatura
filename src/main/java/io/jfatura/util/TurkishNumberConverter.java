package io.jfatura.util;

import java.util.Locale;
import java.util.StringJoiner;

/** Sayıları büyük harfli Türkçe metne çevirir (faturanın {@code not} alanı için). */
public final class TurkishNumberConverter {

    private static final String[] ONES = {"", "BİR", "İKİ", "ÜÇ", "DÖRT", "BEŞ", "ALTI", "YEDİ", "SEKİZ", "DOKUZ"};
    private static final String[] TENS = {
        "", "ON", "YİRMİ", "OTUZ", "KIRK", "ELLİ", "ALTMIŞ", "YETMİŞ", "SEKSEN", "DOKSAN"
    };

    private TurkishNumberConverter() {}

    public static String convertNumber(long number) {
        return toTurkish(number);
    }

    public static String convertNumber(String number) {
        return toTurkish(Long.parseLong(number.trim()));
    }

    public static String convertPriceToText(double price) {
        String fixed = String.format(Locale.ROOT, "%.2f", price);
        String[] parts = fixed.split("\\.");
        String main = parts[0];
        String sub = parts[1];
        if (sub.equals("00")) {
            sub = "0";
        }
        return convertNumber(main) + " LIRA " + convertNumber(sub) + " KURUS";
    }

    private static String convertHundreds(long n) {
        StringJoiner parts = new StringJoiner(" ");
        long h = n / 100;
        long t = (n % 100) / 10;
        long o = n % 10;
        if (h == 1) {
            parts.add("YÜZ");
        } else if (h > 1) {
            parts.add(ONES[(int) h] + " YÜZ");
        }
        if (t > 0) {
            parts.add(TENS[(int) t]);
        }
        if (o > 0) {
            parts.add(ONES[(int) o]);
        }
        return parts.toString();
    }

    private static String toTurkish(long n) {
        if (n == 0) {
            return "SIFIR";
        }
        StringJoiner parts = new StringJoiner(" ");
        long milyar = n / 1_000_000_000L;
        long milyon = (n % 1_000_000_000L) / 1_000_000L;
        long bin = (n % 1_000_000L) / 1_000L;
        long rest = n % 1_000L;
        if (milyar > 0) {
            parts.add(convertHundreds(milyar) + " MİLYAR");
        }
        if (milyon > 0) {
            parts.add(convertHundreds(milyon) + " MİLYON");
        }
        if (bin == 1) {
            parts.add("BİN");
        } else if (bin > 1) {
            parts.add(convertHundreds(bin) + " BİN");
        }
        if (rest > 0) {
            parts.add(convertHundreds(rest));
        }
        return parts.toString();
    }
}
