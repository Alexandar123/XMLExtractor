package com.extractor.xml.client;

import com.extractor.xml.model.EweProducts;
import com.extractor.xml.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class WebAPIClientTest {

    @Mock
    private RestTemplate restTemplate;

    private WebAPIClient webAPIClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webAPIClient = new WebAPIClient(restTemplate);
    }

    @Test
    void getProducts_SuccessfulResponse() {
        String url = "http://apicatalog.ewe.rs:5001/api/?user=infoTWj&secretcode=96928&images=1&currency=rsd";
        var expectedProducts = expectedProduct();

        var responseEntity = new ResponseEntity<>(expectedProducts, HttpStatus.OK);

        when(restTemplate.getForEntity(url, EweProducts.class)).thenReturn(responseEntity);
        when(restTemplate.getForObject(url, EweProducts.class)).thenReturn(expectedProducts);

        var response = webAPIClient.getProducts(url, EweProducts.class);

        assertEquals(expectedProducts, response);
    }

    @Test
    void getProducts_FailedResponse() {
        String url = "http://apicatalog.ewe.rs:5001/api/?user=infoTWj&secretcode=96928&images=1&currency=rsd";
        ResponseEntity<EweProducts> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(eq(url), eq(EweProducts.class)))
                .thenReturn(responseEntity);

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            webAPIClient.getProducts(url, EweProducts.class);
        });
        assertEquals("Failed to retrieve data from the API. HTTP Status: 500 INTERNAL_SERVER_ERROR", thrownException.getMessage());
    }


    private EweProducts expectedProduct() {
        String json = getJsonProduct();

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, EweProducts.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getJsonProduct() {
        return "{" +
                "\"catalog\":[" +
                "{" +
                "\"anProductKey\":1445," +
                "\"acProduct\":\"LAN03260\"," +
                "\"acVideoLink\":\"aRPKUDvNyjM\"," +
                "\"acName\":\"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \"," +
                "\"anAction\":0," +
                "\"anImportant\":1," +
                "\"anDeptKey\":98," +
                "\"acDept\":\"MIKROTIK\"," +
                "\"acInlineSpecification\":\"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"," +
                "\"anNew\":0," +
                "\"anAdvice\":0," +
                "\"anBestBuy\":0," +
                "\"acEan\":\"4752224007124\"," +
                "\"adRelChanged\":\"2024-01-29 01:40:00.803\"," +
                "\"anPrice\":105.3," +
                "\"anOldPrice\":0," +
                "\"anReserved\":0," +
                "\"anRetailPrice\":0," +
                "\"anRecommendedRetailPrice\":0," +
                "\"anPromoPrice\":0," +
                "\"isPromotion\":0," +
                "\"anStockArrival\":0," +
                "\"anDiscount\":0," +
                "\"acCountry\":\"Latvija\"," +
                "\"acSupplier\":\"5 COM DOO\"," +
                "\"anMainCategoryKey\":3," +
                "\"anCategoryKey\":378," +
                "\"anSubCategoryKey\":467," +
                "\"acMainCategory\":\"IT\"," +
                "\"acCategory\":\"Mrežna oprema\"," +
                "\"acSubCategory\":\"Ruteri i ekstenderi\"," +
                "\"isActive\":1," +
                "\"anPaymentAdvance\":4," +
                "\"acProductDescription\":\"\"," +
                "\"urlImages\":[" +
                "{" +
                "\"acImage\":\"https://resource.ewe.rs/products/LAN03260_v.jpg\"," +
                "\"acThumbnail\":\"https://resource.ewe.rs/products/LAN03260_v.jpg\"," +
                "\"acType\":\"image\"," +
                "\"acSeoTitle\":\"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \"," +
                "\"acSeoDescription\":\"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"" +
                "}," +
                "{" +
                "\"acImage\":\"https://resource.ewe.rs/products/LAN03260_1.jpg\"," +
                "\"acThumbnail\":\"https://resource.ewe.rs/products/LAN03260_1.jpg\"," +
                "\"acType\":\"image\"," +
                "\"acSeoTitle\":\"(C52iG-5HaxD2HaxD-TC) hAP ax2, RouterOS L4, ruter \"," +
                "\"acSeoDescription\":\"Osnovne karakteristike - Tip: Ruter  Bežična mreža - Standardi: IEEE 802.11a/n/ac/ax Bežični protok: 1.200Mbps na 5GHz Frekventni opseg: 2.4GHz - 5GHz  Antene - Tip: Unutrašnja Jačina: 4.5dBi na 2.4GHz,4dBi na 5GHz Ostalo: Dual-Band  Žična mreža - Žični protok: 10/100/1000Mbps  Priključci / Slotovi - RJ-45: 1 (PoE),5x (10/100/1000Mbps)  Memorija - Interna: 128MB RAM memorija: 1GB  Fizičke karakteristike - Dimenzije: 120mm x 101mm x 37mm Materijal: Plastika Boja: Siva Pakovanje: Naponski adapter,Set šrafova Stanje: Nekorišćeno  Reklamacioni period - Reklamacioni period: 24 meseca \"" +
                "}," +
                "{" +
                "\"acImage\":\"aRPKUDvNyjM\"," +
                "\"acThumbnail\":\"aRPKUDvNyjM\"," +
                "\"acType\":\"video\"," +
                "\"acSeoTitle\":null," +
                "\"acSeoDescription\":null" +
                "}" +
                "]" +
                "}" +
                "]" +
                "}";
    }
}
