package com.example.gradebook.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teachers")
data class Teacher(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val schoolName: String,
    val passwordHash: String // Simple hash or PIN
)

@Entity(tableName = "classrooms")
data class Classroom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val teacherId: Int, // Foreign Key
    val name: String, // e.g. "1st Prep - A"
    val gradeLevel: String, // e.g. "1st Year"
    val academicYear: String // e.g. "2025-2026"
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val classroomId: Int,
    val name: String,
    val seatNumber: String? = null,
    val nationalId: String? = null, // Optional for official records
    val notes: String? = null
)

@Entity(tableName = "attendance")
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val classroomId: Int,
    val date: Long, // Epoch millis
    val status: AttendanceStatus
)

enum class AttendanceStatus {
    PRESENT, ABSENT, LATE, EXCUSED
}

@Entity(tableName = "grades")
data class GradeRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val studentId: Int,
    val classroomId: Int,
    val subjectName: String, // e.g. "Math", "Geometry", "Algebra"
    val examName: String, // e.g. "Midterm", "Month 1", "Final"
    val score: Double,
    val maxScore: Double,
    val date: Long
)
