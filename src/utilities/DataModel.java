package utilities;

import people.Student;
import people.Teacher;
import courses.Course;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DataModel {

    // Generics ensure type safety
    private List<Student> students;
    private List<Teacher> teachers;
    private List<Course> courses;

    public DataModel() {
        this.students = new ArrayList<>();
        this.teachers = new ArrayList<>();
        this.courses = new ArrayList<>();
    }


    public List<Student> getStudents() { return students; }
    public List<Teacher> getTeachers() { return teachers; }
    public List<Course> getCourses() { return courses; }
    
    public void setStudents(List<Student> students) { this.students = students; }
    public void setTeachers(List<Teacher> teachers) { this.teachers = teachers; }
    public void setCourses(List<Course> courses) { this.courses = courses; }


    public Optional<Student> findStudent(String id) {
        return students.stream().filter(s -> s.getId().equalsIgnoreCase(id)).findFirst();
    }
    
    public Optional<Teacher> findTeacher(String id) {
        return teachers.stream().filter(t -> t.getId().equalsIgnoreCase(id)).findFirst();
    }
    
    public Optional<Course> findCourse(String id) {
        return courses.stream().filter(c -> c.getCourseId().equalsIgnoreCase(id)).findFirst();
    }
}