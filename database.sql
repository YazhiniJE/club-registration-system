
CREATE DATABASE IF NOT EXISTS club_management;

USE club_management;

-- Table 1: Students
CREATE TABLE IF NOT EXISTS students (
    student_id INT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(50) NOT NULL,
    CONSTRAINT chk_department
        CHECK (department IN ('IOT', 'CS', 'AI'))
);

-- Table 2: Clubs
CREATE TABLE IF NOT EXISTS clubs (
    club_id INT PRIMARY KEY AUTO_INCREMENT,
    club_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    head_student_id INT,
    FOREIGN KEY (head_student_id)
        REFERENCES students(student_id)
);

-- Table 3: Club Memberships
CREATE TABLE IF NOT EXISTS club_memberships (
    student_id INT,
    club_id INT,

    PRIMARY KEY (student_id, club_id),

    FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    FOREIGN KEY (club_id)
        REFERENCES clubs(club_id)
);

-- Table 4: Events
CREATE TABLE IF NOT EXISTS events (
    event_id INT PRIMARY KEY AUTO_INCREMENT,
    club_id INT NOT NULL,
    event_name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    event_date DATE NOT NULL,
    event_time TIME NOT NULL,
    venue VARCHAR(100) NOT NULL,

    FOREIGN KEY (club_id)
        REFERENCES clubs(club_id)
);

-- Table 5: Event Registrations
CREATE TABLE IF NOT EXISTS event_registrations (
    student_id INT,
    event_id INT,

    PRIMARY KEY (student_id, event_id),

    FOREIGN KEY (student_id)
        REFERENCES students(student_id),

    FOREIGN KEY (event_id)
        REFERENCES events(event_id)
);
