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
    
}
