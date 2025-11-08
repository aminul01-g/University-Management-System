package people;

import courses.Department;


public class Teacher extends Person {

    private Department department;
    private String subject;


    public Teacher(String id, String name, Department department, String subject) {
        super(id, name);
        this.department = department;
        this.subject = subject;
    }


    public Teacher(String id, String name, Department department) {
        super(id, name);
        this.department = department;
        this.subject = "General";
    }


    @Override
    public String role() {
        return "Teacher";
    }

    public Department getDepartment() { return department; }
    public String getSubject() { return subject; }
    

    @Override
    public void printDetails() {
        super.printDetails();
        System.out.println("  Department: " + department.name());
        System.out.println("  Specialty: " + subject);
    }
}