package utilities;

import people.*;
import courses.*;
import interfaces.Printable;
import java.util.List;
import java.util.Optional;


public class UniversityService {
    private static final int MAX_COURSE_CAPACITY = 30;
    
    private DataModel model;
    private DatabaseService dbService;

    public UniversityService(DataModel model, DatabaseService dbService) {
        if (model == null || dbService == null) {
            throw new IllegalArgumentException("Model and DatabaseService cannot be null");
        }
        this.model = model;
        this.dbService = dbService;
        
        // Initialize the database tables on startup
        try {
            this.dbService.initDatabase();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    // This method is now called by Main to load data at startup
    public void loadDataFromDatabase() {
        dbService.loadData(model);
    }

    public void clearAllData() {
        try {
            dbService.clearDatabase();
            model.getStudents().clear();
            model.getTeachers().clear();
            model.getCourses().clear();
            System.out.println("[System] Database cleared successfully.");
        } catch (Exception e) {
            System.err.println("[Error] Failed to clear database: " + e.getMessage());
            throw new RuntimeException("Failed to clear database", e);
        }
    }

    // --- Student Management ---
    private void validateStudent(String id, String name, String major) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Student ID cannot be empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Student name cannot be empty");
        }
        if (major == null || major.trim().isEmpty()) {
            throw new IllegalArgumentException("Major cannot be empty");
        }
    }

    public boolean addStudent(String id, String name, String major) {
        try {
            validateStudent(id, name, major);
            if (model.findStudent(id).isPresent()) {
                System.err.println("Error adding student: Student ID " + id + " already exists.");
                return false;
            }
            Student s = new Student(id, name, major);
            // Persist first, then update in-memory model to avoid inconsistencies
            dbService.insertStudent(s);
            model.getStudents().add(s);
            System.out.println("Student added: " + name);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding student: " + e.getMessage());
            return false;
        }
    }
    
    // Overloaded method
    public boolean addStudent(String id, String name) {
        return this.addStudent(id, name, "Undeclared");
    }

    public void removeStudent(String id) {
        try {
            Optional<Student> student = model.findStudent(id);
            if (student.isEmpty()) {
                throw new IllegalArgumentException("Student not found");
            }
            // Delete from DB first to ensure persistence; DatabaseService throws on failure
            dbService.deleteStudent(id);
            model.getStudents().remove(student.get());
            System.out.println("Student removed: " + id);
        } catch (Exception e) {
            System.err.println("Error removing student: " + e.getMessage());
        }
    }

    public void listStudents() {
        System.out.println("\n--- All Students ---");
        printList(model.getStudents());
        System.out.println("--------------------");
    }

    // --- Teacher Management ---
    public boolean addTeacher(String id, String name, Department dept, String subject) {
        if (model.findTeacher(id).isPresent()) {
            System.err.println("Error: Teacher ID " + id + " already exists.");
            return false;
        }
        Teacher t = new Teacher(id, name, dept, subject);
        try {
            dbService.insertTeacher(t);
            model.getTeachers().add(t);
            System.out.println("Teacher added: " + name);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding teacher: " + e.getMessage());
            return false;
        }
    }

    public void listTeachers() {
        System.out.println("\n--- All Teachers ---");
        printList(model.getTeachers());
        System.out.println("--------------------");
    }

    // --- Course Management ---
    public boolean addCourse(String id, String name, Department dept) {
        if (model.findCourse(id).isPresent()) {
            System.err.println("Error: Course ID " + id + " already exists.");
            return false;
        }
        Course c = new Course(id, name, dept);
        try {
            dbService.insertCourse(c);
            model.getCourses().add(c);
            System.out.println("Course added: " + name);
            return true;
        } catch (Exception e) {
            System.err.println("Error adding course: " + e.getMessage());
            return false;
        }
    }

    public void listCourses() {
        System.out.println("\n--- All Courses ---");
        printList(model.getCourses());
        System.out.println("-------------------");
    }

