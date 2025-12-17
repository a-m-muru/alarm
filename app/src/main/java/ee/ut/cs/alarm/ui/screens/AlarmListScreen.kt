package ee.ut.cs.alarm.ui.screens

import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.Animatable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.Alarm
import ee.ut.cs.alarm.service.AlarmScheduler
import ee.ut.cs.alarm.ui.components.AlarmCard
import ee.ut.cs.alarm.ui.components.EditAlarmDialog
import ee.ut.cs.alarm.ui.navigation.Screen
import ee.ut.cs.alarm.ui.viewmodel.AlarmListViewModel
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    onNavigate: (String) -> Unit,
    vm: AlarmListViewModel,
    alarmScheduler: AlarmScheduler,
) {
    val alarms by vm.items.collectAsState()
    val streak by vm.streak.collectAsState()
    val prevStreak by vm.previousStreak.collectAsState()
    val context = LocalContext.current

    /**
     * Show toast with time till alarm
     * @param secondsToAlarm Time till alarm in seconds. Negative values are ignored.
     */
    fun showTimeTillAlarmToast(secondsToAlarm: Long) {
        if (secondsToAlarm < 0) return

        val days = secondsToAlarm / (24 * 3600)
        val hours = (secondsToAlarm % (24 * 3600)) / 3600
        val minutes = (secondsToAlarm % 3600) / 60

        val text =
            listOfNotNull(
                if (days > 0) "$days day${if (days > 1) "s" else ""}" else null,
                if (hours > 0) "$hours hour${if (hours > 1) "s" else ""}" else null,
                if (minutes > 0) "$minutes minute${if (minutes > 1) "s" else ""}" else null,
            ).joinToString(", ").ifEmpty { "less than a minute" }

        Toast.makeText(context, "$text till alarm", Toast.LENGTH_LONG).show()
    }

    var showDialog by remember { mutableStateOf(false) }
    var editableAlarm by remember { mutableStateOf(Alarm()) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AlarmTopBar(onNavigate, vm)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editableAlarm =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Alarm(time = LocalTime.now().toSecondOfDay().toUInt() + 60u)
                        } else {
                            Alarm(
                                time =
                                    System
                                        .currentTimeMillis()
                                        .toUInt() / 1000u % (24u * 3600u) + 60u,
                            )
                        }
                    showDialog = true
                },
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            StreakBox(streak, prevStreak)
            if (alarms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No alarms set\nTap + to add your first alarm",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = 48.dp),
                ) {
                    items(
                        items = alarms,
                        key = { it.id },
                    ) { alarm ->
                        AlarmCard(
                            alarm = alarm,
                            cardToggled = { enabled ->
                                val al = alarm.copy(enabled = enabled)
                                vm.updateAlarm(al)
                                if (enabled) {
                                    val secondsToAlarm = alarmScheduler.scheduleAlarm(al)
                                    showTimeTillAlarmToast(secondsToAlarm)
                                } else {
                                    alarmScheduler.cancelAlarm(alarm.id)
                                }
                            },
                            onDelete = {
                                alarmScheduler.cancelAlarm(alarm.id)
                                vm.removeAlarm(alarm)
                            },
                            onEdit = {
                                showDialog = true
                                editableAlarm = alarm.copy(enabled = true)
                            },
                        )
                    }
                }
            }

            if (showDialog) {
                EditAlarmDialog(
                    alarm = editableAlarm,
                    onDismissRequest = { showDialog = false },
                    onSaveRequest = { alarmToSave ->
                        if (vm.hasAlarm(alarmToSave)) {
                            alarmScheduler.cancelAlarm(alarmToSave.id)
                            vm.updateAlarm(alarmToSave)
                        } else {
                            vm.addAlarm(alarmToSave)
                        }
                        val secondsToAlarm = alarmScheduler.scheduleAlarm(alarmToSave)
                        showTimeTillAlarmToast(secondsToAlarm)
                        showDialog = false
                    },
                    alarmScheduler = alarmScheduler,
                )
            }
        }
    }
}

