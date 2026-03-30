import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ManagerDashboard {
    private Stage primaryStage;
    private User currentUser;

    public ManagerDashboard(Stage primaryStage, User currentUser){
        this.primaryStage=primaryStage;
        this.currentUser=currentUser;
    }

    public void initializeComponents() {

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button scheduleBtn    = new Button("Schedule a Movie");
        Button viewShowsBtn   = new Button("View All Showtimes");
        Button maintenanceBtn = new Button("Schedule Maintenance");
        Button reportBtn      = new Button("Generate Revenue Report");
        Button logoutBtn      = new Button("Logout");

        logoutBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                new UserLogin(primaryStage).initializeComponents();
            }
        });

        layout.getChildren().addAll(
                new Label("Welcome Manager " + currentUser.getFirstName() + "!"),
                scheduleBtn, viewShowsBtn, maintenanceBtn, reportBtn,
                logoutBtn
        );

        Scene scene = new Scene(layout, 400, 280);
        primaryStage.setTitle("Manager Dashboard");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private boolean hasConflict(String roomName, String date, String time) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT COUNT(*) FROM showtimes s " +
                "JOIN theater_rooms r ON s.room_id = r.id " +
                "WHERE r.room_name = ? AND s.show_date = ? " +
                "AND ABS(TIMESTAMPDIFF(MINUTE, s.show_time, ?)) < 180;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, roomName);
            stmt.setString(2, date);
            stmt.setString(3, time);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                DBUtils.closeConnection(con, stmt);
                return true;
            }
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Conflict check error: " + e.getMessage());
        }
        return false;
    }

    private void loadMovieNames(ObservableList<String> list) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT title FROM movies ORDER BY title;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("title"));
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Error loading movies: " + e.getMessage());
        }
    }

    private void loadRoomNames(ObservableList<String> list) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT room_name FROM theater_rooms WHERE status = 'available' ORDER BY room_name;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("room_name"));
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    private boolean insertShowtime(String movieTitle, String roomName, String date, String time) {
        Connection con = DBUtils.establishConnection();
        String query = "INSERT INTO showtimes (movie_id, room_id, show_date, show_time, available_seats) " +
                "VALUES ((SELECT id FROM movies WHERE title = ?), " +
                "(SELECT id FROM theater_rooms WHERE room_name = ?), ?, ?, " +
                "(SELECT capacity FROM theater_rooms WHERE room_name = ?));";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, movieTitle);
            stmt.setString(2, roomName);
            stmt.setString(3, date);
            stmt.setString(4, time);
            stmt.setString(5, roomName);
            int rows = stmt.executeUpdate();
            DBUtils.closeConnection(con, stmt);
            return rows == 1;
        } catch (Exception e) {
            System.out.println("Insert showtime error: " + e.getMessage());
            return false;
        }
    }
    
}
