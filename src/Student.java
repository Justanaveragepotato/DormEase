
public class Student extends User {

    private String course;
    private String yearLevel;
    private String assignedRoom;
    private String preferredRoomType;

    public Student(
        String userId,
        String name,
        String username,
        String password,
        String course,
        String yearLevel,
        String preferredRoomType
    ) {

        super(userId, name, username, password);

        this.course = course;
        this.yearLevel = yearLevel;
        setPreferredRoomType(preferredRoomType);
        this.assignedRoom = "Not Assigned";
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public String getAssignedRoom() {
        return assignedRoom;
    }

    public void setAssignedRoom(String assignedRoom) {
        String normalized = ValidationUtils.trimToNull(assignedRoom);
        this.assignedRoom = normalized == null ? "Not Assigned" : normalized;
    }

    public String getPreferredRoomType() {
        return preferredRoomType;
    }

    public boolean setPreferredRoomType(String preferredRoomType) {
        String normalized = Room.normalizeRoomType(preferredRoomType);
        if (normalized == null) {
            return false;
        }
        this.preferredRoomType = normalized;
        return true;
    }

    @Override
    public String getRole() {
        return "Student";
    }
}
