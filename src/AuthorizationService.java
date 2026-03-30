public class AuthorizationService {
    public static boolean isManager(User currentUser) {
        if (currentUser == null) {
            return false;
        }
        return currentUser.getRole().equalsIgnoreCase("manager");
    }
    public static boolean isCustomer(User currentUser) {
        if (currentUser == null) {
            return false;
        }
        return currentUser.getRole().equalsIgnoreCase("customer");
    }
}