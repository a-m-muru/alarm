package ee.ut.cs.alarm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About alarm++",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Simple alarm clock android app to help you ward off sleep for good 😈",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()

                Text(
                    text = "Development Team",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val teamMembers = listOf(
                    "Lead Programmer - Karmen Ott",
                    "Researcher - Rasmus Lille",
                    "Editor - Kristjan Karl Pevgonen",
                    "Project Lead - Aksel Martin Muru",
                    "Presenter - Kauri Remm"
                )

                teamMembers.forEach { member ->
                    Text(
                        text = member,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Divider()

                Text(
                    text = "Planned Features",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                val features = listOf(
                    "• Set alarms for specific weekdays and times",
                    "• Save multiple alarms",
                    "• Enable/disable alarms",
                    "• Minigames & tasks to turn off alarms",
                    "• Streak system for daily wake-ups",
                    "• Custom ringtones",
                    "• Accessibility options",
                    "• Simple tutorial system"
                )

                features.forEach { feature ->
                    Text(
                        text = feature,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Divider()

                Text(
                    text = "Version 1.0",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
