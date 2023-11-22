package com.extractor.xml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ExtractorControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private XMLExtractor xmlExtractor;

    @Test
    @Disabled
    void testExtractData() {
        // Define the endpoint URL
        String url = "http://localhost:" + port + "/extractor/extract";

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create an HTTP entity with the request and headers
        HttpEntity<ExtractDataRequest> httpEntity = new HttpEntity<>(ExtractDataRequest.builder().build(), headers);

        // Send the POST request and get the response
        ResponseEntity<Boolean> responseEntity = restTemplate.postForEntity(url, httpEntity, Boolean.class);

        assertEquals(Boolean.TRUE, responseEntity.getBody());
    }
}
