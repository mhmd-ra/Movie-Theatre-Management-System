import org.mindrot.jbcrypt.BCrypt;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthenticationService {
    public static User authenticate(String username, String suppliedPassword){
        Connection con = DBUtils.establishConnection();
        String query = "SELECT * FROM users WHERE username = ?;";
        User loggedInUser = null;

        try {
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, username);
            ResultSet rs = statement.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("password");
                // The method checkpw extracts the salt from the stored password, uses it
                // to hash the supplied password, and then compares the two values
                boolean correctPassword = BCrypt.checkpw(suppliedPassword, storedPassword);
                if (correctPassword){
                    loggedInUser =  new User(
                            rs.getInt("id"),
                            rs.getString("username"),
                            storedPassword,
                            rs.getString("role"),
                            rs.getString("firstname"),
                            rs.getString("lastname")
                    );
                }
            }

            DBUtils.closeConnection(con, statement);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return loggedInUser;
    }
    public static boolean registerUser(String username, String plainPassword, String role,
                                       String firstName, String lastName) {
        String salt = BCrypt.gensalt(12);
        String hashedPassword = BCrypt.hashpw(plainPassword, salt);

        Connection con = DBUtils.establishConnection();
        String query = "INSERT INTO users (username, password, role, firstname, lastname) VALUES (?, ?, ?, ?, ?);";

        try {
            PreparedStatement statement = con.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, role);
            statement.setString(4, firstName);
            statement.setString(5, lastName);
            int rows = statement.executeUpdate();
            DBUtils.closeConnection(con, statement);
            return rows == 1;
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }    
}
