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

            if (!username.matches("^[a-zA-Z0-9_]+$")) {
                messageLabel.setText("Invalid username format.");
                return;
            }

            User user = authenticate(username, password);

            if (user == null) {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid username or password.");
            } else if (user.getRole().equals("manager")) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Welcome Manager " + user.getFirstName() + "!");
                // ManagerDashboard will go here
            } else if (user.getRole().equals("customer")) {
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

    private User authenticate(String username, String password) {
        User user = null;
        Connection con = DBUtils.establishConnection();
        try {
            // PreparedStatement prevents SQL injection
            String sql = "SELECT * FROM users WHERE username = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                // BCrypt checks the password against the stored hash
                if (BCrypt.checkpw(password, storedHash)) {
                    user = new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            storedHash,
                            rs.getString("role"),
                            rs.getString("first_name"),
                            rs.getString("last_name")
                    );
                }
            }
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return user;
    }
}