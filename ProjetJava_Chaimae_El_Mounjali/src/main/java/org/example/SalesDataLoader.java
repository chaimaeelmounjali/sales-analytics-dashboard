package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SalesDataLoader {


    // Méthode pour charger les données CSV
    public static List<String[]> loadCSV(String csvFile) {
        List<String[]> data = new ArrayList<>();
        String line;
        String csvSplitBy = ",";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            // Lire l'en-tête
            String headerLine = br.readLine();
            if (headerLine != null) {
                String[] headers = headerLine.split(csvSplitBy);
                System.out.println("En-têtes du fichier CSV :");
                for (String header : headers) {
                    System.out.print(header + " | ");
                }
                System.out.println("\n---------------------------------------------");
            }

            // Lire les lignes suivantes et les stocker dans la liste
            while ((line = br.readLine()) != null) {
                String[] values = line.split(csvSplitBy);
                data.add(values);
            }

        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
        }

        return data;
    }
}
