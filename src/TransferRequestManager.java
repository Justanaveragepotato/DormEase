
import java.util.ArrayList;

public class TransferRequestManager {

    private ArrayList<TransferRequest> requests;

    public TransferRequestManager() {
        requests = new ArrayList<>();
    }

    // Submit a new transfer request
    public void submitRequest(
        Student student,
        String preferredRoomType,
        String reason
    ) {

        if (student == null || student.getAssignedRoom().equals("Not Assigned")) {
            System.out.println(
                "You must have an assigned room to request a transfer."
            );
            return;
        }

        String normalizedRoomType = Room.normalizeRoomType(preferredRoomType);
        if (normalizedRoomType == null) {
            System.out.println("Invalid room type! Allowed: Single, Double, Shared.");
            return;
        }

        if (!ValidationUtils.isValidField(reason)) {
            System.out.println("Invalid reason!");
            return;
        }

        for (TransferRequest request : requests) {

            if (isSameStudent(request.getStudent(), student) &&
                request.getStatus().equals("Pending")) {

                System.out.println(
                    "You already have a pending transfer request."
                );
                return;
            }
        }

        TransferRequest request = new TransferRequest(
            student,
            normalizedRoomType,
            reason.trim()
        );

        requests.add(request);

        System.out.println("\nTransfer request submitted successfully!");
        System.out.println("Status: Pending");
    }

    // Display all transfer requests
    public void displayRequests() {

        System.out.println("\n===== TRANSFER REQUESTS =====");

        if (requests.isEmpty()) {
            System.out.println("No transfer requests found.");
            return;
        }

        for (TransferRequest request : requests) {
            request.displayRequest();
        }
    }

    // Approve or reject a request
    public void reviewRequest(
        int requestIndex,
        boolean approve,
        RoomManager roomManager
    ) {

        if (roomManager == null) {
            System.out.println("Room manager is unavailable!");
            return;
        }

        if (requestIndex < 0 || requestIndex >= requests.size()) {
            System.out.println("Invalid request number!");
            return;
        }

        TransferRequest request = requests.get(requestIndex);

        if (!request.getStatus().equals("Pending")) {
            System.out.println("This request has already been reviewed.");
            return;
        }

        if (!approve) {
            request.setStatus("Rejected");
            System.out.println("Transfer request rejected.");
            return;
        }

        Student student = request.getStudent();

        if (student == null || student.getAssignedRoom().equals("Not Assigned")) {
            System.out.println("Student no longer has an assigned room.");
            return;
        }

        boolean transferred = roomManager.transferStudent(
            student,
            request.getPreferredRoomType()
        );

        if (transferred) {
            request.setStatus("Approved");
            System.out.println("Transfer request approved!");
        } else {
            System.out.println("Transfer could not be completed.");
        }
    }

    // Get total number of requests
    public int getRequestCount() {
        return requests.size();
    }

    // Display requests belonging to a specific student
    public void displayStudentRequests(Student student) {

        System.out.println("\n===== MY TRANSFER REQUESTS =====");

        boolean found = false;

        for (TransferRequest request : requests) {

            if (isSameStudent(request.getStudent(), student)) {
                request.displayRequest();
                found = true;
            }
        }

        if (!found) {
            System.out.println("You have no transfer requests.");
        }
    }

    // Get all requests for saving
    public ArrayList<TransferRequest> getRequests() {
        return requests;
    }

    // Restore a previously saved request
    public void restoreRequest(
        Student student,
        String preferredRoomType,
        String reason,
        String status
    ) {

        String normalizedRoomType = Room.normalizeRoomType(preferredRoomType);
        String safeStatus = normalizeStatus(status);
        if (student == null || normalizedRoomType == null || safeStatus == null ||
            !ValidationUtils.isValidField(reason)) {
            return;
        }

        TransferRequest request = new TransferRequest(
            student,
            normalizedRoomType,
            reason.trim()
        );

        request.setStatus(safeStatus);
        requests.add(request);
    }

    private String normalizeStatus(String status) {
        if (status == null) {
            return null;
        }
        if (status.equalsIgnoreCase("Pending")) return "Pending";
        if (status.equalsIgnoreCase("Approved")) return "Approved";
        if (status.equalsIgnoreCase("Rejected")) return "Rejected";
        return null;
    }

    // Remove requests belonging to a deleted student
    public void removeStudentRequests(Student student) {
        requests.removeIf(
            request -> isSameStudent(request.getStudent(), student)
        );
    }

    private boolean isSameStudent(Student a, Student b) {
        if (a == b) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        return a.getUserId() != null && a.getUserId().equalsIgnoreCase(b.getUserId());
    }
}
