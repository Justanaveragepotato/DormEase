import java.util.Scanner;

public class StudentMenu {

    public void run(
        Scanner scanner,
        Student student,
        RoomManager roomManager,
        TransferRequestManager transferManager
    ) {

        int choice = 0;

        do {

            System.out.println("\n======================================");
            System.out.println("           STUDENT DASHBOARD");
            System.out.println("======================================");
            System.out.println("1. View My Profile");
            System.out.println("2. View My Room Details");
            System.out.println("3. View Room Availability");
            System.out.println("4. Submit Transfer Request");
            System.out.println("5. View Transfer Request Status");
            System.out.println("6. Logout");
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
                    // View profile
                    System.out.println("\n===== MY PROFILE =====");
                    System.out.println("Name: " + student.getName());
                    System.out.println("Student ID: " + student.getUserId());
                    System.out.println("Course: " + student.getCourse());
                    System.out.println("Year Level: " + student.getYearLevel());
                    System.out.println("Preferred Room: " + student.getPreferredRoomType());
                    break;

                case 2:
                    // View current room
                    System.out.println("\n===== MY ROOM DETAILS =====");

                    Room currentRoom = roomManager.findRoom(
                        student.getAssignedRoom()
                    );

                    if (currentRoom == null) {
                        System.out.println("You currently have no assigned room.");
                    } else {
                        currentRoom.displayRoomInfo();
                    }
                    break;

                case 3:
                    // View room availability
                    roomManager.displayAllRooms();
                    break;

                case 4:
                    // Submit transfer request
                    if (student.getAssignedRoom().equals("Not Assigned")) {
                        System.out.println("You need an assigned room before requesting a transfer.");
                        break;
                    }

                    System.out.println("\n===== TRANSFER REQUEST =====");
                    System.out.println("Select your preferred room type:");
                    System.out.println("1. Single");
                    System.out.println("2. Double");
                    System.out.println("3. Shared");
                    System.out.print("Choice: ");

                    String roomType;

                    Integer typeChoice = ValidationUtils.parseInt(scanner.nextLine());

                    if (typeChoice == null) {
                        System.out.println("Invalid input! Please enter 1, 2, or 3.");
                        continue;
                    }

                    switch (typeChoice) {
                        case 1:
                            roomType = "Single";
                            break;
                        case 2:
                            roomType = "Double";
                            break;
                        case 3:
                            roomType = "Shared";
                            break;
                        default:
                            System.out.println("Invalid room type! Allowed: Single, Double, Shared.");
                            continue;
                    }

                    System.out.print("Reason for transfer: ");
                    String reason = scanner.nextLine();

                    if (ValidationUtils.isBlank(reason)) {
                        System.out.println("Please provide a reason.");
                        break;
                    }

                    if (ValidationUtils.containsDelimiter(reason)) {
                        System.out.println("The reason cannot contain the '|' character.");
                        break;
                    }

                    transferManager.submitRequest(
                        student, roomType, reason
                    );
                    break;

                case 5:
                    // View personal transfer requests
                    transferManager.displayStudentRequests(student);
                    break;

                case 6:
                    System.out.println("\nLogging out...");
                    break;

                default:
                    System.out.println("Invalid choice! Please try again.");
            }

        } while (choice != 6);
    }
}