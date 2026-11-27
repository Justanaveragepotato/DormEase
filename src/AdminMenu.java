import java.util.Scanner;
import java.util.ArrayList;

public class AdminMenu {

    public void run(
        Scanner scanner,
        RoomManager roomManager,
        WaitingListManager waitingList,
        ArrayList<Student> students,
        TransferRequestManager transferManager,
        LoginManager loginManager
    ) {

        int choice = 0;

        do {

            System.out.println("\n======================================");
            System.out.println("       ADMINISTRATOR DASHBOARD");
            System.out.println("======================================");
            System.out.println("1. View All Rooms");
            System.out.println("2. Add New Room");
            System.out.println("3. Edit Room");
            System.out.println("4. Delete Room");
            System.out.println("5. View All Students");
            System.out.println("6. Register New Student");
            System.out.println("7. View Waiting List");
            System.out.println("8. Automatic Room Assignment");
            System.out.println("9. Manual Room Assignment");
            System.out.println("10. Process Waiting List");
            System.out.println("11. View Transfer Requests");
            System.out.println("12. Approve Transfer Request");
            System.out.println("13. Reject Transfer Request");
            System.out.println("14. Edit Student Record");
            System.out.println("15. Delete Student Record");
            System.out.println("16. Logout");
            System.out.println("======================================");
            System.out.print("Enter your choice: ");

            Integer parsedChoice = ValidationUtils.parseInt(scanner.nextLine());

            if (parsedChoice == null) {
                System.out.println("Invalid input! Enter a number.");
                continue;
            }

            choice = parsedChoice;

            switch (choice) {

                case 1:
                    roomManager.displayAllRooms();
                    break;

                case 2:
                    addRoom(scanner, roomManager, waitingList);
                    break;

                case 3:
                    editRoom(scanner, roomManager);
                    break;

                case 4:
                    deleteRoom(scanner, roomManager);
                    break;

                case 5:
                    displayAllStudents(students);
                    break;

                case 6:
                    registerStudent(
                        scanner, students, loginManager,
                        roomManager, waitingList
                    );
                    break;

                case 7:
                    waitingList.displayWaitingList();
                    break;

                case 8:
                    automaticAssignment(
                        scanner, students, roomManager, waitingList
                    );
                    break;

                case 9:
                    manualAssignment(scanner, students, roomManager, waitingList);
                    break;

                case 10:
                    waitingList.processWaitingList(roomManager);
                    break;

                case 11:
                    transferManager.displayRequests();
                    break;

                case 12:
                    reviewTransfer(
                        scanner, transferManager, roomManager, waitingList, true
                    );
                    break;

                case 13:
                    reviewTransfer(
                        scanner, transferManager, roomManager, waitingList, false
                    );
                    break;

                case 14:
                    editStudent(scanner, students, loginManager);
                    break;

                case 15:
                    deleteStudent(
                        scanner,
                        students,
                        loginManager,
                        roomManager,
                        waitingList,
                        transferManager
                    );
                    break;

                case 16:
                    System.out.println("\nLogging out...");
                    break;

                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 16);
    }

    // =====================================
    // ROOM MANAGEMENT
    // =====================================

    private void addRoom(
        Scanner scanner,
        RoomManager roomManager,
        WaitingListManager waitingList
    ) {
        System.out.println("\n--- ADD NEW ROOM ---");

        System.out.print("Enter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        if (!ValidationUtils.isValidField(roomNumber)) {
            System.out.println("Invalid room number!");
            return;
        }

        String roomType = chooseRoomType(scanner);

        if (roomType == null) {
            return;
        }

        System.out.print("Enter Room Capacity: ");

        Integer capacity = ValidationUtils.parseInt(scanner.nextLine());

        if (capacity == null) {
            System.out.println("Please enter a valid number!");
            return;
        }

        if (capacity < 1) {
            System.out.println("Capacity must be at least 1!");
            return;
        }

        Room room = new Room(roomNumber, roomType, capacity);

        if (roomManager.addRoom(room)) {
            waitingList.processWaitingList(roomManager);
        }
    }

    private void editRoom(Scanner scanner, RoomManager roomManager) {

        System.out.println("\n--- EDIT ROOM ---");

        System.out.print("Enter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        Room room = roomManager.findRoom(roomNumber);

        if (room == null) {
            System.out.println("Room not found!");
            return;
        }

        System.out.println("Current Room Type: " + room.getRoomType());
        String newRoomType = chooseRoomType(scanner);

        if (newRoomType == null) {
            return;
        }

        System.out.println("Current Capacity: " + room.getCapacity());
        System.out.print("Enter New Capacity: ");

        Integer newCapacity = ValidationUtils.parseInt(scanner.nextLine());

        if (newCapacity == null) {
            System.out.println("Please enter a valid number!");
            return;
        }

        if (newCapacity < 1) {
            System.out.println("Capacity must be at least 1!");
            return;
        }

        if (newCapacity < room.getOccupants().size()) {
            System.out.println("Capacity cannot be lower than the current number of occupants!");
            return;
        }

        System.out.print("Should this room be available? (true/false): ");
        Boolean available = ValidationUtils.parseBoolean(scanner.nextLine());

        if (available == null) {
            System.out.println("Please enter true or false!");
            return;
        }

        roomManager.editRoom(
            roomNumber,
            newRoomType,
            newCapacity,
            available
        );
    }

    private void deleteRoom(
        Scanner scanner,
        RoomManager roomManager
    ) {

        System.out.println("\n========== DELETE ROOM ==========");

        System.out.print("Enter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        Room room = roomManager.findRoom(roomNumber);

        if (room == null) {
            System.out.println("Room not found!");
            return;
        }

        room.displayRoomInfo();

        System.out.print("\nAre you sure you want to delete this room? (Y/N): ");

        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        roomManager.deleteRoom(roomNumber);
    }

    // =====================================
    // REGISTER STUDENT
    // =====================================

    
    private void registerStudent(
        Scanner scanner,
        ArrayList<Student> students,
        LoginManager loginManager,
        RoomManager roomManager,
        WaitingListManager waitingList
    ) {

        System.out.println("\n========== REGISTER STUDENT ==========");

        System.out.print("Student ID: ");
        String id = scanner.nextLine().trim();

        if (!ValidationUtils.isValidField(id) ||
            findStudent(students, id) != null) {

            System.out.println("Invalid or existing Student ID!");
            return;
        }

        System.out.print("Full Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        if (!ValidationUtils.isValidField(username) ||
            loginManager.usernameExists(username)) {

            System.out.println("Invalid or existing username!");
            return;
        }

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.print("Course: ");
        String course = scanner.nextLine().trim();

        System.out.print("Year Level: ");
        String yearLevel = scanner.nextLine().trim();

        String roomType = chooseRoomType(scanner);

        // Validate all remaining information
        if (!ValidationUtils.isValidField(name) ||
            !ValidationUtils.isValidField(password) ||
            !ValidationUtils.isValidField(course) ||
            !ValidationUtils.isValidField(yearLevel) ||
            roomType == null) {

            System.out.println(
                "Invalid or incomplete information! Registration cancelled."
            );
            return;
        }

        Student newStudent = new Student(
            id,
            name,
            username,
            password,
            course,
            yearLevel,
            roomType
        );

        if (!loginManager.addUser(newStudent)) {
            System.out.println("Could not create student login. Registration cancelled.");
            return;
        }

        students.add(newStudent);

        System.out.println("\nStudent registered successfully!");

        boolean assigned = roomManager.assignRoom(newStudent);

        if (!assigned) {
            waitingList.addToWaitingList(newStudent);
        }
    }


    // =====================================
    // DISPLAY STUDENTS
    // =====================================

    private void displayAllStudents(ArrayList<Student> students) {

        System.out.println("\n========== STUDENT RECORDS ==========");

        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }

        for (Student student : students) {

            System.out.println("\nStudent ID: " + student.getUserId());
            System.out.println("Name: " + student.getName());
            System.out.println("Username: " + student.getUsername());
            System.out.println("Course: " + student.getCourse());
            System.out.println("Year Level: " + student.getYearLevel());
            System.out.println("Preferred Room: "
                + student.getPreferredRoomType());
            System.out.println("Assigned Room: "
                + student.getAssignedRoom());
        }
    }

    // =====================================
    // AUTOMATIC ROOM ASSIGNMENT
    // =====================================

    private void automaticAssignment(
        Scanner scanner,
        ArrayList<Student> students,
        RoomManager roomManager,
        WaitingListManager waitingList
    ) {

        System.out.print("Enter Student ID: ");

        Student student = findStudent(students, scanner.nextLine());

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        boolean assigned = roomManager.assignRoom(student);

        if (!assigned &&
            student.getAssignedRoom().equals("Not Assigned")) {

            waitingList.addToWaitingList(student);
        } else if (assigned) {
            waitingList.removeFromWaitingList(student);
        }
    }

    // =====================================
    // MANUAL ROOM ASSIGNMENT
    // =====================================

    private void manualAssignment(
        Scanner scanner,
        ArrayList<Student> students,
        RoomManager roomManager,
        WaitingListManager waitingList
    ) {

        System.out.print("Enter Student ID: ");

        Student student = findStudent(students, scanner.nextLine());

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter Room Number: ");
        String roomNumber = scanner.nextLine().trim();

        boolean assigned = roomManager.manuallyAssignRoom(student, roomNumber);

        if (assigned) {
            waitingList.removeFromWaitingList(student);
        }
    }

    // =====================================
    // EDIT STUDENT
    // =====================================

    private void editStudent(
        Scanner scanner,
        ArrayList<Student> students,
        LoginManager loginManager
    ) {

        System.out.println("\n========== EDIT STUDENT ==========");

        System.out.print("Enter Student ID: ");

        Student student = findStudent(students, scanner.nextLine());

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.println("\nCurrent Information:");
        System.out.println("Name: " + student.getName());
        System.out.println("Username: " + student.getUsername());
        System.out.println("Course: " + student.getCourse());
        System.out.println("Year Level: " + student.getYearLevel());
        System.out.println("Preferred Room: "
            + student.getPreferredRoomType());

        System.out.println("\nEnter New Information:");

        System.out.print("New Name: ");
        String name = scanner.nextLine().trim();

        System.out.print("New Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("New Course: ");
        String course = scanner.nextLine().trim();

        System.out.print("New Year Level: ");
        String yearLevel = scanner.nextLine().trim();

        System.out.println("\nNew Preferred Room Type:");
        String roomType = chooseRoomType(scanner);

        if (!ValidationUtils.isValidField(name) ||
            !ValidationUtils.isValidField(username) ||
            !ValidationUtils.isValidField(course) ||
            !ValidationUtils.isValidField(yearLevel) ||
            roomType == null) {

            System.out.println("Invalid information! No changes made.");
            return;
        }

        if (!username.equalsIgnoreCase(student.getUsername()) &&
            loginManager.usernameExists(username)) {

            System.out.println("Username already exists! No changes made.");
            return;
        }

        student.setName(name);
        student.setUsername(username);
        student.setCourse(course);
        student.setYearLevel(yearLevel);
        if (!student.setPreferredRoomType(roomType)) {
            System.out.println("Invalid room type! No changes made.");
            return;
        }

        System.out.println("\nStudent record updated successfully!");
        System.out.println("Note: Existing room assignment was not changed.");
    }

    // =====================================
    // DELETE STUDENT
    // =====================================

    private void deleteStudent(
        Scanner scanner,
        ArrayList<Student> students,
        LoginManager loginManager,
        RoomManager roomManager,
        WaitingListManager waitingList,
        TransferRequestManager transferManager
    ) {

        System.out.println("\n========== DELETE STUDENT ==========");

        System.out.print("Enter Student ID: ");

        Student student = findStudent(students, scanner.nextLine());

        if (student == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.println("\nStudent: " + student.getName());
        System.out.println("Student ID: " + student.getUserId());
        System.out.println("Assigned Room: " + student.getAssignedRoom());

        System.out.print("\nAre you sure you want to delete this student? (Y/N): ");

        String confirmation = scanner.nextLine();

        if (!confirmation.equalsIgnoreCase("Y")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        boolean removed = roomManager.removeStudentFromRoom(student);

        if (!removed) {
            System.out.println("Deletion cancelled because room removal failed.");
            return;
        }

        waitingList.removeFromWaitingList(student);
        transferManager.removeStudentRequests(student);
        loginManager.removeUser(student);
        students.remove(student);

        System.out.println("\nStudent deleted successfully!");

        waitingList.processWaitingList(roomManager);
    }

    // =====================================
    // FIND STUDENT
    // =====================================

    private Student findStudent(
        ArrayList<Student> students,
        String studentId
    ) {

        for (Student student : students) {

            if (student.getUserId().equalsIgnoreCase(studentId.trim())) {
                return student;
            }
        }

        return null;
    }

    // =====================================
    // CHOOSE ROOM TYPE
    // =====================================

    private String chooseRoomType(Scanner scanner) {

        System.out.println("\nRoom Type:");
        System.out.println("1. Single");
        System.out.println("2. Double");
        System.out.println("3. Shared");
        System.out.print("Choose: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1":
                return "Single";

            case "2":
                return "Double";

            case "3":
                return "Shared";

            default:
                System.out.println("Invalid room type!");
                return null;
        }
    }

    // =====================================
    // REVIEW TRANSFER REQUEST
    // =====================================

    private void reviewTransfer(
        Scanner scanner,
        TransferRequestManager transferManager,
        RoomManager roomManager,
        WaitingListManager waitingList,
        boolean approve
    ) {

        if (transferManager.getRequestCount() == 0) {
            System.out.println("No transfer requests available.");
            return;
        }

        transferManager.displayRequests();

        System.out.print("\nEnter request number: ");

        Integer requestNumber = ValidationUtils.parseInt(scanner.nextLine());

        if (requestNumber == null) {
            System.out.println("Invalid request number!");
            return;
        }

        transferManager.reviewRequest(
            requestNumber - 1,
            approve,
            roomManager
        );

        if (approve) {
            waitingList.processWaitingList(roomManager);
        }
    }
}