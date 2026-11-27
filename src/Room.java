
import java.util.ArrayList;

public class Room {

    private String roomNumber;
    private String roomType;
    private int capacity;
    private ArrayList<Student> occupants;
    private boolean available;

    public Room(String roomNumber, String roomType, int capacity) {

        this.roomNumber = ValidationUtils.trimToNull(roomNumber);
        this.roomType = normalizeRoomType(roomType);
        this.capacity = capacity;
        this.occupants = new ArrayList<>();
        this.available = true;
    }

    public static boolean isValidRoomType(String roomType) {
        return ValidationUtils.isValidRoomType(roomType);
    }

    public static String normalizeRoomType(String roomType) {
        return ValidationUtils.normalizeRoomType(roomType);
    }

    // Getters
    public String getRoomNumber() {
        return roomNumber;
    }

    public String getRoomType() {
        return roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public ArrayList<Student> getOccupants() {
        return occupants;
    }

    public int getAvailableBeds() {
        return capacity - occupants.size();
    }

    public boolean isAvailable() {
        return available && getAvailableBeds() > 0;
    }

    // Setters
    public boolean setRoomType(String roomType) {

        String normalized = normalizeRoomType(roomType);
        if (normalized == null) {
            return false;
        }

        this.roomType = normalized;
        return true;
    }

    public boolean setCapacity(int capacity) {

        if (capacity < 1 || capacity < occupants.size()) {
            return false;
        }

        this.capacity = capacity;
        return true;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isRoomEnabled() {
        return available;
    }

    // Add student to room
    public boolean addStudent(Student student) {

        if (!isAvailable() ||
            occupants.contains(student) ||
            !student.getAssignedRoom().equals("Not Assigned")) {

            return false;
        }

        occupants.add(student);
        student.setAssignedRoom(roomNumber);

        return true;
    }

    // Remove student from room
    public boolean removeStudent(Student student) {

        if (occupants.remove(student)) {
            student.setAssignedRoom("Not Assigned");
            return true;
        }

        return false;
    }

    // Display room information
    public void displayRoomInfo() {

        System.out.println("\nRoom: " + roomNumber);
        System.out.println("Type: " + roomType);
        System.out.println("Capacity: " + capacity);
        System.out.println("Current Occupants: " + occupants.size());
        System.out.println("Available Beds: " + getAvailableBeds());

        System.out.println("Status: " +
            (isAvailable() ? "Available" : "Full / Unavailable"));
    }
}
