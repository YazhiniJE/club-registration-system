
package com.example.clubmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.clubmanagement.model.ClubMembership;

@RestController
@RequestMapping("/clubs")
public class ClubController {

    private final JdbcTemplate jdbc;

    // Constructor
    public ClubController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 1. View all clubs
    @GetMapping
    public List<Map<String, Object>> getAllClubs() {

        String sql = "SELECT * FROM clubs";

        return jdbc.queryForList(sql);
    }

    // 2. View one club
    @GetMapping("/{id}")
    public ResponseEntity<?> getClub(@PathVariable int id) {

        String sql = "SELECT * FROM clubs WHERE club_id = ?";

        List<Map<String, Object>> result =
                jdbc.queryForList(sql, id);

        if (result.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Club not found!");
        }

        return ResponseEntity.ok(result.get(0));
    }

    // 3. Add a new club (admin function)
    @PostMapping
    public ResponseEntity<String> addClub(
            @RequestBody Map<String, String> data) {

        String clubName = data.get("clubName");
        String description = data.get("description");

        if (clubName == null || clubName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body("Club name cannot be empty!");
        }

        String sql = "INSERT INTO clubs "
                + "(club_name, description) VALUES (?, ?)";

        jdbc.update(sql, clubName.trim(), description);

        return ResponseEntity.ok("Club added successfully!");
    }

    // 4. Join a club
    @PostMapping("/{id}/join")
    public ResponseEntity<String> joinClub(
            @PathVariable int id,
            @RequestBody ClubMembership membership) {

        int studentId = membership.getStudentId();

        if (studentId <= 0) {
            return ResponseEntity.badRequest()
                    .body("Invalid student ID!");
        }

        Integer studentCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE student_id = ?",
                Integer.class, studentId);

        if (studentCount == null || studentCount == 0) {
            return ResponseEntity.status(404)
                    .body("Student not found!");
        }

        Integer clubCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM clubs WHERE club_id = ?",
                Integer.class, id);

        if (clubCount == null || clubCount == 0) {
            return ResponseEntity.status(404)
                    .body("Club not found!");
        }

        String sql = "INSERT INTO club_memberships "
                + "(student_id, club_id) VALUES (?, ?)";

        try {
            jdbc.update(sql, studentId, id);

            return ResponseEntity.ok(
                    "Joined club successfully!");

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body("Student is already a member of this club!");
        }
    }

    // 5. Leave a club
    @DeleteMapping("/{id}/leave/{studentId}")
    public ResponseEntity<String> leaveClub(
            @PathVariable int id,
            @PathVariable int studentId) {

        String sql = "DELETE FROM club_memberships "
                + "WHERE student_id = ? AND club_id = ?";

        int rows = jdbc.update(sql, studentId, id);

        if (rows == 0) {
            return ResponseEntity.status(404)
                    .body("Club membership not found!");
        }

        return ResponseEntity.ok("Left club successfully!");
    }

    // 6. View club members
    @GetMapping("/{id}/members")
    public List<Map<String, Object>> getClubMembers(
            @PathVariable int id) {

        String sql = "SELECT s.student_id, s.name, s.department "
                + "FROM students s "
                + "JOIN club_memberships cm "
                + "ON s.student_id = cm.student_id "
                + "WHERE cm.club_id = ?";

        return jdbc.queryForList(sql, id);
    }

    // 7. View clubs joined by a student
    @GetMapping("/student/{studentId}")
    public List<Map<String, Object>> getStudentClubs(
            @PathVariable int studentId) {

        String sql = "SELECT c.* FROM clubs c "
                + "JOIN club_memberships cm "
                + "ON c.club_id = cm.club_id "
                + "WHERE cm.student_id = ?";

        return jdbc.queryForList(sql, studentId);
    }

    // 8. Assign club head (admin function)
    @PutMapping("/{id}/head/{studentId}")
    public ResponseEntity<String> assignClubHead(
            @PathVariable int id,
            @PathVariable int studentId) {

        Integer studentCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE student_id = ?",
                Integer.class, studentId);

        if (studentCount == null || studentCount == 0) {
            return ResponseEntity.status(404)
                    .body("Student not found!");
        }

        String sql = "UPDATE clubs SET head_student_id = ? "
                + "WHERE club_id = ?";

        int rows = jdbc.update(sql, studentId, id);

        if (rows == 0) {
            return ResponseEntity.status(404)
                    .body("Club not found!");
        }

        return ResponseEntity.ok(
                "Club head assigned successfully!");
    }
}
