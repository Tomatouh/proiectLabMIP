package org.example;

public class OrderItem {
    private Produs produs;
    private int cantitate;

    public OrderItem(Produs produs, int cantitate) {
        this.produs = produs;
        this.cantitate = cantitate;
    }

    public Produs getProdus() {
        return produs;
    }

    public int getCantitate() {
        return cantitate;
    }

    public double getLinieTotal() {
        return produs.getPret() * cantitate;
    }
}
