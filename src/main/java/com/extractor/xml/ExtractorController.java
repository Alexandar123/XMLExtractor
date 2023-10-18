package com.extractor.xml;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/extractor")
public class ExtractorController {

    @Autowired
    private XMLExtractor xmlExtractor;

    @PostMapping("/extract")
    @Operation(summary = "Extract data from xml file")
    public ResponseEntity<Boolean> extractData(@RequestBody ExtractDataRequest request) {
        return ResponseEntity.ok(xmlExtractor.extractData(request));
    }
}
