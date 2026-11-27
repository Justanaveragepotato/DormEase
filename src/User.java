public abstract class User {

    private String userId;
    private String name;
    private String username;
    private String password;

    public User(String userId, String name, String username, String password) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    // Login validation
    public boolean login(String username, String password) {
        return this.username.equals(username)
                && this.password.equals(password);
    }

    // Every user type must define its role
    public abstract String getRole();
}