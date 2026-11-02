
package com.example.clubmanagement.model;

import java.time.LocalDate;
import java.time.LocalTime;

// Represents an event organized by a club
public class Event {

    private int eventId;
    private int clubId;
    private String eventName;
    private String description;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private String venue;

    // Default constructor
    public Event() {
    }

    // Parameterized constructor
    public Event(int eventId, int clubId, String eventName,
                 String description, LocalDate eventDate,
                 LocalTime eventTime, String venue) {

        this.eventId = eventId;
        this.clubId = clubId;
        this.eventName = eventName;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.venue = venue;
    }

    // Getters and setters
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public LocalTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
}

