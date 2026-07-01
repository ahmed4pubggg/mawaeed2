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
}
