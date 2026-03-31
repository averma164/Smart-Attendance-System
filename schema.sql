-- Smart Attendance Management System - Database Schema
-- Run this file in MySQL before starting the application

CREATE DATABASE IF NOT EXISTS smart_attendance;
USE smart_attendance;

-- Users table for login authentication
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL
);

-- Students table
CREATE TABLE IF NOT EXISTS students (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    roll_no VARCHAR(20) NOT NULL UNIQUE
);

-- Attendance table
CREATE TABLE IF NOT EXISTS attendance (
    id INT AUTO_INCREMENT PRIMARY KEY,
    student_id INT NOT NULL,
    date DATE NOT NULL,
    status ENUM('Present', 'Absent') NOT NULL DEFAULT 'Absent',
    FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    UNIQUE KEY unique_attendance (student_id, date)
);

-- Default admin user (username: admin, password: admin123)
INSERT IGNORE INTO users (username, password) VALUES ('admin', 'admin123');

-- Sample student data
INSERT IGNORE INTO students (name, roll_no) VALUES
    ('Alice Johnson', 'CS001'),
    ('Bob Smith', 'CS002'),
    ('Carol Davis', 'CS003');
