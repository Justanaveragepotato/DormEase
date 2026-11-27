
public class TransferRequest {

    private Student student;
    private String preferredRoomType;
    private String reason;
    private String status;

    public TransferRequest(
        Student student,
        String preferredRoomType,
        String reason
    ) {
        this.student = student;
        this.preferredRoomType = preferredRoomType;
        this.reason = reason;
        this.status = "Pending";
    }

    // Getters
    public Student getStudent() {
        return student;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public String getReason() {
        return reason;
    }

    public String getStatus() {
        return status;
    }

    // Update request status
    public void setStatus(String status) {
        this.status = status;
    }

    // Display request details
    public void displayRequest() {

        System.out.println("\n------------------------------");
        System.out.println("Student: " + student.getName());
        System.out.println("Student ID: " + student.getUserId());
        System.out.println("Current Room: " + student.getAssignedRoom());
        System.out.println("Requested Room Type: " + preferredRoomType);
        System.out.println("Reason: " + reason);
        System.out.println("Status: " + status);
        System.out.println("------------------------------");
    }
}