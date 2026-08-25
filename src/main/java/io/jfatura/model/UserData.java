package io.jfatura.model;

import org.jspecify.annotations.Nullable;

/**
 * Portal kullanıcı bilgileri (normalize İngilizce anahtarlı public API şekli).
 */
public record UserData(
        String taxIDOrTRID,
        String title,
        String name,
        String surname,
        @Nullable String registryNo,
        @Nullable String mersisNo,
        @Nullable String taxOffice,
        @Nullable String fullAddress,
        @Nullable String buildingName,
        @Nullable String buildingNumber,
        @Nullable String doorNumber,
        @Nullable String town,
        @Nullable String district,
        @Nullable String city,
        @Nullable String zipCode,
        @Nullable String country,
        @Nullable String phoneNumber,
        @Nullable String faxNumber,
        @Nullable String email,
        @Nullable String webSite,
        @Nullable String businessCenter) {

    public static Builder builder(String taxIDOrTRID, String title, String name, String surname) {
        return new Builder(taxIDOrTRID, title, name, surname);
    }

    public static final class Builder {

        private final String taxIDOrTRID;
        private final String title;
        private final String name;
        private final String surname;
        private String registryNo;
        private String mersisNo;
        private String taxOffice;
        private String fullAddress;
        private String buildingName;
        private String buildingNumber;
        private String doorNumber;
        private String town;
        private String district;
        private String city;
        private String zipCode;
        private String country;
        private String phoneNumber;
        private String faxNumber;
        private String email;
        private String webSite;
        private String businessCenter;

        private Builder(String taxIDOrTRID, String title, String name, String surname) {
            this.taxIDOrTRID = taxIDOrTRID;
            this.title = title;
            this.name = name;
            this.surname = surname;
        }

        public Builder registryNo(String v) {
            this.registryNo = v;
            return this;
        }

        public Builder mersisNo(String v) {
            this.mersisNo = v;
            return this;
        }

        public Builder taxOffice(String v) {
            this.taxOffice = v;
            return this;
        }

        public Builder fullAddress(String v) {
            this.fullAddress = v;
            return this;
        }

        public Builder buildingName(String v) {
            this.buildingName = v;
            return this;
        }

        public Builder buildingNumber(String v) {
            this.buildingNumber = v;
            return this;
        }

        public Builder doorNumber(String v) {
            this.doorNumber = v;
            return this;
        }

        public Builder town(String v) {
            this.town = v;
            return this;
        }

        public Builder district(String v) {
            this.district = v;
            return this;
        }

        public Builder city(String v) {
            this.city = v;
            return this;
        }

        public Builder zipCode(String v) {
            this.zipCode = v;
            return this;
        }

        public Builder country(String v) {
            this.country = v;
            return this;
        }

        public Builder phoneNumber(String v) {
            this.phoneNumber = v;
            return this;
        }

        public Builder faxNumber(String v) {
            this.faxNumber = v;
            return this;
        }

        public Builder email(String v) {
            this.email = v;
            return this;
        }

        public Builder webSite(String v) {
            this.webSite = v;
            return this;
        }

        public Builder businessCenter(String v) {
            this.businessCenter = v;
            return this;
        }

        public UserData build() {
            return new UserData(
                    taxIDOrTRID,
                    title,
                    name,
                    surname,
                    registryNo,
                    mersisNo,
                    taxOffice,
                    fullAddress,
                    buildingName,
                    buildingNumber,
                    doorNumber,
                    town,
                    district,
                    city,
                    zipCode,
                    country,
                    phoneNumber,
                    faxNumber,
                    email,
                    webSite,
                    businessCenter);
        }
    }
}
