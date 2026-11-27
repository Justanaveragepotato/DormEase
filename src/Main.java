public class Main{

    public static void main(String[] args) {

        Student student = new Student(
            "S001",
            "Juan Dela Cruz",
            "juan123",
            "student123",
            "BSIT",
            "2nd Year"
        );

        Administrator admin1 = new Administrator(
            "A001",
            "Maria Santos",
            "admin123",
            "admin123",
            "Dormitory Manager"
        );

        // Polymorphism demonstration
        User[] users = {student, admin1};

        System.out.println("===============================");
        System.out.println("   DORMITORY MANAGEMENT SYSTEM");
        System.out.println("===============================");

        for (User user : users) {
            System.out.println("\nUser ID: " + user.getUserId());
            System.out.println("Name: " + user.getName());
            System.out.println("Role: " + user.getRole());
        }

        System.out.println("\n================================");
        System.out.println("System initialized successfully!");
    }
}