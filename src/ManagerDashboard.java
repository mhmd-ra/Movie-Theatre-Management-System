import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
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
//menu
    public void initializeComponents() {

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Button scheduleBtn    = new Button("Schedule a Movie");
        Button viewShowsBtn   = new Button("View All Showtimes");
        Button maintenanceBtn = new Button("Schedule Maintenance");
        Button reportBtn      = new Button("Generate Revenue Report");
        Button logoutBtn      = new Button("Logout");

        scheduleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { showScheduleMovie(); }
        });
        viewShowsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { showAllShowtimes(); }
        });
        maintenanceBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { showScheduleMaintenance(); }
        });
        reportBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { showGenerateReport(); }
        });

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

    private void showScheduleMovie() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label movieLabel = new Label("Select Movie:");
        ComboBox<String> movieCombo = new ComboBox<>();
        ObservableList<String> movieNames = FXCollections.observableArrayList();
        loadMovieNames(movieNames);
        movieCombo.setItems(movieNames);

        Label roomLabel = new Label("Select Theatre Room:");
        ComboBox<String> roomCombo = new ComboBox<>();
        ObservableList<String> roomNames = FXCollections.observableArrayList();
        loadRoomNames(roomNames);
        roomCombo.setItems(roomNames);

        Label dateLabel = new Label("Show Date (dd-mm-yyyy):");
        TextField dateField = new TextField();

        Label timeLabel = new Label("Show Time (HH:mm):");
        TextField timeField = new TextField();

        Button scheduleBtn = new Button("Schedule Movie");
        scheduleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String movieName = movieCombo.getValue();
                String roomName  = roomCombo.getValue();
                String date      = dateField.getText().trim();
                String time      = timeField.getText().trim();

                if (movieName == null || roomName == null || date.isEmpty() || time.isEmpty()) {
                    showAlert("Input Error", "All fields are required.");
                    return;
                }
                if (!InputValidator.validateDate(date)) {
                    showAlert("Input Error", "Invalid date format. Use dd-mm-yyyy.");
                    return;
                }
                if (!InputValidator.validateTime(time)) {
                    showAlert("Input Error", "Invalid time format. Use HH:mm (24-hour).");
                    return;
                }
                String[] parts = date.split("-");
                String sqlDate = parts[2] + "-" + parts[1] + "-" + parts[0];

                if (hasConflict(roomName, sqlDate, time)) {
                    showAlert("Conflict", "Scheduling conflict detected for this room at that time.");
                    return;
                }
                boolean success = insertShowtime(movieName, roomName, sqlDate, time);
                if (success) {
                    showInfo("Success", "Movie scheduled successfully.");
                } else {
                    showAlert("Error", "Failed to schedule movie. Please try again.");
                }
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { initializeComponents(); }
        });

        layout.getChildren().addAll(
                new Label("Schedule a Movie"),
                movieLabel, movieCombo,
                roomLabel, roomCombo,
                dateLabel, dateField,
                timeLabel, timeField,
                scheduleBtn, backBtn
        );

        Scene scene = new Scene(layout, 400, 420);
        primaryStage.setTitle("Schedule Movie");
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

    private void setRoomStatus(String roomName, String status) {
        Connection con = DBUtils.establishConnection();
        String query = "UPDATE theater_rooms SET status = ? WHERE room_name = ?;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setString(1, status);
            stmt.setString(2, roomName);
            stmt.executeUpdate();
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Room status update error: " + e.getMessage());
        }
    }

    public static class ReportRow {
        private final javafx.beans.property.SimpleStringProperty  movieTitle;
        private final javafx.beans.property.SimpleIntegerProperty totalBookings;
        private final javafx.beans.property.SimpleStringProperty  revenue;

        public ReportRow(String movieTitle, int totalBookings, String revenue) {
            this.movieTitle = new javafx.beans.property.SimpleStringProperty(movieTitle);
            this.totalBookings = new javafx.beans.property.SimpleIntegerProperty(totalBookings);
            this.revenue = new javafx.beans.property.SimpleStringProperty(revenue);
        }

        public String getMovieTitle(){ return movieTitle.get();}
        public int getTotalBookings(){ return totalBookings.get();}
        public String getRevenue(){ return revenue.get();}
    }

    private void loadReportSummary(Label revenueLabel, Label bookingsLabel, Label customersLabel) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT COUNT(*) AS total_bookings, SUM(total_price) AS total_revenue, " +
                "COUNT(DISTINCT customer_id) AS unique_customers " +
                "FROM bookings WHERE status = 'confirmed';";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                // safeAdd used to verify total_bookings count doesn't overflow int
                int totalBookings = rs.getInt("total_bookings");
                SafeMath.safeAdd(totalBookings, 0);
                revenueLabel.setText("Total Revenue: " + rs.getInt("total_revenue") + " QAR");
                bookingsLabel.setText("Total Bookings: " + totalBookings);
                customersLabel.setText("Unique Customers: " + rs.getInt("unique_customers"));
            }
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Report summary error: " + e.getMessage());
        }
    }

    private void loadRevenuePerMovie(ObservableList<ReportRow> list) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT m.title, COUNT(b.id) AS total_bookings, SUM(b.total_price) AS revenue " +
                "FROM bookings b " +
                "JOIN showtimes s ON b.showtime_id = s.id " +
                "JOIN movies m ON s.movie_id = m.id " +
                "WHERE b.status = 'confirmed' " +
                "GROUP BY m.title ORDER BY revenue DESC;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new ReportRow(
                        rs.getString("title"),
                        rs.getInt("total_bookings"),
                        String.valueOf(rs.getInt("revenue"))
                ));
            }
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Revenue report error: " + e.getMessage());
        }
    }

    private void showGenerateReport() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label totalRevenueLabel   = new Label("Total Revenue: loading...");
        Label totalBookingsLabel  = new Label("Total Bookings: loading...");
        Label totalCustomersLabel = new Label("Unique Customers: loading...");

        loadReportSummary(totalRevenueLabel, totalBookingsLabel, totalCustomersLabel);

        TableView<ReportRow> table = new TableView<>();

        TableColumn<ReportRow, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(200);

        TableColumn<ReportRow, Integer> bookingsCol = new TableColumn<>("Total Bookings");
        bookingsCol.setCellValueFactory(new PropertyValueFactory<>("totalBookings"));
        bookingsCol.setPrefWidth(110);

        TableColumn<ReportRow, String> revenueCol = new TableColumn<>("Revenue (QAR)");
        revenueCol.setCellValueFactory(new PropertyValueFactory<>("revenue"));
        revenueCol.setPrefWidth(110);

        table.getColumns().addAll(movieCol, bookingsCol, revenueCol);

        ObservableList<ReportRow> rows = FXCollections.observableArrayList();
        loadRevenuePerMovie(rows);
        table.setItems(rows);

        Button backBtn = new Button("Back");
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { initializeComponents(); }
        });

        layout.getChildren().addAll(
                new Label("Revenue Report"),
                totalRevenueLabel, totalBookingsLabel, totalCustomersLabel,
                new Separator(),
                new Label("Revenue Per Movie:"),
                table,
                backBtn
        );

        Scene scene = new Scene(layout, 500, 480);
        primaryStage.setTitle("Revenue Report");
        primaryStage.setScene(scene);
        primaryStage.show();
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

    private void showScheduleMaintenance() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        Label roomLabel = new Label("Select Room:");
        ComboBox<String> roomCombo = new ComboBox<>();
        ObservableList<String> roomNames = FXCollections.observableArrayList();
        loadAllRoomNames(roomNames);
        roomCombo.setItems(roomNames);

        Label dateLabel = new Label("Maintenance Date (dd-mm-yyyy):");
        TextField dateField = new TextField();

        Label durationLabel = new Label("Duration (hours, 1-24):");
        TextField durationField = new TextField();

        Label descLabel = new Label("Description:");
        TextField descField = new TextField();

        Button scheduleBtn = new Button("Schedule Maintenance");
        scheduleBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String roomName = roomCombo.getValue();
                String date     = dateField.getText().trim();
                String duration = durationField.getText().trim();
                String desc     = descField.getText().trim();

                if (roomName == null || date.isEmpty() || duration.isEmpty() || desc.isEmpty()) {
                    showAlert("Input Error", "All fields are required.");
                    return;
                }
                if (!InputValidator.validateDate(date)) {
                    showAlert("Input Error", "Invalid date format. Use dd-mm-yyyy.");
                    return;
                }
                if (!InputValidator.validateDurationHours(duration)) {
                    showAlert("Input Error", "Duration must be a whole number between 1 and 24.");
                    return;
                }
                String[] parts = date.split("-");
                String sqlDate = parts[2] + "-" + parts[1] + "-" + parts[0];

                boolean success = insertMaintenance(roomName, sqlDate, Integer.parseInt(duration), desc);
                if (success) {
                    setRoomStatus(roomName, "maintenance");
                    showInfo("Success", "Maintenance scheduled. Room marked as unavailable.");
                } else {
                    showAlert("Error", "Failed to schedule maintenance.");
                }
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { initializeComponents(); }
        });

        layout.getChildren().addAll(
                new Label("Schedule Room Maintenance"),
                roomLabel, roomCombo,
                dateLabel, dateField,
                durationLabel, durationField,
                descLabel, descField,
                scheduleBtn, backBtn
        );

        Scene scene = new Scene(layout, 400, 420);
        primaryStage.setTitle("Schedule Maintenance");
        primaryStage.setScene(scene);
        primaryStage.show();
    }


}
