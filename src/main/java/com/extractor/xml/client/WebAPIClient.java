package com.extractor.xml.client;

import com.extractor.xml.model.Product;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
@Slf4j
public class WebAPIClient {

    private final RestTemplate restTemplate;

    public Product getProducts(String url, Class<? extends Product> clazz) {
        ResponseEntity<Product> responseEntity = (ResponseEntity<Product>) restTemplate.getForEntity(url, clazz);
        if (responseEntity.getStatusCode().is2xxSuccessful()) {
            return restTemplate.getForObject(url, clazz);
        } else {
            log.error("Failed to retrieve data from the API. HTTP Status: " + responseEntity.getStatusCode());
            throw new RuntimeException("Failed to retrieve data from the API. HTTP Status: " + responseEntity.getStatusCode());
        }
    }
}
