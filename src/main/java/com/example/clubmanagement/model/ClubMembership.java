
package com.example.clubmanagement.model;

// Represents a student joining a club
public class ClubMembership {

    private int studentId;
    private int clubId;

    // Default constructor
    public ClubMembership() {
    }

    // Parameterized constructor
    public ClubMembership(int studentId, int clubId) {
        this.studentId = studentId;
        this.clubId = clubId;
    }

    // Getter for student ID
    public int getStudentId() {
        return studentId;
    }

    // Setter for student ID
    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Getter for club ID
    public int getClubId() {
        return clubId;
    }

    // Setter for club ID
    public void setClubId(int clubId) {
        this.clubId = clubId;
    }
}
