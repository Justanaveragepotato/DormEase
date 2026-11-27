import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.PrintStream;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

/**
 * Swing interface for DormEase.
 * It reuses the existing managers and does not change the console program.
 */
public class DormEaseGUI extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final String[] ROOM_TYPES = {"Single", "Double", "Shared"};

    private final ArrayList<Student> students;
    private final LoginManager loginManager;
    private final RoomManager roomManager;
    private final WaitingListManager waitingList;
    private final TransferRequestManager transferManager;
    private final FileManager fileManager;

    private final PrintStream originalOut;
    private final JTextArea logArea;

    private final JPanel cards = new JPanel(new CardLayout());
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel loginMessage;

    private DefaultTableModel roomModel;
    private DefaultTableModel studentModel;
    private DefaultTableModel waitingModel;
    private DefaultTableModel requestModel;
    private DefaultTableModel myRequestModel;

    private JTable roomTable;
    private JTable studentTable;
    private JTable waitingTable;
    private JTable requestTable;
    private JTable myRequestTable;
    private JCheckBox showAllRequestsCheck;

    private final ArrayList<Integer> visibleRequestIndices = new ArrayList<>();

    private JLabel profileText;
    private JLabel roomText;
    private JComboBox<String> transferTypeCombo;
    private JTextField transferReasonField;

    private User currentUser;

    public DormEaseGUI(
        ArrayList<Student> students,
        LoginManager loginManager,
        RoomManager roomManager,
        WaitingListManager waitingList,
        TransferRequestManager transferManager,
        FileManager fileManager
    ) {

        super("DormEase - Room Assignment & Transfer System");

        this.students = students;
        this.loginManager = loginManager;
        this.roomManager = roomManager;
        this.waitingList = waitingList;
        this.transferManager = transferManager;
        this.fileManager = fileManager;
        this.originalOut = System.out;

        this.logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);

        System.setOut(new PrintStream(new GuiOutput(logArea), true));

        setSize(1100, 760);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new SaveOnClose());

        buildUi();
        printStartupSummary();
    }

    // =====================================
    // UI CONSTRUCTION
    // =====================================

    private void buildUi() {

        JPanel root = new JPanel(new BorderLayout());

        cards.add(buildLoginPanel(), "login");
        cards.add(buildAdminPanel(), "admin");
        cards.add(buildStudentPanel(), "student");
        root.add(cards, BorderLayout.CENTER);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Activity Log"));
        logPanel.setPreferredSize(new Dimension(0, 160));
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        root.add(logPanel, BorderLayout.SOUTH);

        setContentPane(root);
        showCard("login");
    }

    private JPanel buildLoginPanel() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseConstraints();

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        loginMessage = new JLabel(" ");

        addFormRow(panel, gbc, 0, "Username:", usernameField);
        addFormRow(panel, gbc, 1, "Password:", passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        passwordField.addActionListener(e -> login());

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(loginButton, gbc);

        gbc.gridy = 3;
        panel.add(loginMessage, gbc);

        JLabel hint = new JLabel(
            "<html><br>Administrator: <b>admin</b> / <b>admin123</b></html>"
        );
        gbc.gridy = 4;
        panel.add(hint, gbc);

        return panel;
    }

    private JPanel buildAdminPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Rooms", buildRoomsTab());
        tabs.addTab("Students", buildStudentsTab());
        tabs.addTab("Waiting List", buildWaitingTab());
        tabs.addTab("Transfer Requests", buildRequestsTab());
        panel.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Save Now");
        saveButton.addActionListener(e -> {
            saveData();
            showInfo("All records were saved.");
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        bottom.add(saveButton);
        bottom.add(logoutButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildRoomsTab() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Add New Room"));
        GridBagConstraints gbc = baseConstraints();

        JTextField numberField = new JTextField(12);
        JComboBox<String> typeCombo = new JComboBox<>(ROOM_TYPES);
        JSpinner capacitySpinner =
            new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

        addFormRow(form, gbc, 0, "Room Number:", numberField);
        addFormRow(form, gbc, 1, "Room Type:", typeCombo);
        addFormRow(form, gbc, 2, "Capacity:", capacitySpinner);

        JButton addButton = new JButton("Add Room");
        addButton.addActionListener(e -> {
            String number = numberField.getText().trim();

            if (!ValidationUtils.isValidField(number)) {
                showError("Enter a valid room number.");
                return;
            }

            String type = (String) typeCombo.getSelectedItem();
            int capacity = (Integer) capacitySpinner.getValue();

            if (roomManager.addRoom(new Room(number, type, capacity))) {
                numberField.setText("");
                saveData();
            }

            refreshRooms();
            refreshStudents();
            refreshWaitingList();
        });

        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        form.add(addButton, gbc);

        panel.add(form, BorderLayout.NORTH);

        roomModel = new DefaultTableModel(
            new String[]{
                "Room No", "Type", "Capacity", "Occupants", "Available Beds", "Status"
            },
            0
        );
        roomTable = new JTable(roomModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton editButton = new JButton("Edit Selected Room");
        editButton.addActionListener(e -> editSelectedRoom());

        JButton deleteButton = new JButton("Delete Selected Room");
        deleteButton.addActionListener(e -> deleteSelectedRoom());

        buttons.add(editButton);
        buttons.add(deleteButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildStudentsTab() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Register New Student"));
        GridBagConstraints gbc = baseConstraints();

        JTextField idField = new JTextField(12);
        JTextField nameField = new JTextField(16);
        JTextField usernameFieldNew = new JTextField(14);
        JPasswordField passwordFieldNew = new JPasswordField(14);
        JTextField courseField = new JTextField(12);
        JTextField yearField = new JTextField(12);
        JComboBox<String> typeCombo = new JComboBox<>(ROOM_TYPES);

        addFormRow(form, gbc, 0, "Student ID:", idField);
        addFormRow(form, gbc, 1, "Full Name:", nameField);
        addFormRow(form, gbc, 2, "Username:", usernameFieldNew);
        addFormRow(form, gbc, 3, "Password:", passwordFieldNew);
        addFormRow(form, gbc, 4, "Course:", courseField);
        addFormRow(form, gbc, 5, "Year Level:", yearField);
        addFormRow(form, gbc, 6, "Preferred Room Type:", typeCombo);

        JButton registerButton = new JButton("Register Student");
        registerButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String username = usernameFieldNew.getText().trim();
            String password = new String(passwordFieldNew.getPassword());
            String course = courseField.getText().trim();
            String year = yearField.getText().trim();
            String type = (String) typeCombo.getSelectedItem();

            if (!ValidationUtils.isValidField(id) ||
                !ValidationUtils.isValidField(name) ||
                !ValidationUtils.isValidField(username) ||
                !ValidationUtils.isValidField(password) ||
                !ValidationUtils.isValidField(course) ||
                !ValidationUtils.isValidField(year) ||
                type == null) {

                showError("Please complete all fields with valid values.");
                return;
            }

            if (findStudentById(id) != null) {
                showError("That student ID already exists.");
                return;
            }

            if (loginManager.usernameExists(username)) {
                showError("That username already exists.");
                return;
            }

            Student student = new Student(
                id, name, username, password, course, year, type
            );

            if (!loginManager.addUser(student)) {
                showError("The student login could not be created.");
                return;
            }

            students.add(student);

            if (!roomManager.assignRoom(student)) {
                waitingList.addToWaitingList(student);
            }

            idField.setText("");
            nameField.setText("");
            usernameFieldNew.setText("");
            passwordFieldNew.setText("");
            courseField.setText("");
            yearField.setText("");

            saveData();
            refreshStudents();
            refreshRooms();
            refreshWaitingList();
        });

        gbc.gridx = 1;
        gbc.gridy = 7;
        gbc.fill = GridBagConstraints.NONE;
        form.add(registerButton, gbc);

        panel.add(form, BorderLayout.NORTH);

        studentModel = new DefaultTableModel(
            new String[]{
                "Student ID", "Name", "Username", "Course", "Year Level",
                "Preferred", "Assigned Room"
            },
            0
        );
        studentTable = new JTable(studentModel);
        studentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(studentTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton autoButton = new JButton("Auto Assign");
        autoButton.addActionListener(e -> autoAssignSelectedStudent());

        JButton manualButton = new JButton("Manual Assign");
        manualButton.addActionListener(e -> manuallyAssignSelectedStudent());

        JButton editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editSelectedStudent());

        JButton deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedStudent());

        buttons.add(autoButton);
        buttons.add(manualButton);
        buttons.add(editButton);
        buttons.add(deleteButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildWaitingTab() {

        JPanel panel = new JPanel(new BorderLayout());

        waitingModel = new DefaultTableModel(
            new String[]{"#", "Student ID", "Name", "Preferred Room Type"},
            0
        );
        waitingTable = new JTable(waitingModel);
        waitingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(waitingTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton processButton = new JButton("Process Waiting List");
        processButton.addActionListener(e -> {
            waitingList.processWaitingList(roomManager);
            saveData();
            refreshRooms();
            refreshStudents();
            refreshWaitingList();
        });

        JButton removeButton = new JButton("Remove Selected");
        removeButton.addActionListener(e -> {
            Student student = selectedWaitingStudent();

            if (student == null) {
                showError("Select a student from the waiting list.");
                return;
            }

            waitingList.removeFromWaitingList(student);
            saveData();
            refreshWaitingList();
        });

        buttons.add(processButton);
        buttons.add(removeButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildRequestsTab() {

        JPanel panel = new JPanel(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        showAllRequestsCheck = new JCheckBox("Show all requests");
        showAllRequestsCheck.addActionListener(e -> refreshRequests());
        top.add(showAllRequestsCheck);
        panel.add(top, BorderLayout.NORTH);

        requestModel = new DefaultTableModel(
            new String[]{
                "#", "Student ID", "Student", "Current Room",
                "Requested Type", "Reason", "Status"
            },
            0
        );
        requestTable = new JTable(requestModel);
        requestTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        panel.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton approveButton = new JButton("Approve Selected");
        approveButton.addActionListener(e -> reviewSelectedRequest(true));

        JButton rejectButton = new JButton("Reject Selected");
        rejectButton.addActionListener(e -> reviewSelectedRequest(false));

        buttons.add(approveButton);
        buttons.add(rejectButton);
        panel.add(buttons, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildStudentPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Profile", buildProfileTab());
        tabs.addTab("My Room", buildMyRoomTab());
        tabs.addTab("Submit Transfer", buildTransferTab());
        tabs.addTab("My Requests", buildMyRequestsTab());
        panel.add(tabs, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton saveButton = new JButton("Save Now");
        saveButton.addActionListener(e -> {
            saveData();
            showInfo("All records were saved.");
        });

        JButton logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());

        bottom.add(saveButton);
        bottom.add(logoutButton);
        panel.add(bottom, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildProfileTab() {

        JPanel panel = new JPanel(new BorderLayout());
        profileText = new JLabel(" ");
        profileText.setVerticalAlignment(SwingConstants.TOP);
        panel.add(new JScrollPane(profileText), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildMyRoomTab() {

        JPanel panel = new JPanel(new BorderLayout());
        roomText = new JLabel(" ");
        roomText.setVerticalAlignment(SwingConstants.TOP);
        panel.add(new JScrollPane(roomText), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildTransferTab() {

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = baseConstraints();

        transferTypeCombo = new JComboBox<>(ROOM_TYPES);
        transferReasonField = new JTextField(30);

        addFormRow(panel, gbc, 0, "Preferred Room Type:", transferTypeCombo);
        addFormRow(panel, gbc, 1, "Reason:", transferReasonField);

        JButton submitButton = new JButton("Submit Transfer Request");
        submitButton.addActionListener(e -> submitTransferRequest());

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(submitButton, gbc);

        return panel;
    }

    private JPanel buildMyRequestsTab() {

        JPanel panel = new JPanel(new BorderLayout());

        myRequestModel = new DefaultTableModel(
            new String[]{"#", "Requested Type", "Reason", "Status", "Current Room"},
            0
        );
        myRequestTable = new JTable(myRequestModel);
        panel.add(new JScrollPane(myRequestTable), BorderLayout.CENTER);

        return panel;
    }

    // =====================================
    // ACTIONS
    // =====================================

    private void login() {

        String username = ValidationUtils.trimToNull(usernameField.getText());
        String password = new String(passwordField.getPassword());

        if (username == null || ValidationUtils.isBlank(password)) {
            loginMessage.setText("Username and password are required.");
            return;
        }

        User user = loginManager.authenticate(username, password);

        if (user == null) {
            loginMessage.setText("Invalid username or password.");
            System.out.println("Invalid login attempt for: " + username);
            return;
        }

        currentUser = user;
        loginMessage.setText(" ");
        passwordField.setText("");

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.getRole() + " " + user.getName());

        if (user instanceof Administrator) {
            refreshAllAdmin();
            showCard("admin");
        } else if (user instanceof Student) {
            setCurrentStudent((Student) user);
            showCard("student");
        }
    }

    private void logout() {

        saveData();
        currentUser = null;
        passwordField.setText("");
        loginMessage.setText(" ");
        System.out.println("\nUser logged out. Data saved.");
        showCard("login");
    }

    private void submitTransferRequest() {

        Student student = currentStudent();

        if (student == null) {
            showError("No student is logged in.");
            return;
        }

        String type = (String) transferTypeCombo.getSelectedItem();
        String reason = transferReasonField.getText();

        transferManager.submitRequest(student, type, reason);
        transferReasonField.setText("");

        saveData();
        refreshMyRequests();
        refreshStudentView(student);
        refreshRequests();
    }

    private void editSelectedRoom() {

        Room room = selectedRoom();

        if (room == null) {
            showError("Select a room to edit.");
            return;
        }

        String type = (String) JOptionPane.showInputDialog(
            this,
            "Room type:",
            "Edit Room",
            JOptionPane.QUESTION_MESSAGE,
            null,
            ROOM_TYPES,
            room.getRoomType()
        );

        if (type == null) {
            return;
        }

        Integer capacity = ValidationUtils.parseInt(
            JOptionPane.showInputDialog(
                this,
                "Capacity:",
                room.getCapacity()
            )
        );

        if (capacity == null) {
            showError("Enter a valid capacity.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
            this,
            "Should this room be available?",
            "Edit Room",
            JOptionPane.YES_NO_OPTION
        );

        boolean available = answer == JOptionPane.YES_OPTION;

        if (roomManager.editRoom(room.getRoomNumber(), type, capacity, available)) {
            saveData();
        }

        refreshRooms();
        refreshStudents();
        refreshWaitingList();
    }

    private void deleteSelectedRoom() {

        Room room = selectedRoom();

        if (room == null) {
            showError("Select a room to delete.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
            this,
            "Delete room " + room.getRoomNumber() + "?",
            "Delete Room",
            JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (roomManager.deleteRoom(room.getRoomNumber())) {
            saveData();
        }

        refreshRooms();
        refreshStudents();
    }

    private void autoAssignSelectedStudent() {

        Student student = selectedStudent();

        if (student == null) {
            showError("Select a student to assign.");
            return;
        }

        if (roomManager.assignRoom(student)) {
            waitingList.removeFromWaitingList(student);
            saveData();
        }

        refreshStudents();
        refreshRooms();
        refreshWaitingList();
        refreshRequests();
        refreshStudentView(student);
    }

    private void manuallyAssignSelectedStudent() {

        Student student = selectedStudent();

        if (student == null) {
            showError("Select a student to assign.");
            return;
        }

        ArrayList<String> availableRooms = new ArrayList<>();

        for (Room room : roomManager.getRooms()) {
            if (room.isAvailable()) {
                availableRooms.add(room.getRoomNumber());
            }
        }

        if (availableRooms.isEmpty()) {
            showError("There are no available rooms.");
            return;
        }

        String roomNumber = (String) JOptionPane.showInputDialog(
            this,
            "Choose an available room:",
            "Manual Assignment",
            JOptionPane.QUESTION_MESSAGE,
            null,
            availableRooms.toArray(new String[0]),
            null
        );

        if (roomNumber == null) {
            return;
        }

        if (roomManager.manuallyAssignRoom(student, roomNumber)) {
            waitingList.removeFromWaitingList(student);
            saveData();
        }

        refreshStudents();
        refreshRooms();
        refreshWaitingList();
        refreshStudentView(student);
    }

    private void editSelectedStudent() {

        Student student = selectedStudent();

        if (student == null) {
            showError("Select a student to edit.");
            return;
        }

        String name = prompt("New full name:", student.getName());
        if (name == null) return;

        String username = prompt("New username:", student.getUsername());
        if (username == null) return;

        String course = prompt("New course:", student.getCourse());
        if (course == null) return;

        String year = prompt("New year level:", student.getYearLevel());
        if (year == null) return;

        String type = (String) JOptionPane.showInputDialog(
            this,
            "New preferred room type:",
            "Edit Student",
            JOptionPane.QUESTION_MESSAGE,
            null,
            ROOM_TYPES,
            student.getPreferredRoomType()
        );

        if (type == null) {
            return;
        }

        if (!ValidationUtils.isValidField(name) ||
            !ValidationUtils.isValidField(username) ||
            !ValidationUtils.isValidField(course) ||
            !ValidationUtils.isValidField(year)) {

            showError("Please enter valid information.");
            return;
        }

        if (!username.equalsIgnoreCase(student.getUsername()) &&
            loginManager.usernameExists(username)) {

            showError("That username already exists.");
            return;
        }

        student.setName(name);
        student.setUsername(username);
        student.setCourse(course);
        student.setYearLevel(year);
        student.setPreferredRoomType(type);

        System.out.println("Student record updated: " + student.getUserId());
        saveData();
        refreshStudents();
        refreshStudentView(student);
    }

    private void deleteSelectedStudent() {

        Student student = selectedStudent();

        if (student == null) {
            showError("Select a student to delete.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
            this,
            "Delete " + student.getName() + " (" + student.getUserId() + ")?",
            "Delete Student",
            JOptionPane.YES_NO_OPTION
        );

        if (answer != JOptionPane.YES_OPTION) {
            return;
        }

        if (!roomManager.removeStudentFromRoom(student)) {
            showError("Deletion cancelled because the room could not be updated.");
            return;
        }

        waitingList.removeFromWaitingList(student);
        transferManager.removeStudentRequests(student);
        loginManager.removeUser(student);
        students.remove(student);

        System.out.println("\nStudent deleted successfully!");
        waitingList.processWaitingList(roomManager);
        saveData();

        refreshStudents();
        refreshRooms();
        refreshWaitingList();
        refreshRequests();

        if (currentUser == student) {
            logout();
        }
    }

    private void reviewSelectedRequest(boolean approve) {

        int row = requestTable.getSelectedRow();

        if (row < 0 || row >= visibleRequestIndices.size()) {
            showError("Select a transfer request.");
            return;
        }

        int requestIndex = visibleRequestIndices.get(row);
        transferManager.reviewRequest(requestIndex, approve, roomManager);
        saveData();

        refreshRequests();
        refreshStudents();
        refreshRooms();
        refreshWaitingList();
        refreshMyRequests();

        Student student = currentStudent();
        if (student != null) {
            refreshStudentView(student);
        }
    }

    // =====================================
    // TABLE REFRESH
    // =====================================

    private void refreshAllAdmin() {
        refreshRooms();
        refreshStudents();
        refreshWaitingList();
        refreshRequests();
    }

    private void refreshRooms() {

        roomModel.setRowCount(0);

        for (Room room : roomManager.getRooms()) {
            roomModel.addRow(new Object[]{
                room.getRoomNumber(),
                room.getRoomType(),
                room.getCapacity(),
                room.getOccupants().size(),
                room.getAvailableBeds(),
                room.isAvailable() ? "Available" : "Full / Unavailable"
            });
        }
    }

    private void refreshStudents() {

        studentModel.setRowCount(0);

        for (Student student : students) {
            studentModel.addRow(new Object[]{
                student.getUserId(),
                student.getName(),
                student.getUsername(),
                student.getCourse(),
                student.getYearLevel(),
                student.getPreferredRoomType(),
                student.getAssignedRoom()
            });
        }
    }

    private void refreshWaitingList() {

        waitingModel.setRowCount(0);

        int position = 1;

        for (Student student : waitingList.getWaitingStudents()) {
            waitingModel.addRow(new Object[]{
                position++,
                student.getUserId(),
                student.getName(),
                student.getPreferredRoomType()
            });
        }
    }

    private void refreshRequests() {

        requestModel.setRowCount(0);
        visibleRequestIndices.clear();

        boolean showAll = showAllRequestsCheck != null &&
            showAllRequestsCheck.isSelected();

        for (int i = 0; i < transferManager.getRequestCount(); i++) {

            TransferRequest request = transferManager.getRequests().get(i);

            if (!showAll && !"Pending".equalsIgnoreCase(request.getStatus())) {
                continue;
            }

            visibleRequestIndices.add(i);
            Student student = request.getStudent();

            requestModel.addRow(new Object[]{
                i + 1,
                student == null ? "" : student.getUserId(),
                student == null ? "" : student.getName(),
                student == null ? "" : student.getAssignedRoom(),
                request.getPreferredRoomType(),
                request.getReason(),
                request.getStatus()
            });
        }
    }

    private void refreshMyRequests() {

        myRequestModel.setRowCount(0);

        Student student = currentStudent();

        if (student == null) {
            return;
        }

        int number = 1;

        for (TransferRequest request : transferManager.getRequests()) {
            Student owner = request.getStudent();

            if (owner == null ||
                !owner.getUserId().equalsIgnoreCase(student.getUserId())) {
                continue;
            }

            myRequestModel.addRow(new Object[]{
                number++,
                request.getPreferredRoomType(),
                request.getReason(),
                request.getStatus(),
                owner.getAssignedRoom()
            });
        }
    }

    private void setCurrentStudent(Student student) {
        currentUser = student;
        refreshStudentView(student);
    }

    private void refreshStudentView(Student student) {

        if (student == null) {
            return;
        }

        profileText.setText(
            "<html>"
            + "<b>Name:</b> " + html(student.getName()) + "<br>"
            + "<b>Student ID:</b> " + html(student.getUserId()) + "<br>"
            + "<b>Username:</b> " + html(student.getUsername()) + "<br>"
            + "<b>Course:</b> " + html(student.getCourse()) + "<br>"
            + "<b>Year Level:</b> " + html(student.getYearLevel()) + "<br>"
            + "<b>Preferred Room Type:</b> "
            + html(student.getPreferredRoomType()) + "<br>"
            + "<b>Assigned Room:</b> " + html(student.getAssignedRoom())
            + "</html>"
        );

        Room room = roomManager.findRoom(student.getAssignedRoom());

        if (room == null) {
            roomText.setText(
                "<html><b>Assigned room:</b> "
                + html(student.getAssignedRoom()) + "</html>"
            );
        } else {
            StringBuilder occupants = new StringBuilder();

            for (Student occupant : room.getOccupants()) {
                if (occupants.length() > 0) {
                    occupants.append(", ");
                }
                occupants.append(occupant.getName());
            }

            roomText.setText(
                "<html>"
                + "<b>Room Number:</b> " + html(room.getRoomNumber()) + "<br>"
                + "<b>Room Type:</b> " + html(room.getRoomType()) + "<br>"
                + "<b>Capacity:</b> " + room.getCapacity() + "<br>"
                + "<b>Occupants:</b> " + html(occupants.toString()) + "<br>"
                + "<b>Available Beds:</b> " + room.getAvailableBeds() + "<br>"
                + "<b>Status:</b> "
                + (room.isAvailable() ? "Available" : "Full / Unavailable")
                + "</html>"
            );
        }

        refreshMyRequests();
    }

    // =====================================
    // HELPERS
    // =====================================

    private void printStartupSummary() {

        System.out.println("======================================");
        System.out.println("       DORMEASE GUI SYSTEM READY");
        System.out.println("======================================");

        User admin = loginManager.authenticate("admin", "admin123");

        User[] accounts = new User[students.size() + (admin == null ? 0 : 1)];
        int index = 0;

        if (admin != null) {
            accounts[index++] = admin;
        }

        for (Student student : students) {
            accounts[index++] = student;
        }

        for (User user : accounts) {
            if (user != null) {
                System.out.println(
                    "- " + user.getRole() + " | "
                    + user.getName() + " | "
                    + user.getUserId()
                );
            }
        }

        System.out.println("\nDefault administrator login: admin / admin123");
    }

    private Student currentStudent() {
        return currentUser instanceof Student ? (Student) currentUser : null;
    }

    private Student selectedStudent() {

        int row = studentTable.getSelectedRow();

        if (row < 0 || row >= students.size()) {
            return null;
        }

        return students.get(row);
    }

    private Room selectedRoom() {

        int row = roomTable.getSelectedRow();

        if (row < 0 || row >= roomManager.getRooms().size()) {
            return null;
        }

        return roomManager.getRooms().get(row);
    }

    private Student selectedWaitingStudent() {

        int row = waitingTable.getSelectedRow();

        if (row < 0) {
            return null;
        }

        Student[] waiting = waitingList.getWaitingStudents().toArray(new Student[0]);
        return row < waiting.length ? waiting[row] : null;
    }

    private Student findStudentById(String id) {

        if (ValidationUtils.isBlank(id)) {
            return null;
        }

        for (Student student : students) {
            if (student.getUserId().equalsIgnoreCase(id.trim())) {
                return student;
            }
        }

        return null;
    }

    private String prompt(String message, String initial) {
        return JOptionPane.showInputDialog(this, message, initial);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this, message, "DormEase", JOptionPane.ERROR_MESSAGE
        );
    }

    private void showInfo(String message) {
        JOptionPane.showMessageDialog(
            this, message, "DormEase", JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void saveData() {
        fileManager.saveData(students, roomManager, transferManager);
    }

    private void showCard(String name) {
        CardLayout layout = (CardLayout) cards.getLayout();
        layout.show(cards, name);
    }

    private GridBagConstraints baseConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        return gbc;
    }

    private void addFormRow(
        JPanel panel,
        GridBagConstraints gbc,
        int row,
        String label,
        JComponent field
    ) {

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, gbc);
    }

    private String html(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;");
    }

    private class SaveOnClose extends WindowAdapter {

        private static final long serialVersionUID = 1L;

        @Override
        public void windowClosed(WindowEvent e) {
            saveData();
            System.setOut(originalOut);
        }
    }
}
