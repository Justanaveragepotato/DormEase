public class Administrator extends User {

    private String adminPosition;

    public Administrator(String userId, String name, String username, String password, String adminPosition) {
        
        super(userId, name, username, password);

        this.adminPosition = adminPosition;
    }

    public String getAdminPosition() {
        return adminPosition;
    }
    @Override
    public String getRole() {
        return "Administrator";
    }   
}