package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;

/**
 * Classe pour visualiser les données de ventes avec des graphiques JavaFX
 */
public class SalesDataVisualizer extends Application {

    private static final String DEFAULT_CSV_FILE_PATH = "sales_data_sample.csv";
    private SalesDataAnalyzer analyzer;
    private TabPane tabPane;
    private Stage primaryStage;
    private String currentFilePath;

    // Pour les thèmes et styles
    private final String LIGHT_STYLE = "/styles/light-theme.css";
    private final String DARK_STYLE = "/styles/dark-theme.css";
    private boolean isDarkMode = false;



    @Override
    public void start(Stage primaryStage) {
        // Afficher d'abord l'écran de login
        new LoginScreen().show(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
    public void showMainApp (Stage primaryStage) {

        try {
            this.primaryStage = primaryStage;
            primaryStage.setTitle("Analyse des Ventes - Visualisation");

            // Initialiser avec le fichier par défaut
            currentFilePath = DEFAULT_CSV_FILE_PATH;
            analyzer = new SalesDataAnalyzer(currentFilePath);

            // Créer la barre de menu
            MenuBar menuBar = createMenuBar();

            // Créer le panneau d'onglets
            tabPane = new TabPane();
            tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

            // Créer le layout principal
            BorderPane mainLayout = new BorderPane();
            mainLayout.setTop(menuBar);
            mainLayout.setCenter(tabPane);

            // Créer les différents onglets
            createTabs();

            Scene scene = new Scene(mainLayout, 1200, 800);

            // Appliquer le style CSS par défaut
            if (getClass().getResource(LIGHT_STYLE) != null) {
                scene.getStylesheets().add(getClass().getResource(LIGHT_STYLE).toExternalForm());
            }

            primaryStage.setScene(scene);
            primaryStage.show();

            // Afficher un écran de démarrage
            showWelcomeScreen();
        } catch (Exception e) {
            showErrorDialog("Erreur de démarrage", "Impossible de démarrer l'application", e.getMessage());
            e.printStackTrace();
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // Menu Fichier
        Menu fileMenu = new Menu("Fichier");
        MenuItem openItem = new MenuItem("Ouvrir CSV...");
        MenuItem exportPdfItem = new MenuItem("Exporter en PDF...");
        MenuItem exitItem = new MenuItem("Quitter");

        openItem.setOnAction(e -> loadNewCsvFile());
        exportPdfItem.setOnAction(e -> showExportDialog());
        exitItem.setOnAction(e -> primaryStage.close());

        fileMenu.getItems().addAll(openItem, exportPdfItem, new SeparatorMenuItem(), exitItem);

        // Menu Affichage
        Menu viewMenu = new Menu("Affichage");
        MenuItem refreshItem = new MenuItem("Actualiser");
        MenuItem themeItem = new MenuItem("Basculer Thème");

        refreshItem.setOnAction(e -> refreshVisualization());
        themeItem.setOnAction(e -> toggleTheme());

        viewMenu.getItems().addAll(refreshItem, themeItem);

        // Menu Aide
        Menu helpMenu = new Menu("Aide");
        MenuItem aboutItem = new MenuItem("À propos");

        aboutItem.setOnAction(e -> showAboutDialog());

        helpMenu.getItems().add(aboutItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);
        return menuBar;
    }
    private void createDatabaseTab() {
        Tab tab = new Tab("Base de Données");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Importation vers Base de Données MySQL");
        headerLabel.getStyleClass().add("section-header");

        // Formulaire de connexion à la base de données
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));

        // Paramètres de connexion
        Label hostLabel = new Label("Hôte:");
        TextField hostField = new TextField("localhost");
        hostField.setPrefWidth(200);

        Label portLabel = new Label("Port:");
        TextField portField = new TextField("3306");

        Label dbNameLabel = new Label("Nom de la BDD:");
        TextField dbNameField = new TextField("sales_data");

        Label userLabel = new Label("Utilisateur:");
        TextField userField = new TextField("root");

        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();

        // Paramètres d'importation
        Label fileLabel = new Label("Fichier CSV:");
        HBox fileBox = new HBox(10);
        TextField fileField = new TextField(DEFAULT_CSV_FILE_PATH);
        fileField.setPrefWidth(300);
        Button browseButton = new Button("Parcourir");
        fileBox.getChildren().addAll(fileField, browseButton);

        // Action du bouton parcourir
        browseButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Sélectionner un fichier CSV");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
                    new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
            );

            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            if (selectedFile != null) {
                fileField.setText(selectedFile.getAbsolutePath());
            }
        });

        // Options d'importation
        CheckBox createDbCheckBox = new CheckBox("Créer la base de données si elle n'existe pas");
        createDbCheckBox.setSelected(true);

        CheckBox dropTableCheckBox = new CheckBox("Supprimer la table si elle existe déjà");
        dropTableCheckBox.setSelected(false);

        // Bouton pour tester la connexion
        Button testConnectionButton = new Button("Tester la connexion");
        testConnectionButton.setOnAction(e -> {
            testDatabaseConnection(
                    hostField.getText(),
                    portField.getText(),
                    dbNameField.getText(),
                    userField.getText(),
                    passwordField.getText()
            );
        });

        // Bouton pour importer les données
        Button importButton = new Button("Importer les données");
        importButton.getStyleClass().add("primary-button");
        importButton.setOnAction(e -> {
            importCsvToDatabase(
                    fileField.getText(),
                    hostField.getText(),
                    portField.getText(),
                    dbNameField.getText(),
                    userField.getText(),
                    passwordField.getText(),
                    createDbCheckBox.isSelected(),
                    dropTableCheckBox.isSelected()
            );
        });

        // Ajouter les éléments au formulaire
        formGrid.add(hostLabel, 0, 0);
        formGrid.add(hostField, 1, 0);
        formGrid.add(portLabel, 0, 1);
        formGrid.add(portField, 1, 1);
        formGrid.add(dbNameLabel, 0, 2);
        formGrid.add(dbNameField, 1, 2);
        formGrid.add(userLabel, 0, 3);
        formGrid.add(userField, 1, 3);
        formGrid.add(passwordLabel, 0, 4);
        formGrid.add(passwordField, 1, 4);
        formGrid.add(fileLabel, 0, 5);
        formGrid.add(fileBox, 1, 5);

        VBox optionsBox = new VBox(10);
        optionsBox.setPadding(new Insets(10));
        optionsBox.getChildren().addAll(
                createDbCheckBox,
                dropTableCheckBox
        );

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(testConnectionButton, importButton);

        // Zone de log pour afficher les messages d'importation
        Label logLabel = new Label("Journal d'importation:");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setWrapText(true);

        // Définir une méthode statique pour permettre l'accès depuis d'autres méthodes
        System.setProperty("logArea", "");

        vbox.getChildren().addAll(
                headerLabel,
                formGrid,
                optionsBox,
                buttonBox,
                logLabel,
                logArea
        );

        // Pour mettre à jour la zone de log depuis d'autres méthodes
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String logContent = System.getProperty("logArea");
                if (!logContent.equals(logArea.getText())) {
                    Platform.runLater(() -> logArea.setText(logContent));
                }
            }
        }, 0, 500);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void testDatabaseConnection(String host, String port, String dbName, String user, String password) {
        new Thread(() -> {
            appendToLog("Test de connexion à la base de données...");

            String url = "jdbc:mysql://" + host + ":" + port;

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                appendToLog("Connexion réussie à MySQL!");

                // Vérifier si la base de données existe
                try (Statement stmt = conn.createStatement()) {
                    ResultSet rs = stmt.executeQuery("SHOW DATABASES LIKE '" + dbName + "'");
                    if (rs.next()) {
                        appendToLog("La base de données '" + dbName + "' existe.");
                    } else {
                        appendToLog("La base de données '" + dbName + "' n'existe pas encore.");
                    }
                }

            } catch (SQLException e) {
                appendToLog("Erreur de connexion: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void importCsvToDatabase(String csvFilePath, String host, String port,
                                     String dbName, String user, String password,
                                     boolean createDb, boolean dropTable) {
        new Thread(() -> {
            appendToLog("Début de l'importation des données...");

            String url = "jdbc:mysql://" + host + ":" + port;
            Connection conn = null;

            try {
                // Charger le driver JDBC
                Class.forName("com.mysql.cj.jdbc.Driver");
                appendToLog("Driver MySQL chargé avec succès");

                // Établir la connexion
                conn = DriverManager.getConnection(url, user, password);
                appendToLog("Connexion établie avec MySQL");

                // Créer la base de données si nécessaire
                if (createDb) {
                    try (Statement stmt = conn.createStatement()) {
                        stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName);
                        appendToLog("Base de données '" + dbName + "' créée ou déjà existante");
                    }
                }

                // Se connecter à la base de données
                conn.setCatalog(dbName);
                appendToLog("Connecté à la base de données '" + dbName + "'");

                // Lire le fichier CSV pour déterminer les colonnes
                List<String> headers = new ArrayList<>();
                List<String> columnTypes = new ArrayList<>();

                try (BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {
                    String headerLine = br.readLine();
                    if (headerLine != null) {
                        headers = Arrays.asList(headerLine.split(","));
                        appendToLog("Colonnes détectées: " + headers.size());

                        // Déterminer les types de données pour chaque colonne
                        String dataLine = br.readLine();
                        if (dataLine != null) {
                            String[] values = dataLine.split(",");
                            for (int i = 0; i < values.length && i < headers.size(); i++) {
                                String value = values[i].replace("\"", "").trim();
                                String columnName = headers.get(i).replace("\"", "").trim();

                                // Traitement spécial pour les colonnes connues
                                if (columnName.toUpperCase().contains("DATE")) {
                                    columnTypes.add("DATE");
                                    appendToLog("Colonne '" + columnName + "' détectée comme DATE");
                                } else if (isNumeric(value)) {
                                    if (value.contains(".")) {
                                        columnTypes.add("DECIMAL(10,2)");
                                    } else {
                                        columnTypes.add("INT");
                                    }
                                } else if (isDate(value)) {
                                    columnTypes.add("DATE");
                                    appendToLog("Colonne '" + columnName + "' détectée comme DATE");
                                } else {
                                    columnTypes.add("VARCHAR(255)");
                                }
                            }
                        }
                    }
                }

                // Créer la table
                String tableName = "sales_data";
                StringBuilder createTableSQL = new StringBuilder("CREATE TABLE IF NOT EXISTS " + tableName + " (");
                createTableSQL.append("`id` INT AUTO_INCREMENT PRIMARY KEY, ");

                for (int i = 0; i < headers.size(); i++) {
                    String columnName = headers.get(i).replace("\"", "").trim();
                    String columnType = i < columnTypes.size() ? columnTypes.get(i) : "VARCHAR(255)";
                    createTableSQL.append("`").append(columnName).append("` ").append(columnType);

                    if (i < headers.size() - 1) {
                        createTableSQL.append(", ");
                    }
                }

                createTableSQL.append(")");

                try (Statement stmt = conn.createStatement()) {
                    // Supprimer la table si demandé
                    if (dropTable) {
                        stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                        appendToLog("Table '" + tableName + "' supprimée");
                    }

                    // Créer la table
                    stmt.executeUpdate(createTableSQL.toString());
                    appendToLog("Table '" + tableName + "' créée avec succès");

                    // Importer les données
                    appendToLog("Importation des données en cours...");
                    int importedRows = importDataFromCsv(conn, csvFilePath, tableName, headers, columnTypes);
                    appendToLog("Importation terminée. " + importedRows + " lignes importées.");
                }

            } catch (Exception e) {
                appendToLog("Erreur: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                        appendToLog("Connexion fermée");
                    }
                } catch (SQLException e) {
                    appendToLog("Erreur lors de la fermeture de la connexion: " + e.getMessage());
                }
            }
        }).start();
    }

    private int importDataFromCsv(Connection conn, String csvFilePath, String tableName, List<String> headers, List<String> columnTypes)
            throws SQLException, IOException {
        int count = 0;

        // Préparer la requête d'insertion
        StringBuilder insertSQL = new StringBuilder("INSERT INTO " + tableName + " (");
        StringBuilder placeholders = new StringBuilder();

        for (int i = 0; i < headers.size(); i++) {
            insertSQL.append("`").append(headers.get(i).replace("\"", "").trim()).append("`");
            placeholders.append("?");

            if (i < headers.size() - 1) {
                insertSQL.append(", ");
                placeholders.append(", ");
            }
        }

        insertSQL.append(") VALUES (").append(placeholders).append(")");

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL.toString());
             BufferedReader br = new BufferedReader(new FileReader(csvFilePath))) {

            // Ignorer la ligne d'en-tête
            String line = br.readLine();

            // Importer les données
            while ((line = br.readLine()) != null) {
                // Utiliser un analyseur CSV plus robuste pour gérer les guillemets et les virgules dans les valeurs
                String[] values = parseCSVLine(line);

                for (int i = 0; i < values.length && i < headers.size(); i++) {
                    String value = values[i].replace("\"", "").trim();
                    String columnType = i < columnTypes.size() ? columnTypes.get(i) : "VARCHAR(255)";

                    if (value.isEmpty()) {
                        pstmt.setNull(i + 1, Types.VARCHAR);
                    } else if (columnType.equals("DATE")) {
                        // Convertir les dates au format MySQL
                        try {
                            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
                            LocalDateTime dateTime = LocalDateTime.parse(value, inputFormatter);
                            LocalDate date = dateTime.toLocalDate();
                            pstmt.setDate(i + 1, java.sql.Date.valueOf(date));
                        } catch (Exception e) {
                            // Si le format de date est incorrect, essayer d'autres formats
                            try {
                                DateTimeFormatter altFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");
                                LocalDate date = LocalDate.parse(value, altFormatter);
                                pstmt.setDate(i + 1, java.sql.Date.valueOf(date));
                            } catch (Exception ex) {
                                appendToLog("Erreur de conversion de date pour la valeur: " + value);
                                pstmt.setNull(i + 1, Types.DATE);
                            }
                        }
                    } else if (columnType.equals("INT")) {
                        try {
                            pstmt.setInt(i + 1, Integer.parseInt(value));
                        } catch (NumberFormatException e) {
                            pstmt.setNull(i + 1, Types.INTEGER);
                        }
                    } else if (columnType.equals("DECIMAL(10,2)")) {
                        try {
                            pstmt.setBigDecimal(i + 1, new BigDecimal(value));
                        } catch (NumberFormatException e) {
                            pstmt.setNull(i + 1, Types.DECIMAL);
                        }
                    } else {
                        pstmt.setString(i + 1, value);
                    }
                }

                pstmt.addBatch();
                count++;

                if (count % 1000 == 0) {
                    pstmt.executeBatch();
                    appendToLog("Importé " + count + " lignes...");
                }
            }

            pstmt.executeBatch();
        }

        return count;
    }

    private String[] parseCSVLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder field = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '\"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(field.toString());
                field = new StringBuilder();
            } else {
                field.append(c);
            }
        }

        result.add(field.toString());
        return result.toArray(new String[0]);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isDate(String str) {
        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm"),
                DateTimeFormatter.ofPattern("M/d/yyyy"),
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                if (str.contains(":")) {
                    LocalDateTime.parse(str, formatter);
                } else {
                    LocalDate.parse(str, formatter);
                }
                return true;
            } catch (Exception e) {
                // Continuer avec le prochain format
            }
        }

        return false;
    }

    private void appendToLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = timestamp + " - " + message + "\n";

        String currentLog = System.getProperty("logArea");
        System.setProperty("logArea", currentLog + logMessage);
    }

    private void loadNewCsvFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un fichier CSV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );

        File selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            try {
                currentFilePath = selectedFile.getAbsolutePath();
                analyzer = new SalesDataAnalyzer(currentFilePath);
                refreshVisualization();

                // Mettre à jour le titre avec le nom du fichier
                primaryStage.setTitle("Analyse des Ventes - " + selectedFile.getName());
            } catch (Exception e) {
                showErrorDialog("Erreur de chargement", "Impossible de charger le fichier CSV", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void refreshVisualization() {
        // Effacer tous les onglets existants
        tabPane.getTabs().clear();

        // Recréer tous les onglets
        createTabs();
    }
    private void createLoadFromDatabaseTab() {
        Tab tab = new Tab("Charger depuis MySQL");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Charger les données depuis MySQL");
        headerLabel.getStyleClass().add("section-header");

        // Formulaire de connexion avec valeurs par défaut
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));

        Label hostLabel = new Label("Hôte:");
        TextField hostField = new TextField("localhost");

        Label portLabel = new Label("Port:");
        TextField portField = new TextField("3306");

        Label dbNameLabel = new Label("Nom de la BDD:");
        TextField dbNameField = new TextField("sales_data");

        Label userLabel = new Label("Utilisateur:");
        TextField userField = new TextField("root");

        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();
        passwordField.setText("123456789"); // Mot de passe par défaut

        // Bouton de chargement
        Button loadButton = new Button("Charger les données");
        loadButton.getStyleClass().add("primary-button");
        loadButton.setOnAction(e -> {
            loadDataFromDatabase(
                    hostField.getText(),
                    portField.getText(),
                    dbNameField.getText(),
                    userField.getText(),
                    passwordField.getText()
            );
        });

        // Bouton de téléchargement (initialement désactivé)
        Button downloadButton = new Button("Télécharger CSV");
        downloadButton.getStyleClass().add("secondary-button");
        downloadButton.setDisable(true); // Désactivé jusqu'à ce que les données soient chargées
        downloadButton.setOnAction(e -> downloadCurrentCSV());

        // Layout pour les boutons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(loadButton, downloadButton);

        // Zone de log
        Label logLabel = new Label("Journal:");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);
        logArea.setWrapText(true);

        // Ajouter les éléments au formulaire
        formGrid.add(hostLabel, 0, 0);
        formGrid.add(hostField, 1, 0);
        formGrid.add(portLabel, 0, 1);
        formGrid.add(portField, 1, 1);
        formGrid.add(dbNameLabel, 0, 2);
        formGrid.add(dbNameField, 1, 2);
        formGrid.add(userLabel, 0, 3);
        formGrid.add(userField, 1, 3);
        formGrid.add(passwordLabel, 0, 4);
        formGrid.add(passwordField, 1, 4);
        formGrid.add(buttonBox, 0, 5, 2, 1);

        vbox.getChildren().addAll(headerLabel, formGrid, logLabel, logArea);

        // Mise à jour du log en temps réel
        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                String logContent = System.getProperty("dbLogArea");
                if (logContent != null && !logContent.equals(logArea.getText())) {
                    Platform.runLater(() -> logArea.setText(logContent));
                }
            }
        }, 0, 500);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);

        // Ajouter une référence au bouton de téléchargement pour pouvoir l'activer plus tard
        downloadCSVButton = downloadButton;
    }

    // Attribut pour référencer le bouton de téléchargement
    private Button downloadCSVButton;
    // Attribut pour stocker le chemin du fichier CSV temporaire
    private String lastGeneratedCSVPath;

    private void loadDataFromDatabase(String host, String port, String dbName, String user, String password) {
        new Thread(() -> {
            appendToDbLog("Tentative de connexion à la base de données...");

            String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;

            try (Connection conn = DriverManager.getConnection(url, user, password)) {
                appendToDbLog("Connexion réussie à la base de données " + dbName);

                // Vérifier si la table sales_data existe
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet tables = meta.getTables(null, null, "sales_data", new String[] {"TABLE"});

                if (tables.next()) {
                    appendToDbLog("Table sales_data trouvée. Chargement des données...");

                    // Créer un fichier CSV temporaire
                    File tempFile = File.createTempFile("sales_data_", ".csv");
                    tempFile.deleteOnExit();

                    // Stocker le chemin du fichier pour le téléchargement ultérieur
                    lastGeneratedCSVPath = tempFile.getAbsolutePath();

                    try (PrintWriter writer = new PrintWriter(tempFile);
                         Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM sales_data")) {

                        // Écrire les en-têtes
                        ResultSetMetaData rsmd = rs.getMetaData();
                        int columnCount = rsmd.getColumnCount();

                        StringBuilder headerLine = new StringBuilder();
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) headerLine.append(",");
                            headerLine.append(rsmd.getColumnName(i));
                        }
                        writer.println(headerLine.toString());

                        // Écrire les données
                        int rowCount = 0;
                        while (rs.next()) {
                            StringBuilder dataLine = new StringBuilder();
                            for (int i = 1; i <= columnCount; i++) {
                                if (i > 1) dataLine.append(",");
                                String value = rs.getString(i);
                                dataLine.append(value != null ? escapeCSV(value) : "");
                            }
                            writer.println(dataLine.toString());
                            rowCount++;

                            if (rowCount % 1000 == 0) {
                                appendToDbLog("Chargement en cours... " + rowCount + " lignes extraites");
                            }
                        }

                        appendToDbLog(rowCount + " lignes chargées depuis la base de données");

                        // Mettre à jour l'analyseur avec le nouveau fichier
                        currentFilePath = tempFile.getAbsolutePath();
                        analyzer = new SalesDataAnalyzer(currentFilePath);

                        Platform.runLater(() -> {
                            refreshVisualization();
                            primaryStage.setTitle("Analyse des Ventes - Données depuis MySQL");
                            // Activer le bouton de téléchargement
                            downloadCSVButton.setDisable(false);
                        });

                        appendToDbLog("Visualisation mise à jour avec les données de la base");
                        appendToDbLog("Vous pouvez maintenant télécharger les données en CSV");

                    } catch (Exception e) {
                        appendToDbLog("Erreur lors de l'extraction des données: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    appendToDbLog("La table sales_data n'existe pas dans la base de données");
                }

            } catch (SQLException e) {
                appendToDbLog("Erreur de connexion à la base de données: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                appendToDbLog("Erreur de création du fichier temporaire: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Permet à l'utilisateur de télécharger le fichier CSV généré
     */
    private void downloadCurrentCSV() {
        if (lastGeneratedCSVPath == null || lastGeneratedCSVPath.isEmpty()) {
            appendToDbLog("Aucun fichier CSV disponible pour le téléchargement");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer le fichier CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv")
        );
        fileChooser.setInitialFileName("donnees_sales_" +
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv");

        File sourceFile = new File(lastGeneratedCSVPath);
        if (!sourceFile.exists()) {
            appendToDbLog("Erreur: Le fichier source n'existe plus");
            return;
        }

        File targetFile = fileChooser.showSaveDialog(primaryStage);
        if (targetFile != null) {
            try {
                Files.copy(sourceFile.toPath(), targetFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                appendToDbLog("Fichier CSV enregistré avec succès: " + targetFile.getAbsolutePath());
            } catch (IOException e) {
                appendToDbLog("Erreur lors de l'enregistrement du fichier: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void appendToDbLog(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = timestamp + " - " + message + "\n";

        String currentLog = System.getProperty("dbLogArea");
        if (currentLog == null) {
            currentLog = "";
        }
        System.setProperty("dbLogArea", currentLog + logMessage);
    }

    private void createAddSaleTab() {
        Tab tab = new Tab("Ajouter une vente");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.TOP_CENTER);

        Label headerLabel = new Label("Enregistrer une nouvelle vente");
        headerLabel.getStyleClass().add("section-header");

        // Formulaire d'ajout de vente
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);
        formGrid.setPadding(new Insets(20));

        // Première colonne
        int row = 0;

        Label orderNumberLabel = new Label("N° Commande:");
        TextField orderNumberField = new TextField();
        orderNumberField.setPromptText("Ex: 10103");
        formGrid.add(orderNumberLabel, 0, row);
        formGrid.add(orderNumberField, 1, row);
        row++;

        Label quantityOrderedLabel = new Label("Quantité:");
        TextField quantityOrderedField = new TextField();
        quantityOrderedField.setPromptText("Ex: 39");
        formGrid.add(quantityOrderedLabel, 0, row);
        formGrid.add(quantityOrderedField, 1, row);
        row++;

        Label priceEachLabel = new Label("Prix unitaire:");
        TextField priceEachField = new TextField();
        priceEachField.setPromptText("Ex: 94.7");
        formGrid.add(priceEachLabel, 0, row);
        formGrid.add(priceEachField, 1, row);
        row++;

        Label orderLineNumberLabel = new Label("N° Ligne:");
        TextField orderLineNumberField = new TextField();
        orderLineNumberField.setPromptText("Ex: 1");
        formGrid.add(orderLineNumberLabel, 0, row);
        formGrid.add(orderLineNumberField, 1, row);
        row++;

        Label orderDateLabel = new Label("Date commande:");
        DatePicker orderDatePicker = new DatePicker(LocalDate.now());
        formGrid.add(orderDateLabel, 0, row);
        formGrid.add(orderDatePicker, 1, row);
        row++;

        Label statusLabel = new Label("Statut:");
        ComboBox<String> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("Shipped", "In Process", "Cancelled", "Resolved", "Disputed", "On Hold");
        statusComboBox.setValue("Shipped");
        formGrid.add(statusLabel, 0, row);
        formGrid.add(statusComboBox, 1, row);
        row++;

        Label qtrIdLabel = new Label("Trimestre:");
        ComboBox<Integer> qtrIdComboBox = new ComboBox<>();
        qtrIdComboBox.getItems().addAll(1, 2, 3, 4);
        qtrIdComboBox.setValue(getCurrentQuarter());
        formGrid.add(qtrIdLabel, 0, row);
        formGrid.add(qtrIdComboBox, 1, row);
        row++;

        // Deuxième colonne - on réinitialise la ligne
        row = 0;

        Label productLineLabel = new Label("Ligne produit:");
        ComboBox<String> productLineComboBox = new ComboBox<>();
        productLineComboBox.getItems().addAll(
                "Motorcycles", "Classic Cars", "Trucks and Buses",
                "Vintage Cars", "Planes", "Ships", "Trains");
        productLineComboBox.setValue("Motorcycles");
        formGrid.add(productLineLabel, 2, row);
        formGrid.add(productLineComboBox, 3, row);
        row++;

        Label msrpLabel = new Label("MSRP:");
        TextField msrpField = new TextField();
        msrpField.setPromptText("Ex: 95");
        formGrid.add(msrpLabel, 2, row);
        formGrid.add(msrpField, 3, row);
        row++;

        Label productCodeLabel = new Label("Code produit:");
        TextField productCodeField = new TextField();
        productCodeField.setPromptText("Ex: S10_1678");
        formGrid.add(productCodeLabel, 2, row);
        formGrid.add(productCodeField, 3, row);
        row++;

        Label customerNameLabel = new Label("Client:");
        TextField customerNameField = new TextField();
        customerNameField.setPromptText("Ex: Land of Toys Inc.");
        formGrid.add(customerNameLabel, 2, row);
        formGrid.add(customerNameField, 3, row);
        row++;

        Label phoneLabel = new Label("Téléphone:");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Ex: 2125557819");
        formGrid.add(phoneLabel, 2, row);
        formGrid.add(phoneField, 3, row);
        row++;

        Label addressLine1Label = new Label("Adresse 1:");
        TextField addressLine1Field = new TextField();
        formGrid.add(addressLine1Label, 2, row);
        formGrid.add(addressLine1Field, 3, row);
        row++;

        Label addressLine2Label = new Label("Adresse 2:");
        TextField addressLine2Field = new TextField();
        formGrid.add(addressLine2Label, 2, row);
        formGrid.add(addressLine2Field, 3, row);
        row++;

        // Troisième colonne
        row = 0;

        Label cityLabel = new Label("Ville:");
        TextField cityField = new TextField();
        formGrid.add(cityLabel, 4, row);
        formGrid.add(cityField, 5, row);
        row++;

        Label stateLabel = new Label("État/Région:");
        TextField stateField = new TextField();
        formGrid.add(stateLabel, 4, row);
        formGrid.add(stateField, 5, row);
        row++;

        Label postalCodeLabel = new Label("Code postal:");
        TextField postalCodeField = new TextField();
        formGrid.add(postalCodeLabel, 4, row);
        formGrid.add(postalCodeField, 5, row);
        row++;

        Label countryLabel = new Label("Pays:");
        TextField countryField = new TextField();
        countryField.setText("France");
        formGrid.add(countryLabel, 4, row);
        formGrid.add(countryField, 5, row);
        row++;

        Label territoryLabel = new Label("Territoire:");
        ComboBox<String> territoryComboBox = new ComboBox<>();
        territoryComboBox.getItems().addAll("NA", "EMEA", "APAC", "Japan");
        territoryComboBox.setValue("EMEA");
        formGrid.add(territoryLabel, 4, row);
        formGrid.add(territoryComboBox, 5, row);
        row++;

        Label contactLastNameLabel = new Label("Nom contact:");
        TextField contactLastNameField = new TextField();
        formGrid.add(contactLastNameLabel, 4, row);
        formGrid.add(contactLastNameField, 5, row);
        row++;

        Label contactFirstNameLabel = new Label("Prénom contact:");
        TextField contactFirstNameField = new TextField();
        formGrid.add(contactFirstNameLabel, 4, row);
        formGrid.add(contactFirstNameField, 5, row);
        row++;

        Label dealSizeLabel = new Label("Taille deal:");
        ComboBox<String> dealSizeComboBox = new ComboBox<>();
        dealSizeComboBox.getItems().addAll("Small", "Medium", "Large");
        dealSizeComboBox.setValue("Small");
        formGrid.add(dealSizeLabel, 4, row);
        formGrid.add(dealSizeComboBox, 5, row);
        row++;

        // Zone de résultat - Calcul automatique des ventes
        Label salesCalculationLabel = new Label("Calcul des ventes:");
        TextField salesResultField = new TextField();
        salesResultField.setEditable(false);
        salesResultField.setPromptText("Montant calculé automatiquement");
        formGrid.add(salesCalculationLabel, 0, row, 2, 1);
        formGrid.add(salesResultField, 2, row, 2, 1);

        // Calculer le montant des ventes automatiquement
        ChangeListener<String> salesCalculator = (observable, oldValue, newValue) -> {
            try {
                double quantity = !quantityOrderedField.getText().isEmpty() ?
                        Double.parseDouble(quantityOrderedField.getText()) : 0;
                double price = !priceEachField.getText().isEmpty() ?
                        Double.parseDouble(priceEachField.getText()) : 0;
                double sales = quantity * price;
                salesResultField.setText(String.format("%.2f", sales));
            } catch (NumberFormatException e) {
                salesResultField.setText("Valeurs invalides");
            }
        };

        quantityOrderedField.textProperty().addListener(salesCalculator);
        priceEachField.textProperty().addListener(salesCalculator);

        // Boutons
        Button saveButton = new Button("Enregistrer la vente");
        saveButton.getStyleClass().add("primary-button");
        Button clearButton = new Button("Effacer");
        clearButton.getStyleClass().add("secondary-button");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(saveButton, clearButton);

        // Zone de log
        Label logLabel = new Label("Journal:");
        TextArea logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(150);
        logArea.setWrapText(true);

        // Action du bouton d'enregistrement
        saveButton.setOnAction(e -> {
            try {
                // Validation des champs obligatoires
                if (orderNumberField.getText().isEmpty() ||
                        quantityOrderedField.getText().isEmpty() ||
                        priceEachField.getText().isEmpty() ||
                        productCodeField.getText().isEmpty() ||
                        customerNameField.getText().isEmpty() ||
                        msrpField.getText().isEmpty() ||
                        cityField.getText().isEmpty() ||
                        postalCodeField.getText().isEmpty() ||
                        addressLine1Field.getText().isEmpty() ||
                        contactLastNameField.getText().isEmpty() ||
                        contactFirstNameField.getText().isEmpty()) {

                    appendToSalesLog(logArea, "Erreur: Veuillez remplir tous les champs obligatoires");
                    return;
                }

                // Préparer les données pour l'insertion
                String orderNumber = orderNumberField.getText().trim();

                // Validation des champs numériques
                int quantityOrdered;
                try {
                    quantityOrdered = Integer.parseInt(quantityOrderedField.getText().trim());
                    if (quantityOrdered <= 0) {
                        appendToSalesLog(logArea, "Erreur: La quantité doit être un nombre positif");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    appendToSalesLog(logArea, "Erreur: La quantité doit être un nombre entier valide");
                    return;
                }

                double priceEach;
                try {
                    priceEach = Double.parseDouble(priceEachField.getText().trim());
                    if (priceEach <= 0) {
                        appendToSalesLog(logArea, "Erreur: Le prix unitaire doit être un nombre positif");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    appendToSalesLog(logArea, "Erreur: Le prix unitaire doit être un nombre décimal valide");
                    return;
                }

                int orderLineNumber;
                try {
                    orderLineNumber = Integer.parseInt(orderLineNumberField.getText().trim());
                    if (orderLineNumber <= 0) {
                        appendToSalesLog(logArea, "Erreur: Le numéro de ligne doit être un nombre positif");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    appendToSalesLog(logArea, "Erreur: Le numéro de ligne doit être un nombre entier valide");
                    return;
                }

                double msrp;
                try {
                    msrp = Double.parseDouble(msrpField.getText().trim());
                    if (msrp <= 0) {
                        appendToSalesLog(logArea, "Erreur: Le MSRP doit être un nombre positif");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    appendToSalesLog(logArea, "Erreur: Le MSRP doit être un nombre décimal valide");
                    return;
                }

                // Calculer les ventes (même si déjà calculé dans l'interface)
                double sales = quantityOrdered * priceEach;

                LocalDate orderDate = orderDatePicker.getValue();
                String formattedDate = orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + " 00:00:00";

                String status = statusComboBox.getValue();
                int qtrId = qtrIdComboBox.getValue();
                int monthId = orderDate.getMonthValue();
                int yearId = orderDate.getYear();

                String productLine = productLineComboBox.getValue();
                String productCode = productCodeField.getText().trim();
                String customerName = customerNameField.getText().trim();
                String phone = phoneField.getText().trim();
                String addressLine1 = addressLine1Field.getText().trim();
                String addressLine2 = addressLine2Field.getText().trim();
                String city = cityField.getText().trim();
                String state = stateField.getText().trim();
                String postalCode = postalCodeField.getText().trim();
                String country = countryField.getText().trim();
                String territory = territoryComboBox.getValue();
                String contactLastName = contactLastNameField.getText().trim();
                String contactFirstName = contactFirstNameField.getText().trim();
                String dealSize = dealSizeComboBox.getValue();

                // Lancer l'insertion dans un thread séparé
                new Thread(() -> {
                    saveSaleToDatabase(
                            logArea, orderNumber, quantityOrdered, priceEach, orderLineNumber,
                            sales, formattedDate, status, qtrId, monthId, yearId,
                            productLine, msrp, productCode, customerName, phone,
                            addressLine1, addressLine2, city, state, postalCode,
                            country, territory, contactLastName, contactFirstName, dealSize
                    );
                }).start();
            } catch (Exception ex) {
                appendToSalesLog(logArea, "Erreur: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        // Action du bouton d'effacement
        clearButton.setOnAction(e -> {
            // Effacer tous les champs du formulaire
            orderNumberField.clear();
            quantityOrderedField.clear();
            priceEachField.clear();
            orderLineNumberField.clear();
            orderDatePicker.setValue(LocalDate.now());
            statusComboBox.setValue("Shipped");
            qtrIdComboBox.setValue(getCurrentQuarter());
            productLineComboBox.setValue("Motorcycles");
            msrpField.clear();
            productCodeField.clear();
            customerNameField.clear();
            phoneField.clear();
            addressLine1Field.clear();
            addressLine2Field.clear();
            cityField.clear();
            stateField.clear();
            postalCodeField.clear();
            countryField.setText("France");
            territoryComboBox.setValue("EMEA");
            contactLastNameField.clear();
            contactFirstNameField.clear();
            dealSizeComboBox.setValue("Small");
            salesResultField.clear();

            appendToSalesLog(logArea, "Formulaire effacé");
        });

        vbox.getChildren().addAll(headerLabel, formGrid, buttonBox, logLabel, logArea);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    /**
     * Méthode pour calculer le trimestre actuel
     */
    private int getCurrentQuarter() {
        int month = LocalDate.now().getMonthValue();
        return ((month - 1) / 3) + 1;
    }

    /**
     * Méthode pour ajouter un message au journal avec horodatage
     */
    private void appendToSalesLog(TextArea logArea, String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        String logMessage = timestamp + " - " + message + "\n";

        Platform.runLater(() -> {
            logArea.appendText(logMessage);
            logArea.setScrollTop(Double.MAX_VALUE); // Défiler vers le bas
        });
    }

    /**
     * Méthode pour enregistrer une vente dans la base de données
     */
    private void saveSaleToDatabase(TextArea logArea, String orderNumber, int quantityOrdered,
                                    double priceEach, int orderLineNumber, double sales,
                                    String orderDate, String status, int qtrId, int monthId,
                                    int yearId, String productLine, double msrp, String productCode,
                                    String customerName, String phone, String addressLine1,
                                    String addressLine2, String city, String state,
                                    String postalCode, String country, String territory,
                                    String contactLastName, String contactFirstName, String dealSize) {

        appendToSalesLog(logArea, "Tentative de connexion à la base de données...");

        // Connexion à MySQL sans spécifier de base de données initialement
        String baseUrl = "jdbc:mysql://localhost:3306/";
        String user = "root";
        String password = "123456789";

        try {
            // Première connexion pour créer la base de données si elle n'existe pas
            try (Connection conn = DriverManager.getConnection(baseUrl, user, password)) {
                appendToSalesLog(logArea, "Connexion initiale réussie");

                // Créer la base de données si elle n'existe pas
                try (Statement stmt = conn.createStatement()) {
                    stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS sales_data");
                    appendToSalesLog(logArea, "Base de données sales_data vérifiée/créée");
                }
            }

            // Maintenant on se connecte directement à la base de données sales_data
            String dbUrl = baseUrl + "sales_data";
            try (Connection conn = DriverManager.getConnection(dbUrl, user, password)) {
                appendToSalesLog(logArea, "Connexion réussie à la base de données sales_data");

                // Vérifier si la table sales_data existe
                boolean tableExists = false;
                DatabaseMetaData meta = conn.getMetaData();
                try (ResultSet tables = meta.getTables(null, null, "sales_data", null)) {
                    tableExists = tables.next();
                }

                if (!tableExists) {
                    appendToSalesLog(logArea, "La table sales_data n'existe pas. Création en cours...");
                    createSalesDataTable(conn, logArea);
                } else {
                    appendToSalesLog(logArea, "La table sales_data existe déjà");
                }

                // Préparer la requête d'insertion
                String sql = "INSERT INTO sales_data (ORDERNUMBER, QUANTITYORDERED, PRICEEACH, " +
                        "ORDERLINENUMBER, SALES, ORDERDATE, STATUS, QTR_ID, MONTH_ID, YEAR_ID, " +
                        "PRODUCTLINE, MSRP, PRODUCTCODE, CUSTOMERNAME, PHONE, ADDRESSLINE1, " +
                        "ADDRESSLINE2, CITY, STATE, POSTALCODE, COUNTRY, TERRITORY, " +
                        "CONTACTLASTNAME, CONTACTFIRSTNAME, DEALSIZE) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, orderNumber);
                    pstmt.setInt(2, quantityOrdered);
                    pstmt.setDouble(3, priceEach);
                    pstmt.setInt(4, orderLineNumber);
                    pstmt.setDouble(5, sales);
                    pstmt.setString(6, orderDate);
                    pstmt.setString(7, status);
                    pstmt.setInt(8, qtrId);
                    pstmt.setInt(9, monthId);
                    pstmt.setInt(10, yearId);
                    pstmt.setString(11, productLine);
                    pstmt.setDouble(12, msrp);
                    pstmt.setString(13, productCode);
                    pstmt.setString(14, customerName);
                    pstmt.setString(15, phone);
                    pstmt.setString(16, addressLine1);
                    pstmt.setString(17, addressLine2 != null && !addressLine2.isEmpty() ? addressLine2 : "");
                    pstmt.setString(18, city);
                    pstmt.setString(19, state != null && !state.isEmpty() ? state : "");
                    pstmt.setString(20, postalCode);
                    pstmt.setString(21, country);
                    pstmt.setString(22, territory);
                    pstmt.setString(23, contactLastName);
                    pstmt.setString(24, contactFirstName);
                    pstmt.setString(25, dealSize);

                    int rowsAffected = pstmt.executeUpdate();
                    appendToSalesLog(logArea, "Vente enregistrée avec succès! (" + rowsAffected + " ligne(s) insérée(s))");
                    appendToSalesLog(logArea, "Données insérées: Commande #" + orderNumber + " - Produit: " + productCode + " - Client: " + customerName);
                }

            } catch (SQLException e) {
                appendToSalesLog(logArea, "Erreur lors de l'opération sur la base: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            appendToSalesLog(logArea, "Erreur de connexion à MySQL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Méthode pour créer la table sales_data si elle n'existe pas
     */
    private void createSalesDataTable(Connection conn, TextArea logArea) throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS sales_data (" +
                "ORDERNUMBER VARCHAR(20) NOT NULL, " +
                "QUANTITYORDERED INT, " +
                "PRICEEACH DOUBLE, " +
                "ORDERLINENUMBER INT, " +
                "SALES DOUBLE, " +
                "ORDERDATE VARCHAR(50), " +
                "STATUS VARCHAR(20), " +
                "QTR_ID INT, " +
                "MONTH_ID INT, " +
                "YEAR_ID INT, " +
                "PRODUCTLINE VARCHAR(50), " +
                "MSRP DOUBLE, " +
                "PRODUCTCODE VARCHAR(20), " +
                "CUSTOMERNAME VARCHAR(100), " +
                "PHONE VARCHAR(50), " +
                "ADDRESSLINE1 VARCHAR(100), " +
                "ADDRESSLINE2 VARCHAR(100), " +
                "CITY VARCHAR(50), " +
                "STATE VARCHAR(50), " +
                "POSTALCODE VARCHAR(20), " +
                "COUNTRY VARCHAR(50), " +
                "TERRITORY VARCHAR(20), " +
                "CONTACTLASTNAME VARCHAR(50), " +
                "CONTACTFIRSTNAME VARCHAR(50), " +
                "DEALSIZE VARCHAR(20), " +
                "PRIMARY KEY (ORDERNUMBER, ORDERLINENUMBER)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createTableSQL);
            appendToSalesLog(logArea, "Table sales_data créée avec succès");
        } catch (SQLException e) {
            appendToSalesLog(logArea, "Erreur lors de la création de la table: " + e.getMessage());
            throw e;
        }
    }

    private void createTabs() {
        createSummaryTab();
        createSalesByProductLineTab();
        createSalesByQuarterTab();
        createSalesByCountryTab();
        createTopCustomersTab();
        createOrderStatusTab();
        createDealSizeTab();
        createAdvancedAnalysisTab();
        createDataTableTab();
        createDatabaseTab();
        createLoadFromDatabaseTab();
        createAddSaleTab();
        createExportTab();
    }

    private void toggleTheme() {
        Scene scene = primaryStage.getScene();
        isDarkMode = !isDarkMode;

        scene.getStylesheets().clear();
        if (isDarkMode) {
            if (getClass().getResource(DARK_STYLE) != null) {
                scene.getStylesheets().add(getClass().getResource(DARK_STYLE).toExternalForm());
            }
        } else {
            if (getClass().getResource(LIGHT_STYLE) != null) {
                scene.getStylesheets().add(getClass().getResource(LIGHT_STYLE).toExternalForm());
            }
        }
    }


    private void showAboutDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("À propos");
        alert.setHeaderText("Analyseur de Données de Ventes");
        alert.setContentText("Version 1.0\n© 2025 Votre Entreprise\n\nCet outil permet d'analyser et visualiser des données de ventes à partir de fichiers CSV.");

        alert.showAndWait();
    }

    private void showWelcomeScreen() {
        // Créer une alerte personnalisée comme écran de bienvenue
        Alert welcome = new Alert(Alert.AlertType.INFORMATION);
        welcome.setTitle("Bienvenue");
        welcome.setHeaderText("Analyseur de Données de Ventes");
        welcome.setContentText("L'application est prête à être utilisée!\n\n" +
                "Vous pouvez charger un fichier CSV personnalisé depuis le menu Fichier > Ouvrir CSV.\n" +
                "Explorez les différents onglets pour analyser vos données de ventes.");

        welcome.showAndWait();
    }

    private void showErrorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void createSummaryTab() {
        Tab tab = new Tab("Résumé");

        // Obtenir les statistiques générales
        BigDecimal totalSales = analyzer.calculateTotalSales();
        BigDecimal avgSales = analyzer.calculateAverageSales();
        Map<String, BigDecimal> salesStats = analyzer.getNumericColumnStats("SALES");

        // Créer les cartes pour afficher les statistiques
        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Résumé des Ventes");
        headerLabel.getStyleClass().add("section-header");

        HBox statsCards = new HBox(20);
        statsCards.setAlignment(Pos.CENTER);

        // Formatage des nombres
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // Carte pour les ventes totales
        VBox totalSalesCard = createStatsCard("Ventes Totales",
                currencyFormat.format(totalSales), "sales-total");

        // Carte pour la vente moyenne
        VBox avgSalesCard = createStatsCard("Vente Moyenne",
                currencyFormat.format(avgSales), "sales-avg");

        // Carte pour la vente maximale
        VBox maxSalesCard = createStatsCard("Vente Maximale",
                currencyFormat.format(salesStats.get("max")), "sales-max");

        // Carte pour la vente minimale
        VBox minSalesCard = createStatsCard("Vente Minimale",
                currencyFormat.format(salesStats.get("min")), "sales-min");

        statsCards.getChildren().addAll(totalSalesCard, avgSalesCard, maxSalesCard, minSalesCard);

        // Créer un graphique à barres pour la répartition des ventes par année
        Map<String, BigDecimal> salesByYear = analyzer.getSalesByYear();
        BarChart<String, Number> salesByYearChart = createBarChart(
                "Ventes par Année", "Année", "Ventes ($)", salesByYear);

        // Créer un graphique à secteurs pour la répartition des ventes par ligne de produit
        Map<String, BigDecimal> salesByProductLine = analyzer.getSalesByProductLine();
        PieChart salesByProductPieChart = createPieChart(
                "Répartition des Ventes par Ligne de Produit", salesByProductLine);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(salesByYearChart, salesByProductPieChart);

        vbox.getChildren().addAll(headerLabel, statsCards, chartsBox);

        // Ajouter un effet de transition
        FadeTransition fadeIn = new FadeTransition(Duration.millis(1000), vbox);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void createSalesByProductLineTab() {
        Tab tab = new Tab("Ventes par Produit");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyse des Ventes par Ligne de Produit");
        headerLabel.getStyleClass().add("section-header");

        // Obtenir les ventes par ligne de produit
        Map<String, BigDecimal> salesByProductLine = analyzer.getSalesByProductLine();

        // Créer un graphique à barres
        BarChart<String, Number> barChart = createBarChart(
                "Ventes par Ligne de Produit", "Ligne de Produit", "Ventes ($)", salesByProductLine);

        // Créer un graphique à secteurs
        PieChart pieChart = createPieChart("Répartition des Ventes par Ligne de Produit", salesByProductLine);

        // Calculer le prix moyen par ligne de produit
        Map<String, BigDecimal> avgPriceByProductLine = analyzer.getAveragePriceByProductLine();
        BarChart<String, Number> avgPriceChart = createBarChart(
                "Prix Moyen par Ligne de Produit", "Ligne de Produit", "Prix Moyen ($)", avgPriceByProductLine);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(barChart, pieChart);

        vbox.getChildren().addAll(headerLabel, chartsBox, avgPriceChart);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void createSalesByQuarterTab() {
        Tab tab = new Tab("Ventes Temporelles");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyse Temporelle des Ventes");
        headerLabel.getStyleClass().add("section-header");

        // Obtenir les ventes par trimestre
        Map<String, BigDecimal> salesByQuarter = analyzer.getSalesByQuarter();

        // Créer un graphique linéaire
        LineChart<String, Number> lineChart = createLineChart(
                "Ventes par Trimestre", "Trimestre", "Ventes ($)", salesByQuarter);

        // Obtenir les ventes par année
        Map<String, BigDecimal> salesByYear = analyzer.getSalesByYear();
        BarChart<String, Number> yearBarChart = createBarChart(
                "Ventes par Année", "Année", "Ventes ($)", salesByYear);

        // Obtenir les ventes par mois
        Map<String, BigDecimal> salesByMonth = analyzer.getSalesByMonth();
        LineChart<String, Number> monthLineChart = createLineChart(
                "Tendance des Ventes Mensuelles", "Mois", "Ventes ($)", salesByMonth);

        HBox topChartsBox = new HBox(20);
        topChartsBox.setAlignment(Pos.CENTER);
        topChartsBox.getChildren().addAll(lineChart, yearBarChart);

        vbox.getChildren().addAll(headerLabel, topChartsBox, monthLineChart);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void createSalesByCountryTab() {
        Tab tab = new Tab("Ventes par Pays");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyse Géographique des Ventes");
        headerLabel.getStyleClass().add("section-header");

        // Obtenir les ventes par pays
        Map<String, BigDecimal> salesByCountry = analyzer.getSalesByCountry();

        // Créer un graphique à barres
        BarChart<String, Number> barChart = createBarChart(
                "Ventes par Pays", "Pays", "Ventes ($)", salesByCountry);

        // Créer un graphique à secteurs
        PieChart pieChart = createPieChart("Répartition des Ventes par Pays", salesByCountry);

        // Tableaux des résultats
        TableView<CountryData> tableView = new TableView<>();
        tableView.setPrefHeight(300);

        TableColumn<CountryData, String> countryColumn = new TableColumn<>("Pays");
        countryColumn.setCellValueFactory(cellData -> cellData.getValue().countryProperty());

        TableColumn<CountryData, String> salesColumn = new TableColumn<>("Ventes ($)");
        salesColumn.setCellValueFactory(cellData -> cellData.getValue().salesProperty());

        TableColumn<CountryData, String> percentColumn = new TableColumn<>("% du Total");
        percentColumn.setCellValueFactory(cellData -> cellData.getValue().percentProperty());

        tableView.getColumns().addAll(countryColumn, salesColumn, percentColumn);

        // Ajouter les données au tableau
        BigDecimal totalSales = analyzer.calculateTotalSales();
        List<CountryData> countryDataList = new ArrayList<>();

        salesByCountry.forEach((country, sales) -> {
            BigDecimal percentage = sales.multiply(new BigDecimal(100)).divide(totalSales, 2, BigDecimal.ROUND_HALF_UP);
            NumberFormat nf = NumberFormat.getCurrencyInstance(Locale.US);
            countryDataList.add(new CountryData(
                    country,
                    nf.format(sales),
                    percentage.toString() + "%"));
        });

        tableView.getItems().addAll(countryDataList);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(barChart, pieChart);

        vbox.getChildren().addAll(headerLabel, chartsBox, tableView);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void createTopCustomersTab() {
        Tab tab = new Tab("Meilleurs Clients");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyse des Meilleurs Clients");
        headerLabel.getStyleClass().add("section-header");

        // Contrôle de sélection pour le nombre de clients
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);

        Label limitLabel = new Label("Nombre de clients à afficher:");
        ComboBox<Integer> limitComboBox = new ComboBox<>(
                FXCollections.observableArrayList(5, 10, 15, 20, 25, 30));
        limitComboBox.setValue(10);
        Button applyButton = new Button("Appliquer");

        controlBox.getChildren().addAll(limitLabel, limitComboBox, applyButton);

        // Zone pour les graphiques
        VBox chartsContainer = new VBox(20);
        chartsContainer.setAlignment(Pos.CENTER);

        // Charger les données initiales
        updateTopCustomersCharts(chartsContainer, limitComboBox.getValue());

        // Action lors du changement
        applyButton.setOnAction(e ->
                updateTopCustomersCharts(chartsContainer, limitComboBox.getValue()));

        vbox.getChildren().addAll(headerLabel, controlBox, chartsContainer);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void updateTopCustomersCharts(VBox container, int limit) {
        container.getChildren().clear();

        // Obtenir les top clients
        Map<String, BigDecimal> topCustomers = analyzer.getTopCustomers(limit);

        // Créer un graphique à barres
        BarChart<String, Number> barChart = createBarChart(
                "Top " + limit + " des Clients", "Client", "Ventes ($)", topCustomers);

        // Créer un graphique à secteurs
        PieChart pieChart = createPieChart("Répartition des Ventes par Client", topCustomers);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(barChart, pieChart);

        container.getChildren().add(chartsBox);
    }

    private void createOrderStatusTab() {
        Tab tab = new Tab("Statut des Commandes");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyse des Statuts de Commande");
        headerLabel.getStyleClass().add("section-header");

        // Obtenir la distribution des statuts de commande
        Map<String, Long> orderStatusDist = analyzer.getOrderStatusDistribution();

        // Convertir en BigDecimal pour la méthode existante
        Map<String, BigDecimal> statusMap = new HashMap<>();
        orderStatusDist.forEach((key, value) -> statusMap.put(key, new BigDecimal(value)));

        // Créer un graphique à barres
        BarChart<String, Number> barChart = createBarChart(
                "Distribution des Statuts de Commande", "Statut", "Nombre de Commandes", statusMap);

        // Créer un graphique à secteurs
        PieChart pieChart = createPieChart("Répartition des Commandes par Statut", statusMap);

        // Tableaux des résultats
        TableView<StatusData> tableView = new TableView<>();
        tableView.setPrefHeight(300);

        TableColumn<StatusData, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

        TableColumn<StatusData, String> countColumn = new TableColumn<>("Nombre");
        countColumn.setCellValueFactory(cellData -> cellData.getValue().countProperty());

        TableColumn<StatusData, String> percentColumn = new TableColumn<>("Pourcentage");
        percentColumn.setCellValueFactory(cellData -> cellData.getValue().percentProperty());

        tableView.getColumns().addAll(statusColumn, countColumn, percentColumn);

        // Calculer le total des commandes
        long totalOrders = orderStatusDist.values().stream().mapToLong(Long::longValue).sum();

        // Ajouter les données au tableau
        List<StatusData> statusDataList = new ArrayList<>();
        orderStatusDist.forEach((status, count) -> {
            double percentage = (count * 100.0) / totalOrders;
            statusDataList.add(new StatusData(
                    status,
                    count.toString(),
                    String.format("%.2f%%", percentage)));
        });

        tableView.getItems().addAll(statusDataList);

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(barChart, pieChart);

        vbox.getChildren().addAll(headerLabel, chartsBox, tableView);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void createDealSizeTab() {
        Tab tab = new Tab("Taille des Affaires");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyse des Tailles d'Affaires");
        headerLabel.getStyleClass().add("section-header");

        // Obtenir la distribution des tailles d'affaires
        Map<String, Long> dealSizeDist = analyzer.getDealSizeDistribution();

        // Convertir en BigDecimal pour la méthode existante
        Map<String, BigDecimal> dealSizeMap = new HashMap<>();
        dealSizeDist.forEach((key, value) -> dealSizeMap.put(key, new BigDecimal(value)));

        // Créer un graphique à barres
        BarChart<String, Number> barChart = createBarChart(
                "Distribution des Tailles d'Affaires", "Taille", "Nombre d'Affaires", dealSizeMap);

        // Créer un graphique à secteurs
        PieChart pieChart = createPieChart("Répartition des Affaires par Taille", dealSizeMap);

        // Analyse croisée : taille d'affaire par statut de commande
        StackedBarChart<String, Number> stackedBarChart = createStackedBarChart(
                "Taille d'Affaire par Statut de Commande",
                "Taille d'Affaire",
                "Nombre de Commandes",
                analyzeDealSizeByOrderStatus());

        HBox chartsBox = new HBox(20);
        chartsBox.setAlignment(Pos.CENTER);
        chartsBox.getChildren().addAll(barChart, pieChart);

        vbox.getChildren().addAll(headerLabel, chartsBox, stackedBarChart);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private Map<String, Map<String, Long>> analyzeDealSizeByOrderStatus() {
        Map<String, Map<String, Long>> result = new HashMap<>();

        // Parcourir les données de vente
        for (Map<String, String> row : analyzer.getSalesData()) {
            String dealSize = row.getOrDefault("DEALSIZE", "Unknown");
            String status = row.getOrDefault("STATUS", "Unknown");

            // Initialiser la map pour ce deal size si nécessaire
            if (!result.containsKey(dealSize)) {
                result.put(dealSize, new HashMap<>());
            }

            // Mettre à jour le compteur
            Map<String, Long> statusCounts = result.get(dealSize);
            statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
        }

        return result;
    }

    private void createAdvancedAnalysisTab() {
        Tab tab = new Tab("Analyses Avancées");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Analyses Avancées des Ventes");
        headerLabel.getStyleClass().add("section-header");

        // 1. Ventes par mois
        Map<String, BigDecimal> salesByMonth = analyzer.getSalesByMonth();
        LineChart<String, Number> monthlySalesChart = createLineChart(
                "Ventes par Mois", "Mois", "Ventes ($)", salesByMonth);

        // 2. Ratio vente/stock
        Map<String, Double> salesToStockRatio = analyzer.getSalesToStockRatio();
        BarChart<String, Number> ratioChart = createRatioChart(
                "Ratio Vente/Stock par Produit", "Produit", "Ratio", salesToStockRatio);

        // 3. Distribution des prix
        Map<String, Long> priceDistribution = analyzer.getPriceDistribution();
        BarChart<String, Number> priceDistChart = createBarChart(
                "Distribution des Prix Unitaires", "Plage de Prix ($)", "Nombre de Produits",
                priceDistribution.entrySet().stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new BigDecimal(e.getValue()),
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        )));

        // 4. Produits les plus vendus
        Map<String, Integer> topProducts = analyzer.getTopSellingProducts(10);
        BarChart<String, Number> topProductsChart = createIntegerBarChart(
                "Top 10 Produits les Plus Vendus", "Code Produit", "Quantité Vendue", topProducts);

        // Organiser les graphiques
        HBox firstRow = new HBox(20);
        firstRow.setAlignment(Pos.CENTER);
        firstRow.getChildren().addAll(monthlySalesChart, ratioChart);

        HBox secondRow = new HBox(20);
        secondRow.setAlignment(Pos.CENTER);
        secondRow.getChildren().addAll(priceDistChart, topProductsChart);

        vbox.getChildren().addAll(
                headerLabel,
                firstRow,
                secondRow);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }


    // Continuation de la méthode createDataTableTab()
    private void createDataTableTab() {
        Tab tab = new Tab("Tableau de Données");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Données Brutes");
        headerLabel.getStyleClass().add("section-header");

        // Créer un tableau pour afficher les données
        TableView<Map<String, String>> tableView = new TableView<>();

        // Ajouter une barre de recherche
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        Label searchLabel = new Label("Rechercher:");
        TextField searchField = new TextField();
        searchField.setPromptText("Entrez un terme de recherche...");
        Button searchButton = new Button("Rechercher");
        searchBox.getChildren().addAll(searchLabel, searchField, searchButton);

        // Obtenir les données et les en-têtes
        List<Map<String, String>> salesData = analyzer.getSalesData();
        String[] headers = new String[0];
        if (!salesData.isEmpty()) {
            headers = salesData.get(0).keySet().toArray(new String[0]);
        }

        // Créer les colonnes
        for (String header : headers) {
            TableColumn<Map<String, String>, String> column = new TableColumn<>(header);
            column.setCellValueFactory(data -> {
                String value = data.getValue().get(header);
                return javafx.beans.binding.Bindings.createStringBinding(() -> value);
            });
            tableView.getColumns().add(column);
        }

        // Ajouter les données
        tableView.getItems().addAll(salesData);

        // Fonctionnalité de recherche
        AtomicReference<List<Map<String, String>>> originalData = new AtomicReference<>(salesData);
        searchButton.setOnAction(e -> {
            String searchTerm = searchField.getText().toLowerCase();
            if (searchTerm.isEmpty()) {
                tableView.getItems().clear();
                tableView.getItems().addAll(originalData.get());
            } else {
                List<Map<String, String>> filteredData = originalData.get().stream()
                        .filter(row -> row.values().stream()
                                .anyMatch(value -> value != null && value.toLowerCase().contains(searchTerm)))
                        .collect(Collectors.toList());
                tableView.getItems().clear();
                tableView.getItems().addAll(filteredData);
            }
        });

        // Options d'exportation
        HBox exportBox = new HBox(10);
        exportBox.setAlignment(Pos.CENTER);
        Label exportLabel = new Label("Exporter les données:");
        Button csvButton = new Button("CSV");
        Button pdfButton = new Button("PDF");
        exportBox.getChildren().addAll(exportLabel, csvButton, pdfButton);

        tableView.setPrefHeight(600);

        vbox.getChildren().addAll(headerLabel, searchBox, tableView, exportBox);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    private void createExportTab() {
        Tab tab = new Tab("Exporter Rapport");

        VBox vbox = new VBox(20);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label headerLabel = new Label("Exporter un Rapport Complet");
        headerLabel.getStyleClass().add("section-header");

        // Éléments du rapport
        CheckBox summaryCheckBox = new CheckBox("Résumé des ventes");
        summaryCheckBox.setSelected(true);
        CheckBox productLineCheckBox = new CheckBox("Ventes par ligne de produit");
        productLineCheckBox.setSelected(true);
        CheckBox quarterCheckBox = new CheckBox("Ventes par trimestre");
        quarterCheckBox.setSelected(true);
        CheckBox countryCheckBox = new CheckBox("Ventes par pays");
        countryCheckBox.setSelected(true);
        CheckBox customersCheckBox = new CheckBox("Meilleurs clients");
        customersCheckBox.setSelected(true);
        CheckBox statusCheckBox = new CheckBox("Statuts des commandes");
        statusCheckBox.setSelected(false);
        CheckBox dealSizeCheckBox = new CheckBox("Tailles d'affaires");
        dealSizeCheckBox.setSelected(false);
        CheckBox rawDataCheckBox = new CheckBox("Données brutes");
        rawDataCheckBox.setSelected(false);

        // Format du rapport
        Label formatLabel = new Label("Format du rapport:");
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton pdfRadio = new RadioButton("PDF");
        pdfRadio.setToggleGroup(formatGroup);
        pdfRadio.setSelected(true);
        RadioButton csvRadio = new RadioButton("CSV");
        csvRadio.setToggleGroup(formatGroup);

        HBox formatBox = new HBox(20);
        formatBox.setAlignment(Pos.CENTER);
        formatBox.getChildren().addAll(formatLabel, pdfRadio, csvRadio);

        // Options du rapport
        Label optionsLabel = new Label("Options du rapport:");
        CheckBox includeChartsCheckBox = new CheckBox("Inclure les graphiques");
        includeChartsCheckBox.setSelected(true);
        CheckBox includeTablesCheckBox = new CheckBox("Inclure les tableaux");
        includeTablesCheckBox.setSelected(true);
        CheckBox includeStatsCheckBox = new CheckBox("Inclure les statistiques");
        includeStatsCheckBox.setSelected(true);

        HBox optionsBox = new HBox(20);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.getChildren().addAll(optionsLabel, includeChartsCheckBox, includeTablesCheckBox, includeStatsCheckBox);

        // Bouton d'export
        Button exportButton = new Button("Exporter le rapport");
        exportButton.getStyleClass().add("primary-button");
        exportButton.setOnAction(e -> showExportDialog());

        VBox sectionsBox = new VBox(10);
        sectionsBox.setAlignment(Pos.CENTER_LEFT);
        sectionsBox.getChildren().addAll(
                new Label("Sections à inclure:"),
                summaryCheckBox, productLineCheckBox, quarterCheckBox, countryCheckBox,
                customersCheckBox, statusCheckBox, dealSizeCheckBox, rawDataCheckBox
        );

        vbox.getChildren().addAll(headerLabel, sectionsBox, formatBox, optionsBox, exportButton);

        // Ajouter le ScrollPane
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        tab.setContent(scrollPane);
        tabPane.getTabs().add(tab);
    }

    // Voici la méthode showExportDialog corrigée
    private void showExportDialog() {
        // Créer une boîte de dialogue personnalisée pour les options d'export
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Options d'exportation");
        dialog.setHeaderText("Sélectionnez les éléments à inclure dans le rapport");

        // Créer les cases à cocher
        CheckBox summaryCheckBox = new CheckBox("Résumé des ventes");
        summaryCheckBox.setSelected(true);
        CheckBox productLineCheckBox = new CheckBox("Ventes par ligne de produit");
        productLineCheckBox.setSelected(true);
        CheckBox quarterCheckBox = new CheckBox("Ventes par trimestre");
        quarterCheckBox.setSelected(true);
        CheckBox countryCheckBox = new CheckBox("Ventes par pays");
        countryCheckBox.setSelected(true);
        CheckBox customersCheckBox = new CheckBox("Meilleurs clients");
        customersCheckBox.setSelected(true);
        CheckBox statusCheckBox = new CheckBox("Statuts des commandes");
        CheckBox dealSizeCheckBox = new CheckBox("Tailles d'affaires");
        CheckBox rawDataCheckBox = new CheckBox("Données brutes");

        // Options de format
        ToggleGroup formatGroup = new ToggleGroup();
        RadioButton pdfRadio = new RadioButton("PDF");
        pdfRadio.setToggleGroup(formatGroup);
        pdfRadio.setSelected(true);
        RadioButton csvRadio = new RadioButton("CSV");
        csvRadio.setToggleGroup(formatGroup);

        // Options supplémentaires
        CheckBox includeChartsCheckBox = new CheckBox("Inclure les graphiques");
        includeChartsCheckBox.setSelected(true);
        CheckBox includeTablesCheckBox = new CheckBox("Inclure les tableaux");
        includeTablesCheckBox.setSelected(true);
        CheckBox includeStatsCheckBox = new CheckBox("Inclure les statistiques");
        includeStatsCheckBox.setSelected(true);

        // Désactiver les options graphiques et tableaux si CSV est sélectionné
        csvRadio.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                includeChartsCheckBox.setSelected(false);
                includeChartsCheckBox.setDisable(true);
                includeTablesCheckBox.setDisable(true);
            } else {
                includeChartsCheckBox.setDisable(false);
                includeTablesCheckBox.setDisable(false);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Sections:"), 0, 0);
        grid.add(summaryCheckBox, 1, 0);
        grid.add(productLineCheckBox, 1, 1);
        grid.add(quarterCheckBox, 1, 2);
        grid.add(countryCheckBox, 1, 3);
        grid.add(customersCheckBox, 1, 4);
        grid.add(statusCheckBox, 1, 5);
        grid.add(dealSizeCheckBox, 1, 6);
        grid.add(rawDataCheckBox, 1, 7);

        grid.add(new Label("Format:"), 0, 8);
        grid.add(pdfRadio, 1, 8);
        grid.add(csvRadio, 2, 8);

        grid.add(new Label("Options:"), 0, 9);
        grid.add(includeChartsCheckBox, 1, 9);
        grid.add(includeTablesCheckBox, 2, 9);
        grid.add(includeStatsCheckBox, 3, 9);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Exporter le rapport");

            // Définir les extensions en fonction du format sélectionné
            if (pdfRadio.isSelected()) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
            } else {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("Fichiers CSV", "*.csv"));
            }

            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    // Stocker les valeurs sélectionnées dans des variables locales finales
                    final boolean fIncludeSummary = summaryCheckBox.isSelected();
                    final boolean fIncludeProductLine = productLineCheckBox.isSelected();
                    final boolean fIncludeQuarter = quarterCheckBox.isSelected();
                    final boolean fIncludeCountry = countryCheckBox.isSelected();
                    final boolean fIncludeCustomers = customersCheckBox.isSelected();
                    final boolean fIncludeStatus = statusCheckBox.isSelected();
                    final boolean fIncludeDealSize = dealSizeCheckBox.isSelected();
                    final boolean fIncludeRawData = rawDataCheckBox.isSelected();
                    final boolean fIncludeCharts = includeChartsCheckBox.isSelected();
                    final boolean fIncludeTables = includeTablesCheckBox.isSelected();
                    final boolean fIncludeStats = includeStatsCheckBox.isSelected();
                    final boolean fIsPdf = pdfRadio.isSelected();
                    final File fFile = file;

                    // Afficher un indicateur de progression
                    ProgressIndicator progress = new ProgressIndicator();
                    Stage progressStage = new Stage();
                    progressStage.setTitle("Exportation en cours...");
                    progressStage.initModality(Modality.APPLICATION_MODAL);
                    progressStage.setScene(new Scene(new StackPane(progress), 200, 200));
                    progressStage.show();

                    // Utiliser un thread séparé pour l'exportation
                    new Thread(() -> {
                        try {
                            if (fIsPdf) {
                                exportToPdf(
                                        fFile,
                                        fIncludeSummary,
                                        fIncludeProductLine,
                                        fIncludeQuarter,
                                        fIncludeCountry,
                                        fIncludeCustomers,
                                        fIncludeStatus,
                                        fIncludeDealSize,
                                        fIncludeRawData,
                                        fIncludeCharts,
                                        fIncludeTables,
                                        fIncludeStats
                                );
                            } else {
                                exportToCsv(fFile);
                            }

                            // Fermer l'indicateur de progression et afficher un message de succès sur le thread UI
                            Platform.runLater(() -> {
                                progressStage.close();

                                // Afficher un message de confirmation
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Exportation réussie");
                                alert.setHeaderText("Le rapport a été exporté avec succès");
                                alert.setContentText("Le fichier a été enregistré sous: " + fFile.getAbsolutePath());
                                alert.showAndWait();

                                // Proposition d'ouverture du fichier
                                if (fIsPdf) {
                                    Alert openFileAlert = new Alert(Alert.AlertType.CONFIRMATION);
                                    openFileAlert.setTitle("Ouvrir le fichier");
                                    openFileAlert.setHeaderText("Voulez-vous ouvrir le rapport ?");
                                    openFileAlert.setContentText("Le fichier PDF a été créé avec succès.");

                                    if (openFileAlert.showAndWait().get() == ButtonType.OK) {
                                        try {
                                            Desktop.getDesktop().open(fFile);
                                        } catch (IOException e) {
                                            showErrorDialog("Erreur", "Impossible d'ouvrir le fichier", e.getMessage());
                                        }
                                    }
                                }
                            });
                        } catch (Exception ex) {
                            Platform.runLater(() -> {
                                progressStage.close();
                                showErrorDialog("Erreur d'exportation", "Impossible d'exporter le rapport", ex.getMessage());
                                ex.printStackTrace();
                            });
                        }
                    }).start();
                } catch (Exception ex) {
                    showErrorDialog("Erreur d'exportation", "Impossible d'exporter le rapport", ex.getMessage());
                    ex.printStackTrace();
                }
            }
        }
    }


    private void exportToPdf(File file, boolean includeSummary, boolean includeProductLine,
                             boolean includeQuarter, boolean includeCountry, boolean includeCustomers,
                             boolean includeStatus, boolean includeDealSize, boolean includeRawData,
                             boolean includeCharts, boolean includeTables, boolean includeStats) throws IOException {
        // Créer un document PDF
        PDDocument document = new PDDocument();

        // Ajouter une page de titre
        PDPage titlePage = new PDPage(PDRectangle.A4);
        document.addPage(titlePage);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, titlePage)) {
            // Titre principal
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.newLineAtOffset(100, 700);
            contentStream.showText("Rapport d'Analyse des Ventes");
            contentStream.endText();

            // Date de génération
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(100, 650);
            contentStream.showText("Généré le " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();

            // Fichier source
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(100, 630);
            contentStream.showText("Données source : " + currentFilePath);
            contentStream.endText();
        }

        // Exporter les sections sélectionnées
        if (includeSummary) {
            addSummarySection(document, includeCharts, includeStats);
        }

        if (includeProductLine) {
            addProductLineSection(document, includeCharts, includeStats);
        }

        if (includeQuarter) {
            addQuarterSection(document, includeCharts, includeStats);
        }

        if (includeCountry) {
            addCountrySection(document, includeCharts, includeStats);
        }

        if (includeCustomers) {
            addCustomersSection(document, includeCharts, includeStats);
        }

        if (includeStatus) {
            addStatusSection(document, includeCharts, includeStats);
        }

        if (includeDealSize) {
            addDealSizeSection(document, includeCharts, includeStats);
        }

        if (includeRawData) {
            addRawDataSection(document);
        }

        // Sauvegarder le document
        document.save(file);
        document.close();
    }

    private void addSummarySection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Résumé des Ventes");
            contentStream.endText();

            // Statistiques
            float yPosition = 720;
            if (includeStats) {
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, yPosition);
                contentStream.showText("Ventes Totales: " + formatCurrency(analyzer.calculateTotalSales()));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Vente Moyenne: " + formatCurrency(analyzer.calculateAverageSales()));

                Map<String, BigDecimal> salesStats = analyzer.getNumericColumnStats("SALES");
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Vente Maximale: " + formatCurrency(salesStats.get("max")));
                contentStream.newLineAtOffset(0, -20);
                contentStream.showText("Vente Minimale: " + formatCurrency(salesStats.get("min")));
                contentStream.endText();

                // Mise à jour de la position y après les statistiques
                yPosition = 640;
            }
        }

        // Graphiques - ajoutés sur une nouvelle page
        if (includeCharts) {
            // Ajouter une nouvelle page pour les graphiques
            PDPage chartPage = new PDPage(PDRectangle.A4);
            document.addPage(chartPage);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, chartPage)) {
                // Ajouter un titre pour la page de graphiques
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.newLineAtOffset(50, 780);
                contentStream.showText("Graphiques - Résumé des Ventes");
                contentStream.endText();
            }

            // Exporter le graphique des ventes par année sur la nouvelle page
            Map<String, BigDecimal> salesByYear = analyzer.getSalesByYear();
            exportChartToPdf(document, createBarChart("Ventes par Année", "Année", "Ventes ($)", salesByYear), 50, 700);

            // Exporter le graphique à secteurs sur la même page à une position plus basse
            Map<String, BigDecimal> salesByProductLine = analyzer.getSalesByProductLine();
            exportChartToPdf(document, createPieChart("Répartition des Ventes par Ligne de Produit", salesByProductLine), 50, 350);
        }
    }
    private void addProductLineSection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Ventes par Ligne de Produit");
            contentStream.endText();

            // Statistiques
            if (includeStats) {
                Map<String, BigDecimal> salesByProductLine = analyzer.getSalesByProductLine();
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);

                for (Map.Entry<String, BigDecimal> entry : salesByProductLine.entrySet()) {
                    contentStream.showText(entry.getKey() + ": " + formatCurrency(entry.getValue()));
                    contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }

            // Graphiques
            if (includeCharts) {
                Map<String, BigDecimal> salesByProductLine = analyzer.getSalesByProductLine();
                exportChartToPdf(document, createBarChart("Ventes par Ligne de Produit", "Ligne de Produit", "Ventes ($)", salesByProductLine), 50, 500);

                Map<String, BigDecimal> avgPriceByProductLine = analyzer.getAveragePriceByProductLine();
                exportChartToPdf(document, createBarChart("Prix Moyen par Ligne de Produit", "Ligne de Produit", "Prix Moyen ($)", avgPriceByProductLine), 350, 500);
            }
        }
    }

    // Méthodes similaires pour les autres sections...
    private void addQuarterSection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Ventes par Trimestre");
            contentStream.endText();

            // Statistiques
            if (includeStats) {
                Map<String, BigDecimal> salesByQuarter = analyzer.getSalesByQuarter();
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);

                for (Map.Entry<String, BigDecimal> entry : salesByQuarter.entrySet()) {
                    contentStream.showText("Trimestre " + entry.getKey() + ": " + formatCurrency(entry.getValue()));
                    contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }

            // Graphiques
            if (includeCharts) {
                Map<String, BigDecimal> salesByQuarter = analyzer.getSalesByQuarter();
                exportChartToPdf(document, createLineChart("Ventes par Trimestre", "Trimestre", "Ventes ($)", salesByQuarter), 50, 500);

                Map<String, BigDecimal> salesByYear = analyzer.getSalesByYear();
                exportChartToPdf(document, createBarChart("Ventes par Année", "Année", "Ventes ($)", salesByYear), 350, 500);
            }
        }
    }

    private void addCountrySection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Ventes par Pays");
            contentStream.endText();

            // Statistiques
            if (includeStats) {
                Map<String, BigDecimal> salesByCountry = analyzer.getSalesByCountry();
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);

                for (Map.Entry<String, BigDecimal> entry : salesByCountry.entrySet()) {
                    contentStream.showText(entry.getKey() + ": " + formatCurrency(entry.getValue()));
                    contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }

            // Graphiques
            if (includeCharts) {
                Map<String, BigDecimal> salesByCountry = analyzer.getSalesByCountry();
                exportChartToPdf(document, createBarChart("Ventes par Pays", "Pays", "Ventes ($)", salesByCountry), 50, 500);
                exportChartToPdf(document, createPieChart("Répartition des Ventes par Pays", salesByCountry), 350, 500);
            }
        }
    }

    private void addCustomersSection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Meilleurs Clients");
            contentStream.endText();

            // Statistiques
            if (includeStats) {
                Map<String, BigDecimal> topCustomers = analyzer.getTopCustomers(10);
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);

                for (Map.Entry<String, BigDecimal> entry : topCustomers.entrySet()) {
                    contentStream.showText(entry.getKey() + ": " + formatCurrency(entry.getValue()));
                    contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }

            // Graphiques
            if (includeCharts) {
                Map<String, BigDecimal> topCustomers = analyzer.getTopCustomers(10);
                exportChartToPdf(document, createBarChart("Top 10 Clients", "Client", "Ventes ($)", topCustomers), 50, 500);
                exportChartToPdf(document, createPieChart("Répartition par Client", topCustomers), 350, 500);
            }
        }
    }

    private void addStatusSection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Statut des Commandes");
            contentStream.endText();

            // Statistiques
            if (includeStats) {
                Map<String, Long> statusDist = analyzer.getOrderStatusDistribution();
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);

                for (Map.Entry<String, Long> entry : statusDist.entrySet()) {
                    contentStream.showText(entry.getKey() + ": " + entry.getValue() + " commandes");
                    contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }

            // Graphiques
            if (includeCharts) {
                Map<String, Long> statusDist = analyzer.getOrderStatusDistribution();
                Map<String, BigDecimal> statusMap = new HashMap<>();
                statusDist.forEach((key, value) -> statusMap.put(key, new BigDecimal(value)));

                exportChartToPdf(document, createBarChart("Statut des Commandes", "Statut", "Nombre", statusMap), 50, 500);
                exportChartToPdf(document, createPieChart("Répartition par Statut", statusMap), 350, 500);
            }
        }
    }

    private void addDealSizeSection(PDDocument document, boolean includeCharts, boolean includeStats) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Tailles d'Affaires");
            contentStream.endText();

            // Statistiques
            if (includeStats) {
                Map<String, Long> dealSizeDist = analyzer.getDealSizeDistribution();
                contentStream.beginText();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(50, 720);

                for (Map.Entry<String, Long> entry : dealSizeDist.entrySet()) {
                    contentStream.showText(entry.getKey() + ": " + entry.getValue() + " affaires");
                    contentStream.newLineAtOffset(0, -20);
                }
                contentStream.endText();
            }

            // Graphiques
            if (includeCharts) {
                Map<String, Long> dealSizeDist = analyzer.getDealSizeDistribution();
                Map<String, BigDecimal> dealSizeMap = new HashMap<>();
                dealSizeDist.forEach((key, value) -> dealSizeMap.put(key, new BigDecimal(value)));

                exportChartToPdf(document, createBarChart("Tailles d'Affaires", "Taille", "Nombre", dealSizeMap), 50, 500);
                exportChartToPdf(document, createPieChart("Répartition par Taille", dealSizeMap), 350, 500);
                exportChartToPdf(document, createStackedBarChart("Taille d'Affaire par Statut", "Taille", "Nombre", analyzeDealSizeByOrderStatus()), 50, 300);
            }
        }
    }

    private void addRawDataSection(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            // Titre de section
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.newLineAtOffset(50, 750);
            contentStream.showText("Données Brutes");
            contentStream.endText();

            // Informations sur les données
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 720);
            contentStream.showText("Nombre total d'enregistrements: " + analyzer.getSalesData().size());
            contentStream.endText();

            // Note sur l'export CSV
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 10);
            contentStream.newLineAtOffset(50, 690);
            contentStream.showText("Note: Pour les données brutes complètes, veuillez utiliser l'export CSV.");
            contentStream.endText();
        }
    }

