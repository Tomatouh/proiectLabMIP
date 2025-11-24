package org.example;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.util.Collections;

public class Order {
    private List<OrderItem> items = new ArrayList<>();
    // Modificat: TAX_RATE nu mai e static final, ci e setat dinamic
    private double taxRate = 0.09; // Valoare default pentru siguranță
    private static final DecimalFormat DF = new DecimalFormat("#0.00");

    // Strategia default: 0 reducere (lambda simplu)
    private DiscountStrategy discountStrategy = order -> 0.0;

    // NOU: Setter pentru a seta cota TVA din AppConfig
    public void setTaxRate(double taxRate) {
        // Asigurăm că valoarea e rezonabilă
        if (taxRate >= 0) {
            this.taxRate = taxRate;
        }
    }

    public void addItem(Produs produs, int cantitate) {
        if (cantitate <= 0) return;
        items.add(new OrderItem(produs, cantitate));
    }

    // Setter pentru a schimba regula "din zbor"
    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        this.discountStrategy = discountStrategy;
    }

    // Avem nevoie de acces la iteme pentru a calcula reducerile complexe
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getSubtotal() {
        double sum = 0.0;
        for (OrderItem it : items) {
            sum += it.getLinieTotal();
        }
        return sum;
    }

    // Calculăm valoarea reducerii pe baza strategiei curente
    public double getDiscountValue() {
        return discountStrategy.calculeazaReducere(this);
    }

    public double getTax() {
        // Taxa se aplică de obicei la suma de după reducere
        double bazaImpozabila = getSubtotal() - getDiscountValue();
        // Asigurăm că nu taxăm negativ
        if (bazaImpozabila < 0) bazaImpozabila = 0;
        // Modificat: Utilizăm taxRate setat dinamic
        return bazaImpozabila * taxRate;
    }

    public double getTotal() {
        return (getSubtotal() - getDiscountValue()) + getTax();
    }

    public void printReceipt() {
        System.out.println("----- Comandă client -----");
        for (OrderItem it : items) {
            Produs p = it.getProdus();
            int q = it.getCantitate();
            double unit = p.getPret();
            double line = it.getLinieTotal();
            System.out.printf("%d x %s @ %s RON = %s RON%n",
                    q,
                    p.getNume(),
                    DF.format(unit),
                    DF.format(line));
        }
        System.out.println("--------------------------");
        System.out.println("Subtotal:       " + DF.format(getSubtotal()) + " RON");

        // Afișăm reducerea doar dacă există
        double discount = getDiscountValue();
        if (discount > 0) {
            System.out.println("Discount (Promo): -" + DF.format(discount) + " RON");
        }

        // Modificat: Afișăm procentul TVA dinamic
        int tvaProcent = (int) (taxRate * 100);
        System.out.println("Tax (" + tvaProcent + "%):       " + DF.format(getTax()) + " RON");
        System.out.println("Total:          " + DF.format(getTotal()) + " RON");
        System.out.println("--------------------------");
    }
}