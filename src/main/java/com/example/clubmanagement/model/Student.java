
package com.example.clubmanagement.model;

// Student inherits from the User class
public class Student extends User {

    private String department;

    // Constructor
    public Student(int id, String name, String department) {
        super(id, name);
        this.department = department;
    }

    // Getter for department
    public String getDepartment() {
        return department;
    }

    // Setter for department
    public void setDepartment(String department) {
        this.department = department;
    }

    // Overriding the abstract method from User
    @Override
    public String getRole() {
        return "Student";
    }
}
