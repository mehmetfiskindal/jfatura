package io.jfatura.model;

/** Tarih aralığı sorguları için parametre ({@code GG/AA/YYYY}). */
public record DateRange(String startDate, String endDate) {

    public static DateRange of(String startDate, String endDate) {
        return new DateRange(startDate, endDate);
    }
}
