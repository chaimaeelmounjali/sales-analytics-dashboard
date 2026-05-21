package org.example;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.util.Duration;

public class LoginScreen {
    // Constantes pour les identifiants valides
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin123";

    // Définition des couleurs utilisées dans l'interface
    private static final String PRIMARY_COLOR = "#3498db";       // Bleu principal
    private static final String PRIMARY_DARK = "#2980b9";        // Bleu foncé
    private static final String BACKGROUND_COLOR = "#f5f9fc";    // Couleur de fond
    private static final String TEXT_COLOR = "#2c3e50";          // Couleur du texte
    private static final String ERROR_COLOR = "#e74c3c";         // Rouge pour les erreurs
    private static final String FIELD_BORDER = "#dfe6e9";        // Bordure des champs
    private static final String SUCCESS_COLOR = "#27ae60";       // Vert pour les succès

    // Méthode principale qui affiche l'écran de connexion
    public void show(Stage primaryStage) {
        // Configuration de la fenêtre principale
        primaryStage.setTitle("Connexion - Analyse des Ventes");

        // Création du conteneur racine avec BorderPane
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");

        // Effet d'ombre portée pour le panneau de connexion
        DropShadow dropShadow = new DropShadow();
        dropShadow.setRadius(5.0);
        dropShadow.setOffsetX(3.0);
        dropShadow.setOffsetY(3.0);
        dropShadow.setColor(Color.web("#00000033"));

        // Panneau principal pour le formulaire de connexion
        VBox loginPanel = new VBox(20);
        loginPanel.setAlignment(Pos.CENTER);
        loginPanel.setPadding(new Insets(40, 50, 40, 50));
        loginPanel.setMaxWidth(400);
        loginPanel.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 8px;" +
                        "-fx-border-radius: 8px;"
        );
        loginPanel.setEffect(dropShadow);

