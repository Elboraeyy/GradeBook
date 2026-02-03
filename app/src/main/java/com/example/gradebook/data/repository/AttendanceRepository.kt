package com.example.gradebook.data.repository

import com.example.gradebook.data.local.dao.AttendanceDao
import com.example.gradebook.data.local.entities.Attendance
import com.example.gradebook.data.local.entities.AttendanceStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao
) {
    suspend fun saveAttendance(attendanceList: List<Attendance>) {
        attendanceDao.insertAttendances(attendanceList)
    }

    fun getAttendanceForDate(classroomId: Int, date: Long): Flow<List<Attendance>> {
        return attendanceDao.getAttendanceForClassAndDate(classroomId, date)
    }

    fun getAllAttendance(classroomId: Int): Flow<List<Attendance>> {
        return attendanceDao.getAllAttendance(classroomId)
    }
}
