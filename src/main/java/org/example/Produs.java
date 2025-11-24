package org.example;

// Clasă sigilată, permite extinderea doar de către clasele enumerate
public abstract sealed class Produs permits Mancare, Bautura, PizzaCustomizabila {
    private String nume;
    private double pret;
    private CategorieProdus categorie; // Adăugat CategorieProdus

    public Produs(String nume, double pret, CategorieProdus categorie) {
        this.nume = nume;
        this.pret = pret;
        this.categorie = categorie;
    }

    public String getNume() {
        return nume;
    }

    public double getPret() {
        return pret;
    }

    public CategorieProdus getCategorie() {
        return categorie;
    }

    public abstract String getDetalii();

    @Override
    public String toString() {
        return String.format("[%s] %s - %.2f RON (%s)",
                categorie, nume, pret, getDetalii());
    }
}