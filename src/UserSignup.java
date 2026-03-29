import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserSignup {
    private Stage stage;
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private TextField firstNameField = new TextField();
    private TextField lastNameField = new TextField();

    public UserSignup(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void initializeComponents() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button createButton = new Button("Create Account");
        createButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                registerUser();
            }
        });

        Button backButton = new Button("Back");
        backButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new UserLogin(stage).initializeComponents();
            }
        });

        layout.getChildren().addAll(
                new Label("Sign Up"),
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                new Label("First Name:"), firstNameField,
                new Label("Last Name:"), lastNameField,
                createButton,
                backButton
        );

        Scene scene = new Scene(layout, 400, 400);
        stage.setTitle("Sign Up");
        stage.setScene(scene);
        stage.show();
    }

    private void registerUser() {
        String username  = usernameField.getText();
        String password  = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName  = lastNameField.getText();

        if (username.isEmpty() || password.isEmpty() || firstName.isEmpty() || lastName.isEmpty()) {
            showAlert("Input Error", "All fields are required.");
            return;
        }
        if (!InputValidator.validateUsername(username)) {
            showAlert("Input Error", "Invalid username. Use 2-32 letters, digits, or underscores.");
            return;
        }
        if (!InputValidator.validatePassword(password)) {
            showAlert("Input Error", "Password must be 8-64 chars and include a letter and a digit.");
            return;
        }
        if (!InputValidator.validateName(firstName)) {
            showAlert("Input Error", "First name must start with a capital letter (e.g. Sara).");
            return;
        }
        if (!InputValidator.validateName(lastName)) {
            showAlert("Input Error", "Last name must start with a capital letter (e.g. Ahmed).");
            return;
        }

        // Role is always customer for self-registration
        boolean success = AuthenticationService.registerUser(username, password, "customer", firstName, lastName);

        if (success) {
            showInfo("Success", "Account created! You can now log in.");
            new UserLogin(stage).initializeComponents();
        } else {
            showAlert("Error", "Username already exists. Please choose another.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfo(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
