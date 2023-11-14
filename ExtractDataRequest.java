package com.extractor.xml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class ExtractDataRequest {
    private String inputFilePath;
    private String outputFilePath;
    private List<String> xmlElements;
}
