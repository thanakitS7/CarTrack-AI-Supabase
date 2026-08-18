package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.example.data.UserEntity
import com.example.data.VehicleEntity
import com.example.ui.TrackingViewModel

@Composable
fun AdminUserManagementScreen(viewModel: TrackingViewModel) {
    val context = LocalContext.current
    val allVehicles by viewModel.allVehicles.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val activeVehicle by viewModel.activeVehicle.collectAsState()
    val isGoogleSheetsSyncEnabled by viewModel.isGoogleSheetsSyncEnabled.collectAsState()
    val googleSheetsUrl by viewModel.googleSheetsUrl.collectAsState()

    val currentUser by viewModel.currentUser.collectAsState()

    var isAdminUnlocked by remember(currentUser) {
        mutableStateOf(currentUser?.role?.uppercase() in listOf("ADMIN", "MANAGER", "SUPERADMIN"))
    }
    var adminPinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var vehicleToEdit by remember { mutableStateOf<VehicleEntity?>(null) }
    var vehicleToDelete by remember { mutableStateOf<VehicleEntity?>(null) }
    var showAddVehicleDialog by remember { mutableStateOf(false) }

    var selectedAdminTab by remember { mutableStateOf("VEHICLES") } // "VEHICLES" or "USERS"
    var showSqlSetupDialog by remember { mutableStateOf(false) }
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    val filteredVehicles = allVehicles.filter { v ->
        v.name.contains(searchQuery, ignoreCase = true) ||
        v.licensePlate.contains(searchQuery, ignoreCase = true) ||
        v.driverName.contains(searchQuery, ignoreCase = true)
    }

    // Unlocked Admin View vs Locked Protection Screen
    if (!isAdminUnlocked) {
        // Locked Screen asking for Admin Passcode / PIN
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF6750A4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "🔒 ระบบหลังบ้านสำหรับ Admin",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ผู้ใช้ทั่วไป (User) ไม่สามารถแก้ไขข้อมูลได้ กรุณากรอกรหัส Admin PIN เพื่อปลดล็อกเข้าสู่ระบบจัดการ:",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            pinError = false
                        },
                        label = { Text("กรอกรหัส Admin PIN") },
                        leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF38BDF8)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = pinError,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF38BDF8),
                            unfocusedBorderColor = Color(0xFF334155),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (pinError) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "❌ รหัส PIN ไม่ถูกต้อง! กรุณาลองใหม่อีกครั้ง",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val validPins = setOf("0511", "1234", "9999", "0000", "1111")
                            if (adminPinInput.trim() in validPins) {
                                isAdminUnlocked = true
                                Toast.makeText(context, "🔓 เข้าสู่ระบบ Admin เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                            } else {
                                pinError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ปลดล็อกเข้าสู่ระบบ Admin", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "🔑 รหัส PIN เริ่มต้น: 0511 หรือ 1234",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 หมายเหตุ: ข้อมูลที่ Admin แก้ไขในหน้านี้ จะเชื่อมโยงไปแสดงที่หน้าแอพของผู้ใช้รถ (User) และส่งต่อไปยัง Google Sheet ทันทีแบบ Real-time",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Unlocked Admin Panel
        Scaffold(
            containerColor = Color(0xFF0F172A),
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddVehicleDialog = true },
                    containerColor = Color(0xFF6750A4),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("เพิ่มรถ/คนขับ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // Header Title Card
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(Color(0xFF6750A4), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "ระบบหลังบ้าน Admin",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "จัดการข้อมูลผู้ใช้รถ / พนักงานขับรถ & ทะเบียนรถ",
                                        color = Color.LightGray,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    isAdminUnlocked = false
                                    adminPinInput = ""
                                    Toast.makeText(context, "🔒 ล็อกระบบ Admin เรียบร้อย", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = "Lock Admin", tint = Color(0xFFEF4444))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Realtime Connection Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "⚡ เชื่อมโยง Real-time: แก้ไขที่นี่ อัปเดตไปยังหน้า User และ Supabase ทันที",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Supabase Cloud Sync Card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF334155),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "⚡ Supabase Cloud Realtime Connection",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "ซิงค์และบันทึกประวัติการใช้รถและพิกัด GPS ตรงกับฐานข้อมูล Supabase 100%",
                                    color = Color.LightGray,
                                    fontSize = 11.sp,
                                    maxLines = 2,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = {
                                            viewModel.syncVehiclesFromCloud { _, msg ->
                                                viewModel.syncUsersFromCloud { _, msg2 ->
                                                    Toast.makeText(context, "$msg\n$msg2", Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("ดึงข้อมูล Supabase", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    Button(
                                        onClick = { showSqlSetupDialog = true },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("🐘 คำสั่ง SQL สร้าง DB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Selector for Vehicles vs Users
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedAdminTab == "VEHICLES") Color(0xFF0284C7) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedAdminTab = "VEHICLES" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🚘 ยานพาหนะ (${allVehicles.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (selectedAdminTab == "USERS") Color(0xFF10B981) else Color.Transparent,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { selectedAdminTab = "USERS" }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("👥 ฐานข้อมูล User (${allUsers.size})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ค้นหาชื่อคนขับ, ชื่อรถ, เลขทะเบียน หรือ สังกัด...", color = Color.Gray, fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedAdminTab == "VEHICLES") {
                    Text(
                        text = "รายการยานพาหนะทั้งหมด (${filteredVehicles.size} รายการ):",
                        color = Color.LightGray,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredVehicles.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("❌ ไม่พบข้อมูลรถตรงกับที่ค้นหา", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredVehicles, key = { it.id }) { vehicle ->
                                val isSelected = activeVehicle?.id == vehicle.id

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF182232)
                                    ),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8)) else null,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.DirectionsCar,
                                                    contentDescription = null,
                                                    tint = if (isSelected) Color(0xFF38BDF8) else Color.Gray,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = vehicle.name,
                                                        color = Color.White,
                                                        fontSize = 15.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "ทะเบียน: ${vehicle.licensePlate} • รุ่น/ปี: ${vehicle.modelYear}",
                                                        color = Color.LightGray,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = "🏢 ${vehicle.officeName} • 📍 กลุ่มจังหวัด: ${vehicle.provinceGroup}",
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { vehicleToEdit = vehicle },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit Vehicle",
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { vehicleToDelete = vehicle },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete Vehicle",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = null,
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "👤 ผู้ใช้รถ/คนขับ: ",
                                                        color = Color.Gray,
                                                        fontSize = 12.sp
                                                    )
                                                    Text(
                                                        text = vehicle.driverName,
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                if (isSelected) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF0284C7)
                                                    ) {
                                                        Text(
                                                            text = "กำลังแสดงในแอพ User",
                                                            color = Color.White,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else {
                                                    TextButton(
                                                        onClick = {
                                                            viewModel.selectVehicle(vehicle.id)
                                                            Toast.makeText(context, "เลือก ${vehicle.name} (${vehicle.licensePlate}) ให้หน้า User แสดงผลเรียบร้อย", Toast.LENGTH_SHORT).show()
                                                        },
                                                        modifier = Modifier.height(28.dp)
                                                    ) {
                                                        Text("เลือกให้ User ใช้งาน", fontSize = 11.sp, color = Color(0xFF38BDF8))
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "สถานะ: ${vehicle.status} • GPS: ${String.format("%.4f", vehicle.currentLat)}, ${String.format("%.4f", vehicle.currentLng)}",
                                                color = Color.Gray,
                                                fontSize = 10.sp
                                            )

                                            Button(
                                                onClick = {
                                                    viewModel.selectVehicle(vehicle.id)
                                                    Toast.makeText(context, "เลือกคัน ${vehicle.driverName} (${vehicle.licensePlate}) แล้ว", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155)),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(30.dp)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("เลือกคันนี้", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // USERS TAB
                    val filteredUsers = allUsers.filter { u ->
                        u.name.contains(searchQuery, ignoreCase = true) ||
                        u.username.contains(searchQuery, ignoreCase = true) ||
                        u.phone.contains(searchQuery, ignoreCase = true) ||
                        u.officeName.contains(searchQuery, ignoreCase = true) ||
                        u.provinceGroup.contains(searchQuery, ignoreCase = true) ||
                        u.role.contains(searchQuery, ignoreCase = true)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ฐานข้อมูล User ใน Supabase (${filteredUsers.size} คน):",
                            color = Color.LightGray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = { showAddUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ เพิ่ม User ใหม่", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (filteredUsers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("❌ ยังไม่มีข้อมูล User ตรงตามคำค้นหา", color = Color.Gray, fontSize = 14.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = { showAddUserDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                                ) {
                                    Text("+ เพิ่ม User ใหม่ลง Supabase")
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredUsers, key = { it.id }) { user ->
                                val roleColor = when (user.role.uppercase()) {
                                    "MANAGER" -> Color(0xFFF59E0B) // Amber
                                    "ADMIN" -> Color(0xFFA855F7) // Purple
                                    "DISPATCHER" -> Color(0xFF06B6D4) // Cyan
                                    else -> Color(0xFF3B82F6) // Blue for DRIVER
                                }
                                val roleLabel = when (user.role.uppercase()) {
                                    "MANAGER" -> "👑 MANAGER (ผู้จัดการพื้นที่)"
                                    "ADMIN" -> "🛡️ ADMIN (ผู้ดูแลระบบ)"
                                    "DISPATCHER" -> "🚚 DISPATCHER (ผู้จัดเส้นทาง)"
                                    else -> "👤 DRIVER (คนขับรถ)"
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF182232)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = CircleShape,
                                                    color = roleColor.copy(alpha = 0.2f),
                                                    modifier = Modifier.size(38.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = null,
                                                            tint = roleColor,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = user.name,
                                                            color = Color.White,
                                                            fontSize = 15.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Surface(
                                                            shape = RoundedCornerShape(6.dp),
                                                            color = roleColor.copy(alpha = 0.25f)
                                                        ) {
                                                            Text(
                                                                text = user.role,
                                                                color = roleColor,
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = roleLabel,
                                                        color = roleColor,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    if (user.username.isNotBlank()) {
                                                        Text(
                                                            text = "👤 Username: ${user.username}",
                                                            color = Color(0xFF10B981),
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Text(
                                                        text = "📞 เบอร์: ${if (user.phone.isNotBlank()) user.phone else "-"} • 🔑 Pass: ${if (user.password.isNotBlank()) user.password else "123456"}",
                                                        color = Color.LightGray,
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = "🏢 ${user.officeName}",
                                                        color = Color.LightGray,
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = "📍 ควบคุมกลุ่มจังหวัด: ${user.provinceGroup}",
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }

                                            Row {
                                                IconButton(
                                                    onClick = { userToEdit = user },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit User",
                                                        tint = Color(0xFF38BDF8),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = { userToDelete = user },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete User",
                                                        tint = Color(0xFFEF4444),
                                                        modifier = Modifier.size(18.dp)
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
            }
        }
    }

    // Edit Vehicle & Driver Dialog
    if (vehicleToEdit != null) {
        val currentV = vehicleToEdit!!
        var editName by remember { mutableStateOf(currentV.name) }
        var editLicensePlate by remember { mutableStateOf(currentV.licensePlate) }
        var editModelYear by remember { mutableStateOf(currentV.modelYear) }
        var editDriverName by remember { mutableStateOf(currentV.driverName) }
        var editOfficeName by remember { mutableStateOf(currentV.officeName) }
        var editProvinceGroup by remember { mutableStateOf(currentV.provinceGroup) }
        var editProvinceDropdownExpanded by remember { mutableStateOf(false) }

        val provinceList = listOf(
            "บึงกาฬ",
            "หนองบัวลำภู",
            "ขอนแก่น",
            "อุดรธานี",
            "เลย",
            "หนองคาย",
            "มหาสารคาม",
            "กาฬสินธุ์"
        )

        AlertDialog(
            onDismissRequest = { vehicleToEdit = null },
            containerColor = Color.White,
            title = {
                Text("✏️ Admin: แก้ไขข้อมูลผู้ใช้รถ & ยานพาหนะ", color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("การแก้ไขนี้จะอัปเดตไปยังหน้าแอพของผู้ใช้รถ (User) และ Google Sheets / Supabase โดยอัตโนมัติ:", fontSize = 12.sp, color = Color(0xFF49454F))

                    OutlinedTextField(
                        value = editDriverName,
                        onValueChange = { editDriverName = it },
                        label = { Text("👤 ชื่อผู้ใช้รถ / พนักงานขับรถ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editOfficeName,
                        onValueChange = { editOfficeName = it },
                        label = { Text("🏢 ชื่อ ปจ./ปณ. (ชื่อที่ทำการ)") },
                        placeholder = { Text("เช่น ปณ.เมืองขอนแก่น") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editProvinceGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("📍 ชื่อ กลุ่ม ปจ. (กลุ่มจังหวัด)", color = Color(0xFF6750A4)) },
                            trailingIcon = {
                                IconButton(onClick = { editProvinceDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1D1B20))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                disabledTextColor = Color(0xFF1D1B20),
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFFCAC4D0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { editProvinceDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = editProvinceDropdownExpanded,
                            onDismissRequest = { editProvinceDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            provinceList.forEach { province ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = province, 
                                            color = Color(0xFF1D1B20),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        editProvinceGroup = province
                                        editProvinceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("ชื่อเรียกประจำรถ (เช่น รถสิบล้อ #01)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editLicensePlate,
                        onValueChange = { editLicensePlate = it },
                        label = { Text("เลขทะเบียนรถ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editModelYear,
                        onValueChange = { editModelYear = it },
                        label = { Text("รุ่น / ปีรถ") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF1D1B20),
                            unfocusedTextColor = Color(0xFF1D1B20)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateVehicleDetails(
                            vehicleId = currentV.id,
                            name = editName,
                            licensePlate = editLicensePlate,
                            modelYear = editModelYear,
                            driverName = editDriverName,
                            officeName = editOfficeName,
                            provinceGroup = editProvinceGroup
                        )
                        vehicleToEdit = null
                        Toast.makeText(context, "อัปเดตไปยังหน้า User และ Google Sheets เรียบร้อย!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                ) {
                    Text("บันทึกการแก้ไข")
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToEdit = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Delete Vehicle Dialog
    if (vehicleToDelete != null) {
        val targetV = vehicleToDelete!!
        AlertDialog(
            onDismissRequest = { vehicleToDelete = null },
            containerColor = Color.White,
            title = {
                Text("⚠️ ยืนยันลบยานพาหนะ", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Text("คุณต้องการลบข้อมูล ${targetV.name} (${targetV.licensePlate}) ของผู้ขับ '${targetV.driverName}' ออกจากระบบหรือไม่?", color = Color(0xFF1D1B20), fontSize = 13.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVehicle(targetV.id)
                        vehicleToDelete = null
                        Toast.makeText(context, "ลบยานพาหนะเรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                ) {
                    Text("ยืนยันลบ")
                }
            },
            dismissButton = {
                TextButton(onClick = { vehicleToDelete = null }) {
                    Text("ยกเลิก")
                }
            }
        )
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        com.example.ui.screens.AddVehicleDialog(
            onDismiss = { showAddVehicleDialog = false },
            onAdd = { name, plate, model, driver, office, postal, provinceGroup ->
                viewModel.addNewVehicle(name, plate, model, driver, office, postal, provinceGroup)
                showAddVehicleDialog = false
                Toast.makeText(context, "เพิ่มยานพาหนะ $plate ($driver) เรียบร้อย!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // SQL Setup Dialog for Supabase
    if (showSqlSetupDialog) {
        val sqlScript = com.example.util.SupabaseSyncManager.SUPABASE_SQL_SETUP_SCRIPT
        AlertDialog(
            onDismissRequest = { showSqlSetupDialog = false },
            containerColor = Color.White,
            title = {
                Text("🐘 คำสั่ง SQL สร้าง DB ใน Supabase", color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "นำคำสั่ง SQL ด้านล่างนี้ไปวางใน Supabase -> SQL Editor แล้วกด Run เพื่อสร้างตาราง users, vehicles และ telemetry_history พร้อม RLS Policy:",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F)
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        LazyColumn(modifier = Modifier.padding(10.dp)) {
                            item {
                                Text(
                                    text = sqlScript,
                                    color = Color(0xFF38BDF8),
                                    fontSize = 10.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Supabase SQL", sqlScript)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "📋 คัดลอกคำสั่ง SQL เรียบร้อยแล้ว! นำไปวางใน Supabase SQL Editor ได้เลย", Toast.LENGTH_LONG).show()
                        showSqlSetupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("คัดลอกคำสั่ง SQL")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSqlSetupDialog = false }) {
                    Text("ปิด")
                }
            }
        )
    }

    // Add User Dialog
    if (showAddUserDialog) {
        var addUsername by remember { mutableStateOf("") }
        var addName by remember { mutableStateOf("") }
        var addRole by remember { mutableStateOf("DRIVER") }
        var addPhone by remember { mutableStateOf("") }
        var addPassword by remember { mutableStateOf("123456") }
        var addOffice by remember { mutableStateOf("ปณ.เมืองขอนแก่น") }
        var addProvinceGroup by remember { mutableStateOf("ขอนแก่น (ขก)") }
        var roleDropdownExpanded by remember { mutableStateOf(false) }
        var provinceDropdownExpanded by remember { mutableStateOf(false) }

        val roles = listOf(
            "DRIVER" to "👤 DRIVER (พนักงานขับรถ)",
            "MANAGER" to "👑 MANAGER (ผู้จัดการควบคุมกลุ่มจังหวัด)",
            "ADMIN" to "🛡️ ADMIN (ผู้ดูแลระบบทั้งหมด)",
            "DISPATCHER" to "🚚 DISPATCHER (ผู้จัดเส้นทาง)",
            "STAFF" to "💼 STAFF (เจ้าหน้าที่ทั่วไป)"
        )

        val provinceList = listOf(
            "ขอนแก่น (ขก)",
            "อุดรธานี (อด)",
            "อุบลราชธานี (อบ)",
            "นครราชสีมา (นม)",
            "หนองคาย (นค)",
            "เลย (เลย)",
            "มหาสารคาม (มค)",
            "กาฬสินธุ์ (กส)",
            "บึงกาฬ (บก)",
            "หนองบัวลำภู (นภ)",
            "ทุกกลุ่มจังหวัด"
        )

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            containerColor = Color.White,
            title = {
                Text("➕ เพิ่ม User / พนักงานใหม่ลง Supabase", color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = addUsername,
                        onValueChange = { addUsername = it },
                        label = { Text("👤 ชื่อผู้ใช้ / Username") },
                        placeholder = { Text("เช่น driver_somchai") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addName,
                        onValueChange = { addName = it },
                        label = { Text("👤 ชื่อ-นามสกุล") },
                        placeholder = { Text("เช่น นายสมชาย ใจดี") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = roles.find { it.first == addRole }?.second ?: addRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("🔑 สิทธิ์การใช้งาน (Role)", color = Color(0xFF6750A4)) },
                            trailingIcon = {
                                IconButton(onClick = { roleDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1D1B20))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                disabledTextColor = Color(0xFF1D1B20),
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFFCAC4D0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { roleDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            roles.forEach { (rKey, rLabel) ->
                                DropdownMenuItem(
                                    text = { Text(rLabel, color = Color(0xFF1D1B20), fontSize = 14.sp) },
                                    onClick = {
                                        addRole = rKey
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (addRole == "MANAGER") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "👑 สิทธิ์ MANAGER: สามารถดูและควบคุมยานพาหนะเฉพาะในกลุ่มจังหวัดที่สังกัดได้เท่านั้น",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Province Group Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = addProvinceGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("📍 กลุ่มจังหวัดที่รับผิดชอบ/สังกัด") },
                            trailingIcon = {
                                IconButton(onClick = { provinceDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1D1B20))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                disabledTextColor = Color(0xFF1D1B20),
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFFCAC4D0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { provinceDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = provinceDropdownExpanded,
                            onDismissRequest = { provinceDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            provinceList.forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text(prov, color = Color(0xFF1D1B20), fontSize = 14.sp) },
                                    onClick = {
                                        addProvinceGroup = prov
                                        provinceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = addPhone,
                        onValueChange = { addPhone = it },
                        label = { Text("📞 เบอร์โทรศัพท์") },
                        placeholder = { Text("เช่น 081-234-5678") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addPassword,
                        onValueChange = { addPassword = it },
                        label = { Text("🔑 รหัสผ่าน (Password)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = addOffice,
                        onValueChange = { addOffice = it },
                        label = { Text("🏢 ที่ทำการ / สังกัด") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (addName.isNotBlank()) {
                            viewModel.addUserToSupabase(
                                name = addName,
                                username = addUsername,
                                role = addRole,
                                phone = addPhone,
                                password = addPassword,
                                officeName = addOffice,
                                provinceGroup = addProvinceGroup
                            ) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                            showAddUserDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("เพิ่ม User ลง Supabase")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) { Text("ยกเลิก") }
            }
        )
    }

    // Edit User Dialog
    if (userToEdit != null) {
        val targetUser = userToEdit!!
        var editUsername by remember { mutableStateOf(targetUser.username) }
        var editName by remember { mutableStateOf(targetUser.name) }
        var editRole by remember { mutableStateOf(targetUser.role) }
        var editPhone by remember { mutableStateOf(targetUser.phone) }
        var editPassword by remember { mutableStateOf(targetUser.password) }
        var editOffice by remember { mutableStateOf(targetUser.officeName) }
        var editProvinceGroup by remember { mutableStateOf(targetUser.provinceGroup) }
        var roleDropdownExpanded by remember { mutableStateOf(false) }
        var provinceDropdownExpanded by remember { mutableStateOf(false) }

        val roles = listOf(
            "DRIVER" to "👤 DRIVER (พนักงานขับรถ)",
            "MANAGER" to "👑 MANAGER (ผู้จัดการควบคุมกลุ่มจังหวัด)",
            "ADMIN" to "🛡️ ADMIN (ผู้ดูแลระบบทั้งหมด)",
            "DISPATCHER" to "🚚 DISPATCHER (ผู้จัดเส้นทาง)",
            "STAFF" to "💼 STAFF (เจ้าหน้าที่ทั่วไป)"
        )

        val provinceList = listOf(
            "ขอนแก่น (ขก)",
            "อุดรธานี (อด)",
            "อุบลราชธานี (อบ)",
            "นครราชสีมา (นม)",
            "หนองคาย (นค)",
            "เลย (เลย)",
            "มหาสารคาม (มค)",
            "กาฬสินธุ์ (กส)",
            "บึงกาฬ (บก)",
            "หนองบัวลำภู (นภ)",
            "ทุกกลุ่มจังหวัด"
        )

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            containerColor = Color.White,
            title = { Text("✏️ แก้ไขข้อมูล User ใน Supabase", color = Color(0xFF1D1B20), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editUsername,
                        onValueChange = { editUsername = it },
                        label = { Text("👤 ชื่อผู้ใช้ / Username") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("👤 ชื่อ-นามสกุล") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Role Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = roles.find { it.first == editRole }?.second ?: editRole,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("🔑 สิทธิ์การใช้งาน (Role)", color = Color(0xFF6750A4)) },
                            trailingIcon = {
                                IconButton(onClick = { roleDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1D1B20))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                disabledTextColor = Color(0xFF1D1B20),
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFFCAC4D0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { roleDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = roleDropdownExpanded,
                            onDismissRequest = { roleDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            roles.forEach { (rKey, rLabel) ->
                                DropdownMenuItem(
                                    text = { Text(rLabel, color = Color(0xFF1D1B20), fontSize = 14.sp) },
                                    onClick = {
                                        editRole = rKey
                                        roleDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    if (editRole == "MANAGER") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "👑 สิทธิ์ MANAGER: สามารถดูและควบคุมยานพาหนะเฉพาะในกลุ่มจังหวัดที่สังกัดได้เท่านั้น",
                                color = Color(0xFF92400E),
                                fontSize = 11.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    // Province Group Dropdown
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = editProvinceGroup,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("📍 กลุ่มจังหวัดที่รับผิดชอบ/สังกัด") },
                            trailingIcon = {
                                IconButton(onClick = { provinceDropdownExpanded = true }) {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF1D1B20))
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF1D1B20),
                                unfocusedTextColor = Color(0xFF1D1B20),
                                disabledTextColor = Color(0xFF1D1B20),
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFFCAC4D0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clickable { provinceDropdownExpanded = true }
                        )
                        DropdownMenu(
                            expanded = provinceDropdownExpanded,
                            onDismissRequest = { provinceDropdownExpanded = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            provinceList.forEach { prov ->
                                DropdownMenuItem(
                                    text = { Text(prov, color = Color(0xFF1D1B20), fontSize = 14.sp) },
                                    onClick = {
                                        editProvinceGroup = prov
                                        provinceDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("📞 เบอร์โทรศัพท์") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("🔑 รหัสผ่าน (Password)") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editOffice,
                        onValueChange = { editOffice = it },
                        label = { Text("🏢 ที่ทำการ / สังกัด") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color(0xFF1D1B20), unfocusedTextColor = Color(0xFF1D1B20)),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = targetUser.copy(
                            name = editName,
                            username = editUsername,
                            role = editRole,
                            phone = editPhone,
                            password = editPassword,
                            officeName = editOffice,
                            provinceGroup = editProvinceGroup
                        )
                        viewModel.updateUserInSupabase(updated) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        userToEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                ) { Text("บันทึกการแก้ไข") }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) { Text("ยกเลิก") }
            }
        )
    }

    // Delete User Dialog
    if (userToDelete != null) {
        val targetUser = userToDelete!!
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            containerColor = Color.White,
            title = { Text("⚠️ ยืนยันลบ User", color = Color(0xFFB3261E), fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("คุณต้องการลบข้อมูล '${targetUser.name}' ออกจาก Supabase หรือไม่?", color = Color(0xFF1D1B20), fontSize = 13.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteUserInSupabase(targetUser.id) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB3261E))
                ) { Text("ยืนยันลบ") }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) { Text("ยกเลิก") }
            }
        )
    }
}
