# DormEase — Room Assignment & Transfer System

## 1. Project Title

**DormEase — Room Assignment & Transfer System**

## 2. Project Description

DormEase is a console-based Java application designed to help dormitory administrators manage student housing. It provides an organized system for student registration, room registration, room assignment, waiting list management, transfer requests, and persistent data storage.

The system separates users into two roles:

- **Administrator** — manages student records, rooms, assignments, waiting lists, and transfer requests.
- **Student** — views personal room information and submits room transfer requests.

DormEase aims to make dormitory management more organized, efficient, and reliable through automated processes and input validation.

## 3. Problem Being Addressed

Manual dormitory room assignment can be slow and error-prone. Administrators need a reliable way to:

- Match students to rooms based on their preferred room type.
- Prevent over-capacity and duplicate room assignments.
- Track students who cannot be assigned immediately.
- Review and process transfer requests consistently.
- Preserve student and room records after the program is closed.

DormEase addresses these problems by providing a centralized management system that validates user input, automates room assignment, manages waiting lists, and saves records automatically.

## 4. Project Objectives

- Register and manage student records.
- Register and manage dormitory rooms.
- Assign rooms automatically based on room type and availability.
- Support manual room assignment when needed.
- Maintain a waiting list for students without available rooms.
- Allow students to submit room transfer requests.
- Allow administrators to approve or reject transfer requests.
- Save and restore records using text files.
- Apply core Object-Oriented Programming (OOP) concepts.
- Implement input validation and exception handling.
- Test system functionality and prevent common data-related errors.

## 5. Target Users

- **Dormitory Administrators** — primary users responsible for managing student housing, room assignments, and transfer requests.
- **Students** — users who can view their room information and submit transfer requests.
- **Students on the Waiting List** — students waiting for a compatible room to become available.

## 6. Main Features

- Unified login system for administrators and students.
- Separate administrator and student dashboards.
- Student registration, viewing, editing, and deletion.
- Room registration, editing, deletion, and availability management.
- Automatic room assignment based on preferred room type.
- Manual room assignment by administrators.
- Room capacity and occupancy monitoring.
- Waiting list management using a FIFO queue.
- Waiting list processing when rooms become available.
- Student room transfer request submission.
- Administrator approval and rejection of transfer requests.
- Validation of room types, capacities, student IDs, usernames, and required fields.
- Prevention of duplicate student IDs and usernames.
- Prevention of duplicate pending transfer requests.
- Safe handling of malformed or corrupted saved records.
- Text-file persistence for student, room, and transfer request records.
- Atomic temporary-file replacement during data saving.
- Automated validation and system-flow tests.

## 7. Technologies Used

- **Programming Language:** Java
- **JDK:** Java 17 or newer recommended
- **Interface:** Console / Command-Line Interface
- **Data Storage:** Plain text files
  - `students.txt`
  - `rooms.txt`
  - `transfer_requests.txt`
- **Testing:** Custom Java test runners
  - `ValidationTests.java`
  - `SystemTests.java`
- **Version Control:** Git and GitHub

## 8. Team Members and Roles

| Member | Role |
|---|---|
| Justanaveragepotato | Project lead and main developer |
| yhanji | Initial contributor / project setup |

## 9. OOP Concepts Demonstrated

| Concept | Implementation in DormEase |
|---|---|
| Encapsulation | Private fields with getters and setters in `User`, `Student`, `Administrator`, `Room`, and `TransferRequest`. |
| Inheritance | `Student` and `Administrator` extend the `User` class. |
| Abstraction | `User` is an abstract class that defines shared user properties and the abstract `getRole()` method. |
| Polymorphism | `User[] accounts` stores different `User` subclasses and calls `getRole()` polymorphically. The `printWelcomeMessage(User user)` method also accepts different `User` subtypes. |
| Arrays | `ValidationUtils.VALID_ROOM_TYPES` and `Main.User[] accounts` demonstrate array usage. |
| Exception Handling | Safe parsing helpers in `ValidationUtils`, `try/catch` blocks, and try-with-resources in `FileManager`. |
| File Handling | `FileManager` saves and loads records using text files and temporary files before replacing saved data. |
| Modularity | Separate manager classes organize login, room management, waiting lists, transfer requests, and file operations. |

## 10. Instructions for Running the Program

### A. Requirements

- Java Development Kit (JDK) 17 or newer recommended.
- A Java-compatible terminal or development environment.

### B. Compile and Run the Application

Open a terminal inside the project's `src` folder.

Compile the Java source files:

```bash
javac *.java
```

Run the application:

```bash
java Main
```

### C. Default Administrator Account

The system includes a default administrator account:

```text
Username: admin
Password: admin123
```

### D. Run the Tests

From the same `src` folder, execute:

Run validation tests:

```bash
java ValidationTests
```

Run system tests:

```bash
java SystemTests
```

**Expected Test Results:**

```text
ValidationTests: 68 passed, 0 failed
SystemTests:     37 passed, 0 failed
```

### E. Use a Custom Data Folder

By default, the application uses the configured data directory. A custom directory can be specified using the `dormease.data` system property.

Example:

```bash
java -Ddormease.data="C:\path\to\data" Main
```

The folder will be created automatically if it does not exist.

## 11. Sample Screenshots

Screenshots of the running application can be placed inside the `screenshots/` folder and referenced below.

### Administrator Dashboard

![Administrator Dashboard](screenshots/admin-dashboard.png)

### Student Dashboard

![Student Dashboard](screenshots/student-dashboard.png)

### Room Assignment and Transfer Review

![Room Assignment and Transfer Review](screenshots/room-transfer-review.png)

## 12. Known Limitations

- The system is console-based and does not include a graphical user interface.
- Passwords are stored in plain text in `students.txt`.
- The default administrator account is currently hardcoded in `Main.java`.
- Data is stored locally in text files instead of a database.
- Only one administrator account is created at startup.
- Transfer fallback behavior may assign a student to a compatible available room even if the exact requested type is unavailable.
- Saving is atomic per file, but the three data files are not updated as one single transaction.
- Screenshot files are not yet included in the repository.

## 13. Future Improvements

- Develop a graphical user interface using Java Swing or JavaFX.
- Replace text-file storage with a database.
- Implement password hashing instead of plain-text password storage.
- Support multiple administrator accounts with configurable permissions.
- Generate reports for room occupancy and transfer history.
- Improve transfer rules to support strict requested-room-type matching.
- Use a standard testing framework such as JUnit.
- Add search and filtering features for students and rooms.
- Add exportable room occupancy and management reports.

## 14. Project Contributors

The following members contributed to the development, testing, documentation, and completion of DormEase.

| Contributor | Contribution |
|---|---|
| Jhan Joren Bautista | Main application features, programming, validation, persistence, menus, testing, and system integration |
| Brent Ian Percera | Initial project setup and base classes |
| Jay Perez | Documentation, testing, and project support |
| Dred Nhil Casao | Documentation, testing, and project support |

---

**DormEase — Making Dormitory Room Management More Organized and Efficient.**