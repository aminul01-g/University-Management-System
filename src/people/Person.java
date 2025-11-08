package people;

import interfaces.Printable;
import java.util.Objects;

public abstract class Person implements Printable {

    protected String id;
    protected String name;

    public static final String UNIVERSITY_NAME = "University Management System";

    public Person(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() { return id; }
    public String getName() { return name; }

    public abstract String role();
    @Override
    public void printDetails() {
        System.out.println("ID: " + id + ", Name: " + name + ", Role: " + role());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id.equals(person.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}