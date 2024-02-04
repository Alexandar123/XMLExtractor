package com.extractor.xml.service;

import com.extractor.xml.model.EmallProduct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.extractor.xml.util.ElementaUtil.getCellValueAsInteger;
import static com.extractor.xml.util.ElementaUtil.getCellValueAsString;
import static com.extractor.xml.util.ElementaUtil.sanitizeCategoryFullPath;

@Service
@Slf4j
public class FileService {

    public void writeCsvToFile(BufferedWriter fileWriter, List<String> data) throws IOException {
        fileWriter.write(data.stream()
                .map(value -> "\"" + value.replace("\"", "'") + "\"")
                .collect(Collectors.joining(",")));
        fileWriter.newLine();
    }

    /**
     * Read data from the Emall Excel file and create a Product object based on which the product from the vendor will be correlated
     * @param file - uploaded emall excel file
     * @return product - basic emall product
     */
    @SneakyThrows
    public List<EmallProduct> readProductDataFromExcel(MultipartFile file) {
        List<EmallProduct> products = new ArrayList<>();
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);
            for (Row row : sheet) {
                if (row.getPhysicalNumberOfCells() >= 2) {
                    products.add(
                            EmallProduct.builder()
                                    .skuId(getCellValueAsInteger(row.getCell(0)))
                                    .name(getCellValueAsString(row.getCell(1)))
                                    .elementaId(getCellValueAsInteger(row.getCell(2)))
                                    .nadredjenaKategorija(getCellValueAsString(row.getCell(4)))
                                    .primarnaKategorija(getCellValueAsString(row.getCell(6)))
                                    .sekundarnaKategorija(getCellValueAsString(row.getCell(8)))
                                    .sifraDobavljaca(getCellValueAsString(row.getCell(9)))
                                    .build());
                }
                sanitizeCategoryFullPath(products);
            }
//            workbook.getCreationHelper().createFormulaEvaluator().evaluateAll();
            workbook.close();
        } catch (IOException e) {
            log.error("Error during read data from excel: " + e.getMessage());
            throw new IOException("Error duriing read data from excel: {}", e);
        }
        return products;
    }
}
