package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.StudentEntity
import com.example.ui.theme.*
import kotlinx.coroutines.flow.collectLatest

enum class StudentSortType {
    ALPHABETICAL,
    MODIFICATION_DATE
}

// Arabic days of the week starting Saturday
val WEEK_DAYS = listOf(
    "السبت",
    "الأحد",
    "الإثنين",
    "الثلاثاء",
    "الأربعاء",
    "الخميس",
    "الجمعة"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranAppUi(viewModel: QuranViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Listen to ViewModel events for Toasts/Snackbars
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!viewModel.isInitialized) {
                // Splash / Seeding State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (!viewModel.isAuthenticated) {
                // Authenticate Screen
                LoginScreen(viewModel = viewModel)
            } else {
                // Primary App Interface
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

/**
 * Modern Login Screen with spiritual/Islamic styling
 */
@Composable
fun LoginScreen(viewModel: QuranViewModel) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        LightTeal.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Quran golden emblem
            Card(
                modifier = Modifier
                    .size(130.dp)
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else DarkTeal)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_quran_logo),
                    contentDescription = "شعار المواعيد",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "بَرْنَامَج المَوَاعِيد",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                    letterSpacing = 1.sp
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "لتنظيم مواعيد حلقات القرآن الكريم والاشتراكات",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f) else MediumTeal,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Password Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل الدخول الآمن",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("أدخل كلمة المرور") },
                        placeholder = { Text("الافتراضية: 1234") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            cursorColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "رؤية كلمة السر",
                                    tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MediumTeal
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "قفل",
                                tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MediumTeal
                            )
                        }
                    )

                    viewModel.authError?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { viewModel.login(passwordInput) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Login,
                            contentDescription = "دخول",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "دخول",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Copyright Credit Footer
            Text(
                text = "بواسطة الشيخ أحمد النمس",
                style = MaterialTheme.typography.titleSmall.copy(
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DeepGold,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else LightTeal.copy(alpha = 0.6f), shape = RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

/**
 * Main application interface with tab navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: QuranViewModel) {
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf(
        "جدول المواعيد" to Icons.Filled.CalendarMonth,
        "شؤون الطلاب" to Icons.Filled.People,
        "كلمة السر" to Icons.Filled.Lock
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // App Header - Centered to avoid any empty spaces or offset alignment
        CenterAlignedTopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_quran_logo),
                        contentDescription = "شعار",
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "برنامج المواعيد",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                        )
                    )
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else DarkTeal
            ),
            actions = {
                IconButton(onClick = { viewModel.logout() }) {
                    Icon(
                        imageVector = Icons.Filled.Logout,
                        contentDescription = "خروج",
                        tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.White
                    )
                }
            }
        )

        // Contents Based on Selection
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> AppointmentsTab(viewModel = viewModel)
                1 -> StudentsTab(viewModel = viewModel)
                2 -> PasswordTab(viewModel = viewModel)
            }
        }

        // Dedicated Bottom Copyright Credit & App Bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column {
                // Mini Credit Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else LightTeal.copy(alpha = 0.4f))
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بواسطة الشيخ أحمد النمس حفظه الله",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    tabs.forEachIndexed { index, (label, icon) ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                selectedTextColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                indicatorColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else LightTeal,
                                unselectedIconColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MediumTeal.copy(alpha = 0.7f),
                                unselectedTextColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MediumTeal.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. APPOINTMENTS TAB (MAWA'EED)
// ==========================================

@Composable
fun AppointmentsTab(viewModel: QuranViewModel) {
    val draftHourHeaders = viewModel.draftHourHeaders
    val draftCells = viewModel.draftCells

    // State for editing dialogs
    var editingHeaderIndex by remember { mutableStateOf<Int?>(null) }
    var editingHeaderCurrentValue by remember { mutableStateOf("") }

    var editingCellDayIdx by remember { mutableStateOf<Int?>(null) }
    var editingCellHourIdx by remember { mutableStateOf<Int?>(null) }
    var editingCellCurrentValue by remember { mutableStateOf("") }

    // Check if drafts differ from actual DB values to show a helpful unsaved hint
    val dbHours = viewModel.hourHeaders.collectAsState().value
    val dbCells = viewModel.appointmentCells.collectAsState().value

    val hasUnsavedChanges = remember(draftHourHeaders, draftCells, dbHours, dbCells) {
        val dbHoursMap = dbHours.associate { it.hourIndex to it.name }
        val dbCellsMap = dbCells.associate { (it.dayIndex to it.hourIndex) to it.content }
        draftHourHeaders != dbHoursMap || draftCells != dbCellsMap
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Instructions / Info Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else LightTeal.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "تنبيه",
                        tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "اضغط على الساعات في الأعلى لتعديل توقيتها، واضغط على أي مربع لتعديل المجموعات أو الطلاب. عند الانتهاء اضغط زر التثبيت.",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = if (isSystemInDarkTheme()) LightText else DarkTeal,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Outer Schedule Layout (RTL-Native structure)
            // Enabling bidirectional scroll: whole table scrolls vertically, columns scroll horizontally
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                // 1. Right Column: Days of Week (Fixed/Sticky on Right in RTL)
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    // Top right empty cell
                    Box(
                        modifier = Modifier
                            .size(width = 85.dp, height = 55.dp)
                            .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else DarkTeal)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "اليوم",
                            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    // Days vertical items
                    WEEK_DAYS.forEachIndexed { index, day ->
                        Box(
                            modifier = Modifier
                                .size(width = 85.dp, height = 70.dp)
                                .background(
                                    if (isSystemInDarkTheme()) {
                                        if (index % 2 == 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                                    } else {
                                        if (index % 2 == 0) LightTeal.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
                                    }
                                )
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else DarkTeal,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // 2. Hour columns scrolling horizontally on the left of days column
                for (hourIdx in 0..7) {
                    val hourLabel = draftHourHeaders[hourIdx] ?: "${hourIdx + 12}"
                    Column(
                        modifier = Modifier
                            .width(135.dp)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        // Hour Header Button (Renamable)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(55.dp)
                                .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondaryContainer else MediumTeal)
                                .clickable {
                                    editingHeaderIndex = hourIdx
                                    editingHeaderCurrentValue = hourLabel
                                }
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 4.dp)
                            ) {
                                Text(
                                    text = hourLabel,
                                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = "تعديل الساعة",
                                    tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.tertiary else GoldAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        // Appointment Cells for this Hour Column
                        for (dayIdx in 0..6) {
                            val cellContent = draftCells[dayIdx to hourIdx] ?: ""
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(70.dp)
                                    .background(
                                        if (isSystemInDarkTheme()) {
                                            if (cellContent.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                            else if (dayIdx % 2 == 0) MaterialTheme.colorScheme.surface
                                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                        } else {
                                            if (cellContent.isNotEmpty()) LightTeal.copy(alpha = 0.4f)
                                            else if (dayIdx % 2 == 0) MaterialTheme.colorScheme.surface
                                            else LightTeal.copy(alpha = 0.1f)
                                        }
                                    )
                                    .clickable {
                                        editingCellDayIdx = dayIdx
                                        editingCellHourIdx = hourIdx
                                        editingCellCurrentValue = cellContent
                                    }
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (cellContent.isNotEmpty()) {
                                    Text(
                                        text = cellContent,
                                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurface else DarkTeal,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                } else {
                                    Text(
                                        text = "+",
                                        color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f) else MediumTeal.copy(alpha = 0.4f),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            } // Close Row
            } // Close Box

            // Save / Save Changes Indicator Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Draft status text
                if (hasUnsavedChanges) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "يوجد تعديلات غير محفوظة",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(GreenSuccess, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جميع التغييرات مثبتة",
                            color = GreenSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                 // Main Save (Tathbeet) button
                 Row {
                     Button(
                         onClick = { viewModel.saveAppointments() },
                         colors = ButtonDefaults.buttonColors(
                             containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                             contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                         ),
                         shape = RoundedCornerShape(10.dp)
                     ) {
                         Icon(Icons.Filled.Save, contentDescription = "تثبيت")
                         Spacer(modifier = Modifier.width(6.dp))
                         Text("تثبيت", fontWeight = FontWeight.Bold)
                     }
                 }
            }
        }

        // --- DIALOGS FOR EDITING ---

        // 1. Edit Header (Hour Label) Dialog
        editingHeaderIndex?.let { index ->
            Dialog(onDismissRequest = { editingHeaderIndex = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تعديل تسمية الساعة",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = editingHeaderCurrentValue,
                            onValueChange = { editingHeaderCurrentValue = it },
                            label = { Text("اسم الساعة / التوقيت") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingHeaderIndex = null }) {
                                Text("إلغاء")
                            }
                            Button(
                                onClick = {
                                    viewModel.updateDraftHourHeader(index, editingHeaderCurrentValue)
                                    editingHeaderIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                    contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            ) {
                                Text("حفظ مؤقت")
                            }
                        }
                    }
                }
            }
        }

        // 2. Edit Cell Content Dialog
        if (editingCellDayIdx != null && editingCellHourIdx != null) {
            val dayIdx = editingCellDayIdx!!
            val hourIdx = editingCellHourIdx!!
            val dayName = WEEK_DAYS[dayIdx]
            val hourName = draftHourHeaders[hourIdx] ?: "${hourIdx + 12}"

            Dialog(onDismissRequest = {
                editingCellDayIdx = null
                editingCellHourIdx = null
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تعديل المجموعات والحلقات",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            )
                        )
                        Text(
                            text = "اليوم: $dayName | الساعة: $hourName",
                            style = MaterialTheme.typography.bodySmall.copy(color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant else MediumTeal),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = editingCellCurrentValue,
                            onValueChange = { editingCellCurrentValue = it },
                            label = { Text("مجموعة التحفيظ / اسم الطالب") },
                            placeholder = { Text("مثال: مجموعة المائدة، حلقة الحفظ") },
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Clear option
                            TextButton(
                                onClick = {
                                    editingCellCurrentValue = ""
                                },
                                colors = ButtonColors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color.Red,
                                    disabledContainerColor = Color.Transparent,
                                    disabledContentColor = Color.LightGray
                                )
                            ) {
                                Text("مسح الخانة")
                            }

                            Row {
                                TextButton(onClick = {
                                    editingCellDayIdx = null
                                    editingCellHourIdx = null
                                }) {
                                    Text("إلغاء")
                                }
                                Button(
                                    onClick = {
                                        viewModel.updateDraftCell(dayIdx, hourIdx, editingCellCurrentValue)
                                        editingCellDayIdx = null
                                        editingCellHourIdx = null
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                                    )
                                ) {
                                    Text("حفظ مؤقت")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. STUDENTS & PAYMENTS TAB (ASMA'A)
// ==========================================

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StudentsTab(viewModel: QuranViewModel) {
    val studentList by viewModel.students.collectAsState()
    val draftMonthHeaders = viewModel.draftMonthHeaders
    val draftPayments = viewModel.draftPayments

    // Search and Sort states
    var searchQuery by remember { mutableStateOf("") }
    var sortType by remember { mutableStateOf(StudentSortType.ALPHABETICAL) }

    // Filtered and sorted student list
    val filteredAndSortedStudents = remember(studentList, searchQuery, sortType) {
        studentList
            .filter { student ->
                student.fullName.contains(searchQuery, ignoreCase = true)
            }
            .sortedWith { s1, s2 ->
                when (sortType) {
                    StudentSortType.ALPHABETICAL -> s1.fullName.compareTo(s2.fullName)
                    StudentSortType.MODIFICATION_DATE -> s2.lastModified.compareTo(s1.lastModified)
                }
            }
    }

    // Edit/Add dialog states
    var isAddingStudent by remember { mutableStateOf(false) }
    var addStudentInput by remember { mutableStateOf("") }

    var editingStudentEntity by remember { mutableStateOf<StudentEntity?>(null) }
    var editingStudentInput by remember { mutableStateOf("") }

    var editingMonthIndex by remember { mutableStateOf<Int?>(null) }
    var editingMonthCurrentValue by remember { mutableStateOf("") }

    // Check if drafts differ from DB to show warning
    val dbMonths = viewModel.monthHeaders.collectAsState().value
    val dbPayments = viewModel.payments.collectAsState().value

    val hasUnsavedChanges = remember(draftMonthHeaders, draftPayments, dbMonths, dbPayments) {
        val dbMonthsMap = dbMonths.associate { it.monthIndex to it.name }
        val dbPaymentsMap = dbPayments.associate { (it.studentId to it.monthIndex) to it.paid }
        draftMonthHeaders != dbMonthsMap || draftPayments != dbPaymentsMap
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Options Panel: Add student + Save Payments
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isAddingStudent = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "إضافة طالب")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("إضافة طالب جديد", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                // Quick Save for payments & months
                Button(
                    onClick = { viewModel.saveNamesAndPayments() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.tertiary else DeepGold,
                        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onTertiary else Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Save, contentDescription = "تثبيت الحسابات")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تثبيت الحسابات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Search and Sort controls Card (Replaces help card - Requirement 7 & 8)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else LightTeal.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f) else LightTeal.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Search text field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        placeholder = { 
                            Text(
                                "البحث عن طالب...", 
                                fontSize = 13.sp,
                                color = if (isSystemInDarkTheme()) LightTextSecondary else MediumTeal.copy(alpha = 0.7f)
                            ) 
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = "بحث",
                                tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MediumTeal,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Filled.Clear,
                                        contentDescription = "مسح",
                                        tint = if (isSystemInDarkTheme()) LightTextSecondary else MediumTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            unfocusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant else MediumTeal.copy(alpha = 0.3f),
                            focusedContainerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface else Color.White,
                            unfocusedContainerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Sort Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Sort,
                            contentDescription = "فرز",
                            tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else MediumTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ترتيب حسب:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSystemInDarkTheme()) LightText else DarkTeal
                        )
                        Spacer(modifier = Modifier.width(12.dp))

                        // Alphabetical Sort Button
                        val isAlphaSelected = sortType == StudentSortType.ALPHABETICAL
                        AssistChip(
                            onClick = { sortType = StudentSortType.ALPHABETICAL },
                            label = { Text("أبجدي (أ - ي)", fontSize = 11.sp, fontWeight = if (isAlphaSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                if (isAlphaSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isAlphaSelected) {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else LightTeal
                                } else Color.Transparent,
                                labelColor = if (isAlphaSelected) {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                                } else {
                                    if (isSystemInDarkTheme()) LightTextSecondary else MediumTeal
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isAlphaSelected) {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                                } else {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant else MediumTeal.copy(alpha = 0.3f)
                                }
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Modification Date Sort Button
                        val isDateSelected = sortType == StudentSortType.MODIFICATION_DATE
                        AssistChip(
                            onClick = { sortType = StudentSortType.MODIFICATION_DATE },
                            label = { Text("تاريخ التعديل", fontSize = 11.sp, fontWeight = if (isDateSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                if (isDateSelected) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isDateSelected) {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else LightTeal
                                } else Color.Transparent,
                                labelColor = if (isDateSelected) {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                                } else {
                                    if (isSystemInDarkTheme()) LightTextSecondary else MediumTeal
                                }
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isDateSelected) {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                                } else {
                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outlineVariant else MediumTeal.copy(alpha = 0.3f)
                                }
                            )
                        )
                    }
                }
            }

            if (studentList.isEmpty()) {
                // Empty State illustration / helper (No students registered yet)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = "لا يوجد طلاب",
                            modifier = Modifier.size(80.dp),
                            tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MediumTeal.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا يوجد طلاب مسجلين حالياً",
                            color = if (isSystemInDarkTheme()) LightText else DarkTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "انقر على زر 'إضافة طالب جديد' لبدء التدوين",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSystemInDarkTheme()) LightTextSecondary else MediumTeal.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else if (filteredAndSortedStudents.isEmpty()) {
                // No search results found state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "لا توجد نتائج",
                            modifier = Modifier.size(80.dp),
                            tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MediumTeal.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد نتائج بحث مطابقة",
                            color = if (isSystemInDarkTheme()) LightText else DarkTeal,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تأكد من كتابة الاسم بشكل صحيح أو جرب كلمة أخرى",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSystemInDarkTheme()) LightTextSecondary else MediumTeal.copy(alpha = 0.8f)
                            ),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Unified bidirectional scroll wrapper: whole grid scrolls vertically, columns scroll horizontally
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        // 1. Right Column: Student names (Fixed width, wraps content, scrolled vertically by the parent Box)
                        Column(
                            modifier = Modifier.width(170.dp)
                        ) {
                            // Header student corner
                            Box(
                                modifier = Modifier
                                    .size(width = 170.dp, height = 50.dp)
                                    .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primaryContainer else DarkTeal)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "اسم الطالب ثلاثي",
                                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimaryContainer else Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            // Student name boxes (Clickable for editing/deleting)
                            filteredAndSortedStudents.forEachIndexed { sIdx, student ->
                                Box(
                                    modifier = Modifier
                                        .size(width = 170.dp, height = 65.dp)
                                        .background(
                                            if (sIdx % 2 == 0) {
                                                if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f) else LightTeal.copy(alpha = 0.2f)
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                        )
                                        .combinedClickable(
                                            onClick = {
                                                editingStudentEntity = student
                                                editingStudentInput = student.fullName
                                            },
                                            onLongClick = {
                                                editingStudentEntity = student
                                                editingStudentInput = student.fullName
                                            }
                                        )
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .padding(horizontal = 8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = student.fullName,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSystemInDarkTheme()) LightText else DarkTeal,
                                            fontSize = 13.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "تعديل",
                                            tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) else MediumTeal.copy(alpha = 0.5f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 2. Month Columns (Wraps content, scrolled vertically by the parent Box)
                        for (monthIdx in 0..5) {
                            val monthLabel = draftMonthHeaders[monthIdx] ?: "الشهر ${monthIdx + 1}"
                            Column(
                                modifier = Modifier.width(105.dp)
                            ) {
                                // Month Header Button (Renamable)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .background(if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondaryContainer else MediumTeal)
                                        .clickable {
                                            editingMonthIndex = monthIdx
                                            editingMonthCurrentValue = monthLabel
                                        }
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 2.dp)
                                    ) {
                                        Text(
                                            text = monthLabel,
                                            color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "تعديل الشهر",
                                            tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else GoldAccent,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }

                                // Payment Checkboxes for each student in this month
                                filteredAndSortedStudents.forEachIndexed { sIdx, student ->
                                    val isPaid = draftPayments[student.id to monthIdx] ?: false
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(65.dp)
                                            .background(
                                                if (sIdx % 2 == 0) {
                                                    if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else LightTeal.copy(alpha = 0.1f)
                                                } else {
                                                    MaterialTheme.colorScheme.surface
                                                }
                                            )
                                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.toggleDraftPayment(student.id, monthIdx) },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(
                                                    if (isPaid) {
                                                        GreenSuccess
                                                    } else {
                                                        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceVariant else LightTeal.copy(alpha = 0.5f)
                                                    }
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isPaid) {
                                                        GreenSuccess
                                                    } else {
                                                        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.outline else MediumTeal.copy(alpha = 0.5f)
                                                    },
                                                    RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            if (isPaid) {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = "تم الدفع",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Unsaved adjustments indicator row (Buttons removed - Requirement 2 & 6)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center // Center-aligned for clean visual balance
            ) {
                if (hasUnsavedChanges) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تعديلات حسابية غير محفوظة",
                            color = Color.Red,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isSystemInDarkTheme()) Color(0xFF4ADE80) else GreenSuccess, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "جميع الاشتراكات مثبتة في النظام",
                            color = if (isSystemInDarkTheme()) Color(0xFF4ADE80) else GreenSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- DIALOGS FOR TAB 2 ---

        // 1. Add Student Dialog
        if (isAddingStudent) {
            Dialog(onDismissRequest = { isAddingStudent = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "إضافة طالب جديد",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = addStudentInput,
                            onValueChange = { addStudentInput = it },
                            label = { Text("اسم الطالب ثلاثي") },
                            placeholder = { Text("مثال: عبد الله أحمد محمد") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = {
                                isAddingStudent = false
                                addStudentInput = ""
                            }) {
                                Text("إلغاء")
                            }
                            Button(
                                onClick = {
                                    if (addStudentInput.trim().isNotEmpty()) {
                                        viewModel.addStudent(addStudentInput)
                                        isAddingStudent = false
                                        addStudentInput = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                    contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            ) {
                                Text("إضافة")
                            }
                        }
                    }
                }
            }
        }

        // 2. Edit Student Dialog
        editingStudentEntity?.let { student ->
            Dialog(onDismissRequest = { editingStudentEntity = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تعديل بيانات الطالب",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = editingStudentInput,
                            onValueChange = { editingStudentInput = it },
                            label = { Text("اسم الطالب ثلاثي") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Delete student
                            IconButton(
                                onClick = {
                                    viewModel.deleteStudent(student.id, student.fullName)
                                    editingStudentEntity = null
                                },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "حذف الطالب",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }

                            Row {
                                TextButton(onClick = { editingStudentEntity = null }) {
                                    Text("إلغاء")
                                }
                                Button(
                                    onClick = {
                                        if (editingStudentInput.trim().isNotEmpty()) {
                                            viewModel.updateStudentName(student.id, editingStudentInput)
                                            editingStudentEntity = null
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                        contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                                    )
                                ) {
                                    Text("حفظ")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Edit Month Header Dialog
        editingMonthIndex?.let { index ->
            Dialog(onDismissRequest = { editingMonthIndex = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تعديل اسم العمود المالي",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = editingMonthCurrentValue,
                            onValueChange = { editingMonthCurrentValue = it },
                            label = { Text("اسم الشهر أو البند") },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(onClick = { editingMonthIndex = null }) {
                                Text("إلغاء")
                            }
                            Button(
                                onClick = {
                                    viewModel.updateDraftMonthHeader(index, editingMonthCurrentValue)
                                    editingMonthIndex = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                                    contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                                )
                            ) {
                                Text("تعديل مؤقت")
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. PASSWORD MANAGEMENT TAB
// ==========================================

@Composable
fun PasswordTab(viewModel: QuranViewModel) {
    // Clear alerts when this tab is loaded
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearPasswordChangeMessages()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        LightTeal.copy(alpha = 0.2f)
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.LockOpen,
                contentDescription = "إدارة الأمان",
                tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.tertiary else DeepGold,
                modifier = Modifier.size(65.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "إدارة كلمة المرور وحماية التطبيق",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                ),
                textAlign = TextAlign.Center
            )

            Text(
                text = "يرجى الاحتفاظ بكلمة المرور الجديدة لتتمكن من فتح التطبيق لاحقاً بأمان",
                style = MaterialTheme.typography.bodySmall.copy(color = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onSurfaceVariant else MediumTeal),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Old Password Input
                    OutlinedTextField(
                        value = viewModel.oldPasswordInput,
                        onValueChange = { viewModel.oldPasswordInput = it },
                        label = { Text("كلمة المرور القديمة الحالية") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Lock, contentDescription = "الحالية", tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MediumTeal)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // New Password Input
                    OutlinedTextField(
                        value = viewModel.newPasswordInput,
                        onValueChange = { viewModel.newPasswordInput = it },
                        label = { Text("كلمة المرور الجديدة") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Key, contentDescription = "الجديدة", tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MediumTeal)
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Confirm Password Input
                    OutlinedTextField(
                        value = viewModel.confirmPasswordInput,
                        onValueChange = { viewModel.confirmPasswordInput = it },
                        label = { Text("تأكيد كلمة المرور الجديدة") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            focusedLabelColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal
                        ),
                        leadingIcon = {
                            Icon(Icons.Filled.Key, contentDescription = "تأكيد الجديدة", tint = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.secondary else MediumTeal)
                        }
                    )

                    // Message alerts if any
                    viewModel.passwordChangeMessage?.let { msg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = msg,
                            color = if (viewModel.passwordChangeSuccess) GreenSuccess else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save (Tathbeet) button for password change
                    Button(
                        onClick = { viewModel.changePassword() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else DarkTeal,
                            contentColor = if (isSystemInDarkTheme()) MaterialTheme.colorScheme.onPrimary else Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "تثبيت كلمة السر الجديدة",
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "تثبيت كلمة السر الجديدة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
