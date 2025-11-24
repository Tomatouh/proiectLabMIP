package org.example;

@FunctionalInterface
public interface DiscountStrategy {
    double calculeazaReducere(Order order);
}