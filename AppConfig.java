package org.example;

// Clasa POJO pentru a reprezenta structura fisierului de configurare
public class AppConfig {
    private String numeRestaurant;
    private double cotaTVA;

    // Getteri
    public String getNumeRestaurant() {
        return numeRestaurant;
    }

    public double getCotaTVA() {
        return cotaTVA;
    }
}