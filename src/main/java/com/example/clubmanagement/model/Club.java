
package com.example.clubmanagement.model;

// Represents a college club
public class Club {

    private int clubId;
    private String clubName;
    private String description;
    private Integer headStudentId;

    // Constructor
    public Club(int clubId, String clubName,
                String description, Integer headStudentId) {

        this.clubId = clubId;
        this.clubName = clubName;
        this.description = description;
        this.headStudentId = headStudentId;
    }

    // Getter and setter for Club ID
    public int getClubId() {
        return clubId;
    }

    public void setClubId(int clubId) {
        this.clubId = clubId;
    }

    // Getter and setter for Club Name
    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    // Getter and setter for Description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and setter for Club Head ID
    public Integer getHeadStudentId() {
        return headStudentId;
    }

    public void setHeadStudentId(Integer headStudentId) {
        this.headStudentId = headStudentId;
    }
}