        // En-tête avec logo et titre
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER);

        try {
            // Tentative de chargement du logo
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/logo.png")));
            logoView.setFitHeight(50);
            logoView.setPreserveRatio(true);
            header.getChildren().add(logoView);
        } catch (Exception e) {
            // Fallback si le logo n'est pas trouvé
            Text logoText = new Text("📊");
            logoText.setFont(Font.font("Arial", FontWeight.BOLD, 36));
            logoText.setFill(Color.web(PRIMARY_COLOR));
            header.getChildren().add(logoText);
        }

        // Conteneur pour le titre et le sous-titre
        VBox titleBox = new VBox(5);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Text sceneTitle = new Text("Bienvenue !");
        sceneTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        sceneTitle.setFill(Color.web(TEXT_COLOR));

        Text subTitle = new Text("Connectez-vous pour accéder au tableau de bord");
        subTitle.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        subTitle.setFill(Color.web("#7f8c8d"));

        titleBox.getChildren().addAll(sceneTitle, subTitle);
        header.getChildren().add(titleBox);

        // Création du formulaire avec GridPane
        GridPane formGrid = new GridPane();
        formGrid.setVgap(15);  // Espacement vertical entre les lignes
        formGrid.setHgap(10);  // Espacement horizontal entre les colonnes
        formGrid.setAlignment(Pos.CENTER);

        // Style CSS commun pour les labels
        String labelStyle = "-fx-font-size: 14px; -fx-text-fill: " + TEXT_COLOR + ";";

        // Style CSS commun pour les champs de texte
        String textFieldStyle =
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: " + FIELD_BORDER + ";" +
                        "-fx-border-radius: 5px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-padding: 10px;" +
                        "-fx-font-size: 14px;";

        // Champ Nom d'utilisateur
        Label userLabel = new Label("Nom d'utilisateur");
        userLabel.setStyle(labelStyle);

        TextField userTextField = new TextField();
        userTextField.setPromptText("Entrez votre nom d'utilisateur");
        userTextField.setStyle(textFieldStyle);
        userTextField.setPrefWidth(300);
        // Changement de style quand le champ a le focus
        userTextField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                userTextField.setStyle(textFieldStyle + "-fx-border-color: " + PRIMARY_COLOR + ";");
            } else {
                userTextField.setStyle(textFieldStyle);
            }
        });

        formGrid.add(userLabel, 0, 0);
        formGrid.add(userTextField, 0, 1);

        // Champ Mot de passe
        Label pwLabel = new Label("Mot de passe");
        pwLabel.setStyle(labelStyle);

        PasswordField pwBox = new PasswordField();
        pwBox.setPromptText("Entrez votre mot de passe");
        pwBox.setStyle(textFieldStyle);
        pwBox.setPrefWidth(300);
        // Changement de style quand le champ a le focus
        pwBox.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                pwBox.setStyle(textFieldStyle + "-fx-border-color: " + PRIMARY_COLOR + ";");
            } else {
                pwBox.setStyle(textFieldStyle);
            }
        });

        formGrid.add(pwLabel, 0, 2);
        formGrid.add(pwBox, 0, 3);

        // Case à cocher "Se souvenir de moi"
        CheckBox rememberMe = new CheckBox("Se souvenir de moi");
        rememberMe.setStyle("-fx-text-fill: " + TEXT_COLOR + "; -fx-font-size: 13px;");

        // Lien "Mot de passe oublié"
        Hyperlink forgotPassword = new Hyperlink("Mot de passe oublié ?");
        forgotPassword.setStyle("-fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-size: 13px;");
        forgotPassword.setOnAction(e -> {
            // TODO: Implémenter la récupération de mot de passe
            System.out.println("Récupération de mot de passe demandée");
        });

        // Conteneur pour les options (case à cocher + lien)
        HBox optionsBox = new HBox();
        optionsBox.setAlignment(Pos.CENTER_LEFT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        optionsBox.getChildren().addAll(rememberMe, spacer, forgotPassword);

        // Bouton de connexion
        Button loginBtn = new Button("SE CONNECTER");
        loginBtn.setPrefWidth(300);
        loginBtn.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-cursor: hand;"
        );

        // Effet de survol pour le bouton
        loginBtn.setOnMouseEntered(e -> loginBtn.setStyle(
                "-fx-background-color: " + PRIMARY_DARK + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-cursor: hand;"
        ));

        loginBtn.setOnMouseExited(e -> loginBtn.setStyle(
                "-fx-background-color: " + PRIMARY_COLOR + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 14px;" +
                        "-fx-background-radius: 5px;" +
                        "-fx-padding: 12px 15px;" +
                        "-fx-cursor: hand;"
        ));

        // Zone pour afficher les messages d'erreur
        Text actionTarget = new Text();
        actionTarget.setFill(Color.web(ERROR_COLOR));
        actionTarget.setFont(Font.font("Segoe UI", 13));

        // Pied de page avec lien d'inscription
        HBox footerBox = new HBox();
        footerBox.setAlignment(Pos.CENTER);
        Text noAccountText = new Text("Pas encore de compte ? ");
        noAccountText.setFont(Font.font("Segoe UI", 13));
        noAccountText.setFill(Color.web("#7f8c8d"));

        Hyperlink registerLink = new Hyperlink("Créer un compte");
        registerLink.setStyle("-fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-size: 13px;");
        registerLink.setOnAction(e -> {
            // TODO: Implémenter la création de compte
            System.out.println("Redirection vers l'inscription");
        });

        footerBox.getChildren().addAll(noAccountText, registerLink);

        // Gestion de l'action de connexion
        loginBtn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();

            // Validation des champs vides
            if (username.isEmpty() || password.isEmpty()) {
                actionTarget.setText("Veuillez remplir tous les champs");
                shakeElement(actionTarget);
                return;
            }

            // Vérification des identifiants
            if (VALID_USERNAME.equals(username) && VALID_PASSWORD.equals(password)) {
                // Connexion réussie
                actionTarget.setFill(Color.web(SUCCESS_COLOR));
                actionTarget.setText("Connexion réussie ! Redirection...");

                // Animation de fondu avant la redirection
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1000), root);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.3);
                fadeOut.setOnFinished(event -> {
                    // Redirection vers l'application principale
                    SalesDataVisualizer mainApp = new SalesDataVisualizer();
                    mainApp.showMainApp(primaryStage);
                });
                fadeOut.play();
            } else {
                // Identifiants incorrects
                actionTarget.setText("Nom d'utilisateur ou mot de passe incorrect");
                shakeElement(actionTarget);
            }
        });

        // Ajout de tous les éléments au panneau de connexion
        loginPanel.getChildren().addAll(
                header,
                formGrid,
                optionsBox,
                loginBtn,
                actionTarget,
                footerBox
        );

        // Centrage du panneau de connexion
        root.setCenter(loginPanel);

        // Pied de page de la fenêtre
        Text footerText = new Text("© 2025 Analyse des Ventes - Tous droits réservés");
        footerText.setFont(Font.font("Segoe UI", 12));
        footerText.setFill(Color.web("#95a5a6"));
        BorderPane.setAlignment(footerText, Pos.CENTER);
        BorderPane.setMargin(footerText, new Insets(10));
        root.setBottom(footerText);

        // Configuration de la scène
        Scene scene = new Scene(root, 550, 650);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();

        // Configuration de la fenêtre
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(550);
        primaryStage.setMinHeight(650);

        // Affichage de la fenêtre
        primaryStage.show();

        // Focus initial sur le champ utilisateur
        userTextField.requestFocus();
    }

    /**
     * Animation de secousse pour un élément (utilisé pour les erreurs)
     * @param node L'élément à animer
     */
    private void shakeElement(Node node) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(0), new KeyValue(node.translateXProperty(), 0)),
                new KeyFrame(Duration.millis(50), new KeyValue(node.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(100), new KeyValue(node.translateXProperty(), 5)),
                new KeyFrame(Duration.millis(150), new KeyValue(node.translateXProperty(), -5)),
                new KeyFrame(Duration.millis(200), new KeyValue(node.translateXProperty(), 0))
        );
        timeline.play();
    }
}