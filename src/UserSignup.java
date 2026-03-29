import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserSignup {
    private Stage primaryStage;

    public UserSignup(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void initializeComponents() {
        Label titleLabel = new Label("Create Account");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("2-32 chars, letters/digits/underscore");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("At least 8 chars, include a letter and digit");

        Label firstNameLabel = new Label("First Name:");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("e.g. Sara");

        Label lastNameLabel = new Label("Last Name:");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("e.g. Ahmed");

        Label messageLabel = new Label("");
        messageLabel.setStyle("-fx-text-fill: red;");

        Button createButton = new Button("Create Account");
        createButton.setMaxWidth(Double.MAX_VALUE);

        Button backButton = new Button("Back");
        backButton.setMaxWidth(Double.MAX_VALUE);

        createButton.setOnAction(e -> {
            String username  = usernameField.getText().trim();
            String password  = passwordField.getText().trim();
            String firstName = firstNameField.getText().trim();
            String lastName  = lastNameField.getText().trim();

            // Input validation
            if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
                messageLabel.setText("All fields are required.");
                return;
            }
            if (!InputValidator.validateUsername(username)) {
                messageLabel.setText("Invalid username. Use 2-32 letters, digits, or underscores.");
                return;
            }
            if (!InputValidator.validatePassword(password)) {
                messageLabel.setText("Password must be 8-64 chars and include a letter and digit.");
                return;
            }
            if (!InputValidator.validateName(firstName)) {
                messageLabel.setText("First name must start with a capital letter.");
                return;
            }
            if (!InputValidator.validateName(lastName)) {
                messageLabel.setText("Last name must start with a capital letter.");
                return;
            }

            // Role is always customer for self-registration
            boolean success = AuthenticationService.registerUser(username, password, "customer", firstName, lastName);

            if (success) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Account created! You can now log in.");
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Username already exists. Please choose another.");
            }
        });

        backButton.setOnAction(e -> {
            new UserLogin(primaryStage).initializeComponents();
        });

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER_LEFT);
        layout.getChildren().addAll(
                titleLabel,
                usernameLabel, usernameField,
                passwordLabel, passwordField,
                firstNameLabel, firstNameField,
                lastNameLabel, lastNameField,
                createButton,
                backButton,
                messageLabel
        );

        Scene scene = new Scene(layout, 400, 500);
        primaryStage.setTitle("Sign Up");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
