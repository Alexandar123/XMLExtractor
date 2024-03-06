package com.extractor.xml.vendor;

import com.extractor.xml.ImageDownloader;
import com.extractor.xml.client.WebAPIClient;
import com.extractor.xml.model.EmallProduct;
import com.extractor.xml.model.EweProducts;
import com.extractor.xml.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.extractor.xml.util.ElementaUtil.checkManufactureData;
import static com.extractor.xml.util.ElementaUtil.constructUrlKey;
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
    private static final Map<String, EweProducts> cache = new HashMap<>();

    /**
     * Read Ewe products from link, formatt specifications and update Ewe products with skuId, categories, name and price
     *
     * @param file - Emall excel file with skuId, categories, name and price
     * @return EweProducts - Updated Ewe products ready for file writing
     */
    public EweProducts getEweProducts(MultipartFile file) {
        log.info("Fetch EWE products from URL: " + EWE_API_URL + " STARTED!");
        if (cache.containsKey(EWE_API_URL)) {
            log.info("Fetching from cache...");
            EweProducts cachedProducts = cache.get(EWE_API_URL);
            enrichSpecifications(cachedProducts);
            return updateEweProducts(cachedProducts, file);
        }

        // Fetch from API if not in cache
        log.info("Fetching from url. Not Found In Cache...");
        var eweProducts = (EweProducts) client.getProducts(EWE_API_URL, EweProducts.class);
        enrichSpecifications(eweProducts);

        // Cache the response
        cache.put(EWE_API_URL, eweProducts);

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
                if (Objects.equals(eweProduct.getId(), String.valueOf(emallProduct.getVendorId()))) {
                    eweProduct.setSkuId(emallProduct.getSkuId());
                    eweProduct.setFullCategoryPath(
                            setFullCategory(emallProduct.getNadredjenaKategorija(), emallProduct.getPrimarnaKategorija(), emallProduct.getSekundarnaKategorija()));
                    eweProduct.setAcName(emallProduct.getName());
                    eweProduct.setAnPrice(emallProduct.getMaloprodajnaCena());
                    break;
                }
            }
        }
        sanitizeCountry(eweProducts.getProducts());
        return eweProducts;
    }

    public static void sanitizeCountry(List<EweProducts.EweProduct> eweProducts) {
        Map<String, String> countryMappings = new HashMap<>();
        countryMappings.put("kina", "Kina");
        countryMappings.put("nemačka", "Nemačka");
        countryMappings.put("nemacka", "Nemačka");
        countryMappings.put("madarska", "Mađarska");
        countryMappings.put("usa", "Sjedinjene Države");
        countryMappings.put("sad", "Sjedinjene Države");
        countryMappings.put("vietnam", "Vijetnam");
        countryMappings.put("engleska", "Ujedinjeno Kraljevstvo");
        countryMappings.put("taiwan", "Tajvan");
        countryMappings.put("norveska", "Norveška");
        countryMappings.put("latvija", "Litvanija");
        countryMappings.put("Latvija", "Litvanija");
        countryMappings.put("V.Britanija", "Ujedinjeno Kraljevstvo");

        for (EweProducts.EweProduct eweProduct : eweProducts) {
            String originalCountry = eweProduct.getAcCountry().toLowerCase();
            String englishCountry = countryMappings.get(originalCountry);

            if (englishCountry != null) {
                eweProduct.setAcCountry(englishCountry);
            }
        }
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
                .filter(eweProduct -> !StringUtils.isEmpty(eweProduct.getFullCategoryPath()) && !eweProduct.getFullCategoryPath().contains("#N/A"))
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
                case "http_link": //??????????
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
                case "categories":
                    value = constructUrlKey(eweProduct.getFullCategoryPath());
                    break;
                case "url_key":
                    value = "";
                    break;
                case "name":
                    value = validate(eweProduct.getAcName());
                    break;
                case "description":
                    value = getDescription(eweProduct);
                    break;
                case "tip_proizvoda":
                    value = validate(eweProduct.getAcSubCategory());
                    break;
                case "tax_class_name":
                    value = "Taxable Goods";
                    break;
                case "visibility":
                    value = "Catalog, Search";
                    break;
                case "supplier_code":
                case "barkod":
                case "ean_code":
                    value = validate(eweProduct.getAcEan());
                    break;
                case "price":
                    value = String.valueOf(eweProduct.getAnPrice());
                    break;
                case "country_of_manufacture":
                    value = getCountry(eweProduct.getAcCountry());
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
                case "base_image":
                case "thumbnail_image":
                case "small_image":
                case "swatch_images":
                    value = getImage(eweProduct);
                    break;
                case "brend":
                    value = validate(eweProduct.getAcDept());
                    break;
            }
            rowData.add(value);
        }

        return rowData;
    }

    private static String getCountry(String acCountry) {
        if (acCountry == null || acCountry.isEmpty()) {
            return "Kina";
        }
        return validate(acCountry);
    }

    private static String getDescription(EweProducts.EweProduct eweProduct) {
        String productDescription = Optional.ofNullable(eweProduct.getAcProductDescription()).orElse("");
        String inlineSpecification = Optional.ofNullable(eweProduct.getAcInlineSpecification()).orElse("");
//        String seoDescription = eweProduct.getUrlImages().stream()
//                .findFirst()
//                .map(EweProducts.EweUrlImages::getAcSeoDescription)
//                .orElse("");

        return Stream.of(!StringUtils.isEmpty(productDescription) ? productDescription + "<br><br>" : "", inlineSpecification)
                .filter(desc -> !desc.isEmpty())
                .collect(Collectors.joining("\n\n"));
    }

    private static String getImages(EweProducts.EweProduct eweProduct) {
        return eweProduct.getUrlImages()
                .stream()
                .filter(it -> {
                    String image = it.getAcImage();
                    return image.endsWith(".jpg") || image.endsWith(".jpeg") || image.endsWith(".png")
                            || image.endsWith(".gif") || image.endsWith(".bmp") || image.endsWith(".svg");
                })
                .map(imageUrl -> {
                    ImageDownloader.downloadImage(imageUrl.getAcImage());
                    String[] parts = imageUrl.getAcImage().split("/");
                    return parts[parts.length - 1];
                })
                .collect(Collectors.joining(","));
    }

    private static String getImage(EweProducts.EweProduct eweProduct) {
        var urlImages = eweProduct.getUrlImages();
        if (urlImages != null && !urlImages.isEmpty()) {
            var image = urlImages.get(0).getAcImage();
            if (image.endsWith(".jpg") || image.endsWith(".jpeg") || image.endsWith(".png")
                    || image.endsWith(".gif") || image.endsWith(".bmp") || image.endsWith(".svg")) {
                String validated = validate(urlImages.get(0).getAcImage());
                String[] parts = validated.split("/");
                return parts[parts.length - 1];
            }
        }
        return null;
    }

//    private static void downloadImages(EweProducts.EweProduct eweProduct) {
//        eweProduct.getUrlImages().forEach(it -> {
//            String imageUrl = it.getAcImage();
//            if (imageUrl.endsWith(".jpg") || imageUrl.endsWith(".jpeg") || imageUrl.endsWith(".png")
//                    || imageUrl.endsWith(".gif") || imageUrl.endsWith(".bmp") || imageUrl.endsWith(".svg")) {
//                ImageDownloader.downloadImage(imageUrl);
//            }
//        });
//    }
}