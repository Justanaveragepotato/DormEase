public class Student extends User {

    private String course;
    private String yearLevel;
    private String assignedRoom;

    public Student(String userId, String name, String username, String password, String course, String yearLevel) {

        super(userId, name, username, password);

        this.course = course;
        this.yearLevel = yearLevel;
        this.assignedRoom = "Not Assigned";
    }

    public String getCourse() {
        return course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public String getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(String assignedRoom) {
        this.assignedRoom = assignedRoom;
    }

    @Override
    public String getRole() {
        return "Student";
    }
}