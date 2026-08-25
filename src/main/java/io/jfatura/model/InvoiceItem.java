package io.jfatura.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.jspecify.annotations.Nullable;

/**
 * Fatura kalemi. TS paketindeki tuhaf büyük harfli alan adları
 * ({@code VATRate}, {@code VATAmount}, {@code VATAmountOfTax}) korunmuştur.
 */
@JsonPropertyOrder({"name", "quantity", "unitType", "unitPrice", "price", "discountRate",
    "discountAmount", "discountReason", "discount", "VATRate", "VATAmount", "VATAmountOfTax", "taxRate"})
public record InvoiceItem(
        String name,
        @Nullable Integer quantity,
        @Nullable String unitType,
        @Nullable Double unitPrice,
        Double price,
        @Nullable Double discountRate,
        @Nullable Double discountAmount,
        @Nullable String discountReason,
        @Nullable String discount,
        @JsonProperty("VATRate") @Nullable Double vatRate,
        @JsonProperty("VATAmount") @Nullable Double vatAmount,
        @JsonProperty("VATAmountOfTax") @Nullable Double vatAmountOfTax,
        @Nullable Double taxRate) {

    @JsonPropertyOrder({"name", "quantity", "unitType", "unitPrice", "price", "discountRate",
        "discountAmount", "discountReason", "discount", "VATRate", "VATAmount", "VATAmountOfTax", "taxRate"})
    public static final class Builder {

        private String name;
        private Integer quantity;
        private String unitType;
        private Double unitPrice;
        private Double price;
        private Double discountRate;
        private Double discountAmount;
        private String discountReason;
        private String discount;
        private Double vatRate;
        private Double vatAmount;
        private Double vatAmountOfTax;
        private Double taxRate;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder quantity(Integer quantity) {
            this.quantity = quantity;
            return this;
        }

        public Builder unitType(String unitType) {
            this.unitType = unitType;
            return this;
        }

        public Builder unitPrice(Double unitPrice) {
            this.unitPrice = unitPrice;
            return this;
        }

        public Builder price(double price) {
            this.price = price;
            return this;
        }

        public Builder discountRate(Double discountRate) {
            this.discountRate = discountRate;
            return this;
        }

        public Builder discountAmount(Double discountAmount) {
            this.discountAmount = discountAmount;
            return this;
        }

        public Builder discountReason(String discountReason) {
            this.discountReason = discountReason;
            return this;
        }

        public Builder discount(String discount) {
            this.discount = discount;
            return this;
        }

        public Builder vatRate(Double vatRate) {
            this.vatRate = vatRate;
            return this;
        }

        public Builder vatAmount(Double vatAmount) {
            this.vatAmount = vatAmount;
            return this;
        }

        public Builder vatAmountOfTax(Double vatAmountOfTax) {
            this.vatAmountOfTax = vatAmountOfTax;
            return this;
        }

        public Builder taxRate(Double taxRate) {
            this.taxRate = taxRate;
            return this;
        }

        public InvoiceItem build() {
            if (name == null || price == null) {
                throw new IllegalStateException("InvoiceItem için name ve price zorunludur");
            }
            return new InvoiceItem(name, quantity, unitType, unitPrice, price, discountRate,
                    discountAmount, discountReason, discount, vatRate, vatAmount, vatAmountOfTax, taxRate);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
