package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.io.File;
import java.util.List;
import java.util.Arrays;

public class Main {
    // Definirea numelor de fișiere
    private static final String CONFIG_FILE = "config.json";
    private static final String EXPORT_FILE = "meniu_export.json";

    public static void main(String[] args) throws IOException {
        MenuService menuService = new MenuService();
        AppConfig config = readConfig(); // 1. Citire Robustă a Configurației (0.5p)

        // Verificare dacă citirea a reușit, altfel ne oprim
        if (config == null) {
            // Mesajul de eroare a fost afișat în readConfig(). Ieșire din aplicație.
            return;
        }

        // --- Aplicarea Configurației (0.5p) ---
        Order comanda = new Order();
        comanda.setTaxRate(config.getCotaTVA()); // Setează TVA-ul dinamic
        String restaurantName = config.getNumeRestaurant();
        System.out.println("--- Aplicație Porită: " + restaurantName + " ---");


        // --- 2. POPULARE MENIU (Ca înainte) ---
        menuService.addProdus(new Mancare("Pizza Margherita", 45.0, 450, CategorieProdus.FEL_PRINCIPAL, true));
        menuService.addProdus(new Mancare("Paste Carbonara", 52.5, 400, CategorieProdus.FEL_PRINCIPAL, false));
        menuService.addProdus(new Mancare("Bruschete", 25.0, 200, CategorieProdus.APERITIV, true));
        menuService.addProdus(new Mancare("Lava Cake", 30.0, 150, CategorieProdus.DESERT, true));
        menuService.addProdus(new Mancare("Tiramisu", 32.0, 180, CategorieProdus.DESERT, true));
        menuService.addProdus(new Bautura("Limonadă", 15.0, 400, CategorieProdus.BAUTURI_RACORITOARE));
        menuService.addProdus(new Bautura("Vin Roșu Premium", 120.0, 750, CategorieProdus.BAUTURI_ALCOOLICE));

        // --- 3. DEMO PIZZA BUILDER (Marea Provocare) - Păstrat pentru demonstrație inițială ---
        System.out.println("\n>>> Demo: Creare Pizza Customizabila...");
        // Utilizare fluentă a Builder Pattern
        Produs myPizza = new PizzaCustomizabila.Builder(PizzaCustomizabila.Blat.PUFOS, PizzaCustomizabila.Sos.BBQ)
                .addTopping("Salam")
                .addTopping("Ciuperci")
                .addTopping("Extra Mozzarella")
                .build();
        menuService.addProdus(myPizza); // Adăugat în meniu pentru a fi exportat/raportat
        System.out.println("Produs creat: " + myPizza);

        // --- 4. EXPORT MENIU (0.5p) ---
        System.out.println("\n>>> Funcționalitate Export Meniu:");
        menuService.exportMeniuToJson(EXPORT_FILE);


        // --- 5. DEMO MANAGEMENT QUERIES (Ca înainte) ---
        System.out.println("\n>>> Raport Managerial:");

        // a) Vegetariene (Interogare 1)
        System.out.println("1. Preparate Vegetariene (sortate): " + menuService.getProduseVegetarieneSortate());

        // b) Pret mediu desert (Interogare 2)
        System.out.printf("2. Preț mediu deserturi: %.2f RON%n", menuService.getPretMediuDeserturi());

        // c) Produse scumpe (Interogare 3)
        Optional<Produs> scump = menuService.getProdusMaiScumpDe(100.0);
        scump.ifPresentOrElse(
                p -> System.out.println("3. Am găsit produs scump: " + p.getNume() + " (" + p.getPret() + " RON)"),
                () -> System.out.println("3. Nu avem produse peste 100 RON")
        );

        // Afișare meniu grupat (Utilitar pentru vizualizare)
        menuService.afiseazaMeniuPeCategorii();
        // Demonstrație extragere pe categorie
        System.out.println("\n--- Doar Băuturi Alcoolice ---");
        menuService.getProduseDupaCategorie(CategorieProdus.BAUTURI_ALCOOLICE).forEach(System.out::println);


        // --- 6. FLUX COMANDĂ (Căutare Sigură) ---
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        // Strategie Happy Hour (Ramâne neschimbată)
        DiscountStrategy happyHour = (order) -> {
            double totalDiscount = 0.0;
            for (OrderItem item : order.getItems()) {
                // Se aplică doar la BĂUTURI
                if (item.getProdus().getCategorie() == CategorieProdus.BAUTURI_ALCOOLICE ||
                        item.getProdus().getCategorie() == CategorieProdus.BAUTURI_RACORITOARE) {
                    totalDiscount += item.getLinieTotal() * 0.20;
                }
            }
            return totalDiscount;
        };
        comanda.setDiscountStrategy(happyHour);

        System.out.println("\n>>> Începe preluarea comenzii (Căutare după nume). Tastați 'gata' pentru a termina.");
        System.out.println(">>> Sfat: Tastați 'pizza custom' pentru a configura o pizza."); // NOU SFAT

        while (true) {
            System.out.print("Caută produs (ex: pizza, vin, lama): ");
            String input = br.readLine().trim();

            if (input.equalsIgnoreCase("gata")) break;
            if (input.isEmpty()) continue;

            // NOU: Detectarea input-ului special pentru Pizza Customizabilă
            if (input.equalsIgnoreCase("pizza custom")) {
                Optional<Produs> customPizza = buildCustomPizzaFromCLI(br);

                if (customPizza.isPresent()) {
                    Produs gasit = customPizza.get();
                    System.out.println("Pizza ta a fost creată: " + gasit);

                    // Adăugare directă în comandă (presupunem cantitate 1 pentru pizza custom)
                    // NU adaugăm în menuService pentru că e un produs one-off
                    comanda.addItem(gasit, 1);
                    System.out.println("Adăugat 1x Pizza Customizabilă la comandă!");
                } else {
                    System.out.println("Configurare pizza anulată.");
                }
                continue; // Trecem la următoarea interogare
            }


            // Utilizare Optional pentru Safe Search (0.4p)
            Optional<Produs> rezultat = menuService.cautaProdus(input);

            if (rezultat.isPresent()) {
                Produs gasit = rezultat.get();
                System.out.println("Găsit: " + gasit);

                System.out.print("Cantitate: ");
                try {
                    int qty = Integer.parseInt(br.readLine().trim());
                    comanda.addItem(gasit, qty);
                    System.out.println("Adăugat!");
                } catch (NumberFormatException e) {
                    System.out.println("Cantitate invalidă.");
                }
            } else {
                // Gestionare elegantă a erorii (fără a 'crăpa')
                System.out.println("Produsul '" + input + "' nu a fost găsit. Încercați altceva.");
            }
        }

        comanda.printReceipt();
    }

