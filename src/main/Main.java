package main;

import courses.Department;
import utilities.DataModel;
import utilities.DatabaseService;
import utilities.UniversityService;
import java.util.Scanner;
import people.Person;

public class Main {
    
    private static UniversityService service;
    private static DatabaseService dbService;
    private static Scanner scanner;

    public static void main(String[] args) {
        try {
            dbService = new DatabaseService();
            DataModel model = new DataModel();
            service = new UniversityService(model, dbService);
            scanner = new Scanner(System.in);

            service.loadDataFromDatabase();
        } catch (Exception e) {
            System.err.println("Failed to initialize the system: " + e.getMessage());
            System.err.println("Please ensure you have proper permissions and disk space.");
            System.exit(1);
        }

        System.out.println("--- " + Person.UNIVERSITY_NAME + " (UMS) v1.3 [SQL Edition] ---");
        System.out.println("Type 'help' for a list of commands. Data saves automatically.");

        runCli();
    }

    private static void runCli() {
        boolean running = true;
        while (running) {
            System.out.println("\n=== University Management System Menu ===");
            System.out.println("1. Load Demo Data");
            System.out.println("2. Student Operations");
            System.out.println("3. Teacher Operations");
            System.out.println("4. Course Operations");
            System.out.println("5. View Lists");
            System.out.println("0. Exit");
            System.out.print("\nEnter your choice (0-5): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 0:
                        running = false;
                        break;
                    case 1:
                        service.loadDemoData();
                        break;
                    case 2:
                        showStudentMenu();
                        break;
                    case 3:
                        showTeacherMenu();
                        break;
                    case 4:
                        showCourseMenu();
                        break;
                    case 5:
                        showListMenu();
                        break;
                    default:
                        System.err.println("Invalid choice. Please enter a number between 0 and 5.");
                }
            } catch (NumberFormatException e) {
                System.err.println("Please enter a valid number.");
            } catch (Exception e) {
                System.err.println("An error occurred: " + e.getMessage());
            }
            
            if (running) {
                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();
            }
        }
        scanner.close();
        System.out.println("System shut down.");
    }

    private static void showStudentMenu() {
        while (true) {
            System.out.println("\n=== Student Operations ===");
            System.out.println("1. Add New Student");
            System.out.println("2. Enroll Student in Course");
            System.out.println("0. Return to Main Menu");
            System.out.print("\nEnter your choice (0-2): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    return;
                }

                switch (choice) {
                    case 1:
                        System.out.print("Enter Student ID: ");
                        String id = scanner.nextLine().trim();
                        System.out.print("Enter Student Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Enter Major: ");
                        String major = scanner.nextLine().trim();
                        boolean added = service.addStudent(id, name, major);
                        if (added) System.out.println("Student added successfully!");
                        break;
                    case 2:
                        System.out.print("Enter Student ID: ");
                        String studentId = scanner.nextLine().trim();
                        System.out.println("\nAvailable Courses:");
                        service.listCourses();
                        System.out.print("\nEnter Course ID (or 0 to cancel): ");
                        String courseId = scanner.nextLine().trim();
                        if (!courseId.equals("0")) {
                            boolean ok = service.enrollStudent(studentId, courseId);
                            if (ok) System.out.println("Student enrolled successfully!");
                        }
                        break;
                    default:
                        System.err.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void showTeacherMenu() {
        while (true) {
            System.out.println("\n=== Teacher Operations ===");
            System.out.println("1. Add New Teacher");
            System.out.println("2. Assign Teacher to Course");
            System.out.println("0. Return to Main Menu");
            System.out.println("\nAvailable Departments:");
            System.out.println("COMPUTER_SCIENCE, BUSINESS_ADMINISTRATION, ELECTRICAL_ENGINEERING,");
            System.out.println("ARTS_AND_HUMANITIES, PHYSICS");
            System.out.print("\nEnter your choice (0-2): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    return;
                }

                switch (choice) {
                    case 1:
                        System.out.print("Enter Teacher ID: ");
                        String id = scanner.nextLine().trim();
                        System.out.print("Enter Teacher Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Enter Department Code (from list above): ");
                        String dept = scanner.nextLine().trim().toUpperCase();
                        System.out.print("Enter Subject: ");
                        String subject = scanner.nextLine().trim();
                        boolean tAdded = service.addTeacher(id, name, Department.valueOf(dept), subject);
                        if (tAdded) System.out.println("Teacher added successfully!");
                        break;
                    case 2:
                        System.out.print("Enter Teacher ID: ");
                        String teacherId = scanner.nextLine().trim();
                        System.out.println("\nAvailable Courses:");
                        service.listCourses();
                        System.out.print("\nEnter Course ID (or 0 to cancel): ");
                        String courseId = scanner.nextLine().trim();
                        if (!courseId.equals("0")) {
                            boolean assigned = service.assignTeacher(teacherId, courseId);
                            if (assigned) System.out.println("Teacher assigned successfully!");
                        }
                        break;
                    default:
                        System.err.println("Invalid choice.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Invalid Department code. Please use one from the list above.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void showCourseMenu() {
        while (true) {
            System.out.println("\n=== Course Operations ===");
            System.out.println("1. Add New Course");
            System.out.println("2. View All Courses");
            System.out.println("0. Return to Main Menu");
            System.out.println("\nAvailable Departments:");
            System.out.println("COMPUTER_SCIENCE, BUSINESS_ADMINISTRATION, ELECTRICAL_ENGINEERING,");
            System.out.println("ARTS_AND_HUMANITIES, PHYSICS");
            System.out.print("\nEnter your choice (0-2): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    return;
                }

                switch (choice) {
                    case 1:
                        System.out.print("Enter Course ID: ");
                        String id = scanner.nextLine().trim();
                        System.out.print("Enter Course Name: ");
                        String name = scanner.nextLine().trim();
                        System.out.print("Enter Department Code (from list above): ");
                        String dept = scanner.nextLine().trim().toUpperCase();
                        boolean cAdded = service.addCourse(id, name, Department.valueOf(dept));
                        if (cAdded) System.out.println("Course added successfully!");
                        break;
                    case 2:
                        service.listCourses();
                        break;
                    default:
                        System.err.println("Invalid choice.");
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Error: Invalid Department code. Please use one from the list above.");
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

    private static void showListMenu() {
        while (true) {
            System.out.println("\n=== View Lists ===");
            System.out.println("1. List all Students");
            System.out.println("2. List all Teachers");
            System.out.println("3. List all Courses");
            System.out.println("0. Return to Main Menu");
            System.out.print("\nEnter your choice (0-3): ");

            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                if (choice == 0) {
                    return;
                }

                switch (choice) {
                    case 1:
                        service.listStudents();
                        break;
                    case 2:
                        service.listTeachers();
                        break;
                    case 3:
                        service.listCourses();
                        break;
                    default:
                        System.err.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
            System.out.println("\nPress Enter to continue...");
            scanner.nextLine();
        }
    }

}