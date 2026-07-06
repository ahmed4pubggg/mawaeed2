package com.example.data

import kotlinx.coroutines.flow.Flow

class QuranRepository(private val dao: QuranDao) {

    // Configs
    suspend fun getPassword(): String {
        return dao.getConfig("password")?.value ?: "1234"
    }

    suspend fun setPassword(password: String) {
        dao.saveConfig(ConfigEntity("password", password))
    }

    suspend fun getConfig(key: String, defaultValue: String): String {
        return dao.getConfig(key)?.value ?: defaultValue
    }

    suspend fun saveConfig(key: String, value: String) {
        dao.saveConfig(ConfigEntity(key, value))
    }

    suspend fun getAllConfigs(): List<ConfigEntity> {
        return dao.getAllConfigs()
    }

    // Hour Headers
    val hourHeaders: Flow<List<HourHeaderEntity>> = dao.getHourHeaders()

    suspend fun saveHourHeaders(headers: List<HourHeaderEntity>) {
        dao.saveHourHeaders(headers)
    }

    // Appointment Cells
    val appointmentCells: Flow<List<AppointmentCellEntity>> = dao.getAppointmentCells()

    suspend fun getAppointmentCellsList(): List<AppointmentCellEntity> {
        return dao.getAppointmentCellsList()
    }

    suspend fun saveAppointmentCells(cells: List<AppointmentCellEntity>) {
        dao.saveAppointmentCells(cells)
    }

    // Students
    val students: Flow<List<StudentEntity>> = dao.getStudents()

    suspend fun saveStudent(student: StudentEntity): Long {
        return dao.saveStudent(student)
    }

    suspend fun deleteStudent(studentId: Int) {
        dao.deleteStudent(studentId)
        dao.deletePaymentsForStudent(studentId)
    }

    suspend fun updateStudent(student: StudentEntity) {
        dao.updateStudent(student)
    }

    // Month Headers
    val monthHeaders: Flow<List<MonthHeaderEntity>> = dao.getMonthHeaders()

    suspend fun saveMonthHeaders(headers: List<MonthHeaderEntity>) {
        dao.saveMonthHeaders(headers)
    }

    // Payments
    val payments: Flow<List<PaymentEntity>> = dao.getPayments()

    suspend fun savePayments(payments: List<PaymentEntity>) {
        dao.savePayments(payments)
    }

    /**
     * Initializes default configurations and headers if the database is empty.
     */
    suspend fun initializeDatabaseIfNeeded() {
        // Seed default password "1234" if not present
        if (dao.getConfig("password") == null) {
            dao.saveConfig(ConfigEntity("password", "1234"))
        }

        // Seed default hour headers "12" to "7" if empty
        if (dao.getHourHeadersCount() == 0) {
            val defaultHours = listOf("12", "1", "2", "3", "4", "5", "6", "7")
            val entities = defaultHours.mapIndexed { index, name ->
                HourHeaderEntity(index, name)
            }
            dao.saveHourHeaders(entities)
        }

        // Seed default month headers "يناير" to "يونيو" if empty
        if (dao.getMonthHeadersCount() == 0) {
            val defaultMonths = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو")
            val entities = defaultMonths.mapIndexed { index, name ->
                MonthHeaderEntity(index, name)
            }
            dao.saveMonthHeaders(entities)
        }

        // Seed default month headers 2 "يناير" to "يونيو" if empty
        if (dao.getMonthHeaders2Count() == 0) {
            val defaultMonths = listOf("يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو")
            val entities = defaultMonths.mapIndexed { index, name ->
                MonthHeader2Entity(index, name)
            }
            dao.saveMonthHeaders2(entities)
        }
    }

    // --- GROUP 2 METHODS ---

    val students2: Flow<List<Student2Entity>> = dao.getStudents2()

    suspend fun saveStudent2(student: Student2Entity): Long {
        return dao.saveStudent2(student)
    }

    suspend fun deleteStudent2(studentId: Int) {
        dao.deleteStudent2(studentId)
        dao.deletePaymentsForStudent2(studentId)
    }

    suspend fun updateStudent2(student: Student2Entity) {
        dao.updateStudent2(student)
    }

    val monthHeaders2: Flow<List<MonthHeader2Entity>> = dao.getMonthHeaders2()

    suspend fun saveMonthHeaders2(headers: List<MonthHeader2Entity>) {
        dao.saveMonthHeaders2(headers)
    }

    val payments2: Flow<List<Payment2Entity>> = dao.getPayments2()

    suspend fun savePayments2(payments: List<Payment2Entity>) {
        dao.savePayments2(payments)
    }

    suspend fun deleteAllStudentsAndPayments1() {
        dao.deleteAllStudents()
        dao.deleteAllPayments()
    }

    suspend fun deleteAllStudentsAndPayments2() {
        dao.deleteAllStudents2()
        dao.deleteAllPayments2()
    }

    suspend fun importBackup(
        studentsList: List<StudentEntity>,
        hourHeadersList: List<HourHeaderEntity>,
        appointmentCellsList: List<AppointmentCellEntity>,
        monthHeadersList: List<MonthHeaderEntity>,
        paymentsList: List<PaymentEntity>,
        students2List: List<Student2Entity>,
        monthHeaders2List: List<MonthHeader2Entity>,
        payments2List: List<Payment2Entity>,
        configsList: List<ConfigEntity>
    ) {
        dao.deleteAllStudents()
        dao.deleteAllHourHeaders()
        dao.deleteAllAppointmentCells()
        dao.deleteAllMonthHeaders()
        dao.deleteAllPayments()

        dao.deleteAllStudents2()
        dao.deleteAllMonthHeaders2()
        dao.deleteAllPayments2()

        if (studentsList.isNotEmpty()) {
            studentsList.forEach { dao.saveStudent(it) }
        }
        if (hourHeadersList.isNotEmpty()) {
            dao.saveHourHeaders(hourHeadersList)
        }
        if (appointmentCellsList.isNotEmpty()) {
            dao.saveAppointmentCells(appointmentCellsList)
        }
        if (monthHeadersList.isNotEmpty()) {
            dao.saveMonthHeaders(monthHeadersList)
        }
        if (paymentsList.isNotEmpty()) {
            dao.savePayments(paymentsList)
        }

        if (students2List.isNotEmpty()) {
            students2List.forEach { dao.saveStudent2(it) }
        }
        if (monthHeaders2List.isNotEmpty()) {
            dao.saveMonthHeaders2(monthHeaders2List)
        }
        if (payments2List.isNotEmpty()) {
            dao.savePayments2(payments2List)
        }

        configsList.forEach { config ->
            if (config.key != "password") {
                dao.saveConfig(config)
            }
        }
    }
}
