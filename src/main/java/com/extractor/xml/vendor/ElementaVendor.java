package com.extractor.xml.vendor;

import com.extractor.xml.model.ElementaProduct;
import com.extractor.xml.model.EmallProduct;
import com.extractor.xml.service.FileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.extractor.xml.util.ElementaUtil.checkManufactureData;
import static com.extractor.xml.util.ElementaUtil.constructUrlKey;
import static com.extractor.xml.util.ElementaUtil.getImages;
import static com.extractor.xml.util.ElementaUtil.replaceDiacritics;
import static com.extractor.xml.util.ElementaUtil.sanitazeAttributeName;
import static com.extractor.xml.util.ElementaUtil.sanitizeCountry;
import static com.extractor.xml.util.ElementaUtil.setFullCategory;
import static com.extractor.xml.util.ElementaUtil.validate;
import static com.extractor.xml.util.EmallUtil.getCSVHeaders;

@Service
@Slf4j
@AllArgsConstructor
public class ElementaVendor {

    private FileService fileService;

    public static List<String> getCSVHeadersWithSpecifications(List<ElementaProduct> products) {
        var csvHeader = new ArrayList<>(getCSVHeaders());

        for (ElementaProduct product : products) {
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

    /**
     * Correlate Emall products with vendor (Elementa) products to extract vendor data and create a list of products.
     *
     * @param elementaXMLProducts - list of elementa products
     * @param file                - emall excel file
     * @return elementaXMLProducts - list of updated and ready for import products
     */
    public List<ElementaProduct> updateElementaProducts(List<ElementaProduct> elementaXMLProducts, MultipartFile file) {
        var products = fileService.readProductDataFromExcel(file);

        for (ElementaProduct xmlProduct : elementaXMLProducts) {
            for (EmallProduct product : products) {
                if (xmlProduct.getElementaId() == product.getVendorId()) {
                    xmlProduct.setSkuId(product.getSkuId());
                    xmlProduct.setFullCategoryPath(
                            setFullCategory(product.getNadredjenaKategorija(), product.getPrimarnaKategorija(), product.getSekundarnaKategorija()));
                    xmlProduct.setNaziv(product.getName());
                    break;
                }
            }
        }
        return elementaXMLProducts;
    }

    /**
     * Extract excel data from vendor(Elementa) and map it to POJO
     *
     * @param elementaXMLFile - excel file with Elementa structur
     * @return elementaXMLProductList - list of Elementa POJO
     */
    public List<ElementaProduct> readElementaProducts(MultipartFile elementaXMLFile) {
        List<ElementaProduct> elementaXMLProductList = new ArrayList<>();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(elementaXMLFile.getInputStream());

            Element root = document.getDocumentElement();
            NodeList productListNodes = root.getElementsByTagName("product");

            for (int productIndex = 0; productIndex < productListNodes.getLength(); productIndex++) {
                Element productElement = (Element) productListNodes.item(productIndex);
                ElementaProduct elementaXMLProduct = createProductFromElement(productElement);
                elementaXMLProductList.add(elementaXMLProduct);
            }

        } catch (Exception e) {
            log.error("Error during read data from Elementa xml file: " + e.getMessage());
        }

        sanitizeCountry(elementaXMLProductList);
        return elementaXMLProductList;
    }

    private static ElementaProduct createProductFromElement(Element productElement) {
        ElementaProduct elementaXMLProduct = new ElementaProduct();
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

    private static String getTipFromSpecifications(Element productElement) {
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

    private static Map<String, String> getSpecifications(Element productElement) {
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

    private static void processProductNode(ElementaProduct elementaXMLProduct, Element element) {
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

    /**
     * Elementa Products
     */
    public List<List<String>> getCSVData(List<ElementaProduct> elementaXMLProducts, List<String> csvHeader, int skip, int limit) {
        Stream<ElementaProduct> productsStream = elementaXMLProducts.stream()
                .filter(elementaXMLProduct -> elementaXMLProduct.getFullCategoryPath() != null && !elementaXMLProduct.getFullCategoryPath().contains("#N/A"))
                .skip(skip);

        if (limit != -1) {
            productsStream = productsStream.limit(limit);
        }
        return productsStream.map(item -> addDataForEmallHeader(item, csvHeader))
                .collect(Collectors.toList());
    }
}
