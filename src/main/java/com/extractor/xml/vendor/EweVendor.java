package com.extractor.xml.vendor;

import com.extractor.xml.client.WebAPIClient;
import com.extractor.xml.model.EmallProduct;
import com.extractor.xml.model.EweProducts;
import com.extractor.xml.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.extractor.xml.util.ElementaUtil.checkManufactureData;
import static com.extractor.xml.util.ElementaUtil.setFullCategory;
import static com.extractor.xml.util.ElementaUtil.validate;
import static com.extractor.xml.util.EmallUtil.getCSVHeaders;

@Service
@Slf4j
@AllArgsConstructor
public class EweVendor {

    private WebAPIClient client;
    private FileService fileService;
    private static final String EWE_API_URL = "http://apicatalog.ewe.rs:5001/api/?user=infoTWj&secretcode=96928&images=1&currency=rsd";

    /**
     * Read Ewe products from link, formatt specifications and update Ewe products with skuId, categories, name and price
     *
     * @param file - Emall excel file with skuId, categories, name and price
     * @return EweProducts - Updated Ewe products ready for file writing
     */
    public EweProducts getEweProducts(MultipartFile file) {
        log.info("Fetch EWE products from URL: " + EWE_API_URL + " STARTED!");
        var eweProducts = (EweProducts) client.getProducts(EWE_API_URL, EweProducts.class);
        enrichSpecifications(eweProducts);
        return updateEweProducts(eweProducts, file);
    }


    private void enrichSpecifications(EweProducts products) {
        products.getProducts()
                .forEach(this::formatInlineSpecification);
    }

    public EweProducts updateEweProducts(EweProducts eweProducts, MultipartFile file) {
        var products = fileService.readProductDataFromExcel(file);

        for (int i = 0; i < eweProducts.getProducts().size(); i++) {
            EweProducts.EweProduct eweProduct = eweProducts.getProducts().get(i);
            for (EmallProduct emallProduct : products) {
                if (eweProduct.getAnProductKey() == emallProduct.getVendorId()) {
                    eweProduct.setSkuId(emallProduct.getSkuId());
                    eweProduct.setFullCategoryPath(
                            setFullCategory(emallProduct.getNadredjenaKategorija(), emallProduct.getPrimarnaKategorija(), emallProduct.getSekundarnaKategorija()));
                    eweProduct.setAcName(emallProduct.getName());
                    eweProduct.setAnPrice(emallProduct.getMaloprodajnaCena());
                    break;
                }
            }
        }
        return eweProducts;
    }

    private void formatInlineSpecification(EweProducts.EweProduct product) {
        StringBuilder formatted = new StringBuilder();
        String[] sections = product.getAcInlineSpecification().split("\\s{2,}");
        for (String section : sections) {
            String[] lines = section.split("\\s{2,}");
            for (String line : lines) {
                formatted.append(line.trim()).append("\n");
            }
            formatted.append("\n");
        }
        product.setAcInlineSpecification(formatted.toString());
    }

    /**
     * EWE Products
     */
    public List<List<String>> getAndMapToCSVData(EweProducts eweProducts, int skip, int limit) {
        var csvHeader = getCSVHeaders();
        Stream<EweProducts.EweProduct> productsStream = eweProducts.getProducts().stream()
                .skip(skip);

        if (limit != -1) {
            productsStream = productsStream.limit(limit);
        }

        return productsStream.map(item -> addDataForEmallHeader(item, csvHeader))
                .collect(Collectors.toList());
    }

    public static List<String> addDataForEmallHeader(EweProducts.EweProduct eweProduct, List<String> headers) {
        List<String> rowData = new ArrayList<>();

        for (String header : headers) {
            String value = "";
            switch (header) {
                case "sku":
                    value = String.valueOf(eweProduct.getSkuId());
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
                case "commodity_group_code":
                case "sector":
                case "width":
                case "height":
                case "depth":
                case "dimensions":
                case "ocena_usaglasenosti":
                case "uputstvo":
                case "barkod":
                case "supplier_code": //??????????
                case "http_link": //??????????
                case "categories": //??????????
                case "tip_proizvoda": //??????????
                case "notify_on_stock_below":
                    value = "";
                    break;
                case "product_websites":
                case "websites":
                    value = "base";
                    break;
                case "attribute_set_code":
                    value = "Default";
                    break;
                case "product_type":
                    value = "simple";
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
//                case "categories":
//                    value = constructUrlKey(eweProduct.getFullCategoryPath());
//                    break;
                case "url_key":
                    value = "";
                    break;
                case "name":
                    value = validate(eweProduct.getAcName());
                    break;
                case "description":
                    value = validate(eweProduct.getAcProductDescription()) + "\n\n" + eweProduct.getAcInlineSpecification();
                    break;
                case "tax_class_name":
                    value = "Taxable Goods";
                    break;
                case "visibility":
                    value = "Catalog, Search";
                    break;
                case "ean_code":
                    value = validate(eweProduct.getAcEan());
                    break;
                case "price":
                    value = String.valueOf(eweProduct.getAnPrice());
                    break;
                case "country_of_manufacture":
                    value = validate(eweProduct.getAcCountry());
                    break;
                case "qty":
                    value = String.valueOf(eweProduct.getAnStock());
                    break;
                case "max_cart_qty":
                    value = "0";
                    break;
                case "manufacturer":
                    value = checkManufactureData(eweProduct.getAcDept());
                    break;
                case "supplier":
                    value = validate(eweProduct.getAcSupplier());
                    break;
                case "additional_images":
                    value = getImages(eweProduct);
                    break;
                case "base":
                case "thumbnail":
                case "small_image":
                case "swatch_images":
                    value = (eweProduct.getUrlImages() != null && !eweProduct.getUrlImages().isEmpty()) ? validate(eweProduct.getUrlImages().get(0).getAcImage()) : null;
                    break;
                case "brend":
                    value = validate(eweProduct.getAcDept());
                    break;
            }
            rowData.add(value);
        }

        return rowData;
    }

    private static String getImages(EweProducts.EweProduct eweProduct) {
        List<String> imageUrls = new ArrayList<>();
        eweProduct.getUrlImages()
                .forEach(it -> imageUrls.add(it.getAcImage()));

        return String.join(",", imageUrls);
    }
}