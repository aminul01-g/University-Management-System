package utilities;

import courses.Course;
import courses.Department;
import people.Student;
import people.Teacher;

import java.sql.*;


public class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:ums.db";

    private boolean persistenceEnabled = true;

    /**
     * Establishes a connection to the SQLite database.
     * @return a Connection object.
     * @throws SQLException if a database access error occurs.
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }


    public void clearDatabase() {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; clearDatabase skipped.");
            return;
        }
        String[] tables = {"ENROLLMENTS", "COURSES", "TEACHERS", "STUDENTS"};
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            for (String table : tables) {
                stmt.execute("DELETE FROM " + table);
            }
            System.out.println("[DB] Database tables cleared successfully.");
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to clear database: " + e.getMessage(), e);
        }
    }

    public void initDatabase() {

        try (Connection conn = connect()) {
            System.out.println("[DB] Database connection established successfully.");
        } catch (SQLException e) {

            persistenceEnabled = false;
            System.err.println("[DB Warning] Persistence disabled: " + e.getMessage());
            System.err.println("[DB Warning] Continuing in in-memory mode. To enable persistence, add an SQLite JDBC driver to the classpath.");
            return;
        }

        // SQL for creating tables
        String createStudents = "CREATE TABLE IF NOT EXISTS STUDENTS (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  major TEXT" +
                ");";

        String createTeachers = "CREATE TABLE IF NOT EXISTS TEACHERS (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  department TEXT," +
                "  subject TEXT" +
                ");";

        String createCourses = "CREATE TABLE IF NOT EXISTS COURSES (" +
                "  id TEXT PRIMARY KEY," +
                "  name TEXT NOT NULL," +
                "  department TEXT," +
                "  teacher_id TEXT," +
                "  FOREIGN KEY(teacher_id) REFERENCES TEACHERS(id)" +
                ");";


        String createEnrollments = "CREATE TABLE IF NOT EXISTS ENROLLMENTS (" +
                "  student_id TEXT NOT NULL," +
                "  course_id TEXT NOT NULL," +
                "  PRIMARY KEY (student_id, course_id)," +
                "  FOREIGN KEY(student_id) REFERENCES STUDENTS(id)," +
                "  FOREIGN KEY(course_id) REFERENCES COURSES(id)" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Execute all table creation statements
            stmt.execute(createStudents);
            stmt.execute(createTeachers);
            stmt.execute(createCourses);
            stmt.execute(createEnrollments);
            
            System.out.println("[DB] Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("[DB Error] " + e.getMessage());
        }
    }

    public void loadData(DataModel model) {
        System.out.println("[DB] Loading data from database...");
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; skipping loadData.");
            return;
        }
        try (Connection conn = connect()) {
            
            // 1. Load Students
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM STUDENTS")) {
                while (rs.next()) {
                    model.getStudents().add(new Student(
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("major")
                    ));
                }
            }

            // 2. Load Teachers
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM TEACHERS")) {
                while (rs.next()) {
                    model.getTeachers().add(new Teacher(
                            rs.getString("id"),
                            rs.getString("name"),
                            Department.valueOf(rs.getString("department")),
                            rs.getString("subject")
                    ));
                }
            }
            
            // 3. Load Courses
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM COURSES")) {
                while (rs.next()) {
                    Course c = new Course(
                            rs.getString("id"),
                            rs.getString("name"),
                            Department.valueOf(rs.getString("department"))
                    );
                    c.assignTeacher(rs.getString("teacher_id")); // Can be null
                    model.getCourses().add(c);
                }
            }

            // 4. Load Enrollments and populate relationships
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT * FROM ENROLLMENTS")) {
                while (rs.next()) {
                    String studentId = rs.getString("student_id");
                    String courseId = rs.getString("course_id");
                    
                    // Find the objects in the model and link them
                    model.findStudent(studentId).ifPresent(s -> s.enroll(courseId));
                    model.findCourse(courseId).ifPresent(c -> c.addStudent(studentId));
                }
            }
            System.out.println("[DB] Data loaded successfully.");

        } catch (SQLException e) {
            System.err.println("[DB Error] Could not load data: " + e.getMessage());
        }
    }

    // --- Data Modification Methods ---
    // These methods write changes to the DB immediately.

    public void insertStudent(Student s) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; insertStudent skipped.");
            return;
        }
        String sql = "INSERT INTO STUDENTS(id, name, major) VALUES(?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, s.getId());
            pstmt.setString(2, s.getName());
            pstmt.setString(3, s.getMajor());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to insert student: " + e.getMessage(), e);
        }
    }

    public void insertTeacher(Teacher t) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; insertTeacher skipped.");
            return;
        }
        String sql = "INSERT INTO TEACHERS(id, name, department, subject) VALUES(?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getId());
            pstmt.setString(2, t.getName());
            pstmt.setString(3, t.getDepartment().name()); // Store enum as string
            pstmt.setString(4, t.getSubject());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to insert teacher: " + e.getMessage(), e);
        }
    }
    
    public void insertCourse(Course c) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; insertCourse skipped.");
            return;
        }
        String sql = "INSERT INTO COURSES(id, name, department, teacher_id) VALUES(?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, c.getCourseId());
            pstmt.setString(2, c.getCourseName());
            pstmt.setString(3, c.getDepartment().name());
            pstmt.setString(4, c.getTeacherId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to insert course: " + e.getMessage(), e);
        }
    }

    public void insertEnrollment(String studentId, String courseId) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; insertEnrollment skipped.");
            return;
        }
        String sql = "INSERT INTO ENROLLMENTS(student_id, course_id) VALUES(?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            pstmt.setString(2, courseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to insert enrollment: " + e.getMessage(), e);
        }
    }

    public void updateTeacherAssignment(String courseId, String teacherId) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; updateTeacherAssignment skipped.");
            return;
        }
        String sql = "UPDATE COURSES SET teacher_id = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, teacherId);
            pstmt.setString(2, courseId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to update teacher assignment: " + e.getMessage(), e);
        }
    }

    public void deleteStudent(String id) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; deleteStudent skipped.");
            return;
        }
        String sql = "DELETE FROM STUDENTS WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // First, delete any enrollments
            String enrollSql = "DELETE FROM ENROLLMENTS WHERE student_id = ?";
            try (PreparedStatement enrollStmt = conn.prepareStatement(enrollSql)) {
                enrollStmt.setString(1, id);
                enrollStmt.executeUpdate();
            }
            // Then delete the student
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to delete student: " + e.getMessage(), e);
        }
    }

    public void updateStudent(Student student) {
        if (!persistenceEnabled) {
            System.out.println("[DB Warning] Persistence disabled; updateStudent skipped.");
            return;
        }
        String sql = "UPDATE STUDENTS SET name = ?, major = ? WHERE id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, student.getName());
            pstmt.setString(2, student.getMajor());
            pstmt.setString(3, student.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("[DB Error] Failed to update student: " + e.getMessage(), e);
        }
    }
}
