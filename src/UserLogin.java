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

public class UserLogin {
    private Scene loginScene;
    private TextField usernameField = new TextField();
    private PasswordField passwordField = new PasswordField();
    private Stage stage;

    public UserLogin(Stage primaryStage) {
        this.stage = primaryStage;
    }

    public void initializeComponents() {
        VBox loginLayout = new VBox(10);
        loginLayout.setPadding(new Insets(10));

        Button loginButton = new Button("Sign In");
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                validateLogin();
            }
        });

        Label orLabel = new Label("or");

        Button signUpButton = new Button("Sign Up");
        signUpButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new UserSignup(stage).initializeComponents();
            }
        });

        loginLayout.getChildren().addAll(
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                loginButton,
                orLabel,
                signUpButton
        );

        loginScene = new Scene(loginLayout, 400, 300);
        stage.setTitle("Movie Theatre - Login");
        stage.setScene(loginScene);
        stage.show();
    }

    private void validateLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Input Error", "Please enter username and password.");
            return;
        }
        if (!InputValidator.validateUsername(username)) {
            showAlert("Input Error", "Invalid username format.");
            return;
        }

        User loggedInUser = AuthenticationService.authenticate(username, password);

        if (loggedInUser != null) {
            if (AuthorizationService.isManager(loggedInUser)) {
                new ManagerDashboard(stage, loggedInUser).initializeComponents();
            } else if (AuthorizationService.isCustomer(loggedInUser)) {
                new CustomerDashboard(stage, loggedInUser).initializeComponents();
            }
        } else {
            showAlert("Authentication Failed", "Invalid username or password.");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}