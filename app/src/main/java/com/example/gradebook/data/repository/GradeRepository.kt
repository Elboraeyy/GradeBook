package com.example.gradebook.data.repository

import com.example.gradebook.data.local.dao.GradeDao
import com.example.gradebook.data.local.entities.GradeRecord
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GradeRepository @Inject constructor(
    private val gradeDao: GradeDao
) {
    suspend fun saveGrades(grades: List<GradeRecord>) {
        gradeDao.insertGrades(grades)
    }

    fun getGradesForExam(classroomId: Int, examName: String): Flow<List<GradeRecord>> {
        return gradeDao.getGradesForExam(classroomId, examName)
    }

    fun getAllGrades(classroomId: Int): Flow<List<GradeRecord>> {
        return gradeDao.getAllGrades(classroomId)
    }
}
