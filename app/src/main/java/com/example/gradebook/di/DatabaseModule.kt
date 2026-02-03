package com.example.gradebook.di

import android.content.Context
import androidx.room.Room
import com.example.gradebook.data.local.AppDatabase
import com.example.gradebook.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "gradebook.db"
        ).fallbackToDestructiveMigration() // For development simplicity
         .build()
    }

    @Provides
    fun provideTeacherDao(db: AppDatabase): TeacherDao = db.teacherDao()

    @Provides
    fun provideClassroomDao(db: AppDatabase): ClassroomDao = db.classroomDao()

    @Provides
    fun provideStudentDao(db: AppDatabase): StudentDao = db.studentDao()

    @Provides
    fun provideAttendanceDao(db: AppDatabase): AttendanceDao = db.attendanceDao()

    @Provides
    fun provideGradeDao(db: AppDatabase): GradeDao = db.gradeDao()
}
