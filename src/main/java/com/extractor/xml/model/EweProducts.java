package com.extractor.xml.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EweProducts extends Product {

    @JsonProperty("catalog")
    private List<EweProduct> products;

    @Data
    public static class EweProduct {
        private int skuId;
        private int anProductKey;//ID artikla
        private String acProduct;//Sifra artikla
        private String acVideoLink;
        private String acName;
        private int anAction;
        private int anImportant;
        private int anDeptKey;
        private String acDept;
        private String acInlineSpecification;
        private int anNew;
        private int anAdvice;
        private int anBestBuy;
        private String acEan;
        private String adRelChanged;
        private double anPrice;
        private int anStock;
        private int anOldPrice;
        private int anReserved;
        private int anRetailPrice;
        private int anRecommendedRetailPrice;
        private int anPromoPrice;
        private int isPromotion;
        private int anStockArrival;
        private int anDiscount;
        private int anRelatedDiscount;
        private String acCountry;
        private String acSupplier;
        private int anMainCategoryKey;
        private int anCategoryKey;
        private int anSubCategoryKey;
        private String acMainCategory;
        private String acCategory;
        private String acSubCategory;
        private int isWish;
        private int isActive;
        private int isRelatedSale;
        private int anPaymentAdvance;
        private String acProductDescription;
        private List<EweUrlImages> urlImages;
        @JsonProperty
        private String fullCategoryPath;
    }

    @Data
    public static class EweUrlImages {
        private String acImage;
        private String acThumbnail;
        private String acType;
        private String acSeoTitle;
        private String acSeoDescription;
    }
}