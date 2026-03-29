import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserLogin {

    private Stage primaryStage;

    // Input validation regex
    private static final int MAX_LENGTH = 50;

    public UserLogin(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initializeComponents() {
        Label titleLabel = new Label("Movie Theatre Login");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label userLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Label passLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");

        Label messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button signUpButton = new Button("Don't have an account? Sign Up");
        signUpButton.setMaxWidth(Double.MAX_VALUE);
        signUpButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #0077cc; -fx-underline: true; -fx-cursor: hand;");

        signUpButton.setOnAction(e -> {
            new UserSignup(primaryStage).initializeComponents();
        });

        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            // Input validation
            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please enter username and password.");
                return;
            }

            if (username.length() > MAX_LENGTH || password.length() > MAX_LENGTH) {
                messageLabel.setText("Input exceeds maximum allowed length.");
                return;
            }

            if (!InputValidator.validateUsername(username)) {
                messageLabel.setText("Invalid username format.");
                return;
            }

            User user = AuthenticationService.authenticate(username, password);

            if (user == null) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid username or password.");
            } else if (user.getRole().equalsIgnoreCase("manager")) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome Manager " + user.getFirstName() + "!");
                // ManagerDashboard will go here
                new ManagerDashboard(primaryStage,user).show();
            } else if (user.getRole().equalsIgnoreCase("customer")) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome " + user.getFirstName() + "!");
                // CustomerDashboard will go here

            }
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(
                titleLabel, userLabel, usernameField,
                passLabel, passwordField,
                loginButton, messageLabel
        );

        Scene scene = new Scene(layout, 400, 350);
        primaryStage.setTitle("Movie Theatre Management System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}