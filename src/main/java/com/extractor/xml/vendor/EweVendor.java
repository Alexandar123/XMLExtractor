package com.extractor.xml.vendor;

import com.extractor.xml.client.WebAPIClient;
import com.extractor.xml.model.EweProducts;
import com.extractor.xml.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.extractor.xml.util.EmallUtil.getCSVHeaders;

@Service
@Slf4j
@AllArgsConstructor
public class EweVendor {

    private WebAPIClient client;
    private FileService fileService;
    private static final String EWE_API_URL = "http://apicatalog.ewe.rs:5001/api/?user=infoTWj&secretcode=96928&images=1&currency=rsd";

    public EweProducts getEweProducts() {
        log.info("Fetch EWE products from URL: " + EWE_API_URL + " STARTED!");
        return (EweProducts) client.getProducts(EWE_API_URL, EweProducts.class);
    }

    public static List<String> getCSVHeadersWithSpecifications(EweProducts products) {
        products.getProducts()
                .forEach(product -> formatInlineSpecification(product));
        var csvHeader = new ArrayList<>(getCSVHeaders());
        return csvHeader;
    }

    public static void formatInlineSpecification(EweProducts.EweProduct product) {
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
}