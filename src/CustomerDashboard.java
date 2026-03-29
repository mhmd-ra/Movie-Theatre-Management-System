import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CustomerDashboard {
    private Stage stage;
    private User currentUser;

    public CustomerDashboard(Stage primaryStage, User currentUser) {
        this.stage = primaryStage;
        this.currentUser = currentUser;
    }

    public void initializeComponents() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button browseBtn   = new Button("Browse Movies & Book Tickets");
        Button bookingsBtn = new Button("My Bookings");
        Button logoutBtn   = new Button("Logout");

//        browseBtn.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) { showBrowseMovies(); }
//        });
//        bookingsBtn.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) { showMyBookings(); }
//        });
        logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new UserLogin(stage).initializeComponents();
            }
        });

        layout.getChildren().addAll(
                new Label("Welcome " + currentUser.getFirstName() + "!"),
                browseBtn, bookingsBtn, logoutBtn
        );

        Scene scene = new Scene(layout, 450, 250);
        stage.setTitle("Customer Dashboard");
        stage.setScene(scene);
        stage.show();
    }

}
