package com.example.gradebook.data.repository

import com.example.gradebook.data.local.dao.TeacherDao
import com.example.gradebook.data.local.entities.Teacher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeacherRepository @Inject constructor(
    private val teacherDao: TeacherDao
) {
    suspend fun registerTeacher(name: String, schoolName: String, passwordHash: String): Result<Teacher> {
        val existing = teacherDao.getTeacherByName(name)
        if (existing != null) {
            return Result.failure(Exception("Teacher account already exists"))
        }
        val teacher = Teacher(name = name, schoolName = schoolName, passwordHash = passwordHash)
        val id = teacherDao.insertTeacher(teacher)
        return Result.success(teacher.copy(id = id.toInt()))
    }

    suspend fun loginTeacher(name: String, passwordHash: String): Result<Teacher> {
        val teacher = teacherDao.getTeacherByName(name) ?: return Result.failure(Exception("Teacher not found"))
        if (teacher.passwordHash == passwordHash) {
            return Result.success(teacher)
        }
        return Result.failure(Exception("Invalid credentials"))
    }
}
