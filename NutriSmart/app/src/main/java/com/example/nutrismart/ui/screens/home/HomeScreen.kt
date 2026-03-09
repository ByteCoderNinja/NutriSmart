package com.example.nutrismart.ui.screens.home

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nutrismart.data.SessionManager
import com.example.nutrismart.data.UserSession
import com.example.nutrismart.data.health.HealthConnectManager
import com.example.nutrismart.data.remote.MealDto
import com.example.nutrismart.ui.screens.home.components.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

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
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
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
                    isImperial = uiState.isImperial,
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
                MacrosRow(
                    state = uiState,
                    isImperial = uiState.isImperial
                )
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
        val bonusSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

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
