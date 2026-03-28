public class AuthorizationService {
    public static boolean isManager(User currentUser) {
        if (currentUser == null) {
            return false;
        }
        return currentUser.getRole().equalsIgnoreCase("MANAGER");
    }
    public static boolean isUser(User currentUser) {
        if (currentUser == null) {
            return false;
        }
        return currentUser.getRole().equalsIgnoreCase("USER");
    }
}