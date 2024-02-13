package com.extractor.xml.client;

import com.extractor.xml.model.EweProducts;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebAPIClientIntegrationTest {

    @LocalServerPort
    private int port;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    @Test
    void testGetEweProducts() {
        // Given
        var url = "http://apicatalog.ewe.rs:5001/api/?user=infoTWj&secretcode=96928&images=1&currency=rsd";

        // When
        var result = restTemplate.getForObject(url, EweProducts.class);

        // Then
        assertNotNull(result);
        assertEquals(3858, result.getProducts().size());
        assertEquals(expectedProduct(), result.getProducts().get(0));
    }

    private EweProducts.EweProduct expectedProduct() {
        String json = getJsonProduct();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, EweProducts.EweProduct.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getJsonProduct() {
        return "{\n" +
                "  \"skuId\": 0,\n" +
                "  \"anProductKey\": 1445,\n" +
                "  \"acProduct\": \"LAN03260\",\n" +
                "  \"acVideoLink\": \"aRPKUDvNyjM\",\n" +
                "  \"acName\": \"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \",\n" +
                "  \"anAction\": 0,\n" +
                "  \"anImportant\": 1,\n" +
                "  \"anDeptKey\": 98,\n" +
                "  \"acDept\": \"MIKROTIK\",\n" +
                "  \"acInlineSpecification\": \"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \",\n" +
                "  \"anNew\": 0,\n" +
                "  \"anAdvice\": 0,\n" +
                "  \"anBestBuy\": 0,\n" +
                "  \"acEan\": \"4752224007124\",\n" +
                "  \"adRelChanged\": \"2024-01-29 01:40:00.803\",\n" +
                "  \"anPrice\": 12951.9,\n" +
                "  \"anStock\": 0,\n" +
                "  \"anOldPrice\": 0,\n" +
                "  \"anReserved\": 0,\n" +
                "  \"anRetailPrice\": 0,\n" +
                "  \"anRecommendedRetailPrice\": 0,\n" +
                "  \"anPromoPrice\": 0,\n" +
                "  \"isPromotion\": 0,\n" +
                "  \"anStockArrival\": 0,\n" +
                "  \"anDiscount\": 0,\n" +
                "  \"anRelatedDiscount\": 0,\n" +
                "  \"acCountry\": \"Latvija\",\n" +
                "  \"acSupplier\": \"5 COM DOO\",\n" +
                "  \"anMainCategoryKey\": 3,\n" +
                "  \"anCategoryKey\": 378,\n" +
                "  \"anSubCategoryKey\": 467,\n" +
                "  \"acMainCategory\": \"IT\",\n" +
                "  \"acCategory\": \"Mrežna oprema\",\n" +
                "  \"acSubCategory\": \"Ruteri i ekstenderi\",\n" +
                "  \"isWish\": 0,\n" +
                "  \"isActive\": 1,\n" +
                "  \"isRelatedSale\": 0,\n" +
                "  \"anPaymentAdvance\": 4,\n" +
                "  \"acProductDescription\": \"\",\n" +
                "  \"urlImages\": [\n" +
                "    {\n" +
                "      \"acImage\": \"https://resource.ewe.rs/products/LAN03260_v.jpg\",\n" +
                "      \"acThumbnail\": \"https://resource.ewe.rs/products/LAN03260_v.jpg\",\n" +
                "      \"acType\": \"image\",\n" +
                "      \"acSeoTitle\": \"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \",\n" +
                "      \"acSeoDescription\": \"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"\n" +
                "    },\n" +
                "    {\n" +
                "      \"acImage\": \"https://resource.ewe.rs/products/LAN03260_1.jpg\",\n" +
                "      \"acThumbnail\": \"https://resource.ewe.rs/products/LAN03260_1.jpg\",\n" +
                "      \"acType\": \"image\",\n" +
                "      \"acSeoTitle\": \"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \",\n" +
                "      \"acSeoDescription\": \"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"\n" +
                "    },\n" +
                "    {\n" +
                "      \"acImage\": \"aRPKUDvNyjM\",\n" +
                "      \"acThumbnail\": \"aRPKUDvNyjM\",\n" +
                "      \"acType\": \"video\",\n" +
                "      \"acSeoTitle\": null,\n" +
                "      \"acSeoDescription\": null\n" +
                "    }\n" +
                "  ]\n" +
                "}";
    }
//    private String getJsonProduct() {
//        return "{" +
//                "\"catalog\":[" +
//                "{" +
//                "\"anProductKey\":1445," +
//                "\"acProduct\":\"LAN03260\"," +
//                "\"acVideoLink\":\"aRPKUDvNyjM\"," +
//                "\"acName\":\"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \"," +
//                "\"anAction\":0," +
//                "\"anImportant\":1," +
//                "\"anDeptKey\":98," +
//                "\"acDept\":\"MIKROTIK\"," +
//                "\"acInlineSpecification\":\"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"," +
//                "\"anNew\":0," +
//                "\"anAdvice\":0," +
//                "\"anBestBuy\":0," +
//                "\"acEan\":\"4752224007124\"," +
//                "\"adRelChanged\":\"2024-01-29 01:40:00.803\"," +
//                "\"anPrice\":105.3," +
//                "\"anOldPrice\":0," +
//                "\"anReserved\":0," +
//                "\"anRetailPrice\":0," +
//                "\"anRecommendedRetailPrice\":0," +
//                "\"anPromoPrice\":0," +
//                "\"isPromotion\":0," +
//                "\"anStockArrival\":0," +
//                "\"anDiscount\":0," +
//                "\"acCountry\":\"Latvija\"," +
//                "\"acSupplier\":\"5 COM DOO\"," +
//                "\"anMainCategoryKey\":3," +
//                "\"anCategoryKey\":378," +
//                "\"anSubCategoryKey\":467," +
//                "\"acMainCategory\":\"IT\"," +
//                "\"acCategory\":\"Mrežna oprema\"," +
//                "\"acSubCategory\":\"Ruteri i ekstenderi\"," +
//                "\"isActive\":1," +
//                "\"anPaymentAdvance\":4," +
//                "\"acProductDescription\":\"\"," +
//                "\"urlImages\":[" +
//                "{" +
//                "\"acImage\":\"https://resource.ewe.rs/products/LAN03260_v.jpg\"," +
//                "\"acThumbnail\":\"https://resource.ewe.rs/products/LAN03260_v.jpg\"," +
//                "\"acType\":\"image\"," +
//                "\"acSeoTitle\":\"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \"," +
//                "\"acSeoDescription\":\"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"" +
//                "}," +
//                "{" +
//                "\"acImage\":\"https://resource.ewe.rs/products/LAN03260_1.jpg\"," +
//                "\"acThumbnail\":\"https://resource.ewe.rs/products/LAN03260_1.jpg\"," +
//                "\"acType\":\"image\"," +
//                "\"acSeoTitle\":\"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \"," +
//                "\"acSeoDescription\":\"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"" +
//                "}," +
//                "{" +
//                "\"acImage\":\"aRPKUDvNyjM\"," +
//                "\"acThumbnail\":\"aRPKUDvNyjM\"," +
//                "\"acType\":\"video\"," +
//                "\"acSeoTitle\":null," +
//                "\"acSeoDescription\":null" +
//                "}" +
//                "]" +
//                "}" +
//                "]" +
//                "}";
//    }
}

