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

## Project Structure

```
UMS/
├── src/
│   ├── main/
│   │   └── Main.java           # Entry point and CLI interface
│   ├── courses/
│   │   └── Course.java         # Course management
│   ├── people/
│   │   ├── Student.java        # Student entity
│   │   └── Teacher.java        # Teacher entity
│   └── utilities/
│       ├── DatabaseService.java # Database operations
│       ├── DataModel.java      # In-memory data storage
│       └── UniversityService.java # Business logic
└── bin/                        # Compiled classes
```

## Setup and Running

1. Compile the project:
```bash
mkdir -p bin
javac -d bin $(find src -name '*.java')
```

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