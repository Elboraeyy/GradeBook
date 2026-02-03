package com.example.gradebook.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.gradebook.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TeacherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: Teacher): Long

    @Query("SELECT * FROM teachers WHERE name = :name LIMIT 1")
    suspend fun getTeacherByName(name: String): Teacher?
}

@Dao
interface ClassroomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClassroom(classroom: Classroom): Long

    @Query("SELECT * FROM classrooms WHERE teacherId = :teacherId")
    fun getClassroomsForTeacher(teacherId: Int): Flow<List<Classroom>>

    @Query("SELECT * FROM classrooms WHERE id = :id")
    suspend fun getClassroomById(id: Int): Classroom?
}

@Dao
interface StudentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Query("SELECT * FROM students WHERE classroomId = :classroomId ORDER BY CAST(seatNumber AS INTEGER) ASC, name ASC")
    fun getStudentsByClassroom(classroomId: Int): Flow<List<Student>>
    
    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: Int): Student?
}

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendances(attendanceList: List<Attendance>)

    @Query("SELECT * FROM attendance WHERE classroomId = :classroomId AND date = :date")
    fun getAttendanceForClassAndDate(classroomId: Int, date: Long): Flow<List<Attendance>>

    @Query("SELECT * FROM attendance WHERE classroomId = :classroomId")
    fun getAllAttendance(classroomId: Int): Flow<List<Attendance>>
}

@Dao
interface GradeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrade(grade: GradeRecord)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGrades(grades: List<GradeRecord>)

    @Query("SELECT * FROM grades WHERE classroomId = :classroomId AND examName = :examName")
    fun getGradesForExam(classroomId: Int, examName: String): Flow<List<GradeRecord>>

    @Query("SELECT * FROM grades WHERE studentId = :studentId")
    fun getGradesForStudent(studentId: Int): Flow<List<GradeRecord>>

    @Query("SELECT * FROM grades WHERE classroomId = :classroomId")
    fun getAllGrades(classroomId: Int): Flow<List<GradeRecord>>
}
