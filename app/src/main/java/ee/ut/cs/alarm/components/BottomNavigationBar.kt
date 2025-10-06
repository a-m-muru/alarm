package ee.ut.cs.alarm.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ee.ut.cs.alarm.navigation.Screen

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        NavigationBarItem(
            icon = { 
                Text(
                    text = "Alarms",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            label = { Text("Alarms") },
            selected = currentRoute == Screen.Alarms.route,
            onClick = { onNavigate(Screen.Alarms.route) }
        )
        
        NavigationBarItem(
            icon = { 
                Text(
                    text = "+",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            label = { Text("Add") },
            selected = currentRoute == Screen.AddAlarm.route,
            onClick = { onNavigate(Screen.AddAlarm.route) }
        )
        
        NavigationBarItem(
            icon = { 
                Text(
                    text = "About",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            },
            label = { Text("About") },
            selected = currentRoute == Screen.About.route,
            onClick = { onNavigate(Screen.About.route) }
        )
    }
}
