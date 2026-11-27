import java.util.Scanner;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // =====================================
        // 1. INITIALIZE SYSTEM MANAGERS
        // =====================================

        ArrayList<Student> students = new ArrayList<>();

        LoginManager loginManager = new LoginManager();
        RoomManager roomManager = new RoomManager();
        WaitingListManager waitingList = new WaitingListManager();

        TransferRequestManager transferManager =
            new TransferRequestManager();

        FileManager fileManager = new FileManager();

        // Default administrator account
        Administrator admin = new Administrator(
            "A001",
            "Dormitory Admin",
            "admin",
            "admin123",
            "Dormitory Manager"
        );

        if (!loginManager.addUser(admin)) {
            System.out.println("Default administrator account could not be created.");
            scanner.close();
            return;
        }

        // =====================================
        // 2. LOAD OR INITIALIZE DATA
        // =====================================

        System.out.println("======================================");
        System.out.println("       STUDENT DORMITORY SYSTEM");
        System.out.println("======================================");

        if (fileManager.hasSavedData()) {

            System.out.println("\nLoading saved system data...");

            fileManager.loadData(
                students,
                roomManager,
                loginManager,
                transferManager
            );

            // Restore unassigned students to waiting list
            for (Student student : students) {

                if (student.getAssignedRoom().equals("Not Assigned")) {
                    waitingList.addToWaitingList(student);
                }
            }

        } else {

            System.out.println("\nFirst-time system setup...");
            System.out.println("Creating default records...");

            // Create sample students
            Student student1 = new Student(
                "S001", "Juan Dela Cruz", "juan123",
                "student123", "BSIT", "2nd Year", "Double"
            );

            Student student2 = new Student(
                "S002", "Pedro Santos", "pedro123",
                "student123", "BSCS", "1st Year", "Double"
            );

            Student student3 = new Student(
                "S003", "Ana Reyes", "ana123",
                "student123", "BSIT", "2nd Year", "Double"
            );

            Student[] newStudents = { student1, student2, student3 };

            for (Student student : newStudents) {
                if (loginManager.addUser(student)) {
                    students.add(student);
                }
            }

            // Create default rooms
            roomManager.addRoom(new Room("101", "Double", 2));
            roomManager.addRoom(new Room("102", "Single", 1));
            roomManager.addRoom(new Room("103", "Shared", 4));

            // Initial room assignment
            System.out.println("\nInitializing room assignments...");

            for (Student student : students) {

                boolean assigned = roomManager.assignRoom(student);

                if (!assigned) {
                    waitingList.addToWaitingList(student);
                }
            }

            // Save initial records
            fileManager.saveData(students, roomManager, transferManager);
        }

        // Array of superclass references holding different User subclasses.
        // getRole() is called polymorphically for each object.
        User[] accounts = new User[students.size() + 1];
        accounts[0] = admin;

        for (int i = 0; i < students.size(); i++) {
            accounts[i + 1] = students.get(i);
        }

        displayAccountSummary(accounts);

        // =====================================
        // 3. LOGIN SESSION LOOP
        // =====================================

        boolean running = true;

        while (running) {

            System.out.println("\n======================================");
            System.out.println("              USER LOGIN");
            System.out.println("======================================");
            System.out.println("1. Login");
            System.out.println("2. Exit System");
            System.out.print("Choose an option: ");

            String menuChoice = scanner.nextLine().trim();

            if (menuChoice.equals("2")) {
                running = false;
                continue;
            }

            if (!menuChoice.equals("1")) {
                System.out.println("Invalid choice!");
                continue;
            }

            System.out.print("\nUsername: ");
            String username = ValidationUtils.trimToNull(scanner.nextLine());

            System.out.print("Password: ");
            String password = scanner.nextLine();

            if (username == null || ValidationUtils.isBlank(password)) {
                System.out.println("\nUsername and password cannot be blank.");
                continue;
            }

            User loggedInUser =
                loginManager.authenticate(username, password);

            if (loggedInUser == null) {
                System.out.println("\nInvalid username or password.");
                continue;
            }

            printWelcomeMessage(loggedInUser);

            // =====================================
            // 4. OPEN USER DASHBOARD
            // =====================================

            if (loggedInUser instanceof Administrator) {

                AdminMenu adminMenu = new AdminMenu();

                adminMenu.run(
                    scanner,
                    roomManager,
                    waitingList,
                    students,
                    transferManager,
                    loginManager
                );

            } else if (loggedInUser instanceof Student) {

                Student loggedInStudent = (Student) loggedInUser;

                StudentMenu studentMenu = new StudentMenu();

                studentMenu.run(
                    scanner,
                    loggedInStudent,
                    roomManager,
                    transferManager
                );
            }

            // Save changes after each dashboard session
            fileManager.saveData(students, roomManager, transferManager);

            System.out.println("\nYou have been logged out.");
        }

        // =====================================
        // 5. SAVE AND TERMINATE PROGRAM
        // =====================================

        System.out.println("\n======================================");
        System.out.println("       SYSTEM SHUTTING DOWN");
        System.out.println("======================================");

        fileManager.saveData(students, roomManager, transferManager);

        System.out.println("All saved records have been updated.");
        System.out.println("Goodbye!");

        scanner.close();
    }

    private static void displayAccountSummary(User[] users) {

        System.out.println("\n===== LOADED ACCOUNTS =====");

        if (users == null || users.length == 0) {
            System.out.println("No accounts loaded.");
            return;
        }

        for (User user : users) {
            if (user == null) {
                continue;
            }
            System.out.println(
                "- " + user.getRole() + " | "
                + user.getName() + " | "
                + user.getUserId()
            );
        }
    }

    private static void printWelcomeMessage(User user) {

        System.out.println("\nLogin successful!");
        System.out.println(
            "Welcome, " + user.getRole() + " " + user.getName()
        );
    }
}