package com.extractor.xml;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
@Service
public class XMLExtractor {

    public boolean extractData(ExtractDataRequest request) {
        try {
            //String inputFilePath = "/home/intv0016/Desktop/XMLExtractor/XMLExtractors/src/main/resources/test.xml";
            String outputFilePath = generateOutputFilePath(request.getOutputFilePath());

            Document doc = loadXmlDocument(request.getInputFilePath());
            Document outputDoc = createXmlDocument();

            Element root = createRootElement(outputDoc);
            Element products = createProductsElement(outputDoc);

            NodeList productList = doc.getElementsByTagName("product");

            for (int i = 0; i < productList.getLength(); i++) {
                Element product = (Element) productList.item(i);
                Element numId = (Element) product.getElementsByTagName("numId").item(0);
                Element specifications = (Element) product.getElementsByTagName("specifications").item(0);

                if (numId != null && specifications != null) {
                    Element newProduct = createProductElement(outputDoc, numId, specifications);
                    products.appendChild(newProduct);
                }
            }

            root.appendChild(products);
            outputDoc.appendChild(root);

            saveXmlDocument(outputDoc, outputFilePath);

            log.info("XML izlazna datoteka je generisana.");
            System.out.println("XML izlazna datoteka je generisana.");

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static String generateOutputFilePath(String outputFilePath) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        return null == outputFilePath ? "output_" + timestamp + ".xml" : outputFilePath;
    }

    private static Document loadXmlDocument(String filePath) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(filePath));
    }

    private static Document createXmlDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }

    private static Element createRootElement(Document doc) {
        return doc.createElement("xml");
    }

    private static Element createProductsElement(Document doc) {
        return doc.createElement("products");
    }

    private static Element createProductElement(Document doc, Element numId, Element specifications) {
        Element newProduct = doc.createElement("product");
        newProduct.appendChild(doc.importNode(numId, true));
        newProduct.appendChild(doc.importNode(specifications, true));
        return newProduct;
    }

    private static void saveXmlDocument(Document doc, String filePath) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }
}
