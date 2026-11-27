
import java.util.Queue;
import java.util.LinkedList;

public class WaitingListManager {

    private Queue<Student> waitingStudents;

    public WaitingListManager() {
        waitingStudents = new LinkedList<>();
    }

    // Add student to waiting list
    public void addToWaitingList(Student student) {

        if (student == null || !student.getAssignedRoom().equals("Not Assigned")) {
            return;
        }

        if (waitingStudents.contains(student)) {
            System.out.println("Student is already on the waiting list.");
            return;
        }

        waitingStudents.offer(student);

        System.out.println(
            student.getName() + " has been added to the waiting list."
        );
    }

    // Remove student from waiting list
    public boolean removeFromWaitingList(Student student) {
        return student != null && waitingStudents.remove(student);
    }

    public Queue<Student> getWaitingStudents() {
        return new LinkedList<>(waitingStudents);
    }

    // Display waiting list
    public void displayWaitingList() {

        System.out.println("\n===== WAITING LIST =====");

        if (waitingStudents.isEmpty()) {
            System.out.println("No students currently waiting.");
            return;
        }

        int position = 1;

        for (Student student : waitingStudents) {

            System.out.println(
                position + ". " + student.getName()
                + " - Preferred: " + student.getPreferredRoomType()
            );

            position++;
        }
    }

    // Process waiting list
    public void processWaitingList(RoomManager roomManager) {

        if (roomManager == null) {
            return;
        }

        System.out.println("\n===== PROCESSING WAITING LIST =====");

        int size = waitingStudents.size();

        if (size == 0) {
            System.out.println("Waiting list is empty.");
            return;
        }

        for (int i = 0; i < size; i++) {

            Student student = waitingStudents.poll();

            if (student == null) {
                continue;
            }

            if (!student.getAssignedRoom().equals("Not Assigned")) {
                continue;
            }

            boolean assigned = roomManager.assignRoom(student);

            if (!assigned) {
                waitingStudents.offer(student);
            }
        }
    }
}
