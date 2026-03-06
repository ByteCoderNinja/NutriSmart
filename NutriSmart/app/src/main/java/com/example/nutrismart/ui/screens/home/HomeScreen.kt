package com.example.nutrismart.ui.screens.home

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.health.HealthConnectManager
import com.example.nutrismart.data.remote.MealDto
import com.example.nutrismart.notifications.EatingReminder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMealForDetails by remember { mutableStateOf<Pair<String, MealDto>?>(null) }
    var mealTypeForSwap by remember { mutableStateOf<String?>(null) }
    var showShoppingListBottomSheet by remember { mutableStateOf(false) }
    var showBonusSnackSheet by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val sessionManager = remember { SessionManager(context) }
    val wakeUpTimeStr = sessionManager.getWakeUpTime()
    val userWakeUpTime = remember(wakeUpTimeStr) { LocalTime.parse(wakeUpTimeStr) }

    val healthConnectManager = remember { HealthConnectManager(context) }
    val scope = rememberCoroutineScope()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {}

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    var hasHealthPermissions by remember { mutableStateOf(false) }
    var shouldCheckPermissions by remember { mutableStateOf(true) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = PermissionController.createRequestPermissionResultContract()
    ) { grantedPermissions ->
        if (grantedPermissions.containsAll(permissions)) {
            hasHealthPermissions = true
            scope.launch {
                val steps = healthConnectManager.readTodaySteps()
                val burned = healthConnectManager.readBurnedCalories(steps)
                viewModel.updateHealthData(steps, burned)
            }
        } else {
            hasHealthPermissions = false
        }
    }

    LaunchedEffect(uiState.waterConsumedMl) {
        if (uiState.waterConsumedMl > 0 && uiState.waterConsumedMl == uiState.waterGoalMl) {
            Toast.makeText(context, "Congratulations! You've reached your daily water goal! 💧", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(shouldCheckPermissions) {
        if (shouldCheckPermissions && healthConnectManager.isAvailable) {
            val healthConnectClient = HealthConnectClient.getOrCreate(context)
            val granted = healthConnectClient.permissionController.getGrantedPermissions()

            if (granted.containsAll(permissions)) {
                hasHealthPermissions = true
                scope.launch {
                    val steps = healthConnectManager.readTodaySteps()
                    val burned = healthConnectManager.readBurnedCalories(steps)
                    viewModel.updateHealthData(steps, burned)
                }
            } else {
                hasHealthPermissions = false
            }
            shouldCheckPermissions = false
        }
    }

    LaunchedEffect(Unit) {
        if (uiState.breakfast == null && UserSession.currentUserId != -1L) {
            viewModel.fetchTodayData()
        }
    }

    val breakfastRange = getRecommendedTimeRange(userWakeUpTime, "Breakfast")
    val lunchRange = getRecommendedTimeRange(userWakeUpTime, "Lunch")
    val dinnerRange = getRecommendedTimeRange(userWakeUpTime, "Dinner")
    val snackRange = getRecommendedTimeRange(userWakeUpTime, "Snack")

    LaunchedEffect(uiState.breakfast, uiState.lunch, uiState.dinner, uiState.snack) {
        if (uiState.breakfast != null) {
            val meals = listOf(
                Triple("Breakfast", uiState.breakfast, breakfastRange),
                Triple("Lunch", uiState.lunch, lunchRange),
                Triple("Dinner", uiState.dinner, dinnerRange),
                Triple("Snack", uiState.snack, snackRange)
            )

            meals.forEach { (type, mealDto, range) ->
                mealDto?.let { meal ->
                    if (!meal.consumed) {
                        val times = range.split(" - ")

                        if (times.size == 2) {
                            val startStr = times[0].trim()
                            val endStr = times[1].trim()

                            runCatching {
                                val startTime = LocalTime.parse(startStr)
                                val endTime = LocalTime.parse(endStr)

                                val startDateTime = LocalDateTime.of(LocalDate.now(), startTime)
                                val endDateTime = LocalDateTime.of(LocalDate.now(), endTime)

                                val startMillis = startDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                                val endMillis = endDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                                val currentMillis = System.currentTimeMillis()

                                if (startMillis > currentMillis) {
                                    scheduleMealNotification(context, type, startMillis, false)
                                }
                                if (endMillis > currentMillis) {
                                    scheduleMealNotification(context, type, endMillis, true)
                                }
                            }.onFailure { e ->
                                e.printStackTrace()
                            }
                        }
                    } else {
                        cancelMealNotification(context, type, false)
                        cancelMealNotification(context, type, true)
                    }
                }
            }
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    if (uiState.isLoading && uiState.breakfast == null && !isRefreshing) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading) {
            isRefreshing = false
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.fetchTodayData()

            if (hasHealthPermissions) {
                scope.launch {
                    val steps = healthConnectManager.readTodaySteps()
                    val burned = healthConnectManager.readBurnedCalories(steps)
                    viewModel.updateHealthData(steps, burned)
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
        ) {
            item {
                DateHeaderSection()
                Spacer(modifier = Modifier.height(16.dp))
            }

            item {
                MainStatsCard(
                    state = uiState,
                    hasPermissions = hasHealthPermissions,
                    onAddWaterClick = { viewModel.addWater() },
                    onRemoveWaterClick = { viewModel.removeWater() },
                    onStepsClick = {
                        if (!hasHealthPermissions) {
                            permissionsLauncher.launch(permissions)
                        }
                    },
                    onAdjustWeight = { delta -> viewModel.adjustWeight(delta) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                MacrosRow(state = uiState)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                Text(
                    text = "Today's Meal Plan",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val meals = listOf("Breakfast" to uiState.breakfast, "Lunch" to uiState.lunch, "Dinner" to uiState.dinner, "Snack" to uiState.snack)

                meals.forEach { (type, meal) ->
                    meal?.let {
                        val timeRange = getRecommendedTimeRange(userWakeUpTime, type)

                        Text(
                            text = timeRange,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 8.dp, bottom = 4.dp, top = 8.dp)
                        )

                        MealCardButton(
                            mealType = type,
                            meal = it,
                            onCardClick = { selectedMealForDetails = type to it },
                            onToggleConsume = { consumed ->
                                viewModel.toggleMeal(it.id, type, consumed)
                                if (consumed) {
                                    cancelMealNotification(context, type, false)
                                    cancelMealNotification(context, type, true)
                                }
                            },
                            onSwapClick = { mealTypeForSwap = type; viewModel.loadAlternatives(type) }
                        )
                    }
                }

                if (uiState.burnedCalories >= 100) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Bonus Meal (Earned!)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (uiState.bonusSnack != null) {
                        MealCardButton(
                            mealType = "Bonus Snack",
                            meal = uiState.bonusSnack!!,
                            onCardClick = { selectedMealForDetails = "Bonus Snack" to uiState.bonusSnack!! },
                            onToggleConsume = { consumed -> viewModel.toggleBonusSnackConsumed(consumed) },
                            onSwapClick = {
                                showBonusSnackSheet = true
                                viewModel.loadBonusSnacks()
                            }
                        )
                    } else {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showBonusSnackSheet = true
                                    viewModel.loadBonusSnacks()
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFE8F5E9))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Claim your Bonus Snack!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "You burned ${uiState.burnedCalories} kcal. Tap to choose a treat.",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clickable { showShoppingListBottomSheet = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF5DB056))
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "View Grocery List",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }

    if (showShoppingListBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showShoppingListBottomSheet = false }) {
            ShoppingListContent(uiState, viewModel)
        }
    }

    mealTypeForSwap?.let { type ->
        MealSwapBottomSheet(
            mealType = type,
            alternatives = uiState.alternatives,
            isLoading = uiState.isSwapping,
            onDismiss = { mealTypeForSwap = null },
            onSelected = { newMealId -> viewModel.swapMeal(type, newMealId); mealTypeForSwap = null }
        )
    }

    if (showBonusSnackSheet) {
        val bonusSheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)

        LaunchedEffect(uiState.isSwapping) {
            if (!uiState.isSwapping) {
                bonusSheetState.partialExpand()
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showBonusSnackSheet = false },
            sheetState = bonusSheetState
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
                Text("Choose your Bonus Snack", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Showing items under ${uiState.burnedCalories} kcal", fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isSwapping) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (uiState.availableBonusSnacks.isEmpty()) {
                    Text("No snacks available for this amount of calories.", modifier = Modifier.padding(16.dp))
                } else {
                    LazyColumn {
                        items(uiState.availableBonusSnacks) { meal ->
                            OutlinedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        viewModel.selectBonusSnack(meal)
                                        showBonusSnackSheet = false
                                    }
                            ) {
                                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(meal.name, fontWeight = FontWeight.Bold)
                                        Text("${meal.calories} kcal | P:${meal.protein} C:${meal.carbs} F:${meal.fat}", fontSize = 12.sp)
                                    }
                                    Icon(Icons.Default.ChevronRight, null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    selectedMealForDetails?.let { (type, meal) ->
        MealDetailBottomSheet(mealType = type, meal = meal, onDismiss = { selectedMealForDetails = null })
    }
}

@SuppressLint("ScheduleExactAlarm")
fun scheduleMealNotification(
    context: Context,
    mealType: String,
    timeInMillis: Long,
    isReminder: Boolean
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EatingReminder::class.java).apply {
        putExtra("MEAL_TYPE", mealType)
        putExtra("IS_REMINDER", isReminder)
    }

    val requestCode = mealType.hashCode() + (if (isReminder) 1 else 0)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent)
    }
}

fun cancelMealNotification(
    context: Context,
    mealType: String,
    isReminder: Boolean
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, EatingReminder::class.java)
    val requestCode = mealType.hashCode() + (if (isReminder) 1 else 0)

    val pendingIntent = PendingIntent.getBroadcast(
        context,
        requestCode,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    try {
        alarmManager.cancel(pendingIntent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
fun DateHeaderSection() {
    val formatter = DateTimeFormatter.ofPattern("EEEE, d MMM")
    val todayDate = LocalDate.now().format(formatter)

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = todayDate,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun MainStatsCard(
    state: HomeUiState,
    hasPermissions: Boolean,
    onAddWaterClick: () -> Unit,
    onRemoveWaterClick: () -> Unit,
    onStepsClick: () -> Unit,
    onAdjustWeight: (Double) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Calories Remaining", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${state.caloriesRemaining}", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStepsClick() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("Steps", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (hasPermissions) {
                        Text("${state.steps} / ${state.stepsGoal}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Text("Tap to connect Health", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            WaterTrackerSection(
                state.waterConsumedMl,
                state.waterGoalMl,
                state.glassSizeMl,
                onAddWaterClick,
                onRemoveClick = onRemoveWaterClick
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            WeightTrackerSection(
                currentWeight = state.weight,
                onAdjustWeight = onAdjustWeight
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaterTrackerSection(
    consumedMl: Int,
    goalMl: Int,
    glassSize: Int,
    onAddClick: () -> Unit,
    onRemoveClick: () -> Unit
) {
    val consumedGlassesCount = consumedMl / glassSize
    val waterColor = Color(0xFF2196F3)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Water Intake", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text("$consumedMl ml / $goalMl ml", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(12.dp))

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(consumedGlassesCount) {
                Icon(
                    imageVector = Icons.Filled.LocalDrink,
                    contentDescription = "Consumed glass",
                    tint = waterColor,
                    modifier = Modifier
                        .padding(horizontal = 6.dp)
                        .size(48.dp)
                        .clickable { onRemoveClick() }
                )
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(48.dp)
                    .clickable { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add water",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MacrosRow(state: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val carbsProgress = if (state.carbsGoal > 0) state.carbsConsumed.toFloat() / state.carbsGoal else 0f
        val proteinProgress = if (state.proteinGoal > 0) state.proteinConsumed.toFloat() / state.proteinGoal else 0f
        val fatProgress = if (state.fatGoal > 0) state.fatConsumed.toFloat() / state.fatGoal else 0f

        MacroCard(modifier = Modifier.weight(1f), title = "Carbs", value = "${state.carbsConsumed}g", progress = carbsProgress, color = Color(0xFF2881B4))
        MacroCard(modifier = Modifier.weight(1f), title = "Protein", value = "${state.proteinConsumed}g", progress = proteinProgress, color = Color(0xFF5DB056))
        MacroCard(modifier = Modifier.weight(1f), title = "Fat", value = "${state.fatConsumed}g", progress = fatProgress, color = Color(0xFFFFA726))
    }
}

@Composable
fun MacroCard(modifier: Modifier = Modifier, title: String, value: String, progress: Float, color: Color) {
    OutlinedCard(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun MealCardButton(
    mealType: String,
    meal: MealDto,
    onCardClick: () -> Unit,
    onToggleConsume: (Boolean) -> Unit,
    onSwapClick: () -> Unit
) {
    val isConsumed = meal.consumed
    val cardAlpha = if (isConsumed) 0.5f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() }
            .alpha(cardAlpha),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mealType,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Swap meal",
                        modifier = Modifier.size(16.dp).clickable { onSwapClick() },
                        tint = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = meal.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${meal.calories} kcal", fontSize = 13.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("•", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("P: ${meal.protein}g  C: ${meal.carbs}g  F: ${meal.fat}g", fontSize = 13.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            CircularCheckbox(
                checked = isConsumed,
                onCheckedChange = { checked -> onToggleConsume(checked) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealSwapBottomSheet(mealType: String, alternatives: List<MealDto>, isLoading: Boolean, onDismiss: () -> Unit, onSelected: (Long) -> Unit) {
    val sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = false)

    LaunchedEffect(isLoading) {
        if (!isLoading) {
            sheetState.partialExpand()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp).padding(bottom = 32.dp)) {
            Text(
                "Choose another $mealType",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier
                    .height(16.dp)
            )
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                LazyColumn {
                    items(alternatives) { meal ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onSelected(meal.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                ) {
                                    Text(
                                        meal.name,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("${meal.calories} kcal | P:${meal.protein} C:${meal.carbs} F:${meal.fat}", fontSize = 12.sp)
                                }
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShoppingListContent(state: HomeUiState, viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            "Shopping List",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (state.shoppingList?.items.isNullOrEmpty()) {
            Text("Your shopping list is empty.")
        } else {
            LazyColumn {
                items(state.shoppingList!!.items.sortedBy { it.id }) { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = item.checked,
                            onCheckedChange = { viewModel.toggleShoppingListItem(item.id, it) }
                        )
                        Column {
                            Text(item.name, fontSize = 16.sp)
                            Text(item.category, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealDetailBottomSheet(
    mealType: String,
    meal: MealDto,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
        ) {
            Text(
                text = mealType.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF5DB056),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = meal.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 32.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${meal.calories}",
                    fontSize = 56.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5DB056)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "kcal",
                    fontSize = 20.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MacroDetailItem("Carbs", "${meal.carbs}g", Color(0xFF2881B4))
                MacroDetailItem("Protein", "${meal.protein}g", Color(0xFF5DB056))
                MacroDetailItem("Fat", "${meal.fat}g", Color(0xFFFFA726))
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.RestaurantMenu, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Ingredients & Quantity", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = meal.quantityDetails ?: "No quantity details available.",
                    modifier = Modifier.padding(20.dp),
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

@Composable
fun MacroDetailItem(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(95.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CircularCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (checked) Color(0xFF4CAF50) else Color.Transparent
    val borderColor = if (checked) Color(0xFF4CAF50) else Color.Gray

    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(2.dp, borderColor, CircleShape)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun getRecommendedTimeRange(wakeUpTime: LocalTime, mealType: String): String {
    val formatter = DateTimeFormatter.ofPattern("HH:mm")

    val (startOffset, endOffset) = when (mealType.uppercase()) {
        "BREAKFAST" -> Pair(1L, 2L)
        "LUNCH" -> Pair(5L, 6L)
        "SNACK" -> Pair(8L, 9L)
        "DINNER" -> Pair(10L, 11L)
        "BONUS SNACK" -> Pair(12L, 13L)
        else -> Pair(0L, 0L)
    }

    val startTime = wakeUpTime.plusHours(startOffset)
    val endTime = wakeUpTime.plusHours(endOffset)

    return "${startTime.format(formatter)} - ${endTime.format(formatter)}"
}

@Composable
fun WeightTrackerSection(
    currentWeight: Double,
    onAdjustWeight: (Double) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Weight", fontSize = 16.sp, fontWeight = FontWeight.Medium)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            RepeatingIconButton(
                icon = Icons.Default.Remove,
                onClick = { onAdjustWeight(-0.1) }
            )

            Text(
                text = "${currentWeight} kg",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            RepeatingIconButton(
                icon = Icons.Default.Add,
                onClick = { onAdjustWeight(0.1) }
            )
        }
    }
}

@Composable
fun RepeatingIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onClick()

                        val job = coroutineScope.launch {
                            delay(400)
                            while (true) {
                                onClick()
                                delay(100)
                            }
                        }

                        tryAwaitRelease()
                        job.cancel()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
    }
}