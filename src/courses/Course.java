package courses;

import interfaces.Printable;
import java.util.ArrayList;
import java.util.List;


public class Course implements Printable {

    private String courseId;
    private String courseName;
    private Department department;
    private String teacherId;
    private List<String> enrolledStudentIds;

    public Course(String courseId, String courseName, Department department) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.department = department;
        this.teacherId = "TBD"; // To Be Determined
        
        // Collections Framework
        this.enrolledStudentIds = new ArrayList<>();
    }

    // --- Getters ---
    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }
    public Department getDepartment() { return department; } // Needed for DB
    public String getTeacherId() { return teacherId; }
    public List<String> getEnrolledStudentIds() { return enrolledStudentIds; }
    
    // --- Business Logic Methods ---
    public void assignTeacher(String teacherId) {
        this.teacherId = (teacherId == null) ? "TBD" : teacherId;
    }

    public void addStudent(String studentId) {
        if (!enrolledStudentIds.contains(studentId)) {
            enrolledStudentIds.add(studentId);
        }
    }

    public void removeStudent(String studentId) {
        enrolledStudentIds.remove(studentId);
    }
    


    @Override
    public void printDetails() {
        System.out.println("Course ID: " + courseId + " [" + department.name() + "]");
        System.out.println("  Name: " + courseName);
        System.out.println("  Teacher ID: " + teacherId);
        System.out.println("  Enrollment: " + enrolledStudentIds.size() + " student(s)");
    }
}