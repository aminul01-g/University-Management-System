package utilities;

import courses.Course;
import courses.Department;
import people.Student;
import people.Teacher;

import java.sql.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.stream.Stream;


public class DatabaseService {

    private static final String DB_URL = "jdbc:sqlite:ums.db";

    private enum PersistenceMode { SQL, FILE, NONE }

    private PersistenceMode persistenceMode = PersistenceMode.SQL;
    private static final Path DATA_DIR = Paths.get("data");

    /**
     * Establishes a connection to the SQLite database.
     * @return a Connection object.
     * @throws SQLException if a database access error occurs.
     */
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }


    public void clearDatabase() {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; clearDatabase skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
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
            return;
        }

        // FILE mode: truncate CSV files
        try {
            Files.newBufferedWriter(DATA_DIR.resolve("students.csv"), StandardOpenOption.TRUNCATE_EXISTING).close();
            Files.newBufferedWriter(DATA_DIR.resolve("teachers.csv"), StandardOpenOption.TRUNCATE_EXISTING).close();
            Files.newBufferedWriter(DATA_DIR.resolve("courses.csv"), StandardOpenOption.TRUNCATE_EXISTING).close();
            Files.newBufferedWriter(DATA_DIR.resolve("enrollments.csv"), StandardOpenOption.TRUNCATE_EXISTING).close();
            System.out.println("[DB] File-based data cleared successfully.");
        } catch (IOException e) {
            throw new RuntimeException("[DB Error] Failed to clear file-based storage: " + e.getMessage(), e);
        }
    }

    public void initDatabase() {

        // Try to establish SQL connection first. If JDBC is not available, fall back to file persistence.
        try (Connection conn = connect()) {
            System.out.println("[DB] Database connection established successfully.");
            persistenceMode = PersistenceMode.SQL;
        } catch (SQLException e) {
            // SQL not available; fall back to simple file-based persistence
            System.err.println("[DB Warning] Could not open SQLite connection: " + e.getMessage());
            System.err.println("[DB Warning] Falling back to file-based persistence in './data' directory.");
            persistenceMode = PersistenceMode.FILE;
            try {
                if (!Files.exists(DATA_DIR)) {
                    Files.createDirectories(DATA_DIR);
                }
                // Ensure CSV files exist
                Path studentsF = DATA_DIR.resolve("students.csv");
                Path teachersF = DATA_DIR.resolve("teachers.csv");
                Path coursesF = DATA_DIR.resolve("courses.csv");
                Path enrollF = DATA_DIR.resolve("enrollments.csv");
                if (!Files.exists(studentsF)) Files.createFile(studentsF);
                if (!Files.exists(teachersF)) Files.createFile(teachersF);
                if (!Files.exists(coursesF)) Files.createFile(coursesF);
                if (!Files.exists(enrollF)) Files.createFile(enrollF);
                System.out.println("[DB] File persistence initialized.");
            } catch (IOException io) {
                persistenceMode = PersistenceMode.NONE;
                System.err.println("[DB Error] Failed to initialize file persistence: " + io.getMessage());
            }
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
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; skipping loadData.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
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
            return;
        }

        // FILE mode: read CSVs from data directory
        try {
            Path studentsF = DATA_DIR.resolve("students.csv");
            if (Files.exists(studentsF)) {
                try (Stream<String> lines = Files.lines(studentsF)) {
                    lines.forEach(line -> {
                        if (line.trim().isEmpty()) return;
                        String[] parts = line.split("\\,");
                        String id = parts.length > 0 ? parts[0] : "";
                        String name = parts.length > 1 ? parts[1] : "";
                        String major = parts.length > 2 ? parts[2] : "";
                        model.getStudents().add(new Student(id, name, major));
                    });
                }
            }

            Path teachersF = DATA_DIR.resolve("teachers.csv");
            if (Files.exists(teachersF)) {
                try (Stream<String> lines = Files.lines(teachersF)) {
                    lines.forEach(line -> {
                        if (line.trim().isEmpty()) return;
                        String[] parts = line.split("\\,");
                        String id = parts.length > 0 ? parts[0] : "";
                        String name = parts.length > 1 ? parts[1] : "";
                        String dept = parts.length > 2 ? parts[2] : "";
                        String subject = parts.length > 3 ? parts[3] : "";
                        try {
                            model.getTeachers().add(new Teacher(id, name, Department.valueOf(dept), subject));
                        } catch (IllegalArgumentException iae) {
                            // skip invalid department entries
                        }
                    });
                }
            }

            Path coursesF = DATA_DIR.resolve("courses.csv");
            if (Files.exists(coursesF)) {
                try (Stream<String> lines = Files.lines(coursesF)) {
                    lines.forEach(line -> {
                        if (line.trim().isEmpty()) return;
                        String[] parts = line.split("\\,");
                        String id = parts.length > 0 ? parts[0] : "";
                        String name = parts.length > 1 ? parts[1] : "";
                        String dept = parts.length > 2 ? parts[2] : "";
                        String teacherId = parts.length > 3 ? parts[3] : null;
                        try {
                            Course c = new Course(id, name, Department.valueOf(dept));
                            if (teacherId != null && !teacherId.isEmpty()) c.assignTeacher(teacherId);
                            model.getCourses().add(c);
                        } catch (IllegalArgumentException iae) {
                            // skip invalid department entries
                        }
                    });
                }
            }

            Path enrollF = DATA_DIR.resolve("enrollments.csv");
            if (Files.exists(enrollF)) {
                try (Stream<String> lines = Files.lines(enrollF)) {
                    lines.forEach(line -> {
                        if (line.trim().isEmpty()) return;
                        String[] parts = line.split("\\,");
                        String studentId = parts.length > 0 ? parts[0] : "";
                        String courseId = parts.length > 1 ? parts[1] : "";
                        model.findStudent(studentId).ifPresent(s -> s.enroll(courseId));
                        model.findCourse(courseId).ifPresent(c -> c.addStudent(studentId));
                    });
                }
            }
            System.out.println("[DB] Data loaded from files successfully.");
        } catch (IOException ioe) {
            System.err.println("[DB Error] Failed to load file-based data: " + ioe.getMessage());
        }
    }

    // --- Data Modification Methods ---
    // These methods write changes to the DB immediately.

    public void insertStudent(Student s) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; insertStudent skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
            String sql = "INSERT INTO STUDENTS(id, name, major) VALUES(?,?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, s.getId());
                pstmt.setString(2, s.getName());
                pstmt.setString(3, s.getMajor());
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to insert student: " + e.getMessage(), e);
            }
        }

        // FILE mode
        Path studentsF = DATA_DIR.resolve("students.csv");
        String line = String.join(",", escapeCsv(s.getId()), escapeCsv(s.getName()), escapeCsv(s.getMajor()));
        try {
            Files.write(studentsF, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to write student to file: " + ioe.getMessage(), ioe);
        }
    }

    public void insertTeacher(Teacher t) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; insertTeacher skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
            String sql = "INSERT INTO TEACHERS(id, name, department, subject) VALUES(?,?,?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, t.getId());
                pstmt.setString(2, t.getName());
                pstmt.setString(3, t.getDepartment().name()); // Store enum as string
                pstmt.setString(4, t.getSubject());
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to insert teacher: " + e.getMessage(), e);
            }
        }

        Path teachersF = DATA_DIR.resolve("teachers.csv");
        String line = String.join(",", escapeCsv(t.getId()), escapeCsv(t.getName()), escapeCsv(t.getDepartment().name()), escapeCsv(t.getSubject()));
        try {
            Files.write(teachersF, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to write teacher to file: " + ioe.getMessage(), ioe);
        }
    }
    
    public void insertCourse(Course c) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; insertCourse skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
            String sql = "INSERT INTO COURSES(id, name, department, teacher_id) VALUES(?,?,?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, c.getCourseId());
                pstmt.setString(2, c.getCourseName());
                pstmt.setString(3, c.getDepartment().name());
                pstmt.setString(4, c.getTeacherId());
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to insert course: " + e.getMessage(), e);
            }
        }

        Path coursesF = DATA_DIR.resolve("courses.csv");
        String line = String.join(",", escapeCsv(c.getCourseId()), escapeCsv(c.getCourseName()), escapeCsv(c.getDepartment().name()), escapeCsv(c.getTeacherId() == null ? "" : c.getTeacherId()));
        try {
            Files.write(coursesF, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to write course to file: " + ioe.getMessage(), ioe);
        }
    }

    public void insertEnrollment(String studentId, String courseId) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; insertEnrollment skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
            String sql = "INSERT INTO ENROLLMENTS(student_id, course_id) VALUES(?,?)";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, studentId);
                pstmt.setString(2, courseId);
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to insert enrollment: " + e.getMessage(), e);
            }
        }

        Path enrollF = DATA_DIR.resolve("enrollments.csv");
        String line = String.join(",", escapeCsv(studentId), escapeCsv(courseId));
        try {
            Files.write(enrollF, (line + System.lineSeparator()).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to write enrollment to file: " + ioe.getMessage(), ioe);
        }
    }

    public void updateTeacherAssignment(String courseId, String teacherId) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; updateTeacherAssignment skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
            String sql = "UPDATE COURSES SET teacher_id = ? WHERE id = ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, teacherId);
                pstmt.setString(2, courseId);
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to update teacher assignment: " + e.getMessage(), e);
            }
        }

        // FILE mode: rewrite courses file replacing matching course id line
        Path coursesF = DATA_DIR.resolve("courses.csv");
        try {
            if (!Files.exists(coursesF)) return;
            Stream<String> lines = Files.lines(coursesF);
            java.util.List<String> updated = lines.map(line -> {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(courseId)) {
                    String name = parts.length > 1 ? parts[1] : "";
                    String dept = parts.length > 2 ? parts[2] : "";
                    return String.join(",", escapeCsv(courseId), name, dept, escapeCsv(teacherId == null ? "" : teacherId));
                }
                return line;
            }).toList();
            lines.close();
            Files.write(coursesF, String.join(System.lineSeparator(), updated).getBytes());
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to update teacher assignment in file: " + ioe.getMessage(), ioe);
        }
    }

    public void deleteStudent(String id) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; deleteStudent skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
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
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to delete student: " + e.getMessage(), e);
            }
        }

        // FILE mode: remove lines from enrollments and students files
        try {
            Path enrollF = DATA_DIR.resolve("enrollments.csv");
            if (Files.exists(enrollF)) {
                Stream<String> lines = Files.lines(enrollF);
                java.util.List<String> filtered = lines.filter(line -> {
                    if (line.trim().isEmpty()) return false;
                    String[] parts = line.split(",");
                    return parts.length < 1 || !parts[0].equals(id);
                }).toList();
                lines.close();
                Files.write(enrollF, String.join(System.lineSeparator(), filtered).getBytes());
            }

            Path studentsF = DATA_DIR.resolve("students.csv");
            if (Files.exists(studentsF)) {
                Stream<String> lines = Files.lines(studentsF);
                java.util.List<String> filtered = lines.filter(line -> {
                    if (line.trim().isEmpty()) return false;
                    String[] parts = line.split(",");
                    return parts.length < 1 || !parts[0].equals(id);
                }).toList();
                lines.close();
                Files.write(studentsF, String.join(System.lineSeparator(), filtered).getBytes());
            }
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to delete student from file storage: " + ioe.getMessage(), ioe);
        }
    }

    public void updateStudent(Student student) {
        if (persistenceMode == PersistenceMode.NONE) {
            System.out.println("[DB Warning] Persistence disabled; updateStudent skipped.");
            return;
        }
        if (persistenceMode == PersistenceMode.SQL) {
            String sql = "UPDATE STUDENTS SET name = ?, major = ? WHERE id = ?";
            try (Connection conn = connect();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, student.getName());
                pstmt.setString(2, student.getMajor());
                pstmt.setString(3, student.getId());
                pstmt.executeUpdate();
                return;
            } catch (SQLException e) {
                throw new RuntimeException("[DB Error] Failed to update student: " + e.getMessage(), e);
            }
        }

        // FILE mode: rewrite students file replacing matching id
        Path studentsF = DATA_DIR.resolve("students.csv");
        try {
            if (!Files.exists(studentsF)) return;
            Stream<String> lines = Files.lines(studentsF);
            java.util.List<String> updated = lines.map(line -> {
                String[] parts = line.split(",");
                if (parts.length > 0 && parts[0].equals(student.getId())) {
                    return String.join(",", escapeCsv(student.getId()), escapeCsv(student.getName()), escapeCsv(student.getMajor()));
                }
                return line;
            }).toList();
            lines.close();
            Files.write(studentsF, String.join(System.lineSeparator(), updated).getBytes());
        } catch (IOException ioe) {
            throw new RuntimeException("[DB Error] Failed to update student in file storage: " + ioe.getMessage(), ioe);
        }
    }

    // Simple CSV escaping to avoid breaking on commas/newlines
    private static String escapeCsv(String s) {
        if (s == null) return "";
        return s.replace("\n", " ").replace("\r", " ").replace(",", ";");
    }
}
