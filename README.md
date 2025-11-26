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

   # University Management System (UMS)

   A Java-based CLI application for managing students, teachers, courses and enrollments.

   This repository now supports two persistence modes:

   - SQL mode (recommended): uses SQLite via the `sqlite-jdbc` driver.
   - File fallback mode: plain CSV files in `./data/` (used only if SQLite driver is missing).

   Important: when the SQLite JDBC driver is available on the classpath the app uses `ums.db` and SQL mode; otherwise it falls back to CSV files so data is not lost completely.

   ## Contents

   ```
   UMS/
   ├── src/                      # Java source files
   ├── data/                     # (created automatically when using file fallback)
   │   ├── students.csv
   │   ├── teachers.csv
   │   ├── courses.csv
   │   └── enrollments.csv
   ├── target/                   # Maven build output
   └── pom.xml                   # Maven build + dependency declaration (sqlite-jdbc)
   ```

   ## Quick Start

   Using Maven (recommended — gets the SQLite driver automatically):

   ```bash
   # compile and run (exec plugin will run main.Main)
   mvn -DskipTests=true compile exec:java
   ```

   If you prefer to compile and run manually without Maven:

   ```bash
   # compile
   mkdir -p bin
   javac -d bin $(find src -name "*.java")

   # run WITHOUT persistence (in-memory / file fallback depending on driver)
   java -cp bin main.Main

   # run WITH SQLite persistence (add JDBC driver JAR to classpath)
   java -cp "bin:lib/sqlite-jdbc.jar" main.Main
   ```

   Using the packaged Maven setup is easiest because `pom.xml` declares `org.xerial:sqlite-jdbc` and the exec plugin runs the app with dependencies present.

   ## Persistence Modes

   - **SQL mode**: When the SQLite driver is available, the app will print:

     `[DB] Database connection established successfully.`

     and use a file `ums.db` in the working directory. All mutations are persisted to the database.

   - **File fallback (CSV)**: If the driver is not found you'll see:

     `[DB Warning] Falling back to file-based persistence in './data' directory.`

     The app then creates `data/students.csv`, `data/teachers.csv`, `data/courses.csv`, and `data/enrollments.csv` and writes changes there.

   Notes
   -----

   - The application will always try SQL first and then fall back to files — this prevents data loss when the JDBC driver is missing.
   - CLI messages now only show success when the operation actually completed (e.g., `enrollStudent` returns success boolean).

   ## Enabling full SQL persistence (detailed)

   1. Use Maven (recommended):

      ```bash
      mvn -DskipTests=true compile exec:java
      ```

      The `exec:java` run uses the dependencies declared in `pom.xml`, including `sqlite-jdbc`.

   2. Or add the JDBC JAR manually and run the program with it on the classpath:

      ```bash
      mkdir -p lib
      wget -O lib/sqlite-jdbc.jar https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.42.0.0/sqlite-jdbc-3.42.0.0.jar
      java -cp "bin:lib/sqlite-jdbc.jar" main.Main
      ```

   ## CSV → DB Migration helper

   If you've already used the CSV fallback and now want to import CSV records into `ums.db`, I can add a one-shot migration utility that:

   - Reads `data/*.csv` and inserts rows into the corresponding SQL tables (`STUDENTS`, `TEACHERS`, `COURSES`, `ENROLLMENTS`).
   - Optionally backs up the CSVs before importing.

   If you want this, tell me and I will implement the migration command (or add a small class `utilities/MigrateCsvToDb.java`).

   ## Troubleshooting

   - "No suitable driver found for jdbc:sqlite:ums.db": add SQLite JDBC to the classpath or run via Maven as shown above.
   - File permissions: ensure the process can create/write `ums.db` and files under `./data/`.
   - Wrong Java version: compile with matching `--release` or run with the correct JDK.

   ## Development notes & next steps

   - Add the optional CSV → DB migration tool (I can add it).
   - Add unit tests (JUnit) for `UniversityService` and `DatabaseService`.
   - Consider switching CSV fallback to JSON for more robust data representation.

   ## Contributing

   Fork, create a branch, implement, and open a PR. For dependency-managed builds prefer Maven (`pom.xml` is included).

   ## Contact

   Open issues or PRs at `https://github.com/aminul01-g/University-Management-System`.
