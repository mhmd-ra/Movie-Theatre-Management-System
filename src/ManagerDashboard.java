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

    private void showAllShowtimes() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<Showtime> table = new TableView<>();

        TableColumn<Showtime, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(40);

        TableColumn<Showtime, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(150);

        TableColumn<Showtime, String> roomCol = new TableColumn<>("Room");
        roomCol.setCellValueFactory(new PropertyValueFactory<>("roomName"));
        roomCol.setPrefWidth(70);

        TableColumn<Showtime, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("showDate"));
        dateCol.setPrefWidth(100);

        TableColumn<Showtime, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("showTime"));
        timeCol.setPrefWidth(70);

        TableColumn<Showtime, Integer> seatsCol = new TableColumn<>("Seats Left");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        seatsCol.setPrefWidth(80);

        table.getColumns().addAll(idCol, movieCol, roomCol, dateCol, timeCol, seatsCol);

        ObservableList<Showtime> list = FXCollections.observableArrayList();
        Connection con = DBUtils.establishConnection();
        String query = "SELECT s.id, m.title, r.room_name, s.show_date, s.show_time, s.available_seats " +
                "FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.id " +
                "JOIN theater_rooms r ON s.room_id = r.id " +
                "ORDER BY s.show_date, s.show_time;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Showtime(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("room_name"),
                        rs.getString("show_date"),
                        rs.getString("show_time"),
                        rs.getInt("available_seats")
                ));
            }
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Error loading showtimes: " + e.getMessage());
        }
        table.setItems(list);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { initializeComponents(); }
        });

        layout.getChildren().addAll(new Label("All Showtimes"), table, backBtn);

        Scene scene = new Scene(layout, 580, 480);
        primaryStage.setTitle("All Showtimes");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadAllRoomNames(ObservableList<String> list) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT room_name FROM theater_rooms ORDER BY room_name;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getString("room_name"));
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    private boolean insertMaintenance(String roomName, String date, int duration, String desc) {
        Connection con = DBUtils.establishConnection();
        String query = "INSERT INTO maintenance (room_id, maintenance_date, duration_hours, description) " +
                "VALUES ((SELECT id FROM theater_rooms WHERE room_name = ?), ?, ?, ?);";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, roomName);
            stmt.setString(2, date);
            stmt.setInt(3, duration);
            stmt.setString(4, desc);
            int rows = stmt.executeUpdate();
            DBUtils.closeConnection(con, stmt);
            return rows == 1;
        } catch (Exception e) {
            System.out.println("Insert maintenance error: " + e.getMessage());
            return false;
        }
    }

}
