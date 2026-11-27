
import java.util.ArrayList;

public class RoomManager {

    private ArrayList<Room> rooms;

    public RoomManager() {
        rooms = new ArrayList<>();
    }

    // Register a room
    public boolean addRoom(Room room) {
        return addRoom(room, true);
    }

    // Restore a room from saved data without noisy console output
    public boolean restoreRoom(Room room) {
        return addRoom(room, false);
    }

    private boolean addRoom(Room room, boolean verbose) {

        if (room == null ||
            room.getRoomNumber() == null ||
            room.getRoomNumber().trim().isEmpty() ||
            !Room.isValidRoomType(room.getRoomType()) ||
            room.getCapacity() < 1) {

            if (verbose) {
                System.out.println("Invalid room information!");
            }
            return false;
        }

        if (findRoom(room.getRoomNumber()) != null) {
            if (verbose) {
                System.out.println("Room number already exists!");
            }
            return false;
        }

        rooms.add(room);

        if (verbose) {
            System.out.println("Room registered successfully!");
        }
        return true;
    }

    // Find a room using its room number
    public Room findRoom(String roomNumber) {

        for (Room room : rooms) {
            if (room.getRoomNumber().equalsIgnoreCase(roomNumber)) {
                return room;
            }
        }

        return null;
    }

    // Edit room information
    public boolean editRoom(
        String roomNumber,
        String newRoomType,
        int newCapacity,
        boolean available
    ) {

        Room room = findRoom(roomNumber);

        if (room == null) {
            System.out.println("Room does not exist!");
            return false;
        }

        if (!Room.isValidRoomType(newRoomType)) {
            System.out.println("Invalid room type! Allowed: Single, Double, Shared.");
            return false;
        }

        if (newCapacity < 1 ||
            newCapacity < room.getOccupants().size()) {

            System.out.println(
                "Capacity cannot be less than the current occupants!"
            );
            return false;
        }

        room.setRoomType(newRoomType);
        room.setCapacity(newCapacity);
        room.setAvailable(available);

        System.out.println("Room updated successfully!");
        return true;
    }

    // Delete an empty room
    public boolean deleteRoom(String roomNumber) {

        Room room = findRoom(roomNumber);

        if (room == null) {
            System.out.println("Room does not exist!");
            return false;
        }

        if (!room.getOccupants().isEmpty()) {
            System.out.println(
                "Cannot delete a room that still has occupants!"
            );
            return false;
        }

        rooms.remove(room);

        System.out.println("Room deleted successfully!");
        return true;
    }

    // Automatically assign a student
    public boolean assignRoom(Student student) {

        if (student == null) {
            System.out.println("Student does not exist!");
            return false;
        }

        if (!Room.isValidRoomType(student.getPreferredRoomType())) {
            System.out.println("Student has an invalid preferred room type!");
            return false;
        }

        if (!student.getAssignedRoom().equals("Not Assigned")) {
            System.out.println("Student already has a room!");
            return false;
        }

        for (Room room : rooms) {

            if (room.isAvailable() &&
                room.getRoomType().equalsIgnoreCase(
                    student.getPreferredRoomType())) {

                if (room.addStudent(student)) {
                    System.out.println("\nRoom assigned successfully!");
                    System.out.println("Student: " + student.getName());
                    System.out.println("Assigned Room: " + room.getRoomNumber());
                    return true;
                }
            }
        }

        System.out.println("\nNo suitable room available.");
        return false;
    }

    // Manually assign a student to a selected room
    public boolean manuallyAssignRoom(Student student, String roomNumber) {

        if (student == null) {
            System.out.println("Student does not exist!");
            return false;
        }

        if (!student.getAssignedRoom().equals("Not Assigned")) {
            System.out.println("Student already has a room!");
            return false;
        }

        Room selectedRoom = findRoom(roomNumber);

        if (selectedRoom == null) {
            System.out.println("Room does not exist!");
            return false;
        }

        if (!selectedRoom.isAvailable()) {
            System.out.println("Room is full or unavailable!");
            return false;
        }

        if (!selectedRoom.addStudent(student)) {
            System.out.println("Room assignment failed!");
            return false;
        }

        System.out.println("\nManual assignment successful!");
        System.out.println("Student: " + student.getName());
        System.out.println("Assigned Room: " + selectedRoom.getRoomNumber());

        return true;
    }

    // Remove student from their current room
    public boolean removeStudentFromRoom(Student student) {

        if (student == null) {
            return true;
        }

        if (student.getAssignedRoom().equals("Not Assigned")) {
            return true;
        }

        Room currentRoom = findRoom(student.getAssignedRoom());

        if (currentRoom == null) {
            System.out.println("Student's assigned room could not be found!");
            return false;
        }

        if (currentRoom.removeStudent(student)) {
            System.out.println("Student removed from Room "
                + currentRoom.getRoomNumber() + ".");
            return true;
        }

        System.out.println("Could not remove student from room.");
        return false;
    }

    // Transfer a student to another room
    public boolean transferStudent(
        Student student,
        String preferredRoomType
    ) {

        if (student == null || student.getAssignedRoom().equals("Not Assigned")) {
            System.out.println("Student does not have an assigned room!");
            return false;
        }

        if (!Room.isValidRoomType(preferredRoomType)) {
            System.out.println("Invalid room type! Allowed: Single, Double, Shared.");
            return false;
        }

        Room currentRoom = findRoom(student.getAssignedRoom());

        if (currentRoom == null) {
            System.out.println("Current room could not be found!");
            return false;
        }

        Room destinationRoom = null;
        Room sameTypeRoom = null;

        for (Room room : rooms) {

            if (room == currentRoom || !room.isAvailable()) {
                continue;
            }

            if (room.getRoomType().equalsIgnoreCase(preferredRoomType)) {
                destinationRoom = room;
                break;
            }

            if (sameTypeRoom == null &&
                room.getRoomType().equalsIgnoreCase(currentRoom.getRoomType())) {
                sameTypeRoom = room;
            }
        }

        if (destinationRoom == null) {
            destinationRoom = sameTypeRoom;
        }

        if (destinationRoom == null) {
            System.out.println("No suitable room available for transfer.");
            return false;
        }

        if (!currentRoom.removeStudent(student)) {
            System.out.println("Could not remove student from current room.");
            return false;
        }

        if (!destinationRoom.addStudent(student)) {

            currentRoom.addStudent(student);

            System.out.println("Transfer failed. Original room restored.");
            return false;
        }

        System.out.println("\nRoom transfer successful!");
        System.out.println("Student: " + student.getName());
        System.out.println("Previous Room: " + currentRoom.getRoomNumber());
        System.out.println("New Room: " + destinationRoom.getRoomNumber());

        return true;
    }

    // Display all registered rooms
    public void displayAllRooms() {

        System.out.println("\n===== DORMITORY ROOMS =====");

        if (rooms.isEmpty()) {
            System.out.println("No rooms registered.");
            return;
        }

        for (Room room : rooms) {
            room.displayRoomInfo();
        }
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }
}
