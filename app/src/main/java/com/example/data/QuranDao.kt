package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface QuranDao {

    // Configs
    @Query("SELECT * FROM configs WHERE `key` = :key LIMIT 1")
    suspend fun getConfig(key: String): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: ConfigEntity)

    @Query("SELECT * FROM configs")
    suspend fun getAllConfigs(): List<ConfigEntity>

    // Hour Headers
    @Query("SELECT * FROM hour_headers ORDER BY hourIndex ASC")
    fun getHourHeaders(): Flow<List<HourHeaderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHourHeaders(headers: List<HourHeaderEntity>)

    @Query("SELECT COUNT(*) FROM hour_headers")
    suspend fun getHourHeadersCount(): Int

    // Appointment Cells
    @Query("SELECT * FROM appointment_cells")
    fun getAppointmentCells(): Flow<List<AppointmentCellEntity>>

    @Query("SELECT * FROM appointment_cells")
    suspend fun getAppointmentCellsList(): List<AppointmentCellEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAppointmentCells(cells: List<AppointmentCellEntity>)

    // Students
    @Query("SELECT * FROM students ORDER BY id ASC")
    fun getStudents(): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStudent(student: StudentEntity): Long

    @Query("DELETE FROM students WHERE id = :studentId")
    suspend fun deleteStudent(studentId: Int)

    @Update
    suspend fun updateStudent(student: StudentEntity)

    // Month Headers
    @Query("SELECT * FROM month_headers ORDER BY monthIndex ASC")
    fun getMonthHeaders(): Flow<List<MonthHeaderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMonthHeaders(headers: List<MonthHeaderEntity>)

    @Query("SELECT COUNT(*) FROM month_headers")
    suspend fun getMonthHeadersCount(): Int

    // Payments
    @Query("SELECT * FROM payments")
    fun getPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePayments(payments: List<PaymentEntity>)

    @Query("DELETE FROM payments WHERE studentId = :studentId")
    suspend fun deletePaymentsForStudent(studentId: Int)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    @Query("DELETE FROM hour_headers")
    suspend fun deleteAllHourHeaders()

    @Query("DELETE FROM appointment_cells")
    suspend fun deleteAllAppointmentCells()

    @Query("DELETE FROM month_headers")
    suspend fun deleteAllMonthHeaders()

    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments()

    // --- GROUP 2 DATA ACCESS ---

    // Students 2
    @Query("SELECT * FROM students2 ORDER BY id ASC")
    fun getStudents2(): Flow<List<Student2Entity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStudent2(student: Student2Entity): Long

    @Query("DELETE FROM students2 WHERE id = :studentId")
    suspend fun deleteStudent2(studentId: Int)

    @Update
    suspend fun updateStudent2(student: Student2Entity)

    // Month Headers 2
    @Query("SELECT * FROM month_headers2 ORDER BY monthIndex ASC")
    fun getMonthHeaders2(): Flow<List<MonthHeader2Entity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMonthHeaders2(headers: List<MonthHeader2Entity>)

    @Query("SELECT COUNT(*) FROM month_headers2")
    suspend fun getMonthHeaders2Count(): Int

    // Payments 2
    @Query("SELECT * FROM payments2")
    fun getPayments2(): Flow<List<Payment2Entity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePayments2(payments: List<Payment2Entity>)

    @Query("DELETE FROM payments2 WHERE studentId = :studentId")
    suspend fun deletePaymentsForStudent2(studentId: Int)

    @Query("DELETE FROM students2")
    suspend fun deleteAllStudents2()

    @Query("DELETE FROM month_headers2")
    suspend fun deleteAllMonthHeaders2()

    @Query("DELETE FROM payments2")
    suspend fun deleteAllPayments2()
}

