package people;

import java.util.ArrayList;
import java.util.List;


public class Student extends Person {

    private String major;
    private List<String> enrolledCourseIds;

    public Student(String id, String name, String major) {
        super(id, name);
        this.major = major;
        this.enrolledCourseIds = new ArrayList<>();
    }

    public Student(String id, String name) {
        super(id, name);
        this.major = "Undeclared";
        this.enrolledCourseIds = new ArrayList<>();
    }

    @Override // Annotation
    public String role() {
        return "Student";
    }

    public String getMajor() { return major; }
    public List<String> getEnrolledCourseIds() { return enrolledCourseIds; }

    public boolean isEnrolledIn(String courseId) {
        return enrolledCourseIds.contains(courseId);
    }

    public void unenroll(String courseId) {
        enrolledCourseIds.remove(courseId);
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public void enroll(String courseId) {
        if (!enrolledCourseIds.contains(courseId)) {
            enrolledCourseIds.add(courseId);
        }
    }

    public void drop(String courseId) {
        enrolledCourseIds.remove(courseId);
    }

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("  Major: " + major);
        System.out.println("  Enrolled Courses: " + enrolledCourseIds.size());
    }
}