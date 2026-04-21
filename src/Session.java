public class Session {

    private static User currentUser;

    public static void startSession(User user) {
        currentUser = user;
    }

    public static int getUserId() {
        return currentUser != null ? currentUser.getUserId() : 0;
    }

    public static String getEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public static String getFirstName() {
        return currentUser != null ? currentUser.getFirstName() : null;
    }

    public static String getLastName() {
        return currentUser != null ? currentUser.getLastName() : null;
    }

    public static String getFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    public static UserRole getRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    public static boolean isActive() {
        return currentUser != null && currentUser.isActive();
    }

    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Student getStudent() {
        if (currentUser instanceof Student) {
            return (Student) currentUser;
        }
        return null;
    }

    public static SCO getSCO() {
        if (currentUser instanceof SCO) {
            return (SCO) currentUser;
        }
        return null;
    }

    public static void clearSession() {
        currentUser = null;
    }
}