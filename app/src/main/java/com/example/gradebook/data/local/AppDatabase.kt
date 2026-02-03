package com.example.gradebook.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.gradebook.data.local.dao.*
import com.example.gradebook.data.local.entities.*

@Database(
    entities = [
        Teacher::class,
        Classroom::class,
        Student::class,
        Attendance::class,
        GradeRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun teacherDao(): TeacherDao
    abstract fun classroomDao(): ClassroomDao
    abstract fun studentDao(): StudentDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun gradeDao(): GradeDao
}