    // --- Enrollment Management ---
    public boolean enrollStudent(String studentId, String courseId) {
        studentId = normalizeId(studentId);
        courseId = normalizeId(courseId);

        Optional<Student> s_opt = model.findStudent(studentId);
        Optional<Course> c_opt = model.findCourse(courseId);

        if (s_opt.isEmpty()) {
            System.err.println("Error during enrollment: Student not found (" + studentId + ")");
            return false;
        }
        if (c_opt.isEmpty()) {
            System.err.println("Error during enrollment: Course not found (" + courseId + ")");
            return false;
        }

        Student student = s_opt.get();
        Course course = c_opt.get();

        if (course.getEnrolledStudentIds().size() >= MAX_COURSE_CAPACITY) {
            System.err.println("Error during enrollment: Course has reached maximum capacity");
            return false;
        }

        if (student.isEnrolledIn(courseId)) {
            System.err.println("Error during enrollment: Student already enrolled in this course");
            return false;
        }

        // Transaction-like operation
        try {
            student.enroll(courseId);
            course.addStudent(studentId);
            dbService.insertEnrollment(studentId, courseId);
            System.out.println("Student " + student.getName() + " enrolled in " + course.getCourseName());
            return true;
        } catch (Exception e) {
            // Rollback memory changes if database operation fails
            student.unenroll(courseId);
            course.removeStudent(studentId);
            System.err.println("Error during enrollment: " + e.getMessage());
            return false;
        }
    }

    public boolean assignTeacher(String teacherId, String courseId) {
        teacherId = normalizeId(teacherId);
        courseId = normalizeId(courseId);

        Optional<Teacher> t_opt = model.findTeacher(teacherId);
        Optional<Course> c_opt = model.findCourse(courseId);

        if (t_opt.isEmpty()) {
            System.err.println("Error: Teacher not found (" + teacherId + ")");
            return false;
        }
        if (c_opt.isEmpty()) {
            System.err.println("Error: Course not found (" + courseId + ")");
            return false;
        }

        Teacher teacher = t_opt.get();
        Course course = c_opt.get();
        try {
            // Update database first
            dbService.updateTeacherAssignment(courseId, teacherId);
            // Update in-memory model only if DB update succeeded
            course.assignTeacher(teacherId);
            System.out.println("Teacher " + teacher.getName() + " assigned to " + course.getCourseName());
            return true;
        } catch (Exception e) {
            System.err.println("Error assigning teacher: " + e.getMessage());
            return false;
        }
    }
    

    private void printList(List<? extends Printable> list) {
        if (list.isEmpty()) {
            System.out.println("  (No entries found in cache)");
            return;
        }
        for (Printable p : list) {
            p.printDetails();
            System.out.println(); 
        }
    }

    // Normalize user-provided IDs: trim, and strip anything after a space or '['
    private String normalizeId(String id) {
        if (id == null) return null;
        id = id.trim();
        // If user copied the printed line like "333 [COMPUTER_SCIENCE]", strip the bracket part
        int bracket = id.indexOf('[');
        if (bracket >= 0) {
            id = id.substring(0, bracket).trim();
        }
        // If there's extra descriptive text after a space, take first token
        int sp = id.indexOf(' ');
        if (sp > 0) {
            id = id.substring(0, sp).trim();
        }
        return id;
    }
    // --- Utilities ---
    public void loadDemoData() {
        if (!model.getStudents().isEmpty()) {
            System.out.println("[System] Data already exists. Demo not loaded.");
            return;
        }
        System.out.println("[System] Loading demo data into memory and database...");
        
        // These methods now automatically write to the DB
        addStudent("S101", "Alice Smith", "Computer Science");
        addStudent("S102", "Bob Johnson", "Business");
        
        addTeacher("T201", "Dr. Alan Turing", Department.COMPUTER_SCIENCE, "Algorithms");
        addTeacher("T202", "Dr. Eva Core", Department.BUSINESS_ADMINISTRATION, "Marketing");
        
        addCourse("CS101", "Intro to Programming", Department.COMPUTER_SCIENCE);
        addCourse("BUS101", "Principles of Management", Department.BUSINESS_ADMINISTRATION);
        
        assignTeacher("T201", "CS101");
        assignTeacher("T202", "BUS101");
        
        enrollStudent("S101", "CS101");
        enrollStudent("S102", "BUS101");
        enrollStudent("S101", "BUS101");
        System.out.println("[System] Demo data loaded.");
    }
    
    public void updateStudentMajor(String studentId, String newMajor) {
        try {
            Optional<Student> student = model.findStudent(studentId);
            if (student.isEmpty()) {
                throw new IllegalArgumentException("Student not found");
            }
            Student s = student.get();
            String oldMajor = s.getMajor();
            s.setMajor(newMajor);
            try {
                dbService.updateStudent(s);
                System.out.println("Student major updated successfully");
            } catch (Exception e) {
                s.setMajor(oldMajor);
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Error updating student major: " + e.getMessage());
        }
    }
}

