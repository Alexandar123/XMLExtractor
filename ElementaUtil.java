package com.extractor.xml.util;

import com.extractor.xml.model.ElementaProduct;
import com.extractor.xml.model.ElementaXMLProduct;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class ElementaUtil {
    public static void sanitizeCountry(List<ElementaXMLProduct> elementaProducts) {
        // Create a mapping of Serbian country names to English names, all in lowercase
        Map<String, String> countryMappings = new HashMap<>();
//        countryMappings.put("kina", "Kina");
        countryMappings.put("kina", "China");
        countryMappings.put("belgija", "Belgium");
        countryMappings.put("nemačka", "Germany");
        countryMappings.put("nemacka", "Germany");
        countryMappings.put("mađarska", "Hungary");
        countryMappings.put("sad", "United States");
        countryMappings.put("engleska", "United Kingdom");
        countryMappings.put("srbija", "Serbia");
        countryMappings.put("češka", "Czech Republic");
        countryMappings.put("taiwan", "Taiwan, Province of China");
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
        countryMappings.put("norveska", "Norway");
        countryMappings.put("madarska", "Hungary");
        countryMappings.put("francuska", "France");
        countryMappings.put("austrija", "Austria");
        countryMappings.put("tajland", "Thailand");
        countryMappings.put("rusija", "Russia");

        for (ElementaXMLProduct elementaProduct : elementaProducts) {
            String originalCountry = elementaProduct.getZemljaPorekla().toLowerCase();
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

    public static String setFullCategory(String nadredjenaKategorija, String primarnaKategorija, String
            sekundarnaKategorija) {
        return "Default Category/Shop/" +
                nadredjenaKategorija +
                "/" +
                primarnaKategorija +
                "/" +
                sekundarnaKategorija;
    }

    public static String validate(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        return data;
    }

    public static String checkManufactureData(String data) {
        if (data == null || data.isEmpty()) {
            return "";
        }
        int length = data.length();
        while (length > 0 && data.charAt(length - 1) == ' ') {
            length--;
        }

        return data.substring(0, length);
    }


    public static String getCellValueAsString(Cell cell) {
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

    public static double getCellValueAsDouble(Cell cell) {
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

    public static int getCellValueAsInteger(Cell cell) {
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

    public static String getDesktopPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows
            return System.getProperty("user.home") + File.separator + "Desktop";
        } else if (os.contains("mac")) {
            // macOS
            return System.getProperty("user.home") + File.separator + "Desktop";
        } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
            // Linux or Unix
            return System.getProperty("user.home") + File.separator + "Desktop";
        }
        return null; // Unsupported OS
    }

    public static String getImages(ElementaXMLProduct elementaXMLProduct) {
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

    public static String replaceDiacritics(String text) {
        text = text.replace("đ", "d");
        String normalizedText = Normalizer.normalize(text, Normalizer.Form.NFD);
        normalizedText = normalizedText.replaceAll("[^\\p{ASCII}]", "");
        return normalizedText;
    }

    public static String constructUrlKey(String input) {
        String replaced = input.replace(",", "-");
        return replaced.replace("- ", "-");
    }

    public static boolean isSpecialAttribute(String attributeName) {
        return "32_kanalni_audio".equalsIgnoreCase(attributeName)
                || "3g__wifi".equalsIgnoreCase(attributeName)
                || "4k_rezolucija_pri_60_hz".equalsIgnoreCase(attributeName)
                || "4k_rezolucija_pri_30_hz".equalsIgnoreCase(attributeName)
                || "a_".equalsIgnoreCase(attributeName)
                || "b_".equalsIgnoreCase(attributeName)
                || "3d_preko_hdmi".equalsIgnoreCase(attributeName);
    }

    public static String sanitazeAttributeName(String attributeName) {
        attributeName = attributeName.replace(" ", "_");
        if ("boja".equalsIgnoreCase(attributeName)) {
            attributeName = "color";
        }

        if (attributeName.endsWith("_")) {
            attributeName = attributeName.substring(0, attributeName.length() - 1);
        }

        if (isSpecialAttribute(attributeName)) {
            attributeName = "attr_" + attributeName;
        }
        return attributeName;
    }

}
