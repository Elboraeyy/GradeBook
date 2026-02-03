package com.example.gradebook.data.repository

import com.example.gradebook.data.local.dao.ClassroomDao
import com.example.gradebook.data.local.dao.StudentDao
import com.example.gradebook.data.local.entities.Classroom
import com.example.gradebook.data.local.entities.Student
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val classroomDao: ClassroomDao,
    private val studentDao: StudentDao
) {
    fun getClassroomsForTeacher(teacherId: Int): Flow<List<Classroom>> {
        return classroomDao.getClassroomsForTeacher(teacherId)
    }

    suspend fun addClassroom(teacherId: Int, name: String, gradeLevel: String, academicYear: String): Result<Int> {
        val classroom = Classroom(
            teacherId = teacherId,
            name = name,
            gradeLevel = gradeLevel,
            academicYear = academicYear
        )
        val id = classroomDao.insertClassroom(classroom)
        return Result.success(id.toInt())
    }

    suspend fun getClassroom(id: Int): Classroom? {
        return classroomDao.getClassroomById(id)
    }

    suspend fun addStudentToClass(classroomId: Int, name: String, seatNumber: String?, notes: String? = null) {
        val student = Student(
            classroomId = classroomId,
            name = name,
            seatNumber = seatNumber,
            notes = notes
        )
        studentDao.insertStudent(student)
    }
    
    fun getStudents(classroomId: Int): Flow<List<Student>> {
        return studentDao.getStudentsByClassroom(classroomId)
    }
}
