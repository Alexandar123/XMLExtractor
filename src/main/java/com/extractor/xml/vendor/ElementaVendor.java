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

import static com.extractor.xml.util.ElementaUtil.replaceDiacritics;
import static com.extractor.xml.util.ElementaUtil.sanitazeAttributeName;
import static com.extractor.xml.util.ElementaUtil.sanitizeCountry;
import static com.extractor.xml.util.ElementaUtil.setFullCategory;
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
}
