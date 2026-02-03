package com.example.gradebook.data.utils

import android.content.Context
import android.net.Uri
import com.example.gradebook.data.local.entities.Attendance
import com.example.gradebook.data.local.entities.GradeRecord
import com.example.gradebook.data.local.entities.Student
import dagger.hilt.android.qualifiers.ApplicationContext
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStream
import javax.inject.Inject

class ExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportClassReport(
        outputStream: OutputStream,
        className: String,
        students: List<Student>,
        attendance: List<Attendance>,
        grades: List<GradeRecord>
    ): Result<Unit> {
        return try {
            val workbook = XSSFWorkbook()
            
            // Sheet 1: Students & Grades
            val sheet = workbook.createSheet("Grades & Attendance")
            val headerRow = sheet.createRow(0)
            headerRow.createCell(0).setCellValue("Name")
            headerRow.createCell(1).setCellValue("Seat Number")
            headerRow.createCell(2).setCellValue("Total Absences")
            
            // Dynamic Headers for Exams
            val uniqueExams = grades.map { it.examName }.distinct().sorted()
            uniqueExams.forEachIndexed { index, exam ->
                headerRow.createCell(3 + index).setCellValue(exam)
            }

            // Fill Data
            students.forEachIndexed { index, student ->
                val row = sheet.createRow(1 + index)
                row.createCell(0).setCellValue(student.name)
                row.createCell(1).setCellValue(student.seatNumber ?: "")
                
                // Calculate Absences
                val absentCount = attendance.count { it.studentId == student.id && it.status.name == "ABSENT" }
                row.createCell(2).setCellValue(absentCount.toDouble())
                
                // Fill Grades
                uniqueExams.forEachIndexed { examIndex, exam ->
                    val grade = grades.find { it.studentId == student.id && it.examName == exam }
                    val cell = row.createCell(3 + examIndex)
                    if (grade != null) {
                        cell.setCellValue(grade.score)
                    } else {
                        cell.setCellValue("-")
                    }
                }
            }

            workbook.write(outputStream)
            workbook.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportClassReportPdf(
        outputStream: OutputStream,
        className: String,
        students: List<Student>,
        attendance: List<Attendance>,
        grades: List<GradeRecord>
    ): Result<Unit> {
        return try {
            val pdfDocument = android.graphics.pdf.PdfDocument()
            val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = android.graphics.Paint()
            val titlePaint = android.graphics.Paint().apply { 
                textSize = 24f 
                isFakeBoldText = true 
                color = android.graphics.Color.BLACK
            }
            val textPaint = android.graphics.Paint().apply { textSize = 12f }
            
            var y = 50f
            canvas.drawText("Class Report: $className", 50f, y, titlePaint)
            y += 40f
            
            val uniqueExams = grades.map { it.examName }.distinct().sorted()
            
            // Header
            var x = 50f
            canvas.drawText("Name", x, y, textPaint)
            x += 150f
            canvas.drawText("Absences", x, y, textPaint)
            x += 80f
            uniqueExams.forEach { exam ->
                canvas.drawText(exam, x, y, textPaint)
                x += 80f
            }
            y += 20f
            canvas.drawLine(40f, y, 550f, y, paint)
            y += 30f

            students.forEach { student ->
                if (y > 800) {
                    // Simple pagination handling: just cut off for this MVP or start new page (requires loop restructure)
                    // For MVP simplicity, we just print what fits.
                    return@forEach
                }
                x = 50f
                canvas.drawText(student.name, x, y, textPaint)
                x += 150f
                
                val absentCount = attendance.count { it.studentId == student.id && it.status.name == "ABSENT" }
                canvas.drawText(absentCount.toString(), x, y, textPaint)
                x += 80f
                
                uniqueExams.forEach { exam ->
                    val grade = grades.find { it.studentId == student.id && it.examName == exam }
                    val score = grade?.score?.toString() ?: "-"
                    canvas.drawText(score, x, y, textPaint)
                    x += 80f
                }
                y += 20f
            }
            
            pdfDocument.finishPage(page)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
