import java.io.*;
import java.nio.file.*;
import java.util.*;

public class SystemTests {

    static int passed = 0;
    static int failed = 0;

    static void ok(boolean cond, String name) {
        if (cond) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    static Path tempDir(String name) throws Exception {
        return Files.createTempDirectory("dormease-system-" + name);
    }

    static Student findStudent(ArrayList<Student> students, String id) {
        for (Student student : students) {
            if (student.getUserId().equalsIgnoreCase(id)) {
                return student;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        testFullUserFlow();
        testInvalidInputDoesNotCorrupt();
        testConfiguredDataDirAndAtomicSave();

        System.out.println("\nRESULT " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testFullUserFlow() throws Exception {
        Path dir = tempDir("flow");

        ArrayList<Student> students = new ArrayList<>();
        LoginManager loginManager = new LoginManager();
        RoomManager roomManager = new RoomManager();
        WaitingListManager waitingList = new WaitingListManager();
        TransferRequestManager transferManager = new TransferRequestManager();
        FileManager fileManager = new FileManager(dir.toFile());

        loginManager.addUser(new Administrator(
            "A001", "Dorm Admin", "admin", "admin123", "Manager"
        ));

        Student s1 = new Student("S001", "Juan", "juan", "pass", "BSIT", "2nd", "Double");
        Student s2 = new Student("S002", "Ana", "ana", "pass", "BSIT", "2nd", "Double");
        Student s3 = new Student("S003", "Pedro", "pedro", "pass", "BSIT", "1st", "Double");

        loginManager.addUser(s1);
        loginManager.addUser(s2);
        loginManager.addUser(s3);
        students.add(s1);
        students.add(s2);
        students.add(s3);

        roomManager.addRoom(new Room("101", "Double", 1));
        roomManager.addRoom(new Room("102", "Double", 1));
        roomManager.addRoom(new Room("103", "Single", 1));

        ok(roomManager.assignRoom(s1), "auto-assign first student");
        ok(roomManager.assignRoom(s2), "auto-assign second student");
        ok(!roomManager.assignRoom(s3), "third double student has no room");

        waitingList.addToWaitingList(s3);
        ok(waitingList.getWaitingStudents().size() == 1, "student enters waiting list");

        AdminMenu adminMenu = new AdminMenu();

        try (Scanner addRooms = new Scanner(
            "2\n104\n2\n1\n" +
            "2\n105\n1\n1\n" +
            "16\n"
        )) {
            adminMenu.run(addRooms, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok(roomManager.findRoom("104") != null, "admin adds Double room");
        ok(roomManager.findRoom("105") != null, "admin adds Single room");
        ok(waitingList.getWaitingStudents().isEmpty(), "waiting list processed after room added");
        ok("104".equals(s3.getAssignedRoom()), "waiting student assigned to new room");

        try (Scanner register = new Scanner(
            "6\nS004\nNew Student\nnewuser\nnewpass\nIT\n1st Year\n2\n16\n"
        )) {
            adminMenu.run(register, roomManager, waitingList, students, transferManager, loginManager);
        }

        Student s4 = findStudent(students, "S004");
        ok(s4 != null, "admin registers new student");
        ok(loginManager.usernameExists("newuser"), "new student login created");
        ok(s4 != null && "Not Assigned".equals(s4.getAssignedRoom()),
            "new double student enters waiting list when full");
        ok(waitingList.getWaitingStudents().size() == 1, "registered student is waiting");

        try (Scanner manual = new Scanner("9\nS004\n105\n16\n")) {
            adminMenu.run(manual, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok("105".equals(s4.getAssignedRoom()), "admin manually assigns student");
        ok(waitingList.getWaitingStudents().isEmpty(), "manual assignment clears waiting list");

        roomManager.addRoom(new Room("106", "Double", 1));

        try (Scanner studentInput = new Scanner("4\n2\nneed quiet\n6\n")) {
            new StudentMenu().run(studentInput, s4, roomManager, transferManager);
        }

        ok(transferManager.getRequestCount() == 1, "student submits transfer request");
        ok("Double".equals(transferManager.getRequests().get(0).getPreferredRoomType()),
            "transfer request stores chosen room type");

        try (Scanner approve = new Scanner("12\n1\n16\n")) {
            adminMenu.run(approve, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok("Approved".equals(transferManager.getRequests().get(0).getStatus()),
            "admin approves transfer request");
        ok("106".equals(s4.getAssignedRoom()), "approved transfer moves student");
        ok(roomManager.findRoom("105").getOccupants().isEmpty(), "old room becomes available");

        transferManager.submitRequest(s4, "Shared", "different floor");

        try (Scanner reject = new Scanner("13\n2\n16\n")) {
            adminMenu.run(reject, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok("Rejected".equals(transferManager.getRequests().get(1).getStatus()),
            "admin rejects transfer request");
        ok("106".equals(s4.getAssignedRoom()), "rejected transfer leaves room unchanged");

        try (Scanner delete = new Scanner("15\nS002\nY\n16\n")) {
            adminMenu.run(delete, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok(findStudent(students, "S002") == null, "admin deletes student");
        ok(!loginManager.usernameExists("ana"), "deleted student login removed");
        ok(roomManager.findRoom("102").getOccupants().isEmpty(), "deleted student room becomes available");

        fileManager.saveData(students, roomManager, transferManager);

        ArrayList<Student> loadedStudents = new ArrayList<>();
        LoginManager loadedLogin = new LoginManager();
        RoomManager loadedRooms = new RoomManager();
        TransferRequestManager loadedTransfers = new TransferRequestManager();

        new FileManager(dir.toFile()).loadData(
            loadedStudents,
            loadedRooms,
            loadedLogin,
            loadedTransfers
        );

        ok(loadedStudents.size() == 3, "restart restores remaining students");
        ok(loadedLogin.usernameExists("newuser"), "restart restores student logins");
        ok(loadedRooms.findRoom("106").getOccupants().size() == 1,
            "restart restores room assignments");
        ok(loadedTransfers.getRequestCount() == 2, "restart restores transfer requests");
    }

    static void testInvalidInputDoesNotCorrupt() throws Exception {
        Path dir = tempDir("invalid");

        ArrayList<Student> students = new ArrayList<>();
        LoginManager loginManager = new LoginManager();
        RoomManager roomManager = new RoomManager();
        WaitingListManager waitingList = new WaitingListManager();
        TransferRequestManager transferManager = new TransferRequestManager();

        roomManager.addRoom(new Room("201", "Double", 1));

        AdminMenu adminMenu = new AdminMenu();

        try (Scanner badRoom = new Scanner("2\n999\n9\n16\n")) {
            adminMenu.run(badRoom, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok(roomManager.findRoom("999") == null, "invalid room type is not added");
        ok(roomManager.getRooms().size() == 1, "invalid room action leaves rooms unchanged");

        try (Scanner badStudent = new Scanner("6\n\n16\n")) {
            adminMenu.run(badStudent, roomManager, waitingList, students, transferManager, loginManager);
        }

        ok(students.isEmpty(), "blank student ID is not registered");
        ok(!loginManager.usernameExists("user"), "invalid registration does not create login");
    }

    static void testConfiguredDataDirAndAtomicSave() throws Exception {
        Path dir = tempDir("configured");
        System.setProperty("dormease.data", dir.toString());

        try {
            ArrayList<Student> students = new ArrayList<>();
            students.add(new Student("S001", "A", "a", "p", "CS", "1", "Single"));

            RoomManager roomManager = new RoomManager();
            roomManager.addRoom(new Room("301", "Single", 1));

            TransferRequestManager transferManager = new TransferRequestManager();

            new FileManager().saveData(students, roomManager, transferManager);

            ok(Files.exists(dir.resolve("students.txt")), "configured data dir used for save");
            ok(Files.exists(dir.resolve("rooms.txt")), "room file saved in configured dir");
            ok(Files.exists(dir.resolve("transfer_requests.txt")), "transfer file saved in configured dir");
            ok(!Files.exists(dir.resolve("students.txt.tmp")), "no student temp file left behind");
            ok(!Files.exists(dir.resolve("rooms.txt.tmp")), "no room temp file left behind");
        } finally {
            System.clearProperty("dormease.data");
        }
    }
}