// Méthodes similaires pour les autres sections (addProductLineSection, addQuarterSection, etc.)

    // Méthode pour créer et exporter des graphiques en PDF
    private void exportChartToPdf(PDDocument document, Chart chart, float x, float y) throws IOException {
        // Définir une taille fixe pour le graphique (en pixels)
        final int chartWidth = 500;
        final int chartHeight = 300; // Réduit la hauteur pour éviter les débordements

        // Nous devons générer l'image du graphique sur le thread JavaFX
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<BufferedImage> imageReference = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                // Créer une nouvelle scène temporaire pour le rendu du graphique
                Scene tempScene = new Scene(new StackPane(chart), chartWidth, chartHeight);

                // Ajuster la taille du graphique pour l'exportation
                chart.setPrefSize(chartWidth, chartHeight);

                // S'assurer que le graphique est correctement disposé
                chart.setAnimated(false); // Désactiver les animations pour un rendu immédiat

                // Appliquer les styles CSS si nécessaire
                if (chart.getScene() == null) {
                    tempScene.getStylesheets().addAll(getStylesheets());
                }

                // Attendre un court instant pour que le graphique soit rendu
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                // Capturer l'image du graphique
                SnapshotParameters params = new SnapshotParameters();
                WritableImage image = chart.snapshot(params, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                imageReference.set(bufferedImage);
            } catch (Exception e) {
                System.err.println("Erreur lors de la génération du graphique: " + e.getMessage());
                e.printStackTrace();
            } finally {
                latch.countDown();
            }
        });

        try {
            // Attendre que l'image soit générée sur le thread JavaFX
            boolean completed = latch.await(5, TimeUnit.SECONDS); // Ajouter un timeout
            if (!completed) {
                throw new IOException("Timeout while generating chart image");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while generating chart image", e);
        }

        BufferedImage bufferedImage = imageReference.get();
        if (bufferedImage == null) {
            throw new IOException("Failed to generate chart image");
        }

        // Convertir en image PDF
        PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);

        // Obtenir la dernière page du document (page actuelle)
        PDPage currentPage = document.getPage(document.getNumberOfPages() - 1);

        // Calculer la hauteur réelle à utiliser sur la page PDF (A4)
        float scale = 0.5f; // Réduire davantage pour s'assurer que ça rentre
        float scaledWidth = pdImage.getWidth() * scale;
        float scaledHeight = pdImage.getHeight() * scale;

        // Vérifier que les coordonnées sont dans les limites de la page
        float pageHeight = currentPage.getMediaBox().getHeight();
        if (y < scaledHeight || y > pageHeight) {
            // Ajuster y pour s'assurer que le graphique est visible
            y = pageHeight - scaledHeight - 50; // 50 points de marge en bas
        }

        // Ne pas laisser x trop proche du bord
        if (x + scaledWidth > currentPage.getMediaBox().getWidth()) {
            x = currentPage.getMediaBox().getWidth() - scaledWidth - 50; // 50 points de marge à droite
        }

        // Ajouter l'image à la page actuelle
        try (PDPageContentStream contentStream = new PDPageContentStream(document, currentPage, PDPageContentStream.AppendMode.APPEND, true)) {
            // Déboguer les dimensions
            System.out.println("Ajout du graphique à: x=" + x + ", y=" + y + ", largeur=" + scaledWidth + ", hauteur=" + scaledHeight);

            contentStream.drawImage(pdImage, x, y - scaledHeight, scaledWidth, scaledHeight);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'ajout de l'image au PDF: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        // Ajouter une petite attente pour s'assurer que tout est bien traité
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    // Méthode pour récupérer les feuilles de style pour les graphiques
    private List<String> getStylesheets() {
        List<String> stylesheets = new ArrayList<>();

        // Si la scène actuelle a des feuilles de style, les récupérer
        if (primaryStage.getScene() != null && primaryStage.getScene().getStylesheets() != null) {
            stylesheets.addAll(primaryStage.getScene().getStylesheets());
        }

        // Ajouter un style par défaut pour les graphiques si nécessaire
        String defaultStyle =
                ".chart { -fx-background-color: white; }" +
                        ".chart-title { -fx-font-size: 14px; -fx-font-weight: bold; }" +
                        ".axis-label { -fx-font-size: 12px; }" +
                        ".chart-pie-label-line { -fx-stroke: #8b4513; -fx-fill: #8b4513; }" +
                        ".chart-pie-label { -fx-fill: #000000; -fx-font-size: 11px; }" +
                        ".chart-legend { -fx-background-color: transparent; -fx-padding: 10px; }" +
                        ".chart-legend-item { -fx-font-size: 10px; }";

        // Créer un fichier CSS temporaire avec ces styles
        try {
            File tempCssFile = File.createTempFile("chart-styles", ".css");
            try (PrintWriter writer = new PrintWriter(tempCssFile)) {
                writer.println(defaultStyle);
            }
            stylesheets.add(tempCssFile.toURI().toURL().toExternalForm());
            tempCssFile.deleteOnExit();
        } catch (Exception e) {
            System.err.println("Erreur lors de la création du fichier CSS temporaire: " + e.getMessage());
        }

        return stylesheets;
    }

    private String formatCurrency(BigDecimal amount) {
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
    }

    private void exportToCsv(File file) throws IOException {
        // Implémenter l'exportation CSV
        // Pour l'instant, nous exportons juste les données brutes
        java.io.PrintWriter writer = new java.io.PrintWriter(file);

        // Écrire les en-têtes
        List<Map<String, String>> salesData = analyzer.getSalesData();
        if (!salesData.isEmpty()) {
            writer.println(String.join(",", salesData.get(0).keySet()));

            // Écrire les données
            for (Map<String, String> row : salesData) {
                writer.println(row.values().stream()
                        .map(value -> escapeCSV(value))
                        .collect(Collectors.joining(",")));
            }
        }

        writer.close();
    }

    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }

        // Échapper les virgules et guillemets
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    // Méthodes utilitaires pour créer les graphiques
    private VBox createStatsCard(String title, String value, String styleClass) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
        card.getStyleClass().add("stats-card");
        if (styleClass != null) {
            card.getStyleClass().add(styleClass);
        }

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("card-value");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private BarChart<String, Number> createBarChart(String title, String xAxis, String yAxis, Map<String, BigDecimal> data) {
        CategoryAxis xAxisObj = new CategoryAxis();
        NumberAxis yAxisObj = new NumberAxis();
        xAxisObj.setLabel(xAxis);
        yAxisObj.setLabel(yAxis);

        BarChart<String, Number> barChart = new BarChart<>(xAxisObj, yAxisObj);
        barChart.setTitle(title);
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);

        data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        barChart.getData().add(series);
        barChart.setPrefSize(500, 400);

        return barChart;
    }

    private BarChart<String, Number> createIntegerBarChart(String title, String xAxis, String yAxis, Map<String, Integer> data) {
        CategoryAxis xAxisObj = new CategoryAxis();
        NumberAxis yAxisObj = new NumberAxis();
        xAxisObj.setLabel(xAxis);
        yAxisObj.setLabel(yAxis);

        BarChart<String, Number> barChart = new BarChart<>(xAxisObj, yAxisObj);
        barChart.setTitle(title);
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);

        data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        barChart.getData().add(series);
        barChart.setPrefSize(500, 400);

        return barChart;
    }

    private BarChart<String, Number> createRatioChart(String title, String xAxis, String yAxis, Map<String, Double> data) {
        CategoryAxis xAxisObj = new CategoryAxis();
        NumberAxis yAxisObj = new NumberAxis();
        xAxisObj.setLabel(xAxis);
        yAxisObj.setLabel(yAxis);

        BarChart<String, Number> barChart = new BarChart<>(xAxisObj, yAxisObj);
        barChart.setTitle(title);
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);

        data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10) // Limiter à 10 éléments pour la lisibilité
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        barChart.getData().add(series);
        barChart.setPrefSize(500, 400);

        return barChart;
    }

    private LineChart<String, Number> createLineChart(String title, String xAxis, String yAxis, Map<String, BigDecimal> data) {
        CategoryAxis xAxisObj = new CategoryAxis();
        NumberAxis yAxisObj = new NumberAxis();
        xAxisObj.setLabel(xAxis);
        yAxisObj.setLabel(yAxis);

        LineChart<String, Number> lineChart = new LineChart<>(xAxisObj, yAxisObj);
        lineChart.setTitle(title);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(title);

        // Trier les entrées par clé pour un affichage chronologique
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry ->
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));

        lineChart.getData().add(series);
        lineChart.setPrefSize(500, 400);

        return lineChart;
    }

    private PieChart createPieChart(String title, Map<String, BigDecimal> data) {
        PieChart pieChart = new PieChart();
        pieChart.setTitle(title);

        // Limiter le nombre de segments pour la lisibilité
        data.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10) // Top 10
                .forEach(entry -> {
                    PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue().doubleValue());
                    pieChart.getData().add(slice);
                });

        pieChart.setPrefSize(500, 400);

        return pieChart;
    }

    private StackedBarChart<String, Number> createStackedBarChart(String title, String xAxis, String yAxis, Map<String, Map<String, Long>> data) {
        CategoryAxis xAxisObj = new CategoryAxis();
        NumberAxis yAxisObj = new NumberAxis();
        xAxisObj.setLabel(xAxis);
        yAxisObj.setLabel(yAxis);

        StackedBarChart<String, Number> stackedBarChart = new StackedBarChart<>(xAxisObj, yAxisObj);
        stackedBarChart.setTitle(title);

        // Créer une série pour chaque statut
        Set<String> statuses = new HashSet<>();
        data.values().forEach(map -> statuses.addAll(map.keySet()));

        for (String status : statuses) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(status);

            for (Map.Entry<String, Map<String, Long>> entry : data.entrySet()) {
                Long value = entry.getValue().getOrDefault(status, 0L);
                series.getData().add(new XYChart.Data<>(entry.getKey(), value));
            }

            stackedBarChart.getData().add(series);
        }

        stackedBarChart.setPrefSize(800, 400);

        return stackedBarChart;
    }

    // Classes internes pour le stockage des données dans les tableaux
    public class CountryData {
        private final javafx.beans.property.SimpleStringProperty country;
        private final javafx.beans.property.SimpleStringProperty sales;
        private final javafx.beans.property.SimpleStringProperty percent;

        public CountryData(String country, String sales, String percent) {
            this.country = new javafx.beans.property.SimpleStringProperty(country);
            this.sales = new javafx.beans.property.SimpleStringProperty(sales);
            this.percent = new javafx.beans.property.SimpleStringProperty(percent);
        }

        public javafx.beans.property.SimpleStringProperty countryProperty() {
            return country;
        }

        public javafx.beans.property.SimpleStringProperty salesProperty() {
            return sales;
        }

        public javafx.beans.property.SimpleStringProperty percentProperty() {
            return percent;
        }
    }

    public class StatusData {
        private final javafx.beans.property.SimpleStringProperty status;
        private final javafx.beans.property.SimpleStringProperty count;
        private final javafx.beans.property.SimpleStringProperty percent;

        public StatusData(String status, String count, String percent) {
            this.status = new javafx.beans.property.SimpleStringProperty(status);
            this.count = new javafx.beans.property.SimpleStringProperty(count);
            this.percent = new javafx.beans.property.SimpleStringProperty(percent);
        }

        public javafx.beans.property.SimpleStringProperty statusProperty() {
            return status;
        }

        public javafx.beans.property.SimpleStringProperty countProperty() {
            return count;
        }

        public javafx.beans.property.SimpleStringProperty percentProperty() {
            return percent;
        }
    }
}