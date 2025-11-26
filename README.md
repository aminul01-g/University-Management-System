# University Management System (UMS)

A Java-based command-line application for managing university resources including students, teachers, courses, and enrollments. The system supports both in-memory operations and SQLite database persistence.

## Features

- Student Management
  - Add/remove students
  - Update student information
  - Track student enrollments
  - View all students

- Teacher Management
  - Add new teachers
  - Assign teachers to courses
  - Track teacher departments and subjects
  - View all teachers

- Course Management
  - Create new courses
  - Manage course enrollments
  - Track course departments
  - Assign teachers to courses
  - View all courses

- Department Support
  - Computer Science
  - Business Administration
  - Electrical Engineering
  - Arts and Humanities
  - Physics


```
UMS/
├── src/
│   ├── main/
│   ├── courses/
│   │   └── Course.java         # Course management
│   ├── people/
│   │   ├── Student.java        # Student entity
│   │   └── Teacher.java        # Teacher entity
│       ├── DatabaseService.java # Database operations
│       ├── DataModel.java      # In-memory data storage
│       └── UniversityService.java # Business logic
└── bin/                        # Compiled classes
```


1. Compile the project:
```bash
mkdir -p bin
javac -d bin $(find src -name '*.java')
```
  ## Contributing

  Contributions are welcome. If you'd like to add a feature, fix a bug, or improve documentation:

  - Fork the repository
  - Create a feature branch: `git checkout -b feat/your-feature`
  - Make your changes and add tests where appropriate
  - Commit and push: `git commit -am "feat: summary" && git push origin feat/your-feature`
  - Open a Pull Request and describe your changes

  Please follow small, focused commits and include a clear description of the problem your change addresses.

  ## Building a runnable JAR

  To create a single runnable JAR (simple approach using the JDK):

  ```bash
  mkdir -p out
  javac -d out $(find src -name '*.java')
  jar --create --file ums.jar -C out .
  ```

  Then run:

  ```bash
  java -cp "ums.jar:sqlite-jdbc-3.40.0.0.jar" main.Main
  ```

  If you don't need persistence, omit the JDBC jar from the classpath.

  ## Troubleshooting

  1. "No suitable driver found for jdbc:sqlite:ums.db"

    This means the SQLite JDBC driver is not on the classpath. Download a suitable sqlite-jdbc JAR and run the application with it on the classpath, for example:

    ```bash
    java -cp "bin:sqlite-jdbc-3.40.0.0.jar" main.Main
    ```

    Alternatively, the project contains a runtime fallback: when a JDBC driver is not found the application will continue in an in-memory mode but persistence (saving to `ums.db`) will be disabled. To enable persistence, add the JDBC driver to the classpath.

  2. "Compiled by a more recent version of the Java Runtime"

    Recompile using `--release 17` or run with a matching JDK/JRE. Example compile targeting Java 17:

    ```bash
    javac --release 17 -d bin $(find src -name '*.java')
    ```

  3. Database file permissions

    If you see permission errors writing `ums.db`, ensure the running user has write permissions in the working directory or run the program from a directory where it can create files.

  ## Tests and Validation

  There are no automated unit tests included yet. To validate manually, run the program and use the "Load Demo Data" menu option, or exercise the CLI flows (add students, courses, assign teachers, enroll students) and verify the output.

  ## Notes & Next Steps

  - Add automated unit tests (JUnit) for `UniversityService` business logic.
  - Add a build tool (Maven/Gradle) for dependency and packaging management.
  - Add integration tests that exercise DB persistence with the SQLite JDBC driver.

  ## Contact

  If you have questions or want to collaborate, create an issue in the repository or reach out via the GitHub profile at `https://github.com/aminul01-g`.
2. Run the application:
```bash
java -cp bin main.Main
```

### Database Support

The system can operate in two modes:
- **In-Memory Mode**: No external dependencies required
- **Persistent Mode**: Requires SQLite JDBC driver (optional)

To enable database persistence:
1. Download the SQLite JDBC driver (e.g., `sqlite-jdbc-3.40.0.0.jar`)
2. Add it to the classpath when running:
```bash
java -cp "bin:sqlite-jdbc-3.40.0.0.jar" main.Main
```

## Usage Examples

1. Add a new student:
```
=== Student Operations ===
1. Add New Student
Enter Student ID: S103
Enter Student Name: John Doe
Enter Major: Computer Science
```

2. Create a new course:
```
=== Course Operations ===
1. Add New Course
Enter Course ID: CS102
Enter Course Name: Data Structures
Enter Department: COMPUTER_SCIENCE
```

3. Enroll students in courses:
```
=== Student Operations ===
2. Enroll Student in Course
Enter Student ID: S103
Enter Course ID: CS102
```

## Technical Details

- Written in Java
- Command Line Interface (CLI)
- Modular architecture (MVC-like pattern)
- Support for SQLite persistence
- Exception handling and input validation
- Automatic database schema creation