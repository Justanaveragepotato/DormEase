import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ValidationTests {

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
        return Files.createTempDirectory("dormease-" + name);
    }

    public static void main(String[] args) throws Exception {
        testUtilityValidation();
        testRoomTypeNormalization();
        testRoomValidation();
        testStudentValidation();
        testLoginManagerValidation();
        testTransferValidation();
        testWaitingListEdgeCases();
        testPolymorphismAndArray();
        testFileLoadValidation();

        System.out.println("\nRESULT " + passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    static void testUtilityValidation() {
        ok(ValidationUtils.isBlank(null), "null is blank");
        ok(ValidationUtils.isBlank("   "), "whitespace is blank");
        ok(!ValidationUtils.isBlank("A"), "nonblank text accepted");

        ok(ValidationUtils.containsDelimiter("a|b"), "delimiter detected");
        ok(ValidationUtils.isValidField("  valid  "), "valid field accepted");
        ok(!ValidationUtils.isValidField("   "), "blank field rejected");
        ok(!ValidationUtils.isValidField("a|b"), "delimited field rejected");

        ok(Integer.valueOf(5).equals(ValidationUtils.parseInt(" 5 ")), "parseInt accepts number");
        ok(ValidationUtils.parseInt("abc") == null, "parseInt rejects text");
        ok(Boolean.TRUE.equals(ValidationUtils.parseBoolean("TRUE")), "parseBoolean true");
        ok(Boolean.FALSE.equals(ValidationUtils.parseBoolean(" false ")), "parseBoolean false");
        ok(ValidationUtils.parseBoolean("yes") == null, "parseBoolean rejects other text");
    }

    static void testRoomTypeNormalization() {
        ok("Single".equals(Room.normalizeRoomType("single")), "normalize single");
        ok("Double".equals(Room.normalizeRoomType(" DOUBLE ")), "normalize double");
        ok("Shared".equals(Room.normalizeRoomType("shared")), "normalize shared");
        ok(Room.normalizeRoomType("1") == null, "reject numeric room type");
        ok(Room.normalizeRoomType("Suite") == null, "reject unknown room type");
        ok(Room.normalizeRoomType("") == null, "reject empty room type");
        ok(Room.normalizeRoomType(null) == null, "reject null room type");
    }

    static void testRoomValidation() {
        Room r = new Room(" 101 ", "single", 2);
        ok("101".equals(r.getRoomNumber()), "room constructor trims room number");
        ok("Single".equals(r.getRoomType()), "room constructor normalizes type");
        ok(!r.setRoomType("9"), "room setter rejects numeric type");
        ok("Single".equals(r.getRoomType()), "room type unchanged after invalid set");
        ok(r.setRoomType("double"), "room setter accepts valid type");
        ok("Double".equals(r.getRoomType()), "room setter normalizes valid type");
        ok(!r.setCapacity(0), "room rejects zero capacity");
        ok(r.setCapacity(3), "room accepts valid capacity");

        RoomManager rm = new RoomManager();
        ok(!rm.addRoom(new Room("102", "1", 2)), "room manager rejects invalid type add");
        ok(rm.getRooms().isEmpty(), "invalid room not added");
        ok(rm.addRoom(new Room("103", "Shared", 2)), "room manager accepts valid type");
        ok(!rm.editRoom("103", "7", 2, true), "editRoom rejects invalid type");
        ok("Shared".equals(rm.findRoom("103").getRoomType()), "editRoom keeps old type on invalid input");
    }

    static void testStudentValidation() {
        Student s = new Student("S1", "A", "a", "p", "CS", "1", "single");
        ok("Single".equals(s.getPreferredRoomType()), "student normalizes preferred type");
        ok(!s.setPreferredRoomType("3"), "student rejects numeric preferred type");
        ok("Single".equals(s.getPreferredRoomType()), "student preferred type unchanged on invalid set");
        ok(s.setPreferredRoomType("shared"), "student accepts valid preferred type");
        ok("Shared".equals(s.getPreferredRoomType()), "student normalizes valid preferred type");

        s.setAssignedRoom(" 102 ");
        ok("102".equals(s.getAssignedRoom()), "assigned room is trimmed");
        s.setAssignedRoom("   ");
        ok("Not Assigned".equals(s.getAssignedRoom()), "blank assigned room resets to Not Assigned");
        s.setAssignedRoom(null);
        ok("Not Assigned".equals(s.getAssignedRoom()), "null assigned room resets to Not Assigned");
    }

    static void testLoginManagerValidation() {
        LoginManager lm = new LoginManager();
        Student s = new Student("S1", "A", "alice", "p", "CS", "1", "Single");
        Administrator admin = new Administrator("A1", "Admin", "admin", "pw", "Manager");

        ok(lm.addUser(s), "valid user added");
        ok(!lm.addUser(null), "null user rejected");
        ok(!lm.addUser(new Student("S2", "B", "  ", "p", "CS", "1", "Single")),
            "blank username rejected");
        ok(!lm.addUser(new Student("S3", "C", "ALICE", "p", "CS", "1", "Single")),
            "duplicate username rejected case-insensitively");
        ok(lm.addUser(admin), "administrator added");
        ok(lm.authenticate("admin", "pw") == admin, "authentication returns admin");
        ok(lm.authenticate("alice", "wrong") == null, "wrong password rejected");
    }

    static void testTransferValidation() {
        RoomManager rm = new RoomManager();
        rm.addRoom(new Room("201", "Single", 1));
        rm.addRoom(new Room("202", "Double", 1));

        Student s = new Student("S1", "A", "a", "p", "CS", "1", "Single");
        ok(rm.assignRoom(s), "assign initial room");

        TransferRequestManager tm = new TransferRequestManager();

        tm.submitRequest(s, "7", "reason");
        ok(tm.getRequestCount() == 0, "transfer rejects numeric room type");

        tm.submitRequest(s, "Double", "  need quieter room  ");
        ok(tm.getRequestCount() == 1, "transfer accepts valid type");
        ok("Double".equals(tm.getRequests().get(0).getPreferredRoomType()),
            "transfer stores normalized type");
        ok("need quieter room".equals(tm.getRequests().get(0).getReason()),
            "transfer trims reason");

        tm.submitRequest(s, "Single", "another");
        ok(tm.getRequestCount() == 1, "duplicate pending transfer blocked");

        tm.restoreRequest(s, "9", "bad", "Pending");
        ok(tm.getRequestCount() == 1, "restore rejects invalid room type");
        tm.restoreRequest(s, "Single", "bad", "Weird");
        ok(tm.getRequestCount() == 1, "restore rejects invalid status");
    }

    static void testWaitingListEdgeCases() {
        RoomManager rm = new RoomManager();
        rm.addRoom(new Room("301", "Single", 1));

        Student assigned = new Student("S1", "A", "a", "p", "CS", "1", "Single");
        Student unassigned = new Student("S2", "B", "b", "p", "CS", "1", "Double");

        ok(rm.assignRoom(assigned), "assign student before waiting-list test");

        WaitingListManager wl = new WaitingListManager();
        wl.addToWaitingList(null);
        wl.addToWaitingList(assigned);
        ok(wl.getWaitingStudents().isEmpty(), "assigned/null students are not queued");

        wl.addToWaitingList(unassigned);
        wl.addToWaitingList(unassigned);
        ok(wl.getWaitingStudents().size() == 1, "duplicate waiting-list entry prevented");

        wl.processWaitingList(null);
        ok(wl.getWaitingStudents().size() == 1, "null room manager leaves queue unchanged");

        wl.processWaitingList(rm);
        ok(wl.getWaitingStudents().size() == 1, "unmatched student requeued after failed assignment");
        ok(unassigned.getAssignedRoom().equals("Not Assigned"), "requeued student stays unassigned");
    }

    static void testPolymorphismAndArray() {
        User[] accounts = {
            new Administrator("A1", "Admin", "admin", "pw", "Manager"),
            new Student("S1", "Stu", "stu", "pw", "CS", "1", "Double")
        };

        ok(accounts.length == 2, "User array holds multiple subclasses");
        ok("Administrator".equals(accounts[0].getRole()), "Administrator getRole dispatched");
        ok("Student".equals(accounts[1].getRole()), "Student getRole dispatched");
    }

    static void testFileLoadValidation() throws Exception {
        Path testDir = tempDir("load");

        Files.write(testDir.resolve("rooms.txt"), Arrays.asList(
            "101|Single|2|true",
            "102|9|2|true",
            "103|Double|maybe|true",
            "104|Shared|abc|true",
            "105|Shared|2|true"
        ));

        Files.write(testDir.resolve("students.txt"), Arrays.asList(
            "S1|A|u1|p|CS|1|Single|Not Assigned",
            "S2|B|u2|p|CS|1|7|Not Assigned",
            "S3|C|u1|p|CS|1|Double|Not Assigned",
            "S1|D|u3|p|CS|1|Double|Not Assigned",
            "S4|E|u4|p|CS|1|Double|999"
        ));

        Files.write(testDir.resolve("transfer_requests.txt"), Arrays.asList(
            "S1|Single|ok|Pending",
            "S1|9|bad|Pending",
            "S1|Double|bad|Weird",
            "S9|Single|bad|Pending"
        ));

        ArrayList<Student> students = new ArrayList<>();
        RoomManager rm = new RoomManager();
        LoginManager lm = new LoginManager();
        TransferRequestManager tm = new TransferRequestManager();

        new FileManager(testDir.toFile()).loadData(students, rm, lm, tm);

        ok(rm.getRooms().size() == 2, "load skips bad room records and continues");
        ok(students.size() == 2, "load skips invalid/duplicate students but keeps orphaned student");
        ok(tm.getRequestCount() == 1, "load skips invalid/orphan transfer requests");
        ok(students.get(1).getUserId().equals("S4") &&
            students.get(1).getAssignedRoom().equals("Not Assigned"),
            "orphan assignment falls back to Not Assigned");
    }
}
