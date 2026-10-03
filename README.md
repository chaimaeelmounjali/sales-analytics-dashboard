# 📈 JavaFX Sales Analytics & Business Intelligence Dashboard

[![Java](https://img.shields.io/badge/Java-17-orange.svg?logo=openjdk)](https://www.oracle.com/java/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17-blue.svg?logo=java)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36.svg?logo=apachemaven)](https://maven.apache.org/)
[![PDFBox](https://img.shields.io/badge/Apache%20PDFBox-Report%20Export-red.svg)](https://pdfbox.apache.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

*Bilingual README: [Français](#-version-française) | [English](#-english-version)*

---

## 🇫🇷 Version Française

### 🎯 Objectif
Le projet **Sales Analytics Dashboard** est une application desktop d'aide à la décision et d'intelligence d'affaires (BI) développée en Java 17 et JavaFX. Elle vise à transformer des données de vente brutes issues de fichiers volumineux (CSV) en visualisations interactives claires, indicateurs financiers exploitables et rapports PDF exécutifs générés automatiquement, facilitant ainsi la prise de décision stratégique des gestionnaires commerciaux.

### 🛠️ Stack Technologique
- **Langage & Plateforme** : Java 17 (Architecture orientée objet, Streams API, Concurrence).
- **Interface Graphique (GUI)** : JavaFX 17 (OpenJFX : `javafx-controls`, `javafx-fxml`, `javafx-swing`, animations FadeTransition, styles CSS).
- **Moteur Graphique & Charting** : JavaFX Charts natifs (`LineChart`, `BarChart`, `PieChart`) & intégration JFreeChart.
- **Traitement & Parsing de Données** : OpenCSV 5.9, Java IO / NIO.
- **Export & Reporting** : Apache PDFBox / iText 7.2.5 (capture dynamique des graphiques et génération de rapports PDF professionnels).
- **Build & Gestion des dépendances** : Apache Maven.

### 👩‍💻 Mon Rôle & Contributions
- **Architecture Applicative & Modularité** :
  - Découpage strict selon le patron MVC / séparation des responsabilités : chargement (`SalesDataLoader`), calcul analytique (`SalesDataAnalyzer`), et interface visuelle (`SalesDataVisualizer`).
- **Moteur d'Analyse Métier (`SalesDataAnalyzer`)** :
  - Conception d'algorithmes d'agrégation haute performance avec l'API Java Streams pour calculer le chiffre d'affaires total, le panier moyen, la saisonnalité des ventes et les marges par catégorie de produit.
  - Gestion résiliente des anomalies de formatage et parsing sécurisé des fichiers CSV.
- **Interface Graphique Interactive & Visualisations (`SalesDataVisualizer`)** :
  - Développement de tableaux de bord multi-onglets interactifs avec filtres temporels et par catégories.
  - Implémentation d'un écran d'authentification utilisateur (`LoginScreen`).
  - Intégration de composants visuels modernes (courbes de tendances temporelles, histogrammes comparatifs, diagrammes circulaires de répartition géographique).
- **Génération Automatisée de Rapports PDF** :
  - Capture en mémoire vive des graphiques vectoriels et compilation automatisée d'un rapport PDF prêt à l'impression avec en-tête, métriques clés et visuels.

### 📊 Résultats & Métriques Clés
- **Parsing instantané** : Chargement et traitement fluide de milliers de transactions de vente en quelques fractions de seconde.
- **Export PDF en un clic** : Génération instantanée d'un compte-rendu exécutif illustré de graphiques fidèles.
- **Expérience utilisateur fluide** : Interface desktop moderne et réactive, sans latence grâce au découplage UI / calculs.

---

## 🇬🇧 English Version

### 🎯 Objective
The **Sales Analytics Dashboard** is a desktop Business Intelligence (BI) and sales monitoring application built with Java 17 and JavaFX. It empowers commercial decision-makers by ingesting large-scale transactional sales datasets (CSV), computing critical financial KPIs in real-time, and presenting interactive visual analytics accompanied by automated, print-ready executive PDF reports.

### 🛠️ Tech Stack
- **Language & Core**: Java 17 (Object-Oriented Design, Java Streams API, multi-threading).
- **Graphical User Interface (GUI)**: JavaFX 17 (OpenJFX: Controls, FXML, Swing integration, smooth UI transitions).
- **Visualization Engine**: Native JavaFX Charts (`LineChart`, `BarChart`, `PieChart`) & JFreeChart.
- **Data Ingestion & Parsing**: OpenCSV 5.9, buffered I/O streams.
- **Reporting & Document Generation**: Apache PDFBox & iText 7 (high-resolution chart snapshotting, automated executive PDF compilation).
- **Build System**: Apache Maven.

### 👩‍💻 My Role & Key Contributions
- **Software Architecture & Separation of Concerns**:
  - Structured the codebase into clean layers: ingestion pipeline (`SalesDataLoader`), analytics calculation engine (`SalesDataAnalyzer`), and UI presentation layer (`SalesDataVisualizer`).
- **Data Analytics Engine (`SalesDataAnalyzer`)**:
  - Implemented high-performance aggregation pipelines using Java Streams to compute net revenue, order volume velocity, seasonal growth trends, and product ranking matrices.
  - Built resilient validation handling null or malformed data in CSV inputs.
- **Interactive Dashboard & UX Design (`SalesDataVisualizer`)**:
  - Designed an intuitive dashboard featuring dynamic date and category filtering.
  - Implemented a secure user login gateway (`LoginScreen`).
  - Integrated rich interactive charts tracking monthly trends, product category shares, and regional breakdown.
- **Automated PDF Report Pipeline**:
  - Engineered an automated export service that captures live JavaFX charts into vector images and produces professional multi-page PDF summaries.

### 📊 Key Results & Impact
- **Near-Instantaneous Ingestion**: Smoothly processes multi-thousand transaction records with minimal memory overhead.
- **One-Click Executive Reporting**: Automated generation of publication-grade sales reports.
- **Zero-Latency Desktop Experience**: Highly responsive user experience powered by asynchronous data loading.

---

### 🚀 Quick Start / Démarrage Rapide

#### Prérequis / Prerequisites
- Java JDK 17+
- Apache Maven 3.8+

```bash
# Navigate to the project directory
cd ProjetJava_Chaimae_El_Mounjali

# Compile the project
mvn clean compile

# Run the JavaFX Application
mvn javafx:run
```
