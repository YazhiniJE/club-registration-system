
package com.example.clubmanagement.model;

// Represents a student's registration for an event
public class EventRegistration {

    private int studentId;
    private int eventId;

    // Default constructor
    public EventRegistration() {
    }

    // Parameterized constructor
    public EventRegistration(int studentId, int eventId) {
        this.studentId = studentId;
        this.eventId = eventId;
    }

    // Getter and setter for student ID
    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    // Getter and setter for event ID
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
}
