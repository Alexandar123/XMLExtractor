package com.extractor.xml.controller;

import com.extractor.xml.service.FileService;
import com.extractor.xml.util.EmallUtil;
import com.extractor.xml.vendor.EweVendor;
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

import static com.extractor.xml.util.EmallUtil.createFileName;
import static com.extractor.xml.util.LogUtil.calculateDurationAndGenerateLog;

@RestController
@RequestMapping("/api/v1")
@Slf4j
@AllArgsConstructor
public class EweController {

    private FileService fileService;
    private EweVendor eweVendor;

    @PostMapping(value = "/ewe/generate-csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(summary = "Convert products from ewe format into E-mall Magento specific csv file.", description =
            "Obavezna pravila: E-mall fajl mora biti pripremljen sa <b>SKU</b>, <b>EweId</b> i <b>category putanjama</b> prema kojima ce se ewe proizvodi uvezati u sistem i to sledecim redom: \n" +
                    "1. SKU \n 2. EweId \n 3. Category path \n" +
                    "<br><br><b>Ewe fajl treba da bude pripremljen bez naslova(header-a)</b>")
    public ResponseEntity<ByteArrayResource> generateCsvWithResponse(@RequestParam("emall_excel_file") MultipartFile emallFile,
                                                                     @Parameter(description = "Naziv za CSV fajl koji ce biti generisan u E-mall Magento formatu. Default naziv je <b>ewe_products-trenutniDatumIVreme.csv</b>") @RequestParam(value = "fileName", required = false, defaultValue = "ewe_products") String fileName,
                                                                     @Parameter(description = "Skip vrednost se odnosi na broj elemenata koji ce se preskociti u obradi pocevsi od prvog ewe I.E. ako je skip 3 preskacu se prva tri ewe iz fajla. Default vrednost 0 znaci da se svi elementi obradjuju") @RequestParam(value = "skip", required = false, defaultValue = "0") Integer skip,
                                                                     @Parameter(description = "Limit vrednost se odnosi na broj elemenata koje je potrebno obraditi. I.E. ako je limit 3 obradice se iz celog fajla samo tri ewe. Default vrednost -1 znaci da nema limita.") @RequestParam(value = "limit", required = false, defaultValue = "-1") Integer limit) {
        log.info("Ewe CSV generation started....");
        long startTime = System.currentTimeMillis();

        var eweProducts = eweVendor.getEweProducts(emallFile);
        var csvData = eweVendor.getAndMapToCSVData(eweProducts, skip, limit);

        log.info("csvData size = " + csvData.size());

        byte[] fileContent = fileService.generateCsv(EmallUtil.getCSVHeaders(), csvData);
        calculateDurationAndGenerateLog(startTime);
        if (fileContent != null) {
            return ResponseEntity.ok()
                    .headers(createHeaders(createFileName(fileName), csvData.size()))
                    .contentLength(fileContent.length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(new ByteArrayResource(fileContent));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        // Read data from Emall and correlate it
        // Update Ewe products with skuId and fullCategoryPath
        // Map Ewe data to csv header
        // Generate csv
    }

    private HttpHeaders createHeaders(String fileName, int size) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add("number_of_products", String.valueOf(size));
        return headers;
    }
}