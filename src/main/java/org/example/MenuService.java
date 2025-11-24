package org.example;

import java.util.*;
import java.util.stream.Collectors;
// NOU: Importuri pentru export
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.FileWriter;
import java.io.IOException;

public class MenuService {
    private List<Produs> meniu = new ArrayList<>();

    public void addProdus(Produs p) {
        meniu.add(p);
    }

    public List<Produs> getProduseDupaCategorie(CategorieProdus categorie) {
        return meniu.stream()
                .filter(p -> p.getCategorie() == categorie)
                .collect(Collectors.toList());
    }

    // --- INTEROGARE 1: Filtrare și Sortare (0.2p) ---
    // "Care sunt toate preparatele vegetariene, sortate alfabetic?"
    public List<String> getProduseVegetarieneSortate() {
        return meniu.stream()
                // 1. Filtrare: Păstrăm doar obiectele Mancare care sunt vegetariene
                .filter(p -> p instanceof Mancare && ((Mancare) p).isVegetarian())
                // 2. Sortare: Comparăm după nume
                .sorted(Comparator.comparing(Produs::getNume))
                // 3. Mapare: Extragem doar numele (String) din obiectul Produs
                .map(Produs::getNume)
                // 4. Colectare: Transformăm stream-ul înapoi în Listă
                .collect(Collectors.toList());
    }

    // --- INTEROGARE 2: Agregare (Media aritmetică) (0.2p) ---
    // "Care este prețul mediu al deserturilor?"
    public double getPretMediuDeserturi() {
        return meniu.stream()
                // Filtrăm doar deserturile
                .filter(p -> p.getCategorie() == CategorieProdus.DESERT)
                // Transformăm stream-ul de obiecte în stream de primitive (double)
                .mapToDouble(Produs::getPret)
                // Calculăm media (returnează un OptionalDouble)
                .average()
                // Dacă nu există deserturi, returnăm 0.0 pentru a evita excepțiile
                .orElse(0.0);
    }

    // --- INTEROGARE 3: Căutare condiționată (Finder) (0.2p) ---
    // "Avem vreun preparat care costă mai mult de X RON?"
    public Optional<Produs> getProdusMaiScumpDe(double pragPret) {
        return meniu.stream()
                .filter(p -> p.getPret() > pragPret)
                // Găsește primul element care satisface condiția (scurt-circuitează procesarea)
                .findFirst();
    }

    // --- Căutare Sigură (Safe Search) (0.4p) ---
    // Pentru tableta ospătarului - returnează Optional
    public Optional<Produs> cautaProdus(String textCautat) {
        if (textCautat == null || textCautat.trim().isEmpty()) {
            return Optional.empty();
        }
        String cautatLower = textCautat.toLowerCase();
        return meniu.stream()
                // Căutare case-insensitive care conține textul
                .filter(p -> p.getNume().toLowerCase().contains(cautatLower))
                .findFirst();
    }

    // --- Afișare Meniu Grupat (Utilitar) ---
    public Map<CategorieProdus, List<Produs>> getMeniuGrupatPeCategorii() {
        return meniu.stream()
                .collect(Collectors.groupingBy(Produs::getCategorie));
    }

    public void afiseazaMeniuPeCategorii() {
        System.out.println("\n--- Meniul Organizat pe Categorii ---");
        getMeniuGrupatPeCategorii().forEach((categorie, produse) -> {
            System.out.println("\n### " + categorie + " ###");
            produse.forEach(p -> System.out.println("  - " + p));
        });
    }

    // --- NOU: Funcționalitate de Export în JSON (0.5p) ---
    public void exportMeniuToJson(String fileName) {
        // GsonBuilder cu pretty-printing pentru lizibilitate
        // Adăugăm TypeAdapter pentru a gestiona serializarea claselor moștenite
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                // Registrul de tipuri pentru a include informații despre subclase (Mancare, Bautura, PizzaCustomizabila)
                // Aceasta este necesară pentru ca la o eventuală *deserializare* să știm ce obiect să creăm
                .registerTypeAdapter(Produs.class, new ProdusTypeAdapter()) // Implementarea ProdusTypeAdapter este necesară
                .create();

        try (FileWriter writer = new FileWriter(fileName)) {
            // Serializăm lista de produse
            gson.toJson(meniu, writer);
            System.out.println("Meniul a fost exportat cu succes în: " + fileName);
        } catch (IOException e) {
            // Gestiunea simplă a erorilor de I/O
            System.err.println("Eroare la scrierea fișierului de export: " + e.getMessage());
        }
    }
}