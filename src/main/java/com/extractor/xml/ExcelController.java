package com.extractor.xml;

import com.extractor.xml.model.ElementaProduct;
import com.extractor.xml.model.ElementaXMLProduct;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.extractor.xml.util.ElementaUtil.checkManufactureData;
import static com.extractor.xml.util.ElementaUtil.constructUrlKey;
import static com.extractor.xml.util.ElementaUtil.getCellValueAsDouble;
import static com.extractor.xml.util.ElementaUtil.getCellValueAsInteger;
import static com.extractor.xml.util.ElementaUtil.getCellValueAsString;
import static com.extractor.xml.util.ElementaUtil.getImages;
import static com.extractor.xml.util.ElementaUtil.replaceDiacritics;
import static com.extractor.xml.util.ElementaUtil.sanitazeAttributeName;
import static com.extractor.xml.util.ElementaUtil.sanitizeCategoryFullPath;
import static com.extractor.xml.util.ElementaUtil.sanitizeCountry;
import static com.extractor.xml.util.ElementaUtil.setFullCategory;
import static com.extractor.xml.util.ElementaUtil.validate;

@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ExcelController {

    @PostMapping(value = "/elementa/generate-csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Convert products from elementa format into E-mall Magento specific csv file.", description =
            "Obavezna pravila: E-mall fajl mora biti pripremljen sa <b>SKU</b>, <b>ElementaId</b> i <b>category putanjama</b> prema kojima ce se Elementa proizvodi uvezati u sistem i to sledecim redom: \n" +
                    "1. SKU \n 2. ElementaId \n 3. Category path \n" +
                    "<br><br><b>Elementa fajl treba da bude pripremljen bez naslova(header-a)</b>")
    public ResponseEntity<ByteArrayResource> generateCsvWithResponse(@RequestParam("emall_with_SKU") MultipartFile emallWithSKU,
                                                                     @RequestParam("elementa_XML_file") MultipartFile elementaXMLFile,
                                                                     @Parameter(description = "Naziv za CSV fajl koji ce biti generisan u E-mall Magento formatu. Default naziv je <b>products-trenutniDatumIVreme.csv</b>") @RequestParam(value = "fileName", required = false, defaultValue = "products") String fileName,
                                                                     @Parameter(description = "Skip vrednost se odnosi na broj elemenata koji ce se preskociti u obradi pocevsi od prvog elementa I.E. ako je skip 3 preskacu se prva tri elementa iz fajla. Default vrednost 0 znaci da se svi elementi obradjuju") @RequestParam(value = "skip", required = false, defaultValue = "0") Integer skip,
                                                                     @Parameter(description = "Limit vrednost se odnosi na broj elemenata koje je potrebno obraditi. I.E. ako je limit 3 obradice se iz celog fajla samo tri elementa. Default vrednost -1 znaci da nema limita.") @RequestParam(value = "limit", required = false, defaultValue = "-1") Integer limit) {
        log.info("CSV generation started....");
        List<ElementaXMLProduct> elementaXMLProductList = getElementaXMLProducts(elementaXMLFile);
        List<ElementaXMLProduct> elementaXMLProducts = updateElementaData(elementaXMLProductList, emallWithSKU);

        List<String> csvHeader = getCSVHeaders(elementaXMLProducts);
        List<List<String>> csvData = getCSVData(elementaXMLProducts, csvHeader, skip, limit);

        log.info("csvData = " + csvData.get(0));
        log.info("csvData size = " + csvData.size());

        String fullFileName = fileName + "-" + LocalDateTime.now() + ".csv";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BufferedWriter fileWriter = new BufferedWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {

            fileWriter.write(csvHeader.stream()
                    .map(header -> "\"" + header + "\"")
                    .collect(Collectors.joining(",")));
            fileWriter.newLine();

            for (List<String> data : csvData) {
                List<String> sanitizedData = data.stream()
                        .map(value -> "\"" + value.replace("\"", "'") + "\"")
                        .collect(Collectors.toList());
                fileWriter.write(String.join(",", sanitizedData));
                fileWriter.newLine();
            }

            byte[] fileContent = baos.toByteArray();

            return ResponseEntity.ok()
                    .headers(createHeaders(fullFileName))
                    .contentLength(fileContent.length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(new ByteArrayResource(fileContent));

        } catch (IOException e) {
            log.error("Error during generating CSV: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

//
//    @PostMapping(value = "/elementa/generate-csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    @Operation(summary = "Convert products from elementa format into E-mall Magento specific csv file.", description =
//            "Obavezna pravila: E-mall fajl mora biti pripremljen sa <b>SKU</b>, <b>ElementaId</b> i <b>category putanjama</b> prema kojima ce se Elementa proizvodi uvezati u sistem i to sledecim redom: \n" +
//                    "1. SKU \n 2. ElementaId \n 3. Category path \n" +
//                    "<br><br><b>Elementa fajl treba da bude pripremljen bez naslova(header-a)</b>")
//    public void generateCsv(@RequestParam("emall_with_SKU") MultipartFile emallWithSKU,
//                            @RequestParam("elementa_XML_file") MultipartFile elementaXMLFile,
//                            @Parameter(description = "Naziv za CSV fajl koji ce biti generisan u E-mall Magento formatu. Default naziv je <b>products-trenutniDatumIVreme.csv</b>") @RequestParam(value = "fileName", required = false, defaultValue = "products") String fileName,
//                            @Parameter(description = "Skip vrednost se odnosi na broj elemenata koji ce se preskociti u obradi pocevsi od prvog elementa I.E. ako je skip 3 preskacu se prva tri elementa iz fajla. Default vrednost 0 znaci da se svi elementi obradjuju") @RequestParam(value = "skip", required = false, defaultValue = "0") Integer skip,
//                            @Parameter(description = "Limit vrednost se odnosi na broj elemenata koje je potrebno obraditi. I.E. ako je limit 3 obradice se iz celog fajla samo tri elementa. Default vrednost -1 znaci da nema limita.") @RequestParam(value = "limit", required = false, defaultValue = "-1") Integer limit) {
//        List<ElementaXMLProduct> elementaXMLProductList = getElementaXMLProducts(elementaXMLFile);
//        List<ElementaXMLProduct> elementaXMLProducts = updateElementaData(elementaXMLProductList, emallWithSKU);
//
//        List<String> csvHeader = getCSVHeaders(elementaXMLProducts);
//        List<List<String>> csvData = getCSVData(elementaXMLProducts, csvHeader, skip, limit);
//
//        log.info("csvData = " + csvData.get(0));
//        log.info("csvData size = " + csvData.size());
//
//        String fullFileName = fileName + "-" + LocalDateTime.now() + ".csv";
//        Path filePath = Path.of(Objects.requireNonNull(getDesktopPath()), fullFileName);
//        try (BufferedWriter fileWriter = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
//            // Write the header to the CSV file
//            fileWriter.write(csvHeader.stream()
//                    .map(header -> "\"" + header + "\"")
//                    .collect(Collectors.joining(",")));
//            fileWriter.newLine();
//
//            for (List<String> data : csvData) {
//                List<String> sanitizedData = data.stream()
//                        .map(value -> "\"" + value.replace("\"", "'") + "\"")
//                        .collect(Collectors.toList());
//                fileWriter.write(String.join(",", sanitizedData));
//                fileWriter.newLine();
//            }
//        } catch (IOException e) {
//            log.error("Error during generating csv: " + e.getMessage());
//        }
//    }

    public List<List<String>> getCSVData(List<ElementaXMLProduct> elementaXMLProducts, List<String> csvHeader, int skip, int limit) {
        if (limit == -1) {
            return elementaXMLProducts.stream()
                    .skip(skip)
                    .filter(elementaXMLProduct -> elementaXMLProduct.getSkuId() >= 129619 && elementaXMLProduct.getFullCategoryPath() != null && !elementaXMLProduct.getFullCategoryPath().contains("#N/A"))
                    .map(item -> mapData(item, csvHeader))
                    .collect(Collectors.toList());
        }
        return elementaXMLProducts.stream()
                .skip(skip)
                .limit(limit)
                .filter(elementaXMLProduct -> elementaXMLProduct.getFullCategoryPath() != null && !elementaXMLProduct.getFullCategoryPath().contains("#N/A"))
                .map(item -> mapData(item, csvHeader))
                .collect(Collectors.toList());
    }

    private static List<String> getCSVHeaders(List<ElementaXMLProduct> products) {
        List<String> csvHeader = new ArrayList<>(getCSVHeader());

        for (ElementaXMLProduct product : products) {
            Map<String, String> specifications = product.getSpecifications();

            for (String key : specifications.keySet()) {
                if (!csvHeader.contains(key)) {
                    csvHeader.add(key.toLowerCase());
                }
            }
        }
        csvHeader = new ArrayList<>(new LinkedHashSet<>(csvHeader));
        return csvHeader;
    }

    private static List<String> getCSVHeader() {
        // Define the CSV header
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

    private List<String> mapData(ElementaXMLProduct elementaXMLProduct, List<String> headers) {
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

    public List<ElementaProduct> readExcelData(MultipartFile file) {
        List<ElementaProduct> elementaProducts = new ArrayList<>();
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0); // Assuming the data is in the first sheet
            for (Row row : sheet) {
                if (row.getPhysicalNumberOfCells() >= 2) {
                    elementaProducts.add(
                            ElementaProduct.builder()
                                    .skuId(getCellValueAsInteger(row.getCell(0)))
                                    .elementaId(getCellValueAsInteger(row.getCell(5)))
                                    .name(getCellValueAsString(row.getCell(1)))
                                    .sifraDobavljaca(getCellValueAsString(row.getCell(2)))
                                    .uvoznik(getCellValueAsString(row.getCell(22)))
                                    .zemljaPorekla(getCellValueAsString(row.getCell(24)))
                                    .nadredjenaKategorija(getCellValueAsString(row.getCell(7)))
                                    .primarnaKategorija(getCellValueAsString(row.getCell(9)))
                                    .sekundarnaKategorija(getCellValueAsString(row.getCell(11)))
                                    .maloprodajnaCena(getCellValueAsDouble(row.getCell(31)))
                                    .nabavnaCena(getCellValueAsDouble(row.getCell(32)))
                                    .rabat(getCellValueAsDouble(row.getCell(33)))
                                    .build());
                }
                sanitizeCategoryFullPath(elementaProducts);
            }
//            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            workbook.close();
        } catch (IOException e) {
            log.error("Error during read data from excel: " + e.getMessage());
        }
        return elementaProducts;
    }

    public static List<ElementaXMLProduct> getElementaXMLProducts(MultipartFile elementaXMLFile) {
        List<ElementaXMLProduct> elementaXMLProductList = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(elementaXMLFile.getInputStream());

            Element root = document.getDocumentElement();
            NodeList productListNodes = root.getElementsByTagName("product");

            for (int productIndex = 0; productIndex < productListNodes.getLength(); productIndex++) {
                Element productElement = (Element) productListNodes.item(productIndex);
                ElementaXMLProduct elementaXMLProduct = createProductFromElement(productElement);
                elementaXMLProductList.add(elementaXMLProduct);
            }

        } catch (Exception e) {
            log.error("Error during read data from Elementa xml file: " + e.getMessage());
        }

        sanitizeCountry(elementaXMLProductList);
        return elementaXMLProductList;
    }

    private static ElementaXMLProduct createProductFromElement(Element productElement) {
        ElementaXMLProduct elementaXMLProduct = new ElementaXMLProduct();
        elementaXMLProduct.setTipProizvoda(getTipFromSpecifications(productElement));
        elementaXMLProduct.setSpecifications(getSpecifications(productElement));

        NodeList productChildren = productElement.getChildNodes();
        for (int i = 0; i < productChildren.getLength(); i++) {
            Node node = productChildren.item(i);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                processProductNode(elementaXMLProduct, (Element) node);
            }
        }

        return elementaXMLProduct;
    }

    private static void processProductNode(ElementaXMLProduct elementaXMLProduct, Element element) {
        String nodeName = element.getNodeName();
        String nodeValue = element.getTextContent();
        switch (nodeName) {
            case "numId":
                elementaXMLProduct.setElementaId(Integer.parseInt(nodeValue));
                break;
            case "naziv":
                elementaXMLProduct.setNaziv(nodeValue);
                break;
            case "opis":
                elementaXMLProduct.setOpis(nodeValue);
                break;
            case "uvoznik":
                elementaXMLProduct.setUvoznik(nodeValue);
                break;
            case "proizvodjac":
                elementaXMLProduct.setProizvodjac(nodeValue);
                break;
            case "zemljaPorekla":
                elementaXMLProduct.setZemljaPorekla(nodeValue);
                break;
            case "zemljaUvoza":
                elementaXMLProduct.setZemljaUvoza(nodeValue);
                break;
            case "link":
                elementaXMLProduct.setLink(nodeValue);
                break;
            case "lagerVp":
                elementaXMLProduct.setLagerVp(Integer.parseInt(nodeValue));
                break;
            case "sifraDobavljaca":
                elementaXMLProduct.setSifraDobavljaca(nodeValue);
                break;
            case "slika":
                elementaXMLProduct.setSlika(nodeValue);
                break;
            case "slika2":
                elementaXMLProduct.setSlika2(nodeValue);
                break;
            case "slika3":
                elementaXMLProduct.setSlika3(nodeValue);
                break;
            case "slika4":
                elementaXMLProduct.setSlika4(nodeValue);
                break;
            case "slika5":
                elementaXMLProduct.setSlika5(nodeValue);
                break;
            case "slika6":
                elementaXMLProduct.setSlika6(nodeValue);
                break;
            case "cena":
                elementaXMLProduct.setCena(Double.valueOf(nodeValue));
                break;
            case "vpCena":
                elementaXMLProduct.setVpCena(Double.valueOf(nodeValue));
                break;
            case "netoCena":
                elementaXMLProduct.setNetoCena(Double.valueOf(nodeValue));
                break;
            case "akcijskaCena":
                elementaXMLProduct.setAkcijskaCena(Double.valueOf(nodeValue));
                break;
            case "rabat":
                elementaXMLProduct.setRabat(Double.valueOf(nodeValue));
                break;
            case "akcijskiRabat":
                elementaXMLProduct.setAkcijskiRabat(Double.valueOf(nodeValue));
                break;
            case "ukupanRabat":
                elementaXMLProduct.setUkupanRabat(Double.valueOf(nodeValue));
                break;
            case "unit":
                elementaXMLProduct.setUnit(nodeValue);
                break;
            case "ocenaUsaglasenosti":
                elementaXMLProduct.setOcenaUsaglasenosti(nodeValue);
                break;
            case "uputstvo":
                elementaXMLProduct.setUputstvo(nodeValue);
                break;
            case "barkod":
                elementaXMLProduct.setBarkod(nodeValue);
                break;
        }
    }

    public static String getTipFromSpecifications(Element productElement) {
        NodeList specificationsList = productElement.getElementsByTagName("specifications");
        for (int i = 0; i < specificationsList.getLength(); i++) {
            Element specifications = (Element) specificationsList.item(i);
            NodeList attributes = specifications.getElementsByTagName("attribute");
            NodeList values = specifications.getElementsByTagName("value");

            for (int j = 0; j < attributes.getLength(); j++) {
                Element attribute = (Element) attributes.item(j);
                Element value = (Element) values.item(j);

                String attributeName = attribute.getAttribute("name");
                String attributeValue = value.getTextContent();

                if ("Tip".equals(attributeName)) {
                    return attributeValue;
                }
            }
        }
        return "";
    }

    public static Map<String, String> getSpecifications(Element productElement) {
        NodeList specificationsList = productElement.getElementsByTagName("specifications");
        Map<String, String> stringStringMap = new HashMap<>();
        for (int i = 0; i < specificationsList.getLength(); i++) {
            Element specifications = (Element) specificationsList.item(i);
            NodeList attributes = specifications.getElementsByTagName("attribute");
            NodeList values = specifications.getElementsByTagName("value");

            for (int j = 0; j < attributes.getLength(); j++) {
                Element attribute = (Element) attributes.item(j);
                Element value = (Element) values.item(j);

                if (attribute != null && value != null) {
                    String attributeName = replaceDiacritics(attribute.getAttribute("name"));
                    attributeName = attributeName
                            .replace(".", "")
                            .replace("-", "_")
                            .replace("%", "")
                            .replace("__", "_")
                            .replace("+", "_")
                            .replaceAll("\\(.*?\\)", "")
                            .replace("/", "")
                            .replace("\\", "");

                    String attributeValue = value.getTextContent() == null || value.getTextContent().isEmpty() ? "" : value.getTextContent();
                    attributeValue = replaceDiacritics(attributeValue);
                    stringStringMap.put(sanitazeAttributeName(attributeName), attributeValue);
                }
            }
        }
        return stringStringMap;
    }

    private List<ElementaXMLProduct> updateElementaData(List<ElementaXMLProduct> elementaXMLProducts, MultipartFile file) {
        List<ElementaProduct> elementaProducts = readExcelData(file);

        for (ElementaXMLProduct xmlProduct : elementaXMLProducts) {
            for (ElementaProduct product : elementaProducts) {
                if (xmlProduct.getElementaId() == product.getElementaId()) {
                    xmlProduct.setSkuId(product.getSkuId());
                    xmlProduct.setFullCategoryPath(
                            setFullCategory(product.getNadredjenaKategorija(), product.getPrimarnaKategorija(), product.getSekundarnaKategorija()));
                    break;
                }
            }
        }
        return elementaXMLProducts;
    }

    private HttpHeaders createHeaders(String fileName) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        return headers;
    }
}
