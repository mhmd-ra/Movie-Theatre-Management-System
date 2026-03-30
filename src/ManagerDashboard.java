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
}
