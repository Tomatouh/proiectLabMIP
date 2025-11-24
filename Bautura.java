package org.example;

public final class Bautura extends Produs {
    private int volum; // în mililitri

    public Bautura(String nume, double pret, int volum, CategorieProdus categorie) {
        super(nume, pret, categorie); // Modificat: adăugat categorie
        this.volum = volum;
    }

    @Override
    public String getDetalii() {
        return "Volum: " + volum + "ml";
    }
}