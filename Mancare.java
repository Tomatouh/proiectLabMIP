package org.example;

public final class Mancare extends Produs {
    private int gramaj;
    private boolean isVegetarian;

    public Mancare(String nume, double pret, int gramaj, CategorieProdus categorie, boolean isVegetarian) {
        super(nume, pret, categorie); // Modificat: adăugat categorie
        this.gramaj = gramaj;
        this.isVegetarian = isVegetarian;
    }

    public boolean isVegetarian() {
        return isVegetarian;
    }

    @Override
    public String getDetalii() {
        return "Gramaj: " + gramaj + "g" + (isVegetarian ? " (Veg)" : "");
    }
}