    // --- Metoda pentru citirea robustă a configurației (0.5p) ---
    private static AppConfig readConfig() {
        Gson gson = new Gson();
        File configFile = new File(CONFIG_FILE); // Folosim File pentru a obține calea absolută

        // DIAGNOSTIC: Afișează calea absolută pe care o caută programul.
        System.out.println("Căutare fișier de configurare la calea: " + configFile.getAbsolutePath());

        try (BufferedReader br = new BufferedReader(new FileReader(configFile))) {
            // Încercăm să parsăm JSON-ul în obiectul AppConfig
            AppConfig config = gson.fromJson(br, AppConfig.class);

            // Verificare simplă (ex: am citit ceva, cota TVA e rezonabilă)
            if (config == null || config.getCotaTVA() < 0) {
                throw new JsonSyntaxException("Date de configurare invalide (null sau TVA negativ)");
            }
            System.out.println("Configurație citită cu succes.");
            return config;

        } catch (FileNotFoundException e) {
            // 1. Tratarea Fișierului Lipsă
            // Informăm utilizatorul despre directorul de lucru (user.dir) unde trebuie să plaseze fișierul
            System.err.println("\n❌ Eroare Fatală: Fișierul de configurare (" + CONFIG_FILE + ") lipsește sau nu poate fi accesat.\n" +
                    "Directorul de lucru curent este: " + System.getProperty("user.dir") + "\n" +
                    "Vă rugăm contactați suportul tehnic.");
            return null; // Oprim execuția
        } catch (JsonSyntaxException e) {
            // 2. Tratarea Fișierului Corupt (JSON invalid)
            System.err.println("\n❌ Eroare Fatală: Fișierul de configurare (" + CONFIG_FILE + ") este corupt.\n" +
                    "Verificați sintaxa JSON. Detaliu: " + e.getMessage());
            return null; // Oprim execuția
        } catch (IOException e) {
            // Tratarea altor erori de I/O
            System.err.println("❌ Eroare Fatală de citire a fișierului de configurare: " + e.getMessage());
            return null; // Oprim execuția
        }
    }

