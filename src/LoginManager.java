
import java.util.ArrayList;

public class LoginManager {

    private ArrayList<User> users;

    public LoginManager() {
        users = new ArrayList<>();
    }

    public boolean addUser(User user) {

        if (user == null ||
            ValidationUtils.isBlank(user.getUsername()) ||
            usernameExists(user.getUsername())) {

            return false;
        }

        users.add(user);
        return true;
    }

    // Check if username already exists
    public boolean usernameExists(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return true;
            }
        }

        return false;
    }

    // Remove a user's login account
    public boolean removeUser(User user) {
        return users.remove(user);
    }

    // Authenticate login
    public User authenticate(String username, String password) {
        for (User user : users) {
            if (user.login(username, password)) {
                return user;
            }
        }

        return null;
    }
}
