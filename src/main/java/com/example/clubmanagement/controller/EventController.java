
package com.example.clubmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import com.example.clubmanagement.model.Event;
import com.example.clubmanagement.model.EventRegistration;

@RestController
@RequestMapping("/events")
public class EventController {

    private final JdbcTemplate jdbc;

    public EventController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 1. View all events
    @GetMapping
    public List<Map<String, Object>> getAllEvents() {

        String sql = "SELECT * FROM events ORDER BY event_date, event_time";

        return jdbc.queryForList(sql);
    }

    // 2. View one event
    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable int id) {

        String sql = "SELECT * FROM events WHERE event_id = ?";

        List<Map<String, Object>> events =
                jdbc.queryForList(sql, id);

        if (events.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        return ResponseEntity.ok(events.get(0));
    }

    // 3. View events belonging to a club
    @GetMapping("/club/{clubId}")
    public List<Map<String, Object>> getClubEvents(
            @PathVariable int clubId) {

        String sql = "SELECT * FROM events WHERE club_id = ? "
                + "ORDER BY event_date, event_time";

        return jdbc.queryForList(sql, clubId);
    }

    // Check whether student is the head of the club
    private boolean isClubHead(int clubId, int studentId) {

        String sql = "SELECT COUNT(*) FROM clubs "
                + "WHERE club_id = ? AND head_student_id = ?";

        Integer count = jdbc.queryForObject(
                sql, Integer.class, clubId, studentId);

        return count != null && count > 0;
    }

    // 4. Create an event (club head only)
    @PostMapping
    public ResponseEntity<String> addEvent(
            @RequestBody Event event,
            @RequestParam int headStudentId) {

        if (!isClubHead(event.getClubId(), headStudentId)) {
            return ResponseEntity.status(403)
                    .body("Only the club head can create events!");
        }

        if (event.getEventName() == null
                || event.getEventName().isBlank()
                || event.getEventDate() == null
                || event.getEventTime() == null
                || event.getVenue() == null
                || event.getVenue().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please fill in all required event details!");
        }

        String sql = "INSERT INTO events "
                + "(club_id, event_name, description, "
                + "event_date, event_time, venue) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        jdbc.update(sql,
                event.getClubId(),
                event.getEventName().trim(),
                event.getDescription(),
                event.getEventDate(),
                event.getEventTime(),
                event.getVenue().trim());

        return ResponseEntity.ok("Event created successfully!");
    }

    // 5. Update an event (club head only)
    @PutMapping("/{id}")
    public ResponseEntity<String> updateEvent(
            @PathVariable int id,
            @RequestBody Event event,
            @RequestParam int headStudentId) {

        String findSql = "SELECT club_id FROM events WHERE event_id = ?";

        List<Map<String, Object>> result =
                jdbc.queryForList(findSql, id);

        if (result.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        int clubId = ((Number) result.get(0).get("club_id"))
                .intValue();

        if (!isClubHead(clubId, headStudentId)) {
            return ResponseEntity.status(403)
                    .body("Only the club head can update events!");
        }

        if (event.getEventName() == null
                || event.getEventName().isBlank()
                || event.getEventDate() == null
                || event.getEventTime() == null
                || event.getVenue() == null
                || event.getVenue().isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please fill in all required event details!");
        }

        String sql = "UPDATE events SET event_name = ?, "
                + "description = ?, event_date = ?, "
                + "event_time = ?, venue = ? "
                + "WHERE event_id = ? AND club_id = ?";

        jdbc.update(sql,
                event.getEventName().trim(),
                event.getDescription(),
                event.getEventDate(),
                event.getEventTime(),
                event.getVenue().trim(),
                id,
                clubId);

        return ResponseEntity.ok("Event updated successfully!");
    }

    // 6. Delete an event (club head only)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(
            @PathVariable int id,
            @RequestParam int headStudentId) {

        String findSql = "SELECT club_id FROM events WHERE event_id = ?";

        List<Map<String, Object>> result =
                jdbc.queryForList(findSql, id);

        if (result.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        int clubId = ((Number) result.get(0).get("club_id"))
                .intValue();

        if (!isClubHead(clubId, headStudentId)) {
            return ResponseEntity.status(403)
                    .body("Only the club head can delete events!");
        }

        // Remove registrations before deleting the event
        jdbc.update(
                "DELETE FROM event_registrations WHERE event_id = ?", id);

        jdbc.update(
                "DELETE FROM events WHERE event_id = ?", id);

        return ResponseEntity.ok("Event deleted successfully!");
    }

    // 7. Register a student for an event
    @PostMapping("/{id}/register")
    public ResponseEntity<String> registerForEvent(
            @PathVariable int id,
            @RequestBody EventRegistration registration) {

        if (registration.getStudentId() <= 0) {
            return ResponseEntity.badRequest()
                    .body("Invalid student ID!");
        }

        Integer eventCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM events WHERE event_id = ?",
                Integer.class, id);

        if (eventCount == null || eventCount == 0) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        Integer studentCount = jdbc.queryForObject(
                "SELECT COUNT(*) FROM students WHERE student_id = ?",
                Integer.class, registration.getStudentId());

        if (studentCount == null || studentCount == 0) {
            return ResponseEntity.status(404)
                    .body("Student not found!");
        }

        String sql = "INSERT INTO event_registrations "
                + "(student_id, event_id) VALUES (?, ?)";

        try {
            jdbc.update(sql, registration.getStudentId(), id);

            return ResponseEntity.ok(
                    "Event registration successful!");

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body("Student is already registered for this event!");
        }
    }

    // 8. View students registered for an event
    @GetMapping("/{id}/registrations")
    public List<Map<String, Object>> getEventRegistrations(
            @PathVariable int id) {

        String sql = "SELECT s.student_id, s.name, s.department "
                + "FROM students s "
                + "JOIN event_registrations er "
                + "ON s.student_id = er.student_id "
                + "WHERE er.event_id = ?";

        return jdbc.queryForList(sql, id);
    }

    // 9. View events registered by a student
    @GetMapping("/student/{studentId}")
    public List<Map<String, Object>> getStudentEvents(
            @PathVariable int studentId) {

        String sql = "SELECT e.* FROM events e "
                + "JOIN event_registrations er "
                + "ON e.event_id = er.event_id "
                + "WHERE er.student_id = ? "
                + "ORDER BY e.event_date, e.event_time";

        return jdbc.queryForList(sql, studentId);
    }
}
