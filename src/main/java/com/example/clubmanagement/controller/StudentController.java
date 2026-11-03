
package com.example.clubmanagement.controller;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/students")
public class StudentController {

    private final JdbcTemplate jdbc;

    // Constructor
    public StudentController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    // 1. View all students
    @GetMapping
    public List<Map<String, Object>> getAllStudents() {

        String sql = "SELECT * FROM students";

        return jdbc.queryForList(sql);
    }

    // 2. View a student using student ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getStudent(@PathVariable int id) {

        String sql = "SELECT * FROM students WHERE student_id = ?";

        List<Map<String, Object>> result =
                jdbc.queryForList(sql, id);

        if (result.isEmpty()) {
            return ResponseEntity.status(404)
                    .body("Student not found!");
        }

        return ResponseEntity.ok(result.get(0));
    }

    // 3. Add a new student
    @PostMapping
    public ResponseEntity<String> addStudent(
            @RequestBody Map<String, String> data) {

        String idText = data.get("studentId");
        String name = data.get("name");
        String department = data.get("department");

        // Check for missing details
        if (idText == null || idText.isBlank()
                || name == null || name.isBlank()
                || department == null || department.isBlank()) {

            return ResponseEntity.badRequest()
                    .body("Please fill in all student details!");
        }

        
        String dept = department.trim().toUpperCase();

        if (!dept.equals("IOT") &&
            !dept.equals("CS") &&
            !dept.equals("AI")) {

            return ResponseEntity.badRequest()
                .body("Invalid department! Please check that the department belongs to IOT, AI, or CS.");
        }


        int id;

        // Check whether student ID is a valid number
        try {
            id = Integer.parseInt(idText);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body("Student ID must be a valid number!");
        }

        if (id <= 0) {
            return ResponseEntity.badRequest()
                    .body("Student ID must be positive!");
        }

        String sql = "INSERT INTO students " +
                "(student_id, name, department) VALUES (?, ?, ?)";

        try {
            jdbc.update(sql, id, name.trim(), dept);

            return ResponseEntity.ok("Student added successfully!");

        } 
        catch (DataIntegrityViolationException e) {

        // Check whether the student ID already exists
        Integer count = jdbc.queryForObject(
        "SELECT COUNT(*) FROM students WHERE student_id = ?",
        Integer.class,
        id
        );

        if (count != null && count > 0) {
            return ResponseEntity.badRequest()
                    .body("Student ID already exists!");
        }

        return ResponseEntity.badRequest()
                .body("Invalid student details! Please check that the department belongs to IOT, AI, or CS.");
        }

    }
}
