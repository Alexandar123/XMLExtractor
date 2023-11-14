package com.extractor.xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XMLToCSV {
    public static void main(String[] args) {
        readXMLFileAndConvertToCSV("", "");
    }


    public static void readXMLFileAndConvertToCSV(String xmlFilePath, String csvFilePath) {
        try {
            // Create a DocumentBuilder
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file
            Document document = builder.parse(new File("/home/intv0016/Desktop/XMLExtractor/src/main/resources/test.xml"));

            // Get the root element
            Element root = document.getDocumentElement();

            // Get all "product" elements
            NodeList productList = root.getElementsByTagName("product");
            for (int productIndex = 0; productIndex < productList.getLength(); productIndex++) {
                Element product = (Element) productList.item(productIndex);

                // Iterate through the child elements of each "product" element
                NodeList productChildren = product.getChildNodes();
                for (int i = 0; i < productChildren.getLength(); i++) {
                    Node node = productChildren.item(i);

                    // Check if the node is an element
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        Element element = (Element) node;
                        String nodeName = element.getNodeName();
                        String nodeValue = element.getTextContent();
                        System.out.println(nodeName + ": " + nodeValue);
                    }
                }

                // Print a separator between products
                System.out.println("--------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
