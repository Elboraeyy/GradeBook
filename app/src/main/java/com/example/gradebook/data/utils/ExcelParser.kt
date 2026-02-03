package com.example.gradebook.data.utils

import android.content.Context
import android.net.Uri
import com.example.gradebook.data.local.entities.Student
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import javax.inject.Inject

class ExcelParser @Inject constructor() {

    fun parseStudents(context: Context, uri: Uri, classroomId: Int): Result<List<Student>> {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) return Result.failure(Exception("Cannot open file"))

            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0) // Assume first sheet
            val students = mutableListOf<Student>()

            // Iterate rows, skipping header if likely
            // We'll try to detect header or just start from row 1 (0-indexed)
            
            val rowIterator = sheet.rowIterator()
            var isHeader = true // Simple heuristic: skip first row

            while (rowIterator.hasNext()) {
                val row = rowIterator.next()
                if (isHeader) {
                    isHeader = false
                    continue
                }

                // Assume Column 0 is Name
                val nameCell = row.getCell(0)
                val name = getCellValue(nameCell)

                if (name.isNotBlank()) {
                    // Column 1 is Seat Number (Optional)
                    val seatCell = row.getCell(1)
                    val seatNumber = getCellValue(seatCell)

                    students.add(
                        Student(
                            classroomId = classroomId,
                            name = name,
                            seatNumber = seatNumber.ifBlank { null }
                        )
                    )
                }
            }
            workbook.close()
            Result.success(students)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getCellValue(cell: org.apache.poi.ss.usermodel.Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                // Check if it's an integer
                if (cell.numericCellValue % 1 == 0.0) {
                     cell.numericCellValue.toInt().toString()
                } else {
                    cell.numericCellValue.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            else -> ""
        }
    }
}
