package com.extractor.xml.controller;

import com.extractor.xml.model.ElementaProduct;
import com.extractor.xml.service.FileService;
import com.extractor.xml.vendor.ElementaVendor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.extractor.xml.util.EmallUtil.createFileName;
import static com.extractor.xml.vendor.ElementaVendor.getCSVHeadersWithSpecifications;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@AllArgsConstructor
public class ElementaController {

    private FileService fileService;
    private ElementaVendor elementaVendor;

    @PostMapping(value = "/elementa/generate-csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Convert products from elementa format into E-mall Magento specific csv file.", description =
            "Obavezna pravila: E-mall fajl mora biti pripremljen sa <b>SKU</b>, <b>ElementaId</b> i <b>category putanjama</b> prema kojima ce se Elementa proizvodi uvezati u sistem i to sledecim redom: \n" +
                    "1. SKU \n 2. ElementaId \n 3. Category path \n" +
                    "<br><br><b>Elementa fajl treba da bude pripremljen bez naslova(header-a)</b>")
    public ResponseEntity<ByteArrayResource> generateCsvWithResponse(@RequestParam("emall_excel_file") MultipartFile emallFile,
                                                                     @RequestParam("elementa_xml_file") MultipartFile elementaFile,
                                                                     @Parameter(description = "Naziv za CSV fajl koji ce biti generisan u E-mall Magento formatu. Default naziv je <b>elementa_products-trenutniDatumIVreme.csv</b>") @RequestParam(value = "fileName", required = false, defaultValue = "elementa_products") String fileName,
                                                                     @Parameter(description = "Skip vrednost se odnosi na broj elemenata koji ce se preskociti u obradi pocevsi od prvog elementa I.E. ako je skip 3 preskacu se prva tri elementa iz fajla. Default vrednost 0 znaci da se svi elementi obradjuju") @RequestParam(value = "skip", required = false, defaultValue = "0") Integer skip,
                                                                     @Parameter(description = "Limit vrednost se odnosi na broj elemenata koje je potrebno obraditi. I.E. ako je limit 3 obradice se iz celog fajla samo tri elementa. Default vrednost -1 znaci da nema limita.") @RequestParam(value = "limit", required = false, defaultValue = "-1") Integer limit) {
        log.info("Elementa CSV generation started....");
        List<ElementaProduct> elementaXMLProducts = elementaVendor.readElementaProducts(elementaFile);
        List<ElementaProduct> updatedElementaData = elementaVendor.updateElementaProducts(elementaXMLProducts, emallFile);

        var emallCsvHeaders = getCSVHeadersWithSpecifications(updatedElementaData);
        var csvData = elementaVendor.getCSVData(updatedElementaData, emallCsvHeaders, skip, limit);
        log.info("csvData size = " + csvData.size());

        byte[] fileContent = fileService.generateCsv(emallCsvHeaders, csvData);
        if (fileContent != null) {
            return ResponseEntity.ok()
                    .headers(createHeaders(createFileName(fileName), csvData.size()))
                    .contentLength(fileContent.length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(new ByteArrayResource(fileContent));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    private HttpHeaders createHeaders(String fileName, int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add("number_of_products", String.valueOf(size));
        return headers;
    }
}