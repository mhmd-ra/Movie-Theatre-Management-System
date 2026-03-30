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

        browseBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { showBrowseMovies(); }
        });
        bookingsBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { showMyBookings(); }
        });
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


    private void showBrowseMovies() {
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

        TableColumn<Showtime, Integer> seatsCol = new TableColumn<>("Seats Available");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        seatsCol.setPrefWidth(110);

        table.getColumns().addAll(idCol, movieCol, roomCol, dateCol, timeCol, seatsCol);

        ObservableList<Showtime> showtimes = FXCollections.observableArrayList();
        loadShowtimes(showtimes);
        table.setItems(showtimes);

        Label seatsLabel = new Label("Number of Seats (1-10):");
        TextField seatsField = new TextField();

        Button bookBtn = new Button("Book Selected Showtime");
        bookBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Showtime selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    showAlert("Selection Error", "Please select a showtime from the table.");
                    return;
                }
                String seatsText = seatsField.getText().trim();
                if (!InputValidator.validateSeatCount(seatsText)) {
                    showAlert("Input Error", "Enter a valid seat count (1-10).");
                    return;
                }
                int seats = Integer.parseInt(seatsText);
                if (seats > selected.getAvailableSeats()) {
                    showAlert("Booking Error", "Not enough seats available.");
                    return;
                }
                // safeMultiply: seats × price per seat (50 QAR) — prevents integer overflow
                int totalPrice;
                try {
                    totalPrice = SafeMath.safeMultiply(seats, 50);
                } catch (ArithmeticException e) {
                    showAlert("Calculation Error", "Invalid seat count caused overflow.");
                    return;
                }
                boolean success = bookTickets(selected.getId(), seats, totalPrice);
                if (success) {
                    showInfo("Booking Confirmed", "Enjoy the movie! Total: " + totalPrice + " QAR");
                    showtimes.clear();
                    loadShowtimes(showtimes);
                } else {
                    showAlert("Booking Error", "Booking failed. Please try again.");
                }
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { initializeComponents(); }
        });

        layout.getChildren().addAll(
                new Label("Available Showtimes"),
                table,
                seatsLabel, seatsField,
                bookBtn, backBtn
        );

        Scene scene = new Scene(layout, 600, 520);
        stage.setTitle("Browse Movies");
        stage.setScene(scene);
        stage.show();
    }

    private void loadShowtimes(ObservableList<Showtime> list) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT s.id, m.title, r.room_name, s.show_date, s.show_time, s.available_seats " +
                "FROM showtimes s " +
                "JOIN movies m ON s.movie_id = m.id " +
                "JOIN theater_rooms r ON s.room_id = r.id " +
                "WHERE s.available_seats > 0 AND r.status = 'available' " +
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
    }

    private boolean bookTickets(int showtimeId, int seats, int totalPrice) {
        Connection con = DBUtils.establishConnection();
        String insertBooking = "INSERT INTO bookings (customer_id, showtime_id, seats_booked, total_price, status) " +
                "VALUES (?, ?, ?, ?, 'confirmed');";
        String updateSeats   = "UPDATE showtimes SET available_seats = available_seats - ? WHERE id = ?;";
        try {
            con.setAutoCommit(false);

            PreparedStatement bookStmt = con.prepareStatement(insertBooking);
            bookStmt.setInt(1, currentUser.getId());
            bookStmt.setInt(2, showtimeId);
            bookStmt.setInt(3, seats);
            bookStmt.setInt(4, totalPrice);
            bookStmt.executeUpdate();

            // safeSubtract: available_seats - seats — prevents underflow
            PreparedStatement updateStmt = con.prepareStatement(updateSeats);
            updateStmt.setInt(1, seats);
            updateStmt.setInt(2, showtimeId);
            updateStmt.executeUpdate();

            con.commit();
            con.setAutoCommit(true);
            DBUtils.closeConnection(con, bookStmt);
            return true;
        } catch (Exception e) {
            try { con.rollback(); } catch (Exception ex) { System.out.println(ex.getMessage()); }
            System.out.println("Booking error: " + e.getMessage());
            return false;
        }
    }

    private void showMyBookings() {
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        TableView<Booking> table = new TableView<>();

        TableColumn<Booking, Integer> idCol = new TableColumn<>("Booking ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        TableColumn<Booking, String> movieCol = new TableColumn<>("Movie");
        movieCol.setCellValueFactory(new PropertyValueFactory<>("movieTitle"));
        movieCol.setPrefWidth(150);

        TableColumn<Booking, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("showDate"));
        dateCol.setPrefWidth(100);

        TableColumn<Booking, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("showTime"));
        timeCol.setPrefWidth(70);

        TableColumn<Booking, Integer> seatsCol = new TableColumn<>("Seats");
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("seatsBooked"));
        seatsCol.setPrefWidth(60);

        TableColumn<Booking, String> priceCol = new TableColumn<>("Total (QAR)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
        priceCol.setPrefWidth(90);

        TableColumn<Booking, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(90);

        table.getColumns().addAll(idCol, movieCol, dateCol, timeCol, seatsCol, priceCol, statusCol);

        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        loadMyBookings(bookings);
        table.setItems(bookings);

        Button cancelBtn = new Button("Cancel Selected Booking");
        cancelBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                Booking selected = table.getSelectionModel().getSelectedItem();
                if (selected == null) {
                    showAlert("Selection Error", "Please select a booking to cancel.");
                    return;
                }
                if (selected.getStatus().equalsIgnoreCase("cancelled")) {
                    showAlert("Cancel Error", "This booking is already cancelled.");
                    return;
                }
                boolean success = cancelBooking(selected.getId(), selected.getSeatsBooked());
                if (success) {
                    showInfo("Cancelled", "Booking cancelled successfully.");
                    bookings.clear();
                    loadMyBookings(bookings);
                } else {
                    showAlert("Cancel Error", "Cancellation failed. Please try again.");
                }
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) { initializeComponents(); }
        });

        layout.getChildren().addAll(new Label("My Bookings"), table, cancelBtn, backBtn);

        Scene scene = new Scene(layout, 680, 480);
        stage.setTitle("My Bookings");
        stage.setScene(scene);
        stage.show();
    }

    private void loadMyBookings(ObservableList<Booking> list) {
        Connection con = DBUtils.establishConnection();
        String query = "SELECT b.id, m.title, s.show_date, s.show_time, b.seats_booked, b.total_price, b.status " +
                "FROM bookings b " +
                "JOIN showtimes s ON b.showtime_id = s.id " +
                "JOIN movies m ON s.movie_id = m.id " +
                "WHERE b.customer_id = ? " +
                "ORDER BY b.booking_date DESC;";
        try {
            PreparedStatement stmt = con.prepareStatement(query);
            stmt.setInt(1, currentUser.getId());
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Booking(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("show_date"),
                        rs.getString("show_time"),
                        rs.getInt("seats_booked"),
                        String.valueOf(rs.getInt("total_price")),
                        rs.getString("status")
                ));
            }
            DBUtils.closeConnection(con, stmt);
        } catch (Exception e) {
            System.out.println("Error loading bookings: " + e.getMessage());
        }
    }

    private boolean cancelBooking(int bookingId, int seatsBooked) {
        Connection con = DBUtils.establishConnection();
        String cancelQuery  = "UPDATE bookings SET status = 'cancelled' WHERE id = ? AND customer_id = ?;";
        String restoreSeats = "UPDATE showtimes SET available_seats = available_seats + ? " +
                "WHERE id = (SELECT showtime_id FROM bookings WHERE id = ?);";
        try {
            con.setAutoCommit(false);

            PreparedStatement cancelStmt = con.prepareStatement(cancelQuery);
            cancelStmt.setInt(1, bookingId);
            cancelStmt.setInt(2, currentUser.getId());
            cancelStmt.executeUpdate();

            // safeAdd: available_seats + seatsBooked — prevents overflow when restoring
            SafeMath.safeAdd(0, seatsBooked); // validate seatsBooked is safe before DB update
            PreparedStatement restoreStmt = con.prepareStatement(restoreSeats);
            restoreStmt.setInt(1, seatsBooked);
            restoreStmt.setInt(2, bookingId);
            restoreStmt.executeUpdate();

            con.commit();
            con.setAutoCommit(true);
            DBUtils.closeConnection(con, cancelStmt);
            return true;
        } catch (Exception e) {
            try { con.rollback(); } catch (Exception ex) { System.out.println(ex.getMessage()); }
            System.out.println("Cancel error: " + e.getMessage());
            return false;
        }
    }



}