    // --- NOU: Metodă auxiliară pentru a construi Pizza Customizabilă ---
    private static Optional<Produs> buildCustomPizzaFromCLI(BufferedReader br) throws IOException {
        System.out.println("\n--- Configurator Pizza Customizabilă ---");

        // 1. Alege Blatul
        PizzaCustomizabila.Blat blat = selectBlat(br);
        if (blat == null) {
            return Optional.empty(); // Anulat
        }

        // 2. Alege Sosul
        PizzaCustomizabila.Sos sos = selectSos(br);
        if (sos == null) {
            return Optional.empty(); // Anulat
        }

        // Începe construirea cu elementele obligatorii
        PizzaCustomizabila.Builder builder = new PizzaCustomizabila.Builder(blat, sos);

        // 3. Adaugă Topping-uri
        selectToppinguri(br, builder);

        // 4. Construiește și returnează
        return Optional.of(builder.build());
    }

    // NOU: Metodă auxiliară pentru selectarea Blatului
    private static PizzaCustomizabila.Blat selectBlat(BufferedReader br) throws IOException {
        List<String> options = Arrays.asList(
                PizzaCustomizabila.Blat.PUFOS.name() + " (Default)",
                PizzaCustomizabila.Blat.SUBTIRE.name(),
                PizzaCustomizabila.Blat.INTEGRAL.name()
        );
        System.out.println("Alege tipul de blat (1-" + options.size() + "):");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.print("Alegere (număr sau tastați 'anuleaza'): ");

        String input = br.readLine().trim();
        if (input.equalsIgnoreCase("anuleaza")) return null;

        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= options.size()) {
                // Mapează alegerea numerică la valoarea enum corespunzătoare
                return PizzaCustomizabila.Blat.values()[choice - 1];
            }
            System.out.println("Alegere invalidă. Se folosește PUFOS.");
            return PizzaCustomizabila.Blat.PUFOS; // Default
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Format invalid. Se folosește PUFOS.");
            return PizzaCustomizabila.Blat.PUFOS; // Default
        }
    }

    // NOU: Metodă auxiliară pentru selectarea Sosului
    private static PizzaCustomizabila.Sos selectSos(BufferedReader br) throws IOException {
        List<String> options = Arrays.asList(
                PizzaCustomizabila.Sos.ROSII.name() + " (Default)",
                PizzaCustomizabila.Sos.ALB.name(),
                PizzaCustomizabila.Sos.BBQ.name()
        );
        System.out.println("Alege tipul de sos (1-" + options.size() + "):");
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.print("Alegere (număr sau tastați 'anuleaza'): ");

        String input = br.readLine().trim();
        if (input.equalsIgnoreCase("anuleaza")) return null;

        try {
            int choice = Integer.parseInt(input);
            if (choice >= 1 && choice <= options.size()) {
                // Mapează alegerea numerică la valoarea enum corespunzătoare
                return PizzaCustomizabila.Sos.values()[choice - 1];
            }
            System.out.println("Alegere invalidă. Se folosește ROSII.");
            return PizzaCustomizabila.Sos.ROSII; // Default
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            System.out.println("Format invalid. Se folosește ROSII.");
            return PizzaCustomizabila.Sos.ROSII; // Default
        }
    }

    // NOU: Metodă auxiliară pentru selectarea Topping-urilor
    private static void selectToppinguri(BufferedReader br, PizzaCustomizabila.Builder builder) throws IOException {
        System.out.println("Adaugă topping-uri (costă 3 RON/topping). Tastați 'gata topping' pentru a termina.");

        String[] suggestedToppings = {"Salam", "Ciuperci", "Extra Mozzarella", "Măsline", "Ardei", "Șuncă"};
        System.out.println("Sugestii: " + String.join(", ", suggestedToppings));

        while (true) {
            System.out.print("Topping: ");
            String topping = br.readLine().trim();

            if (topping.equalsIgnoreCase("gata topping")) break;
            if (topping.isEmpty()) continue;

            builder.addTopping(topping);
            // Accesăm field-ul toppinguri direct din Builder pentru a afișa numărul curent
            System.out.println("Topping adăugat. Total topping-uri: " + builder.getToppinguri().size());
        }
    }
}