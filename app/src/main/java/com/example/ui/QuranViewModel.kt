package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuranViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuranRepository

    // Database flows turned into StateFlows
    val students: StateFlow<List<StudentEntity>>
    val hourHeaders: StateFlow<List<HourHeaderEntity>>
    val monthHeaders: StateFlow<List<MonthHeaderEntity>>
    val appointmentCells: StateFlow<List<AppointmentCellEntity>>
    val payments: StateFlow<List<PaymentEntity>>

    // Authentication State
    var isAuthenticated by mutableStateOf(false)
        private set
    var authError by mutableStateOf<String?>(null)

    // Splash / Loading State
    var isInitialized by mutableStateOf(false)
        private set

    // Draft State for Appointments Tab (Mawa'eed)
    var draftHourHeaders by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    var draftCells by mutableStateOf<Map<Pair<Int, Int>, String>>(emptyMap())
        private set

    // Draft State for Students & Payments Tab (Asma'a)
    var draftMonthHeaders by mutableStateOf<Map<Int, String>>(emptyMap())
        private set
    var draftPayments by mutableStateOf<Map<Pair<Int, Int>, Boolean>>(emptyMap())
        private set

    // Password Management Fields
    var oldPasswordInput by mutableStateOf("")
    var newPasswordInput by mutableStateOf("")
    var confirmPasswordInput by mutableStateOf("")
    var passwordChangeMessage by mutableStateOf<String?>(null)
    var passwordChangeSuccess by mutableStateOf(false)

    // UI Toast/Snack Message Trigger
    private val _uiEvent = MutableSharedFlow<String>()
    val uiEvent: SharedFlow<String> = _uiEvent.asSharedFlow()

    init {
        val database = QuranDatabase.getDatabase(application)
        val dao = database.quranDao()
        repository = QuranRepository(dao)

        students = repository.students.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        hourHeaders = repository.hourHeaders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        monthHeaders = repository.monthHeaders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        appointmentCells = repository.appointmentCells.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        payments = repository.payments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Seed database and set up drafts
        viewModelScope.launch {
            repository.initializeDatabaseIfNeeded()
            isInitialized = true

            // Gather and setup initial drafts from db
            launch {
                hourHeaders.collect { list ->
                    if (draftHourHeaders.isEmpty() && list.isNotEmpty()) {
                        draftHourHeaders = list.associate { it.hourIndex to it.name }
                    }
                }
            }
            launch {
                appointmentCells.collect { list ->
                    if (draftCells.isEmpty() && list.isNotEmpty()) {
                        draftCells = list.associate { (it.dayIndex to it.hourIndex) to it.content }
                    }
                }
            }
            launch {
                monthHeaders.collect { list ->
                    if (draftMonthHeaders.isEmpty() && list.isNotEmpty()) {
                        draftMonthHeaders = list.associate { it.monthIndex to it.name }
                    }
                }
            }
            launch {
                payments.collect { list ->
                    if (draftPayments.isEmpty() && list.isNotEmpty()) {
                        draftPayments = list.associate { (it.studentId to it.monthIndex) to it.paid }
                    }
                }
            }
        }
    }

    /**
     * Authenticate with entered password
     */
    fun login(password: String) {
        viewModelScope.launch {
            val correctPassword = repository.getPassword()
            if (password == correctPassword) {
                isAuthenticated = true
                authError = null
                // Force sync drafts from database when authenticating
                syncDraftsFromDb()
            } else {
                authError = "كلمة السر غير صحيحة، يرجى المحاولة مرة أخرى"
            }
        }
    }

    fun logout() {
        isAuthenticated = false
    }

    /**
     * Resets/Synchronizes drafts with database states.
     */
    fun syncDraftsFromDb() {
        draftHourHeaders = hourHeaders.value.associate { it.hourIndex to it.name }
        draftCells = appointmentCells.value.associate { (it.dayIndex to it.hourIndex) to it.content }
        draftMonthHeaders = monthHeaders.value.associate { it.monthIndex to it.name }
        draftPayments = payments.value.associate { (it.studentId to it.monthIndex) to it.paid }
    }

    // --- APPOINTMENTS (MAWA'EED) MUTATORS ---

    fun updateDraftHourHeader(hourIndex: Int, newName: String) {
        val updated = draftHourHeaders.toMutableMap()
        updated[hourIndex] = newName
        draftHourHeaders = updated
    }

    fun updateDraftCell(dayIndex: Int, hourIndex: Int, newContent: String) {
        val updated = draftCells.toMutableMap()
        updated[dayIndex to hourIndex] = newContent
        draftCells = updated
    }

    /**
     * COMMITS APPOINTMENT DRAFTS (Hour headers & cells) to DB.
     */
    fun saveAppointments() {
        viewModelScope.launch {
            try {
                // Save hour headers
                val headerEntities = draftHourHeaders.map { (index, name) ->
                    HourHeaderEntity(index, name)
                }
                repository.saveHourHeaders(headerEntities)

                // Save cells
                val cellEntities = draftCells.map { (key, content) ->
                    AppointmentCellEntity(key.first, key.second, content)
                }
                repository.saveAppointmentCells(cellEntities)

                _uiEvent.emit("تم تثبيت تغييرات جدول المواعيد بنجاح!")
            } catch (e: Exception) {
                _uiEvent.emit("حدث خطأ أثناء حفظ المواعيد: ${e.localizedMessage}")
            }
        }
    }

    // --- STUDENTS & PAYMENTS (ASMA'A) MUTATORS ---

    fun addStudent(fullName: String) {
        if (fullName.trim().isEmpty()) return
        viewModelScope.launch {
            try {
                val newStudent = StudentEntity(fullName = fullName.trim())
                repository.saveStudent(newStudent)
                _uiEvent.emit("تم إضافة الطالب ${fullName.trim()} بنجاح")
            } catch (e: Exception) {
                _uiEvent.emit("حدث خطأ: ${e.localizedMessage}")
            }
        }
    }

    fun deleteStudent(studentId: Int, studentName: String) {
        viewModelScope.launch {
            try {
                repository.deleteStudent(studentId)
                // Clean from draft payments too
                val updatedPayments = draftPayments.toMutableMap()
                val keysToRemove = updatedPayments.keys.filter { it.first == studentId }
                keysToRemove.forEach { updatedPayments.remove(it) }
                draftPayments = updatedPayments

                _uiEvent.emit("تم حذف الطالب $studentName بنجاح")
            } catch (e: Exception) {
                _uiEvent.emit("حدث خطأ أثناء الحذف: ${e.localizedMessage}")
            }
        }
    }

    fun updateStudentName(studentId: Int, newName: String) {
        if (newName.trim().isEmpty()) return
        viewModelScope.launch {
            try {
                repository.updateStudent(StudentEntity(id = studentId, fullName = newName.trim()))
                _uiEvent.emit("تم تعديل اسم الطالب بنجاح")
            } catch (e: Exception) {
                _uiEvent.emit("حدث خطأ: ${e.localizedMessage}")
            }
        }
    }

    fun updateDraftMonthHeader(monthIndex: Int, newName: String) {
        val updated = draftMonthHeaders.toMutableMap()
        updated[monthIndex] = newName
        draftMonthHeaders = updated
    }

    fun toggleDraftPayment(studentId: Int, monthIndex: Int) {
        val updated = draftPayments.toMutableMap()
        val currentStatus = updated[studentId to monthIndex] ?: false
        updated[studentId to monthIndex] = !currentStatus
        draftPayments = updated
    }

    /**
     * COMMITS NAMES & PAYMENTS DRAFTS to DB.
     */
    fun saveNamesAndPayments() {
        viewModelScope.launch {
            try {
                // Save month headers
                val monthEntities = draftMonthHeaders.map { (index, name) ->
                    MonthHeaderEntity(index, name)
                }
                repository.saveMonthHeaders(monthEntities)

                // Save payments
                val paymentEntities = draftPayments.map { (key, paid) ->
                    PaymentEntity(key.first, key.second, paid)
                }
                repository.savePayments(paymentEntities)

                _uiEvent.emit("تم تثبيت تغييرات الأسماء والمدفوعات بنجاح!")
            } catch (e: Exception) {
                _uiEvent.emit("حدث خطأ أثناء حفظ البيانات: ${e.localizedMessage}")
            }
        }
    }

    // --- PASSWORD MANAGEMENT ---

    fun changePassword() {
        val oldPass = oldPasswordInput
        val newPass = newPasswordInput
        val confirmPass = confirmPasswordInput

        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            passwordChangeMessage = "يرجى ملء جميع الحقول المطلوبة"
            passwordChangeSuccess = false
            return
        }

        viewModelScope.launch {
            val correctPassword = repository.getPassword()
            if (oldPass != correctPassword) {
                passwordChangeMessage = "كلمة السر القديمة غير صحيحة"
                passwordChangeSuccess = false
                return@launch
            }

            if (newPass != confirmPass) {
                passwordChangeMessage = "كلمة السر الجديدة غير متطابقة"
                passwordChangeSuccess = false
                return@launch
            }

            if (newPass.length < 4) {
                passwordChangeMessage = "كلمة السر الجديدة يجب ألا تقل عن 4 رموز"
                passwordChangeSuccess = false
                return@launch
            }

            // Save new password
            repository.setPassword(newPass)
            passwordChangeMessage = "تم تغيير كلمة السر بنجاح!"
            passwordChangeSuccess = true

            // Clear inputs
            oldPasswordInput = ""
            newPasswordInput = ""
            confirmPasswordInput = ""

            _uiEvent.emit("تم تغيير كلمة السر بنجاح!")
        }
    }

    fun clearPasswordChangeMessages() {
        passwordChangeMessage = null
        passwordChangeSuccess = false
    }

    /**
     * Verifies the password and saves the draft names and payments.
     */
    fun verifyPasswordAndSave(
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            val correctPassword = repository.getPassword()
            if (password == correctPassword) {
                saveNamesAndPayments()
                onSuccess()
            } else {
                onFailure("كلمة السر غير صحيحة، يرجى المحاولة مرة أخرى")
            }
        }
    }

    fun revertAppointments() {
        draftHourHeaders = hourHeaders.value.associate { it.hourIndex to it.name }
        draftCells = appointmentCells.value.associate { (it.dayIndex to it.hourIndex) to it.content }
        viewModelScope.launch {
            _uiEvent.emit("تم التراجع عن تعديلات جدول المواعيد")
        }
    }

    fun revertNamesAndPayments() {
        draftMonthHeaders = monthHeaders.value.associate { it.monthIndex to it.name }
        draftPayments = payments.value.associate { (it.studentId to it.monthIndex) to it.paid }
        viewModelScope.launch {
            _uiEvent.emit("تم التراجع عن تعديلات شؤون الطلاب والمدفوعات")
        }
    }

    fun getBackupJsonString(): String {
        val root = org.json.JSONObject()
        root.put("backup_version", 1)

        val studentsArray = org.json.JSONArray()
        students.value.forEach {
            val obj = org.json.JSONObject()
            obj.put("id", it.id)
            obj.put("fullName", it.fullName)
            obj.put("lastModified", it.lastModified)
            studentsArray.put(obj)
        }
        root.put("students", studentsArray)

        val hourHeadersArray = org.json.JSONArray()
        hourHeaders.value.forEach {
            val obj = org.json.JSONObject()
            obj.put("hourIndex", it.hourIndex)
            obj.put("name", it.name)
            hourHeadersArray.put(obj)
        }
        root.put("hour_headers", hourHeadersArray)

        val monthHeadersArray = org.json.JSONArray()
        monthHeaders.value.forEach {
            val obj = org.json.JSONObject()
            obj.put("monthIndex", it.monthIndex)
            obj.put("name", it.name)
            monthHeadersArray.put(obj)
        }
        root.put("month_headers", monthHeadersArray)

        val appointmentCellsArray = org.json.JSONArray()
        appointmentCells.value.forEach {
            val obj = org.json.JSONObject()
            obj.put("dayIndex", it.dayIndex)
            obj.put("hourIndex", it.hourIndex)
            obj.put("content", it.content)
            appointmentCellsArray.put(obj)
        }
        root.put("appointment_cells", appointmentCellsArray)

        val paymentsArray = org.json.JSONArray()
        payments.value.forEach {
            val obj = org.json.JSONObject()
            obj.put("studentId", it.studentId)
            obj.put("monthIndex", it.monthIndex)
            obj.put("paid", it.paid)
            paymentsArray.put(obj)
        }
        root.put("payments", paymentsArray)

        return root.toString(2)
    }

    fun importBackupJsonString(jsonString: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val root = org.json.JSONObject(jsonString)
                
                val studentsList = mutableListOf<StudentEntity>()
                if (root.has("students")) {
                    val arr = root.getJSONArray("students")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        studentsList.add(
                            StudentEntity(
                                id = obj.getInt("id"),
                                fullName = obj.getString("fullName"),
                                lastModified = obj.optLong("lastModified", System.currentTimeMillis())
                            )
                        )
                    }
                }

                val hourHeadersList = mutableListOf<HourHeaderEntity>()
                if (root.has("hour_headers")) {
                    val arr = root.getJSONArray("hour_headers")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        hourHeadersList.add(
                            HourHeaderEntity(
                                hourIndex = obj.getInt("hourIndex"),
                                name = obj.getString("name")
                            )
                        )
                    }
                }

                val monthHeadersList = mutableListOf<MonthHeaderEntity>()
                if (root.has("month_headers")) {
                    val arr = root.getJSONArray("month_headers")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        monthHeadersList.add(
                            MonthHeaderEntity(
                                monthIndex = obj.getInt("monthIndex"),
                                name = obj.getString("name")
                            )
                        )
                    }
                }

                val appointmentCellsList = mutableListOf<AppointmentCellEntity>()
                if (root.has("appointment_cells")) {
                    val arr = root.getJSONArray("appointment_cells")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        appointmentCellsList.add(
                            AppointmentCellEntity(
                                dayIndex = obj.getInt("dayIndex"),
                                hourIndex = obj.getInt("hourIndex"),
                                content = obj.getString("content")
                            )
                        )
                    }
                }

                val paymentsList = mutableListOf<PaymentEntity>()
                if (root.has("payments")) {
                    val arr = root.getJSONArray("payments")
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        paymentsList.add(
                            PaymentEntity(
                                studentId = obj.getInt("studentId"),
                                monthIndex = obj.getInt("monthIndex"),
                                paid = obj.getBoolean("paid")
                            )
                        )
                    }
                }

                repository.importBackup(
                    studentsList,
                    hourHeadersList,
                    appointmentCellsList,
                    monthHeadersList,
                    paymentsList
                )

                // Resync drafts so the UI updates immediately
                syncDraftsFromDb()
                _uiEvent.emit("تم استيراد البيانات بنجاح!")
                onSuccess()
            } catch (e: Exception) {
                onFailure(e.localizedMessage ?: "خطأ غير معروف في قراءة ملف النسخة الاحتياطية")
            }
        }
    }
}
