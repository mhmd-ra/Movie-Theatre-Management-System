import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class InputValidator {

    // acceptable Username is made of: letters, numbers, underscore, 2-32 characters

    public static boolean validateUsername(String username) {
        String regex = "^[a-zA-Z0-9_]{2,32}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }//end of vvalidateUsername


    // Name: starts with uppercase, followed by lowercase letters, hyphens
    public static boolean validateName(String name) {
        String regex = "^[A-Z][a-z'-]+$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(name);
        return matcher.matches();
    }//end of validateName

    // Password: at least 8 chars, must contain a letter and a digit
    public static boolean validatePassword(String password) {
        String regex = "^(?=.*[A-Za-z])(?=.*\\d).{8,64}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }//end of validatePassword

    // Seats: positive integer between 1 and 10 (customer can't choose more than 10 seats)
    public static boolean validateSeatCount(String seats) {
        String regex = "^([1-9]|10)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(seats);
        return matcher.matches();
    }//end of validateSeatCount

    static public boolean validateDate(String email) {
        String regex = "^\\d{2}([/-])\\d{2}\\1(\\d{4}|\\d{2})$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }//end of validateDate

    public static boolean validateDurationHours(String hours) {
        String regex = "^([1-9]|1[0-9]|2[0-4])$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(hours);
        return matcher.matches();
    }//end of validatehour
}
