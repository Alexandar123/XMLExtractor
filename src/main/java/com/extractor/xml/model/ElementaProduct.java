package com.extractor.xml.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ElementaProduct extends Product {
    private int skuId;
    private int elementaId;
    private String opis;
    private String naziv;
    private String uvoznik;
    private String proizvodjac;
    private String zemljaPorekla;
    private String zemljaUvoza;
    private String link;
    private String sifraDobavljaca;
    private String unit;

    private Double cena;
    private Double vpCena;
    private Double netoCena;
    private Double akcijskaCena;
    private Double rabat;
    private Double akcijskiRabat;
    private Double ukupanRabat;
    private int lagerVp;
    private String slika;
    private String slika2;
    private String slika3;
    private String slika4;
    private String slika5;
    private String slika6;
    private String fullCategoryPath;
    private String tipProizvoda;
    private String ocenaUsaglasenosti;
    private String uputstvo;
    private String barkod;
    private Map<String, String> specifications;
}