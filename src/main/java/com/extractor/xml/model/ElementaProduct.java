package com.extractor.xml.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ElementaProduct {
    private int skuId;
    private int elementaId;
    private String name;
    private String uvoznik;
    private String zemljaPorekla;
    private String sifraDobavljaca;
    //1
    private String nadredjenaKategorija;
    //2
    private String primarnaKategorija;
    //3
    private String sekundarnaKategorija;

    private Double nabavnaCena;
    private Double maloprodajnaCena;
    private Double rabat;

    private String fullCategoryPath;
    private List<String> images;
}
