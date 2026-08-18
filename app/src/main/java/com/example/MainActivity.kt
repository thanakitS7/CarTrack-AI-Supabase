package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.TrackingViewModel
import com.example.ui.screens.AdminUserManagementScreen
import com.example.ui.screens.AlertLogScreen
import com.example.ui.screens.GeofenceRouteScreen
import com.example.ui.screens.LiveTrackingScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PlaybackScreen
import com.example.ui.screens.VehicleSelectionScreen
import androidx.compose.material.icons.filled.DirectionsCar
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AutoGuardApp()
            }
        }
    }
}

@Composable
fun AutoGuardApp() {
    val viewModel: TrackingViewModel = viewModel()
    val currentUser by viewModel.currentUser.collectAsState()
    val activeVehicle by viewModel.activeVehicle.collectAsState()
    var selectedScreen by remember { mutableStateOf("LIVE_MAP") }
    var isVehicleSelected by remember { mutableStateOf(false) }

    if (currentUser == null) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = {
                isVehicleSelected = false
            }
        )
    } else if (!isVehicleSelected) {
        VehicleSelectionScreen(
            viewModel = viewModel,
            onVehicleConfirmed = { vehicle ->
                isVehicleSelected = true
                selectedScreen = "LIVE_MAP"
            },
            onLogout = {
                viewModel.logout()
                isVehicleSelected = false
            }
        )
    } else {
        val user = currentUser!!
        val roleColor = when (user.role.uppercase()) {
            "MANAGER" -> Color(0xFFF59E0B) // Amber
            "ADMIN" -> Color(0xFFA855F7) // Purple
            "DISPATCHER" -> Color(0xFF06B6D4) // Cyan
            else -> Color(0xFF3B82F6) // Blue
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Surface(
                    color = Color(0xFF0F172A),
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = roleColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(30.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = when (user.role.uppercase()) {
                                            "ADMIN" -> Icons.Default.AdminPanelSettings
                                            "MANAGER" -> Icons.Default.Shield
                                            else -> Icons.Default.Person
                                        },
                                        contentDescription = null,
                                        tint = roleColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(6.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(3.dp),
                                        color = roleColor.copy(alpha = 0.25f)
                                    ) {
                                        Text(
                                            text = user.role,
                                            color = roleColor,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "🏢 ${user.officeName}",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { isVehicleSelected = false },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF38BDF8)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("สลับรถ", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.logout() },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF87171)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("ออก", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            bottomBar = {
                NavigationBar(
                    containerColor = Color(0xFFF3EDF7),
                    contentColor = Color(0xFF1D1B20),
                    tonalElevation = 3.dp
                ) {
                    NavigationBarItem(
                        selected = selectedScreen == "LIVE_MAP",
                        onClick = { selectedScreen = "LIVE_MAP" },
                        icon = { Icon(imageVector = Icons.Default.Map, contentDescription = "Live GPS") },
                        label = { Text("แผนที่สด", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF6750A4),
                            indicatorColor = Color(0xFF6750A4),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == "ALERTS",
                        onClick = { selectedScreen = "ALERTS" },
                        icon = { Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Alerts") },
                        label = { Text("เตือนความเร็ว", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF6750A4),
                            indicatorColor = Color(0xFF6750A4),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == "PLAYBACK",
                        onClick = { selectedScreen = "PLAYBACK" },
                        icon = { Icon(imageVector = Icons.Default.History, contentDescription = "Playback") },
                        label = { Text("ย้อนหลัง", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF6750A4),
                            indicatorColor = Color(0xFF6750A4),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F)
                        )
                    )

                    NavigationBarItem(
                        selected = selectedScreen == "USERS",
                        onClick = { selectedScreen = "USERS" },
                        icon = { Icon(imageVector = Icons.Default.People, contentDescription = "Users") },
                        label = { Text("จัดการ User", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFF6750A4),
                            indicatorColor = Color(0xFF6750A4),
                            unselectedIconColor = Color(0xFF49454F),
                            unselectedTextColor = Color(0xFF49454F)
                        )
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding)) {
                when (selectedScreen) {
                    "LIVE_MAP" -> LiveTrackingScreen(viewModel = viewModel)
                    "ALERTS" -> AlertLogScreen(viewModel = viewModel)
                    "PLAYBACK" -> PlaybackScreen(viewModel = viewModel)
                    "USERS" -> AdminUserManagementScreen(viewModel = viewModel)
                }
            }
        }
    }
}


