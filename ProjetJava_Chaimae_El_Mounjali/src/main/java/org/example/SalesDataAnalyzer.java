package org.example;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Classe pour l'analyse des données de ventes à partir d'un fichier CSV.
 * Fournit diverses méthodes pour calculer des statistiques et métriques de vente.
 */
public class SalesDataAnalyzer {
    // Liste des données de vente, chaque entrée est une Map représentant une ligne du CSV
    private List<Map<String, String>> salesData;

    // En-têtes du fichier CSV
    private final String[] headers;

    // Chemin vers le fichier CSV source
    private String csvFilePath;

    /**
     * Constructeur initialisant l'analyseur avec un fichier CSV
     * @param csvFilePath Chemin vers le fichier CSV contenant les données de vente
     */
    public SalesDataAnalyzer(String csvFilePath) {
        this.salesData = new ArrayList<>();
        this.headers = loadHeaders(csvFilePath); // Charge les en-têtes
        loadData(csvFilePath); // Charge les données
        this.csvFilePath = csvFilePath;
    }

    /**
     * Charge les en-têtes du fichier CSV
     * @param csvFilePath Chemin du fichier CSV
     * @return Tableau des noms de colonnes
     */
    private String[] loadHeaders(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            String headerLine = br.readLine();
            if (headerLine != null) {
                return headerLine.split(",");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[0];
    }

    /**
     * Charge les données du fichier CSV dans la structure de données interne
     * @param csvFilePath Chemin du fichier CSV
     */
    private void loadData(String csvFilePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
            // Ignorer la ligne d'en-tête
            br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                // Split des valeurs en tenant compte des guillemets
                String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

                if (values.length >= headers.length) {
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0; i < headers.length; i++) {
                        // Nettoyage des valeurs : suppression des guillemets et espaces
                        String cleanedValue = values[i].trim();
                        if (cleanedValue.startsWith("\"") && cleanedValue.endsWith("\"") && cleanedValue.length() > 1) {
                            cleanedValue = cleanedValue.substring(1, cleanedValue.length() - 1);
                        }
                        row.put(headers[i], cleanedValue);
                    }
                    salesData.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Convertit une chaîne en BigDecimal de manière sécurisée
     * @param value Valeur à convertir
     * @return BigDecimal correspondant ou zéro en cas d'erreur
     */
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            // Nettoyage et formatage de la valeur
            String cleaned = value.trim().replace("\"", "");
            cleaned = cleaned.replace(',', '.'); // Gestion des formats européens
            return new BigDecimal(cleaned);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calcule le total des ventes
     * @return Montant total des ventes
     */
    public BigDecimal calculateTotalSales() {
        return salesData.stream()
                .map(row -> parseBigDecimal(row.get("SALES")))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calcule la moyenne des ventes
     * @return Moyenne des ventes arrondie à 2 décimales
     */
    public BigDecimal calculateAverageSales() {
        if (salesData.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = calculateTotalSales();
        return total.divide(new BigDecimal(salesData.size()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Obtient les ventes groupées par ligne de produit
     * @return Map avec les lignes de produit et leur total de ventes
     */
    public Map<String, BigDecimal> getSalesByProductLine() {
        Map<String, BigDecimal> result = new HashMap<>();

        salesData.forEach(row -> {
            String productLine = row.getOrDefault("PRODUCTLINE", "Unknown");
            BigDecimal sales = parseBigDecimal(row.get("SALES"));

            result.put(productLine, result.getOrDefault(productLine, BigDecimal.ZERO).add(sales));
        });

        return result;
    }

    /**
     * Obtient les ventes groupées par trimestre
     * @return Map avec les trimestres et leur total de ventes
     */
    public Map<String, BigDecimal> getSalesByQuarter() {
        Map<String, BigDecimal> result = new HashMap<>();

        salesData.forEach(row -> {
            String quarter = row.getOrDefault("QTR_ID", "Unknown");
            BigDecimal sales = parseBigDecimal(row.get("SALES"));

            result.put(quarter, result.getOrDefault(quarter, BigDecimal.ZERO).add(sales));
        });

        return result;
    }

    /**
     * Obtient les ventes groupées par année
     * @return Map avec les années et leur total de ventes
     */
    public Map<String, BigDecimal> getSalesByYear() {
        Map<String, BigDecimal> result = new HashMap<>();

        salesData.forEach(row -> {
            String year = row.getOrDefault("YEAR_ID", "Unknown");
            BigDecimal sales = parseBigDecimal(row.get("SALES"));

            result.put(year, result.getOrDefault(year, BigDecimal.ZERO).add(sales));
        });

        return result;
    }

    /**
     * Obtient les ventes groupées par mois (format YYYY-MM)
     * @return Map avec les mois et leur total de ventes
     */
    public Map<String, BigDecimal> getSalesByMonth() {
        Map<String, BigDecimal> result = new HashMap<>();

        salesData.forEach(row -> {
            String month = row.getOrDefault("MONTH_ID", "Unknown");
            String year = row.getOrDefault("YEAR_ID", "Unknown");
            String key = year + "-" + String.format("%02d", Integer.parseInt(month));
            BigDecimal sales = parseBigDecimal(row.get("SALES"));

            result.put(key, result.getOrDefault(key, BigDecimal.ZERO).add(sales));
        });

        return result;
    }

    /**
     * Obtient les ventes groupées par pays
     * @return Map avec les pays et leur total de ventes
     */
    public Map<String, BigDecimal> getSalesByCountry() {
        Map<String, BigDecimal> result = new HashMap<>();

        salesData.forEach(row -> {
            String country = row.getOrDefault("COUNTRY", "Unknown");
            BigDecimal sales = parseBigDecimal(row.get("SALES"));

            result.put(country, result.getOrDefault(country, BigDecimal.ZERO).add(sales));
        });

        return result;
    }

    /**
     * Obtient les meilleurs clients par volume de ventes
     * @param limit Nombre de clients à retourner
     * @return Map ordonnée des clients et leur total de ventes (du plus élevé au moins élevé)
     */
    public Map<String, BigDecimal> getTopCustomers(int limit) {
        Map<String, BigDecimal> customerSales = new HashMap<>();

        salesData.forEach(row -> {
            String customer = row.getOrDefault("CUSTOMERNAME", "Unknown");
            BigDecimal sales = parseBigDecimal(row.get("SALES"));

            customerSales.put(customer, customerSales.getOrDefault(customer, BigDecimal.ZERO).add(sales));
        });

        return customerSales.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Obtient les produits les plus vendus par quantité
     * @param limit Nombre de produits à retourner
     * @return Map ordonnée des produits et leur quantité vendue (du plus élevé au moins élevé)
     */
    public Map<String, Integer> getTopSellingProducts(int limit) {
        Map<String, Integer> productSales = new HashMap<>();

        salesData.forEach(row -> {
            String product = row.getOrDefault("PRODUCTCODE", "Unknown");
            int quantity = 0;
            try {
                quantity = Integer.parseInt(row.getOrDefault("QUANTITYORDERED", "0").trim());
            } catch (NumberFormatException e) {
                quantity = 0;
            }

            productSales.put(product, productSales.getOrDefault(product, 0) + quantity);
        });

        return productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * Analyse la distribution des statuts de commande
     * @return Map avec les statuts et leur nombre d'occurrences
     */
    public Map<String, Long> getOrderStatusDistribution() {
        return salesData.stream()
                .collect(Collectors.groupingBy(
                        row -> row.getOrDefault("STATUS", "Unknown"),
                        Collectors.counting()
                ));
    }

    /**
     * Obtient la distribution des tailles de deals
     * @return Map avec les tailles et leur nombre d'occurrences
     */
    public Map<String, Long> getDealSizeDistribution() {
        return salesData.stream()
                .collect(Collectors.groupingBy(
                        row -> row.getOrDefault("DEALSIZE", "Unknown"),
                        Collectors.counting()
                ));
    }

    /**
     * Calcule le ratio vente/stock pour chaque produit
     * @return Map avec les produits et leur ratio vente/stock
     */
    public Map<String, Double> getSalesToStockRatio() {
        Map<String, Double> result = new HashMap<>();

        salesData.forEach(row -> {
            String product = row.getOrDefault("PRODUCTCODE", "Unknown");
            BigDecimal sales = parseBigDecimal(row.get("SALES"));
            String stockStr = row.getOrDefault("QUANTITYINSTOCK", "0");

            try {
                int stock = Integer.parseInt(stockStr.trim());
                if (stock > 0) {
                    double ratio = sales.divide(new BigDecimal(stock), 4, RoundingMode.HALF_UP).doubleValue();
                    result.put(product, ratio);
                }
            } catch (NumberFormatException e) {
                // Ignorer les erreurs de conversion
            }
        });

        return result;
    }

    /**
     * Obtient la distribution des prix unitaires par tranches de 10
     * @return Map avec les tranches de prix et leur nombre d'occurrences
     */
    public Map<String, Long> getPriceDistribution() {
        return salesData.stream()
                .collect(Collectors.groupingBy(
                        row -> {
                            BigDecimal price = parseBigDecimal(row.get("PRICEEACH"));
                            int range = price.divide(new BigDecimal(10), 0, RoundingMode.DOWN).intValue();
                            return (range * 10) + "-" + ((range + 1) * 10);
                        },
                        Collectors.counting()
                ));
    }

    /**
     * Calcule les statistiques de base (min, max, avg, sum) pour une colonne numérique
     * @param columnName Nom de la colonne à analyser
     * @return Map contenant les statistiques calculées
     */
    public Map<String, BigDecimal> getNumericColumnStats(String columnName) {
        Map<String, BigDecimal> stats = new HashMap<>();

        List<BigDecimal> values = salesData.stream()
                .map(row -> parseBigDecimal(row.get(columnName)))
                .filter(val -> val.compareTo(BigDecimal.ZERO) != 0) // Exclure les zéros
                .collect(Collectors.toList());

        if (values.isEmpty()) {
            stats.put("min", BigDecimal.ZERO);
            stats.put("max", BigDecimal.ZERO);
            stats.put("avg", BigDecimal.ZERO);
            stats.put("sum", BigDecimal.ZERO);
            return stats;
        }

        stats.put("min", Collections.min(values));
        stats.put("max", Collections.max(values));

        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.put("sum", sum);
        stats.put("avg", sum.divide(new BigDecimal(values.size()), 2, RoundingMode.HALF_UP));

        return stats;
    }

    /**
     * Trouve le produit le plus cher
     * @return Map contenant le code, la description et le prix du produit
     */
    public Map<String, String> getMostExpensiveProduct() {
        return salesData.stream()
                .max(Comparator.comparing(row -> parseBigDecimal(row.get("PRICEEACH"))))
                .map(row -> {
                    Map<String, String> product = new HashMap<>();
                    product.put("code", row.getOrDefault("PRODUCTCODE", "Unknown"));
                    product.put("description", row.getOrDefault("PRODUCTNAME", row.getOrDefault("PRODUCTDESCRIPTION", "Unknown")));
                    product.put("price", row.getOrDefault("PRICEEACH", "0"));
                    return product;
                })
                .orElse(new HashMap<>());
    }

    /**
     * Trouve le produit le moins cher (excluant les prix zéro)
     * @return Map contenant le code, la description et le prix du produit
     */
    public Map<String, String> getLeastExpensiveProduct() {
        return salesData.stream()
                .filter(row -> parseBigDecimal(row.get("PRICEEACH")).compareTo(BigDecimal.ZERO) > 0)
                .min(Comparator.comparing(row -> parseBigDecimal(row.get("PRICEEACH"))))
                .map(row -> {
                    Map<String, String> product = new HashMap<>();
                    product.put("code", row.getOrDefault("PRODUCTCODE", "Unknown"));
                    product.put("description", row.getOrDefault("PRODUCTNAME", row.getOrDefault("PRODUCTDESCRIPTION", "Unknown")));
                    product.put("price", row.getOrDefault("PRICEEACH", "0"));
                    return product;
                })
                .orElse(new HashMap<>());
    }

    /**
     * Groupe les produits par tranches de prix
     * @param priceBandSize Taille de la tranche de prix
     * @return Map avec les tranches de prix et le nombre de produits
     */
    public Map<String, Long> getProductsByPriceBands(BigDecimal priceBandSize) {
        return salesData.stream()
                .collect(Collectors.groupingBy(
                        row -> {
                            BigDecimal price = parseBigDecimal(row.get("PRICEEACH"));
                            int band = price.divide(priceBandSize, 0, RoundingMode.DOWN).intValue();
                            BigDecimal lowerBound = priceBandSize.multiply(new BigDecimal(band));
                            BigDecimal upperBound = priceBandSize.multiply(new BigDecimal(band + 1));
                            return lowerBound + " - " + upperBound;
                        },
                        Collectors.counting()
                ));
    }

    /**
     * Calcule le prix moyen par ligne de produit
     * @return Map avec les lignes de produit et leur prix moyen
     */
    public Map<String, BigDecimal> getAveragePriceByProductLine() {
        Map<String, List<BigDecimal>> pricesByProductLine = new HashMap<>();

        salesData.forEach(row -> {
            String productLine = row.getOrDefault("PRODUCTLINE", "Unknown");
            BigDecimal price = parseBigDecimal(row.get("PRICEEACH"));

            if (price.compareTo(BigDecimal.ZERO) > 0) {
                pricesByProductLine.computeIfAbsent(productLine, k -> new ArrayList<>()).add(price);
            }
        });

        Map<String, BigDecimal> result = new HashMap<>();
        pricesByProductLine.forEach((productLine, prices) -> {
            BigDecimal sum = prices.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = sum.divide(new BigDecimal(prices.size()), 2, RoundingMode.HALF_UP);
            result.put(productLine, avg);
        });

        return result;
    }

    /**
     * Ajoute une nouvelle entrée aux données et au fichier CSV
     * @param newEntry Nouvelle entrée à ajouter
     */
    public void addNewEntry(Map<String, String> newEntry) {
        // Ajout à la structure de données en mémoire
        salesData.add(newEntry);

        // Ajout au fichier CSV
        try (PrintWriter writer = new PrintWriter(new FileWriter(csvFilePath, true))) {
            writer.println(String.join(",", newEntry.values()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Obtient les données de vente brutes
     * @return Liste des données de vente
     */
    public List<Map<String, String>> getSalesData() {
        return salesData;
    }
}