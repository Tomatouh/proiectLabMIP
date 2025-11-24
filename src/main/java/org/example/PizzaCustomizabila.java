package org.example;

import java.util.ArrayList;
import java.util.List;

public final class PizzaCustomizabila extends Produs {
    // Ingrediente specifice
    public enum Blat { SUBTIRE, PUFOS, INTEGRAL }
    public enum Sos { ROSII, ALB, BBQ }

    private final Blat blat;
    private final Sos sos;
    private final List<String> toppinguri;

    // Constructor privat - accesibil doar prin Builder (0.5p)
    private PizzaCustomizabila(Builder builder) {
        // Prețul de bază este 30 RON + 3 RON per topping
        super("Pizza Custom (" + builder.blat + ")",
                30.0 + (builder.toppinguri.size() * 3.0),
                CategorieProdus.FEL_PRINCIPAL); // Categoria e Fel Principal

        this.blat = builder.blat;
        this.sos = builder.sos;
        this.toppinguri = new ArrayList<>(builder.toppinguri);
    }

    @Override
    public String getDetalii() {
        String topStr = toppinguri.isEmpty() ? "Fara topping" : String.join(", ", toppinguri);
        return "Blat: " + blat + ", Sos: " + sos + ", Extra: " + topStr;
    }

    // --- BUILDER STATIC (0.5p) ---
    public static class Builder {
        // Câmpuri obligatorii (setate în constructor)
        private final Blat blat;
        private final Sos sos;

        // Câmpuri opționale
        private final List<String> toppinguri = new ArrayList<>();
        public List<String> getToppinguri() {
            return toppinguri;
        }
        // Constructorul Builder impune elementele obligatorii
        public Builder(Blat blat, Sos sos) {
            this.blat = blat;
            this.sos = sos;
        }

        // Metodă pentru a adăuga topping-uri opționale (chaining)
        public Builder addTopping(String topping) {
            this.toppinguri.add(topping);
            return this; // Returnăm this pentru chaining (metode înlănțuite)
        }

        // Metoda finală de construcție
        public PizzaCustomizabila build() {
            return new PizzaCustomizabila(this);
        }
    }
}