// thanks chat geepeetee
@Composable
fun StreakBox(
    streak: Int,
    previousStreak: Int,
    modifier: Modifier = Modifier,
) {
    Log.d("StreakBox", "streak: $streak, prev: $previousStreak")
    var increased by remember { mutableStateOf(false) }
    increased = streak > previousStreak
    val primColor = MaterialTheme.colorScheme.inversePrimary
    val color =
        when {
            increased -> Color(0xFF4CAF50) // green
            streak < previousStreak -> Color(0xFFF44336) // red
            else -> primColor // neutral
        }

    var streakLevel by remember { mutableStateOf("") }
    var congratulation by remember { mutableStateOf("") }

    val bgColor = remember { Animatable(color) }

    val offset = remember { Animatable(0F) }
    val alpha = remember { Animatable(0F) }
    val scale = remember { Animatable(0F) }
    val rotation = remember { Animatable(0F) }
    val shakeX = remember { Animatable(0F) }
    val shakeY = remember { Animatable(0F) }

    val nonePair = Pair(0, Pair(0, 0))
    val streakLevelDefs =
        arrayOf(
            Pair(1, Pair("", "")),
            Pair(3, Pair("Your Streak is Bronze Level", "")),
            Pair(7, Pair("Your Streak is Silver Level", "A weekful of wakefulness!")),
            Pair(11, Pair("Your Streak is Ice Level", "Chill!")),
            Pair(14, Pair("Your Streak is Gold Level", "Two week boss!")),
            Pair(21, Pair("Your Streak is Platinum Level", "Nice!")),
            Pair(26, Pair("Your Streak is Rose Gold Level", "Well done!")),
            Pair(31, Pair("Your Streak is Cobalt Level", "Oooh!")),
            Pair(41, Pair("Your Streak is Flesh Titanium Level", "You are devoted!")),
            Pair(50, Pair("Your Streak is Matte Gold Level", "Epic Streak")),
            Pair(65, Pair("Your Streak is Flaming Cheese Level", "Cheesed to have you here!")),
            Pair(80, Pair("Your Streak is Granite Level", "You rock!")),
            Pair(100, Pair("Your Streak is Green Diamond Heartburn Level", "Beauticious...")),
            Pair(
                120,
                Pair(
                    "Your Streak is Perfect Blue Spectrogram Level",
                    "Your eyes open to new things every day...",
                ),
            ),
            Pair(150, Pair("Your Streak is Ruby Basalt Papaya Level", "Extreme L33T Str33k")),
            Pair(200, Pair("Your Streak is well done Level", "Meat... Meat... Meat... Meat...")),
            Pair(
                256,
                Pair(
                    "Your Streak is Byte Overflow Runtime Error Level",
                    "0xFF!! See you next time!!",
                ),
            ),
            Pair(300, Pair("Your Streak is Peppy Banana Level", "BANANATASTIC")),
            Pair(
                365,
                Pair(
                    "Your Streak is Ancient Level",
                    "One whole year of wakefulness! Thank you for sticking with ALARM++!!",
                ),
            ),
            Pair((365 * 1.5).toInt(), Pair("Double Kill", "2x score")),
            Pair((365 * 2.0).toInt(), Pair("Triple Kill", "3x score")),
            Pair((365 * 3.0).toInt(), Pair("Monster Kill", "4x score")),
            Pair((365 * 4.0).toInt(), Pair("Kill Kill", "Ultimate Uberdeath")),
            Pair(
                (365 * 5.0).toInt(),
                Pair("Five year anniversary stream", "We play Alarm++ on stream... NOT"),
            ),
            Pair((365 * 6.0).toInt(), Pair("Six Years", "That is too long to comprehend.")),
            Pair((365 * 7.0).toInt(), Pair("Seven Years", "That is too too long to comprehend.")),
            Pair(
                (365 * 8.0).toInt(),
                Pair("Eight Years", "That is too too too long to comprehend."),
            ),
            Pair(
                (365 * 9.0).toInt(),
                Pair("Nine Years", "That is too too too too long to comprehend."),
            ),
            Pair((365 * 10.0).toInt(), Pair("Ten Years", "Thank you.")),
        )

    LaunchedEffect(streak, previousStreak) {
        if (streak < previousStreak) {
            streakLevel = "Your Streak is Stupid Level"
            congratulation = "Streak lost..."
        } else {
            for (i in streakLevelDefs.size - 1 downTo 0) {
                val def = streakLevelDefs[i]
                val nextDef = if (i < streakLevelDefs.size - 1) streakLevelDefs[i + 1] else nonePair
                if (streak >= def.first) {
                    streakLevel = def.second.first
                    var congrats = def.second.second
                    if (nextDef != nonePair) congrats += " ($streak/${nextDef.first})"
                    congratulation = congrats
                    break
                }
            }
        }
    }

    LaunchedEffect(streak, previousStreak) {
        if (streak > previousStreak) {
            bgColor.snapTo(color)
            offset.animateTo(-20F)
            offset.animateTo(0F, animationSpec = tween())
            bgColor.animateTo(primColor, animationSpec = tween())
            increased = true
            alpha.animateTo(1F)
        } else if (streak < previousStreak) {
            bgColor.snapTo(color)
            offset.animateTo(12F)
            offset.animateTo(0F, animationSpec = tween(delayMillis = 300, durationMillis = 1000))
            bgColor.animateTo(
                Color(0xff666666),
                animationSpec = tween(durationMillis = 4000, delayMillis = 1000),
            )
            increased = false
            alpha.animateTo(1F)
        } else {
            alpha.animateTo(1F)
        }
    }
    LaunchedEffect(streak, previousStreak) {
        if (streak > previousStreak) {
            scale.snapTo(2F)
            scale.animateTo(1.99F, animationSpec = tween(durationMillis = 120))
            scale.animateTo(1F, animationSpec = tween(durationMillis = 900))
        } else if (streak < previousStreak) {
            scale.snapTo(2F)
            for (i in 0..8) {
                scale.animateTo(1.99F, animationSpec = tween(durationMillis = 60))
                scale.animateTo(1.66F, animationSpec = tween(durationMillis = 60))
            }
            scale.animateTo(1F, animationSpec = tween(durationMillis = 800))
        }
    }
    LaunchedEffect(streak, previousStreak) {
        if (streak > previousStreak) {
            rotation.animateTo(15F, animationSpec = tween(durationMillis = 300))
            rotation.animateTo(0F)
        } else if (streak < previousStreak) {
            rotation.animateTo(-12F, animationSpec = tween(durationMillis = 600))
            rotation.animateTo(-14F, animationSpec = tween(durationMillis = 700, delayMillis = 50))
            rotation.animateTo(0F, animationSpec = tween(durationMillis = 700))
        }
    }
    LaunchedEffect(streak, previousStreak) {
        if (streak < previousStreak) {
            for (i in 30 downTo 0) {
                shakeX.animateTo(
                    i / 1F * (kotlin.random.Random.nextFloat() * 2 - 1),
                    animationSpec = tween(durationMillis = 20),
                )
                shakeY.animateTo(
                    i / 1F * (kotlin.random.Random.nextFloat() * 2 - 1),
                    animationSpec = tween(durationMillis = 20),
                )
            }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = bgColor.value, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 16.dp)
                .graphicsLayer(
                    translationX = shakeX.value,
                    translationY = shakeY.value,
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Streak: $streak",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.width(12.dp))

            if (streak != previousStreak) {
                Icon(
                    if (increased) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (increased) "Increased" else "Decreased",
                    tint = Color.White,
                    modifier =
                        Modifier
                            .offset(y = offset.value.dp)
                            .scale(scale.value)
                            .rotate(rotation.value),
                )
            }

            Column(
                Modifier
                    .weight(1F)
                    .alpha(alpha.value),
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    streakLevel,
                    textAlign = TextAlign.Right,
                )
                Text(
                    congratulation,
                    textAlign = TextAlign.Right,
                    fontSize = 10.sp,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmTopBar(
    onNavigate: (String) -> Unit,
    vm: AlarmListViewModel,
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("All alarms") },
        actions = {
            Box(contentAlignment = Alignment.TopStart) {
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    DropdownMenuItem(
                        text = { Text("About") },
                        onClick = { onNavigate(Screen.About.route) },
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { onNavigate(Screen.Settings.route) },
                    )
                }
            }
        },
    )
}
