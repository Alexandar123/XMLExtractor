package com.extractor.xml.util;

import com.extractor.xml.model.ElementaProduct;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.extractor.xml.util.ElementaUtil.checkManufactureData;
import static com.extractor.xml.util.ElementaUtil.constructUrlKey;
import static com.extractor.xml.util.ElementaUtil.getImages;
import static com.extractor.xml.util.ElementaUtil.validate;

@UtilityClass
public class EmallUtil {

    public static List<String> getCSVHeaders() {
        // Define the CSV header for E-mall file
        return Arrays.asList(
                "sku", "store_view_code", "attribute_set_code", "product_type", "categories", "product_websites",
                "name", "description", "short_description", "weight", "product_online", "tax_class_name",
                "visibility", "price", "special_price", "special_price_from_date", "special_price_to_date", "url_key",
                "meta_title", "meta_keywords", "meta_description", "created_at", "updated_at", "new_from_date",
                "new_to_date", "display_product_options_in", "map_price", "msrp_price", "map_enabled", "gift_message_available",
                "custom_design", "custom_design_from", "custom_design_to", "custom_layout_update", "page_layout", "product_options_container",
                "msrp_display_actual_price_type", "country_of_manufacture", "additional_attributes", "qty", "out_of_stock_qty", "use_config_min_qty",
                "is_qty_decimal", "allow_backorders", "use_config_backorders", "min_cart_qty", "use_config_min_sale_qty", "max_cart_qty",
                "use_config_max_sale_qty", "is_in_stock", "notify_on_stock_below", "use_config_notify_stock_qty", "manage_stock", "use_config_manage_stock",
                "use_config_qty_increments", "qty_increments", "use_config_enable_qty_inc", "enable_qty_increments", "is_decimal_divided", "website_id",
                "deferred_stock_update", "use_config_deferred_stock_update", "related_skus", "crosssell_skus", "upsell_skus", "hide_from_product_page",
                "custom_options", "associated_skus", "ean_code", "manufacturer", "commodity_group_code", "supplier",
                "supplier_code", "sector", "http_link", "width", "height", "depth", "dimensions",
                "additional_images", "base", "thumbnail", "small_image", "swatch_images",
                "tip_proizvoda", "brend", "websites", "uputstvo", "ocena_usaglasenosti", "barkod"
        );
    }

