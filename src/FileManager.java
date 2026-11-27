
import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;

public class FileManager {

    private final File studentFile;
    private final File roomFile;
    private final File transferFile;

    public FileManager() {
        this(null);
    }

    public FileManager(File dataDir) {
        File dir = dataDir == null ? resolveDataDir() : dataDir;

        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }

        this.studentFile = new File(dir, "students.txt");
        this.roomFile = new File(dir, "rooms.txt");
        this.transferFile = new File(dir, "transfer_requests.txt");
    }

    private File resolveDataDir() {

        String configured = System.getProperty("dormease.data");
        if (!ValidationUtils.isBlank(configured)) {
            return new File(configured.trim());
        }

        File codeDir = null;

        try {
            Path codePath = Paths.get(
                FileManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()
            );
            File codeFile = codePath.toFile();
            codeDir = codeFile.isFile() ? codeFile.getParentFile() : codeFile;
        } catch (Exception ignored) {
        }

        File[] candidates = {
            codeDir,
            codeDir == null ? null : codeDir.getParentFile(),
            new File(System.getProperty("user.dir"))
        };

        for (File dir : candidates) {
            if (dir != null && dir.exists() && hasAnyDataFile(dir)) {
                return dir;
            }
        }

        if (codeDir != null && codeDir.exists()) {
            return codeDir;
        }

        return new File(System.getProperty("user.dir"));
    }

    private boolean hasAnyDataFile(File dir) {
        return new File(dir, "students.txt").exists() ||
            new File(dir, "rooms.txt").exists() ||
            new File(dir, "transfer_requests.txt").exists();
    }

    public boolean hasSavedData() {
        return studentFile.exists() && roomFile.exists();
    }

    // =====================================
    // SAVE DATA
    // =====================================

    public void saveData(
        ArrayList<Student> students,
        RoomManager roomManager,
        TransferRequestManager transferManager
    ) {

        Path studentTemp = toTempPath(studentFile);
        Path roomTemp = toTempPath(roomFile);
        Path transferTemp = toTempPath(transferFile);

        try {
            writeStudents(studentTemp, students);
            writeRooms(roomTemp, roomManager);
            writeTransfers(transferTemp, transferManager);

            replaceFile(studentTemp, studentFile);
            replaceFile(roomTemp, roomFile);
            replaceFile(transferTemp, transferFile);

            System.out.println("Data saved successfully!");

        } catch (IOException e) {
            cleanupTempFiles(studentTemp, roomTemp, transferTemp);
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    private Path toTempPath(File file) {
        return Paths.get(file.getAbsolutePath() + ".tmp");
    }

    private void writeStudents(Path path, ArrayList<Student> students)
            throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            for (Student student : students) {
                writer.write(
                    student.getUserId() + "|" +
                    student.getName() + "|" +
                    student.getUsername() + "|" +
                    student.getPassword() + "|" +
                    student.getCourse() + "|" +
                    student.getYearLevel() + "|" +
                    student.getPreferredRoomType() + "|" +
                    student.getAssignedRoom()
                );
                writer.newLine();
            }
        }
    }

    private void writeRooms(Path path, RoomManager roomManager)
            throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            for (Room room : roomManager.getRooms()) {
                writer.write(
                    room.getRoomNumber() + "|" +
                    room.getRoomType() + "|" +
                    room.getCapacity() + "|" +
                    room.isRoomEnabled()
                );
                writer.newLine();
            }
        }
    }

    private void writeTransfers(
        Path path,
        TransferRequestManager transferManager
    ) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            for (TransferRequest request : transferManager.getRequests()) {
                writer.write(
                    request.getStudent().getUserId() + "|" +
                    request.getPreferredRoomType() + "|" +
                    request.getReason() + "|" +
                    request.getStatus()
                );
                writer.newLine();
            }
        }
    }

    private void replaceFile(Path temp, File target) throws IOException {

        Path targetPath = target.toPath();

        try {
            Files.move(
                temp,
                targetPath,
                StandardCopyOption.REPLACE_EXISTING,
                StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, targetPath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void cleanupTempFiles(Path... tempFiles) {
        for (Path temp : tempFiles) {
            try {
                Files.deleteIfExists(temp);
            } catch (IOException ignored) {
            }
        }
    }

    // =====================================
    // LOAD DATA
    // =====================================

    public void loadData(
        ArrayList<Student> students,
        RoomManager roomManager,
        LoginManager loginManager,
        TransferRequestManager transferManager
    ) {

        loadRooms(roomManager);
        loadStudents(students, roomManager, loginManager);
        loadTransferRequests(students, transferManager);

        System.out.println("Data loading completed.");
    }

    private void loadRooms(RoomManager roomManager) {

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(roomFile))) {

            String line;

            while ((line = reader.readLine()) != null) {
                parseRoomLine(line, roomManager);
            }

        } catch (FileNotFoundException e) {
            System.out.println("No saved room data found.");
        } catch (IOException e) {
            System.out.println("Error loading rooms: " + e.getMessage());
        }
    }

    private void parseRoomLine(String line, RoomManager roomManager) {

        String[] data = line.split("\\|", -1);

        if (data.length != 4) {
            System.out.println("Skipping malformed room record: " + line);
            return;
        }

        String roomNumber = data[0].trim();

        if (!ValidationUtils.isValidField(roomNumber)) {
            System.out.println("Skipping room: invalid room number.");
            return;
        }

        if (!Room.isValidRoomType(data[1])) {
            System.out.println("Skipping room " + roomNumber + ": invalid room type.");
            return;
        }

        Integer capacity = ValidationUtils.parseInt(data[2]);

        if (capacity == null || capacity < 1) {
            System.out.println("Skipping room " + roomNumber + ": invalid capacity.");
            return;
        }

        Boolean available = ValidationUtils.parseBoolean(data[3]);

        if (available == null) {
            System.out.println("Skipping room " + roomNumber + ": invalid availability.");
            return;
        }

        Room room = new Room(roomNumber, data[1], capacity);
        room.setAvailable(available);

        if (!roomManager.restoreRoom(room)) {
            System.out.println("Skipping room " + roomNumber + ": duplicate or invalid room.");
        }
    }

    private void loadStudents(
        ArrayList<Student> students,
        RoomManager roomManager,
        LoginManager loginManager
    ) {

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(studentFile))) {

            String line;

            while ((line = reader.readLine()) != null) {
                parseStudentLine(line, students, roomManager, loginManager);
            }

        } catch (FileNotFoundException e) {
            System.out.println("No saved student data found.");
        } catch (IOException e) {
            System.out.println("Error loading students: " + e.getMessage());
        }
    }

    private void parseStudentLine(
        String line,
        ArrayList<Student> students,
        RoomManager roomManager,
        LoginManager loginManager
    ) {

        String[] data = line.split("\\|", -1);

        if (data.length != 8) {
            System.out.println("Skipping malformed student record: " + line);
            return;
        }

        for (String field : data) {
            if (!ValidationUtils.isValidField(field)) {
                System.out.println("Skipping student: invalid field value.");
                return;
            }
        }

        String id = data[0].trim();
        String username = data[2].trim();

        if (!Room.isValidRoomType(data[6])) {
            System.out.println("Skipping student " + id + ": invalid preferred room type.");
            return;
        }

        if (findStudentById(students, id) != null) {
            System.out.println("Skipping student " + id + ": duplicate student ID.");
            return;
        }

        if (loginManager.usernameExists(username)) {
            System.out.println("Skipping student " + id + ": duplicate username.");
            return;
        }

        Student student = new Student(
            id,
            data[1],
            username,
            data[3],
            data[4],
            data[5],
            data[6]
        );

        if (!loginManager.addUser(student)) {
            System.out.println("Skipping student " + id + ": login could not be created.");
            return;
        }

        students.add(student);

        String assignedRoom = data[7].trim();

        if (assignedRoom.equals("Not Assigned")) {
            return;
        }

        Room room = roomManager.findRoom(assignedRoom);

        if (room == null) {
            System.out.println(
                "Assigned room " + assignedRoom + " not found for " + student.getName()
            );
            return;
        }

        if (!room.addStudent(student)) {
            System.out.println(
                "Could not restore room assignment for " + student.getName()
            );
        }
    }

    private void loadTransferRequests(
        ArrayList<Student> students,
        TransferRequestManager transferManager
    ) {

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(transferFile))) {

            String line;

            while ((line = reader.readLine()) != null) {
                parseTransferLine(line, students, transferManager);
            }

        } catch (FileNotFoundException e) {
            System.out.println("No saved transfer requests found.");
        } catch (IOException e) {
            System.out.println(
                "Error loading transfer requests: " + e.getMessage()
            );
        }
    }

    private void parseTransferLine(
        String line,
        ArrayList<Student> students,
        TransferRequestManager transferManager
    ) {

        String[] data = line.split("\\|", -1);

        if (data.length != 4) {
            System.out.println("Skipping malformed transfer record: " + line);
            return;
        }

        if (!ValidationUtils.isValidField(data[0]) ||
            !ValidationUtils.isValidField(data[2])) {

            System.out.println("Skipping transfer request: invalid field value.");
            return;
        }

        Student student = findStudentById(students, data[0]);

        if (student == null) {
            System.out.println("Skipping transfer request: student not found.");
            return;
        }

        String preferredRoomType = Room.normalizeRoomType(data[1]);

        if (preferredRoomType == null) {
            System.out.println("Skipping transfer request for " + student.getUserId() + ": invalid room type.");
            return;
        }

        transferManager.restoreRequest(
            student,
            preferredRoomType,
            data[2],
            data[3]
        );
    }

    private Student findStudentById(ArrayList<Student> students, String id) {

        if (students == null || ValidationUtils.isBlank(id)) {
            return null;
        }

        for (Student student : students) {
            if (student.getUserId().equalsIgnoreCase(id.trim())) {
                return student;
            }
        }

        return null;
    }
}
