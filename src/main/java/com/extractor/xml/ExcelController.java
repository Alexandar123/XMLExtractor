package com.extractor.xml;

import com.extractor.xml.model.ElementaProduct;
import com.extractor.xml.model.ElementaXMLProduct;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.MediaType;
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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/converter")
public class ExcelController {
    //TODO: 1. FOrmatiranje cena u Magento treba da ide sa decimalnim zarezom, a ne tackom ie 1,324.22 -> 1.324,22 (magento format)
    @PostMapping(value = "/upload", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Read data from excel file")
    public ElementaXMLProduct uploadFile(@RequestParam("excelFile") MultipartFile excelFile) {
        List<ElementaProduct> elementaProducts = readExcelData(excelFile);
        List<ElementaXMLProduct> elementaXMLProducts = getElementaXMLProducts();

        for (ElementaXMLProduct xmlProduct : elementaXMLProducts) {
            for (ElementaProduct product : elementaProducts) {
                if (xmlProduct.getElementaId() == product.getElementaId()) {
                    xmlProduct.setSkuId(product.getSkuId());
                    xmlProduct.setFullCategoryPath(setFullCategory(product.getNadredjenaKategorija(), product.getPrimarnaKategorija(), product.getSekundarnaKategorija()));
                    break;
                }
            }
        }
        return elementaXMLProducts.get(10);
    }

    @PostMapping(value = "/generate-csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Read data from excel file")
    public void generateCsv(@RequestParam("file") MultipartFile file) {
        List<ElementaXMLProduct> elementaXMLProductList = getElementaXMLProducts();
        List<ElementaXMLProduct> elementaXMLProducts = updateElementaData(elementaXMLProductList, file);

        List<String> csvHeader = getCSVHeader();
        List<List<String>> csvData = getCSVData(elementaXMLProducts);
        String filePath = "products" + LocalDateTime.now() + ".csv";
        try (BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath))) {
            // Write the header to the CSV file
            fileWriter.write(csvHeader.stream()
                    .map(header -> "\"" + header + "\"")
                    .collect(Collectors.joining(",")));
            fileWriter.newLine();

            // Write the data to the CSV file
            for (List<String> data : csvData) {
                List<String> sanitizedData = data.stream()
                        .map(value -> "\"" + value + "\"")
                        .collect(Collectors.toList());
                fileWriter.write(String.join(",", sanitizedData));
                fileWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> getCSVHeader() {
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
                "additional_images", "base", "thumbnail", "small_image",
                "tip_proizvoda", "brend"
        );
    }

    public List<List<String>> getCSVData(List<ElementaXMLProduct> elementaXMLProducts) {
        return elementaXMLProducts.stream()
                .skip(6)
                .limit(50) // Limit the stream to the first 50 elements
                .filter(elementaXMLProduct -> elementaXMLProduct.getZemljaPorekla() != null && elementaXMLProduct.getProizvodjac() != null)

                .map(elementaXMLProduct -> Arrays.asList(
                        String.valueOf(elementaXMLProduct.getSkuId()), "", "Default", "simple", elementaXMLProduct.getFullCategoryPath(), "",
                        elementaXMLProduct.getNaziv(), elementaXMLProduct.getOpis(), "", "", "1", "Taxable Goods",
                        "Catalog, Search", String.valueOf(elementaXMLProduct.getCena()), "", "", "", "",
                        "", "", "", "", "", "",
                        "", "", "", "", "", "",
                        "", "", "", "", "", "",
                        "", elementaXMLProduct.getZemljaPorekla(), "", String.valueOf(elementaXMLProduct.getLagerVp()), "0", "1",
                        "0", "0", "1", "1", "0", "0",
                        "1", "1", "", "1", "0", "1",
                        "1", "0", "1", "0", "0", "1",
                        "0", "1", "", "", "", "",
                        "", "", "", elementaXMLProduct.getProizvodjac(), "", elementaXMLProduct.getUvoznik(),
                        getSupplierCode(elementaXMLProduct.getSifraDobavljaca()), "", elementaXMLProduct.getLink(), "", "", "", "", getImages(elementaXMLProduct), getImage(elementaXMLProduct.getSlika()),
                        getImage(elementaXMLProduct.getSlika()), getImage(elementaXMLProduct.getSlika()),
                        elementaXMLProduct.getTipProizvoda(), getBrand(elementaXMLProduct.getProizvodjac())
                ))
                .collect(Collectors.toList());
    }

    //TODO: Proveriti sta da se radi u slucaju kada brend nije definisan? Da li uvesti novi brend "nije poznat" ili nesto drugo?
    //TODO: Proveriti sta da se radi u slucaju kada tip_proizvoda nije definisan? Da li uvesti novi tip_proizvoda "nije poznat" ili nesto drugo?
    private String getBrand(String proizvodjac) {
        if (proizvodjac == null || proizvodjac.isEmpty()) {
            return "UNDEFINED";
        }
        return proizvodjac;
    }

    private List<ElementaXMLProduct> updateElementaData(List<ElementaXMLProduct> elementaXMLProducts, MultipartFile file) {
        List<ElementaProduct> elementaProducts = readExcelData(file);

        for (ElementaXMLProduct xmlProduct : elementaXMLProducts) {
            for (ElementaProduct product : elementaProducts) {
                if (xmlProduct.getElementaId() == product.getElementaId()) {
                    xmlProduct.setSkuId(product.getSkuId());
                    xmlProduct.setFullCategoryPath(setFullCategory(product.getNadredjenaKategorija(), product.getPrimarnaKategorija(), product.getSekundarnaKategorija()));
                    break;
                }
            }
        }
        return elementaXMLProducts;
    }

    // Read data from excel file and return a map of NAS ID and NUM ID
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
                                    .elementaId(getCellValueAsInteger(row.getCell(3)))
                                    .name(getCellValueAsString(row.getCell(1)))
                                    .sifraDobavljaca(getCellValueAsString(row.getCell(2)))
                                    .uvoznik(getCellValueAsString(row.getCell(14)))
                                    .zemljaPorekla(getCellValueAsString(row.getCell(16)))
                                    .nadredjenaKategorija(getCellValueAsString(row.getCell(22)))
                                    .primarnaKategorija(getCellValueAsString(row.getCell(18)))
                                    .sekundarnaKategorija(getCellValueAsString(row.getCell(20)))
                                    .maloprodajnaCena(getCellValueAsDouble(row.getCell(23)))
                                    .nabavnaCena(getCellValueAsDouble(row.getCell(24)))
                                    .rabat(getCellValueAsDouble(row.getCell(25)))
                                    .build());
                }
