public class Session {
    private static int userId;
    private static String email;
    private static String firstName;
    private static String lastName;
    private static String role;

    public static void startSession(int id, String emailAddr, String fname, String lname, String userRole) {
        userId = id;
        email = emailAddr;
        firstName = fname;
        lastName = lname;
        role = userRole;
    }

    public static int getUserId() {
        return userId;
    }

    public static String getEmail() {
        return email;
    }

    public static String getFirstName() {
        return firstName;
    }

    public static String getLastName() {
        return lastName;
    }

    public static String getRole() {
        return role;
    }

    public static boolean isLoggedIn() {
        return userId != 0;
    }

    public static void clearSession() {
        userId = 0;
        email = null;
        firstName = null;
        lastName = null;
        role = null;
    }
}