
package com.example.clubmanagement.controller;

import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.sql.PreparedStatement;
import java.sql.Statement;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

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
        return jdbc.queryForList(
            "SELECT * FROM events ORDER BY event_date, event_time"
        );
    }

    // 2. View one event
    @GetMapping("/{id}")
    public ResponseEntity<?> getEvent(@PathVariable int id) {

        List<Map<String, Object>> events = jdbc.queryForList(
            "SELECT * FROM events WHERE event_id = ?", id
        );

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

        return jdbc.queryForList(
            "SELECT * FROM events WHERE club_id = ? " +
            "ORDER BY event_date, event_time",
            clubId
        );
    }

    // Check whether student is the club head
    private boolean isClubHead(int clubId, int studentId) {

        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM clubs " +
            "WHERE club_id = ? AND head_student_id = ?",
            Integer.class,
            clubId,
            studentId
        );

        return count != null && count > 0;
    }

    // 4. Create an event
    @PostMapping
    public ResponseEntity<String> addEvent(
            @RequestBody Map<String, Object> data,
            @RequestParam int headStudentId) {

        String clubIdText = String.valueOf(
            data.getOrDefault("clubId", "")
        );

        String eventName = String.valueOf(
            data.getOrDefault("eventName", "")
        ).trim();

        String description = String.valueOf(
            data.getOrDefault("description", "")
        ).trim();

        String dateText = String.valueOf(
            data.getOrDefault("eventDate", "")
        );

        String timeText = String.valueOf(
            data.getOrDefault("eventTime", "")
        );

        String venue = String.valueOf(
            data.getOrDefault("venue", "")
        ).trim();

        if (clubIdText.isBlank() ||
            eventName.isBlank() ||
            dateText.isBlank() ||
            timeText.isBlank() ||
            venue.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please fill in all event details!");
        }

        int clubId;
        LocalDate eventDate;
        LocalTime eventTime;

        try {
            clubId = Integer.parseInt(clubIdText);
            eventDate = LocalDate.parse(dateText);
            eventTime = LocalTime.parse(timeText);

        } catch (NumberFormatException |
                 DateTimeParseException e) {

            return ResponseEntity.badRequest()
                    .body("Invalid Club ID, date or time!");
        }

        if (clubId <= 0) {
            return ResponseEntity.badRequest()
                    .body("Club ID must be positive!");
        }

        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM clubs WHERE club_id = ?",
            Integer.class,
            clubId
        );

        if (count == null || count == 0) {
            return ResponseEntity.badRequest()
                    .body("Club does not exist!");
        }

        if (!isClubHead(clubId, headStudentId)) {
            return ResponseEntity.status(403)
                    .body("Only the club head can create events!");
        }

        String sql = "INSERT INTO events " +
            "(club_id, event_name, description, " +
            "event_date, event_time, venue) " +
            "VALUES (?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {

            PreparedStatement ps = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            );

            ps.setInt(1, clubId);
            ps.setString(2, eventName);
            ps.setString(3, description);
            ps.setDate(4, java.sql.Date.valueOf(eventDate));
            ps.setTime(5, java.sql.Time.valueOf(eventTime));
            ps.setString(6, venue);

            return ps;

        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            return ResponseEntity.internalServerError()
                    .body("Event saved, but Event ID could not be retrieved.");
        }

        return ResponseEntity.ok(
            "Event created successfully! Event ID: " +
            generatedId.intValue()
        );
    }

    // 5. Update event date, time or venue
    @PutMapping("/{id}")
    public ResponseEntity<String> updateEvent(
            @PathVariable int id,
            @RequestParam int headStudentId,
            @RequestBody Map<String, String> data) {

        List<Map<String, Object>> events = jdbc.queryForList(
            "SELECT club_id FROM events WHERE event_id = ?", id
        );

        if (events.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        int clubId = ((Number) events.get(0)
                .get("club_id")).intValue();

        if (!isClubHead(clubId, headStudentId)) {
            return ResponseEntity.status(403)
                    .body("Only the club head can update this event!");
        }

        String newDate = data.get("eventDate");
        String newTime = data.get("eventTime");
        String newVenue = data.get("venue");

        if ((newDate == null || newDate.isBlank()) &&
            (newTime == null || newTime.isBlank()) &&
            (newVenue == null || newVenue.isBlank())) {

            return ResponseEntity.badRequest()
                    .body("Enter a date, time or venue to update!");
        }

        try {
            LocalDate date = null;
            LocalTime time = null;

            if (newDate != null && !newDate.isBlank()) {
                date = LocalDate.parse(newDate);
            }

            if (newTime != null && !newTime.isBlank()) {
                time = LocalTime.parse(newTime);
            }

            // All validation is done before updating
            if (date != null) {
                jdbc.update(
                    "UPDATE events SET event_date = ? WHERE event_id = ?",
                    java.sql.Date.valueOf(date),
                    id
                );
            }

            if (time != null) {
                jdbc.update(
                    "UPDATE events SET event_time = ? WHERE event_id = ?",
                    java.sql.Time.valueOf(time),
                    id
                );
            }

            if (newVenue != null && !newVenue.isBlank()) {
                jdbc.update(
                    "UPDATE events SET venue = ? WHERE event_id = ?",
                    newVenue.trim(),
                    id
                );
            }

            return ResponseEntity.ok(
                "Event updated successfully! Event ID: " + id
            );

        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body("Invalid date or time format!");
        }
    }

    // 6. Delete event
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEvent(
            @PathVariable int id,
            @RequestParam int headStudentId) {

        List<Map<String, Object>> events = jdbc.queryForList(
            "SELECT club_id FROM events WHERE event_id = ?", id
        );

        if (events.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        int clubId = ((Number) events.get(0)
                .get("club_id")).intValue();

        if (!isClubHead(clubId, headStudentId)) {
            return ResponseEntity.status(403)
                    .body("Only the club head can delete events!");
        }

        jdbc.update(
            "DELETE FROM event_registrations WHERE event_id = ?", id
        );

        jdbc.update(
            "DELETE FROM events WHERE event_id = ?", id
        );

        return ResponseEntity.ok(
            "Event deleted successfully!"
        );
    }

    // 7. Register student for an event
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
            Integer.class,
            id
        );

        if (eventCount == null || eventCount == 0) {
            return ResponseEntity.status(404)
                    .body("Event not found!");
        }

        Integer studentCount = jdbc.queryForObject(
            "SELECT COUNT(*) FROM students WHERE student_id = ?",
            Integer.class,
            registration.getStudentId()
        );

        if (studentCount == null || studentCount == 0) {
            return ResponseEntity.status(404)
                    .body("Student not found!");
        }

        try {
            jdbc.update(
                "INSERT INTO event_registrations " +
                "(student_id, event_id) VALUES (?, ?)",
                registration.getStudentId(),
                id
            );

            return ResponseEntity.ok(
                "Event registration successful!"
            );

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest()
                    .body("Student is already registered for this event!");
        }
    }

    // 8. View participants
    @GetMapping("/{id}/registrations")
    public List<Map<String, Object>> getEventRegistrations(
            @PathVariable int id) {

        String sql = "SELECT s.student_id, s.name, s.department " +
            "FROM students s " +
            "JOIN event_registrations er " +
            "ON s.student_id = er.student_id " +
            "WHERE er.event_id = ?";

        return jdbc.queryForList(sql, id);
    }

    // 9. View events registered by a student
    @GetMapping("/student/{studentId}")
    public List<Map<String, Object>> getStudentEvents(
            @PathVariable int studentId) {

        String sql = "SELECT e.* FROM events e " +
            "JOIN event_registrations er " +
            "ON e.event_id = er.event_id " +
            "WHERE er.student_id = ? " +
            "ORDER BY e.event_date, e.event_time";

        return jdbc.queryForList(sql, studentId);
    }
}