//                sanitizeCountry(elementaProducts);
                sanitizeCategoryFullPath(elementaProducts);
            }
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return elementaProducts;
    }

    private String getSupplierCode(String sifraDobavljaca) {
        return sifraDobavljaca != null ? sifraDobavljaca : "";
    }

    private String getImages(ElementaXMLProduct elementaXMLProduct) {
        List<String> imageUrls = new ArrayList<>();

        if (elementaXMLProduct.getSlika() != null) {
            imageUrls.add(elementaXMLProduct.getSlika());
        }
        if (elementaXMLProduct.getSlika2() != null) {
            imageUrls.add(elementaXMLProduct.getSlika2());
        }
        if (elementaXMLProduct.getSlika3() != null) {
            imageUrls.add(elementaXMLProduct.getSlika3());
        }
        if (elementaXMLProduct.getSlika4() != null) {
            imageUrls.add(elementaXMLProduct.getSlika4());
        }
        if (elementaXMLProduct.getSlika5() != null) {
            imageUrls.add(elementaXMLProduct.getSlika5());
        }
        if (elementaXMLProduct.getSlika6() != null) {
            imageUrls.add(elementaXMLProduct.getSlika6());
        }

        return String.join(",", imageUrls);
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

    private String getImage(String slika) {
        return slika != null ? slika : "";
    }

    public static List<ElementaXMLProduct> getElementaXMLProducts() {
        List<ElementaXMLProduct> elementaXMLProductList = new ArrayList<>();
        File xmlFile = new File("/home/intv0016/Downloads/dist_proizvodi.xml");
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);

            Element root = document.getDocumentElement();
            NodeList productListNodes = root.getElementsByTagName("product");

            for (int productIndex = 0; productIndex < productListNodes.getLength(); productIndex++) {
                Element productElement = (Element) productListNodes.item(productIndex);
                ElementaXMLProduct elementaXMLProduct = new ElementaXMLProduct();
                elementaXMLProduct.setTipProizvoda(getTipFromSpecifications(productElement));
                NodeList productChildren = productElement.getChildNodes();
                for (int i = 0; i < productChildren.getLength(); i++) {
                    Node node = productChildren.item(i);

                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
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
                        }
                    }
                }
                elementaXMLProductList.add(elementaXMLProduct);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sanitizeCountry(elementaXMLProductList);
        return elementaXMLProductList;
    }

    public static void sanitizeCountry(List<ElementaXMLProduct> elementaProducts) {
        // Create a mapping of Serbian country names to English names, all in lowercase
        Map<String, String> countryMappings = new HashMap<>();
        countryMappings.put("kina", "China");
        countryMappings.put("belgija", "Belgium");
        countryMappings.put("nemačka", "Germany");
        countryMappings.put("mađarska", "Hungary");
        countryMappings.put("sad", "United States");
        countryMappings.put("engleska", "England");
        countryMappings.put("srbija", "Serbia");
        countryMappings.put("češka", "Czech Republic");
        countryMappings.put("taiwan", "Taiwan");
        countryMappings.put("bugarska", "Bulgaria");
        countryMappings.put("malezija", "Malaysia");
        countryMappings.put("poljska", "Poland");
        countryMappings.put("vijetnam", "Vietnam");
        countryMappings.put("turska", "Turkey");
        countryMappings.put("svajcarska", "Switzerland");
        countryMappings.put("slovenija", "Slovenia");
        countryMappings.put("japan", "Japan");
        countryMappings.put("italija", "Italy");
        countryMappings.put("holandija", "Netherlands");
        countryMappings.put("indonezija", "Indonesia");
        countryMappings.put("norveška", "Norway");
        countryMappings.put("madarska", "Hungary");
        countryMappings.put("francuska", "France");
        countryMappings.put("austrija", "Austria");
        countryMappings.put("tajland", "Thailand");
        countryMappings.put("rusija", "Russia");

        // Iterate through the list and update country names
        for (ElementaXMLProduct elementaProduct : elementaProducts) {
            String originalCountry = elementaProduct.getZemljaPorekla().toLowerCase(); // Convert to lowercase for case-insensitive matching
            String englishCountry = countryMappings.get(originalCountry);

            if (englishCountry != null) {
                elementaProduct.setZemljaPorekla(englishCountry);
                elementaProduct.setZemljaUvoza(englishCountry);
            }
        }
    }

    public static void sanitizeCategoryFullPath(List<ElementaProduct> elementaProducts) {
        for (ElementaProduct elementaProduct : elementaProducts) {
            elementaProduct.setFullCategoryPath(
                    "Default Category/Shop/" +
                            elementaProduct.getNadredjenaKategorija() + "/" +
                            elementaProduct.getPrimarnaKategorija() + "/" +
                            elementaProduct.getSekundarnaKategorija()
            );
        }
    }

    private String setFullCategory(String nadredjenaKategorija, String primarnaKategorija, String
            sekundarnaKategorija) {
        return "Default Category/Shop/" +
                nadredjenaKategorija +
                "/" +
                primarnaKategorija +
                "/" +
                sekundarnaKategorija;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.NUMERIC) {
            // Format numeric cell as a string without decimal points
            return String.format("%.0f", cell.getNumericCellValue());
        } else {
            return cell.toString();
        }
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else {
            try {
                return Double.parseDouble(cell.toString());
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
    }

    private int getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return 0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        } else {
            try {
                return Integer.parseInt(cell.toString());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
    }
}