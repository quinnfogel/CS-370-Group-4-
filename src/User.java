import java.time.LocalDateTime;

public class User {

    private int userId;
    private String firstName;
    private String lastName;
    private String email;
    private String passwordHash;
    private UserRole role;
    private boolean isActive;
    private LocalDateTime lastLogin;

    public User(int userId, String firstName, String lastName, String email,
                String passwordHash, UserRole role, boolean isActive, LocalDateTime lastLogin) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.lastLogin = lastLogin;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be blank.");
        }
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be blank.");
        }
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank.");
        }
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash cannot be blank.");
        }
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
    protected void setRole(UserRole role) {
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null.");
        }
        this.role = role;
    }

    public boolean isActive() {
        return isActive;
    }

    public void activateAccount() {
        this.isActive = true;
    }

    public void deactivateAccount() {
        this.isActive = false;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", fullName='" + getFullName() + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", lastLogin=" + lastLogin +
                '}';
    }
}