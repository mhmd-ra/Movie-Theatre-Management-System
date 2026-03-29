import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDashboard {

    private Stage primaryStage;
    private User currentUser;

    public CustomerDashboard(Stage primaryStage, User currentUser) {
        this.primaryStage = primaryStage;
        this.currentUser = currentUser;
    }

    public void initializeComponents() {
        Label titleLabel = new Label("Movie Theatre - Customer Portal");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label welcomeLabel = new Label("Welcome, " + currentUser.getFirstName() + " " + currentUser.getLastName());
        welcomeLabel.setStyle("-fx-font-size: 14px;");

        Button browseBtn = new Button("Browse Movies & Book Tickets");
        browseBtn.setMaxWidth(Double.MAX_VALUE);

        Button bookingsBtn = new Button("My Bookings");
        bookingsBtn.setMaxWidth(Double.MAX_VALUE);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);

        //browseBtn.setOnAction(e -> showBrowseMovies());
        //bookingsBtn.setOnAction(e -> showMyBookings());
        logoutBtn.setOnAction(e -> new UserLogin(primaryStage).initializeComponents());

        VBox layout = new VBox(15);
        layout.setPadding(new Insets(30));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(titleLabel, welcomeLabel, browseBtn, bookingsBtn, logoutBtn);

        Scene scene = new Scene(layout, 450, 350);
        primaryStage.setTitle("Customer Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