    public static List<String> addDataForEmallHeader(ElementaProduct elementaXMLProduct, List<String> headers) {
        List<String> rowData = new ArrayList<>();

        for (String header : headers) {
            String value = "";
            switch (header) {
                case "sku":
                    value = String.valueOf(elementaXMLProduct.getSkuId());
                    break;
                case "store_view_code":
                case "custom_layout_update":
                case "page_layout":
                case "product_options_container":
                case "custom_design_to":
                case "short_description":
                case "weight":
                case "special_price":
                case "special_price_to_date":
                case "special_price_from_date":
                case "meta_title":
                case "meta_keywords":
                case "meta_description":
                case "created_at":
                case "updated_at":
                case "new_from_date":
                case "new_to_date":
                case "display_product_options_in":
                case "map_price":
                case "msrp_price":
                case "map_enabled":
                case "gift_message_available":
                case "custom_design":
                case "custom_design_from":
                case "msrp_display_actual_price_type":
                case "additional_attributes":
                case "related_skus":
                case "crosssell_skus":
                case "upsell_skus":
                case "hide_from_product_page":
                case "custom_options":
                case "associated_skus":
                case "ean_code":
                case "commodity_group_code":
                case "sector":
                case "width":
                case "height":
                case "depth":
                case "dimensions":
                case "notify_on_stock_below":
                    value = "";
                    break;
                case "attribute_set_code":
                    value = "Default";
                    break;
                case "product_type":
                    value = "simple";
                    break;
                case "categories":
                    value = constructUrlKey(elementaXMLProduct.getFullCategoryPath());
                    break;
                case "url_key":
//                    value = constructUrlKey(elementaXMLProduct.getFullCategoryPath());
                    value = "";
                    break;
                case "product_websites":
                case "websites":
                    value = "base";
                    break;
                case "name":
                    value = validate(elementaXMLProduct.getNaziv());
                    break;
                case "description":
                    value = validate(elementaXMLProduct.getOpis());
                    break;
                case "out_of_stock_qty":
                case "is_qty_decimal":
                case "enable_qty_increments":
                case "is_decimal_divided":
                case "allow_backorders":
                case "use_config_min_sale_qty":
                case "deferred_stock_update":
                case "qty_increments":
                    value = "0";
                    break;
                case "product_online":
                case "min_cart_qty":
                case "use_config_min_qty":
                case "is_in_stock":
                case "use_config_notify_stock_qty":
                case "manage_stock":
                case "use_config_manage_stock":
                case "use_config_enable_qty_inc":
                case "use_config_backorders":
                case "use_config_max_sale_qty":
                case "use_config_qty_increments":
                case "website_id":
                case "use_config_deferred_stock_update":
                    value = "1";
                    break;
                case "tax_class_name":
                    value = "Taxable Goods";
                    break;
                case "visibility":
                    value = "Catalog, Search";
                    break;
                case "price":
                    value = String.valueOf(elementaXMLProduct.getCena());
                    break;
                case "country_of_manufacture":
                    value = validate(elementaXMLProduct.getZemljaPorekla());
                    break;
                case "qty":
                    value = String.valueOf(elementaXMLProduct.getLagerVp());
                    break;
                case "max_cart_qty":
                    value = "0";
                    break;
                case "manufacturer":
                    value = checkManufactureData(elementaXMLProduct.getProizvodjac());
                    break;
                case "supplier":
                    value = validate(elementaXMLProduct.getUvoznik());
                    break;
                case "supplier_code":
                    value = validate(elementaXMLProduct.getSifraDobavljaca());
                    break;
                case "http_link":
                    value = elementaXMLProduct.getLink();
                    break;
                case "additional_images":
                    value = getImages(elementaXMLProduct);
                    break;
                case "base":
                case "thumbnail":
                case "small_image":
                case "swatch_images":
                    value = validate(elementaXMLProduct.getSlika());
                    break;
                case "tip_proizvoda":
                    value = validate(elementaXMLProduct.getTipProizvoda());
                    break;
                case "brend":
                    value = validate(elementaXMLProduct.getProizvodjac());
                    break;
                case "ocena_usaglasenosti":
                    value = validate(elementaXMLProduct.getOcenaUsaglasenosti());
                    break;
                case "uputstvo":
                    value = validate(elementaXMLProduct.getUputstvo());
                    break;
                case "barkod":
                    value = validate(elementaXMLProduct.getBarkod());
                    break;
            }

            if (elementaXMLProduct.getSpecifications().containsKey(header)) {
                value = elementaXMLProduct.getSpecifications().get(header);
            }
            rowData.add(value);
        }

        return rowData;
    }

    public List<List<String>> getCSVData(List<ElementaProduct> elementaXMLProducts, List<String> csvHeader, int skip, int limit) {
        if (limit == -1) {
            return elementaXMLProducts.stream()
                    .skip(skip)
                    .filter(elementaXMLProduct -> elementaXMLProduct.getFullCategoryPath() != null && !elementaXMLProduct.getFullCategoryPath().contains("#N/A"))
                    .map(item -> addDataForEmallHeader(item, csvHeader))
                    .collect(Collectors.toList());
        }
        return elementaXMLProducts.stream()
                .filter(elementaXMLProduct -> elementaXMLProduct.getFullCategoryPath() != null && !elementaXMLProduct.getFullCategoryPath().contains("#N/A"))
                .skip(skip)
                .limit(limit)
                .map(item -> addDataForEmallHeader(item, csvHeader))
                .collect(Collectors.toList());
    }
}
