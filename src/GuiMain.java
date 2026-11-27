import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * GUI entry point.
 * The original console Main.java is left unchanged.
 */
public class GuiMain {

    public static void main(String[] args) {

        ArrayList<Student> students = new ArrayList<>();

        LoginManager loginManager = new LoginManager();
        RoomManager roomManager = new RoomManager();
        WaitingListManager waitingList = new WaitingListManager();
        TransferRequestManager transferManager = new TransferRequestManager();
        FileManager fileManager = new FileManager();

        Administrator admin = new Administrator(
            "A001",
            "Dormitory Admin",
            "admin",
            "admin123",
            "Dormitory Manager"
        );

        if (!loginManager.addUser(admin)) {
            System.out.println("Default administrator account could not be created.");
            return;
        }

        if (fileManager.hasSavedData()) {
            fileManager.loadData(
                students,
                roomManager,
                loginManager,
                transferManager
            );

            for (Student student : students) {
                if ("Not Assigned".equals(student.getAssignedRoom())) {
                    waitingList.addToWaitingList(student);
                }
            }

        } else {
            Student[] newStudents = {
                new Student(
                    "S001", "Juan Dela Cruz", "juan123",
                    "student123", "BSIT", "2nd Year", "Double"
                ),
                new Student(
                    "S002", "Pedro Santos", "pedro123",
                    "student123", "BSCS", "1st Year", "Double"
                ),
                new Student(
                    "S003", "Ana Reyes", "ana123",
                    "student123", "BSIT", "2nd Year", "Double"
                )
            };

            for (Student student : newStudents) {
                if (loginManager.addUser(student)) {
                    students.add(student);
                }
            }

            roomManager.addRoom(new Room("101", "Double", 2));
            roomManager.addRoom(new Room("102", "Single", 1));
            roomManager.addRoom(new Room("103", "Shared", 4));

            for (Student student : students) {
                if (!roomManager.assignRoom(student)) {
                    waitingList.addToWaitingList(student);
                }
            }

            fileManager.saveData(students, roomManager, transferManager);
        }

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new DormEaseGUI(
                students,
                loginManager,
                roomManager,
                waitingList,
                transferManager,
                fileManager
            ).setVisible(true);
        });
    }
}
