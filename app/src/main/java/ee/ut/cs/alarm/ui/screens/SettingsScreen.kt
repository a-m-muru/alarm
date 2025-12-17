package ee.ut.cs.alarm.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.data.AlarmGame
import ee.ut.cs.alarm.data.AlarmTrack
import ee.ut.cs.alarm.ui.navigation.Screen
import ee.ut.cs.alarm.ui.viewmodel.UserPrefsViewModel

// dark mode / light mode, allowed games

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    vm: UserPrefsViewModel,
    onNavigation: (String) -> Unit,
) {
    val prefs by vm.prefs.collectAsState()

    var isTrackSelectionExpanded by remember { mutableStateOf(false) }
    var isGameSelectionExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = {
                        onNavigation(Screen.AlarmList.route)
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Go back")
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding),
        ) {
            item {
                Text("Allowed tracks:", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            items(AlarmTrack.entries) { track ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        checked = prefs.allowedTracks.contains(track),
                        onCheckedChange = {
                            if (!it) {
                                vm.updatePrefs(prefs.copy(allowedTracks = prefs.allowedTracks - track))
                            } else {
                                vm.updatePrefs(prefs.copy(allowedTracks = prefs.allowedTracks + track))
                            }
                        },
                    )
                    Text(track.value)
                }
            }

            item {
                Text(
                    "Allowed games:",
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(AlarmGame.entries) { game ->
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(
                        enabled = false,
                        checked = prefs.allowedGames.contains(game),
                        onCheckedChange = {
                            if (!it) {
                                vm.updatePrefs(prefs.copy(allowedGames = prefs.allowedGames - game))
                            } else {
                                vm.updatePrefs(prefs.copy(allowedGames = prefs.allowedGames + game))
                            }
                        },
                    )
                    Text(game.value)
                }
            }
        }
    }
}
