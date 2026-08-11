package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.VehicleEntity
import com.example.ui.TrackingViewModel

@Composable
fun VehicleSelectionScreen(
    viewModel: TrackingViewModel,
    onVehicleConfirmed: (VehicleEntity) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allVehicles by viewModel.allVehicles.collectAsState()
    val selectedVehicleId by viewModel.selectedVehicleId.collectAsState()

    val user = currentUser ?: return

    var filterScope by remember { mutableStateOf("OFFICE") } // "OFFICE", "PROVINCE", "ALL"
    var searchQuery by remember { mutableStateOf("") }
    var showAddVehicleDialog by remember { mutableStateOf(false) }

    // Dialog form state for new vehicle
    var newVehicleName by remember { mutableStateOf("") }
    var newLicensePlate by remember { mutableStateOf("") }
    var newModelYear by remember { mutableStateOf("2024") }
    var newDriverName by remember { mutableStateOf(user.name) }

    // Filter logic
    val filteredVehicles = allVehicles.filter { vehicle ->
        val matchesScope = when (filterScope) {
            "OFFICE" -> {
                val uOffice = user.officeName.trim()
                val vOffice = vehicle.officeName.trim()
                vOffice.contains(uOffice, ignoreCase = true) || uOffice.contains(vOffice, ignoreCase = true) ||
                        (uOffice.contains("ขอนแก่น") && vOffice.contains("ขอนแก่น"))
            }
            "PROVINCE" -> {
                val uProv = user.provinceGroup.replace("\\s*\\(.*\\)".toRegex(), "").trim()
                val vProv = vehicle.provinceGroup.replace("\\s*\\(.*\\)".toRegex(), "").trim()
                vProv.contains(uProv, ignoreCase = true) || uProv.contains(vProv, ignoreCase = true)
            }
            else -> true
        }

        val matchesSearch = searchQuery.isBlank() ||
                vehicle.name.contains(searchQuery, ignoreCase = true) ||
                vehicle.licensePlate.contains(searchQuery, ignoreCase = true) ||
                vehicle.driverName.contains(searchQuery, ignoreCase = true) ||
                vehicle.officeName.contains(searchQuery, ignoreCase = true)

        matchesScope && matchesSearch
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E293B),
                        Color(0xFF090D16)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // User Welcome Header
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF182232)),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = user.role,
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🏢 สังกัด: ${user.officeName}",
                                color = Color(0xFF38BDF8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "📍 กลุ่มจังหวัด: ${user.provinceGroup}",
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = onLogout,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171)),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ออกจากระบบ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Title & Instruction
            Text(
                text = "🚚 เลือกยานพาหนะสำหรับปฏิบัติงาน",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ระบบกรองเฉพาะรถประจำที่ทำการ '${user.officeName}' เป็นหลัก",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = filterScope == "OFFICE",
                    onClick = { filterScope = "OFFICE" },
                    label = { Text("🏢 ที่ทำการนี้ (${user.officeName})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF6750A4),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.LightGray
                    )
                )

                FilterChip(
                    selected = filterScope == "PROVINCE",
                    onClick = { filterScope = "PROVINCE" },
                    label = { Text("📍 กลุ่มจังหวัด", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.LightGray
                    )
                )

                FilterChip(
                    selected = filterScope == "ALL",
                    onClick = { filterScope = "ALL" },
                    label = { Text("🌐 ทั้งหมด (${allVehicles.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF475569),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color.LightGray
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Box & Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ค้นหา ทะเบียน / ชื่อรถ / คนขับ...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF94A3B8)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155)
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                Button(
                    onClick = { showAddVehicleDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("เพิ่มรถ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                IconButton(
                    onClick = {
                        viewModel.resetDatabaseWithKhonKaenData()
                        Toast.makeText(context, "รีเซ็ตข้อมูลรถประจำ ปณ.ขอนแก่น เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .size(52.dp)
                        .background(Color(0xFF334155), RoundedCornerShape(8.dp))
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "รีเซ็ตข้อมูล", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Vehicles List
            if (filteredVehicles.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF182232)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "ไม่พบรายการรถในเงื่อนไขการค้นหา",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "กดปุ่ม 'เพิ่มรถ' ด้านบน หรือกด 'รีเซ็ตข้อมูล' เพื่อโหลดรถ ปณ.ขอนแก่น เข้าสู่ระบบ",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                viewModel.resetDatabaseWithKhonKaenData()
                                Toast.makeText(context, "เพิ่มรถ ปณ.ขอนแก่น สำเร็จ!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text("⚡ โหลดรถสาธิต ปณ.ขอนแก่น", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredVehicles, key = { it.id }) { vehicle ->
                        val isSelected = (vehicle.id == selectedVehicleId)
                        val statusColor = when (vehicle.status.uppercase()) {
                            "MOVING" -> Color(0xFF10B981) // Green
                            "IDLE" -> Color(0xFFF59E0B) // Amber
                            "STOPPED" -> Color(0xFFEF4444) // Red
                            else -> Color(0xFF6B7280) // Gray
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF1E293B) else Color(0xFF131C2A)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF10B981) else Color(0xFF334155),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.selectVehicle(vehicle.id)
                                    onVehicleConfirmed(vehicle)
                                }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = statusColor.copy(alpha = 0.2f),
                                            modifier = Modifier.padding(end = 10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocalShipping,
                                                contentDescription = null,
                                                tint = statusColor,
                                                modifier = Modifier
                                                    .padding(8.dp)
                                                    .size(24.dp)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = vehicle.name,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFF38BDF8).copy(alpha = 0.25f)
                                                ) {
                                                    Text(
                                                        text = "ทะเบียน: ${vehicle.licensePlate}",
                                                        color = Color(0xFF38BDF8),
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "ปี ${vehicle.modelYear}",
                                                    color = Color.Gray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = statusColor.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = when (vehicle.status.uppercase()) {
                                                "MOVING" -> "🟢 วิ่งอยู่ (${vehicle.speedKmh} km/h)"
                                                "IDLE" -> "🟡 จอดติดเครื่อง"
                                                "STOPPED" -> "🔴 ดับเครื่อง"
                                                else -> "⚪ ไม่ทราบสถานะ"
                                            },
                                            color = statusColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Details info row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "🏢 ${vehicle.officeName}",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "👤 คนขับ: ${vehicle.driverName}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "⛽ น้ำมัน: ${vehicle.fuelPercent}%",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        viewModel.selectVehicle(vehicle.id)
                                        onVehicleConfirmed(vehicle)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(42.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) Color(0xFF10B981) else Color(0xFF6750A4)
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.DirectionsCar,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isSelected) "กำลังเลือกคันนี้ (คลิกเพื่อเข้าสู่หน้างาน)" else "เลือกรถคันนี้เพื่อเริ่มงาน",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showAddVehicleDialog = false },
            title = {
                Text(
                    text = "➕ เพิ่มรถใหม่เข้าที่ทำการ ${user.officeName}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newVehicleName,
                        onValueChange = { newVehicleName = it },
                        label = { Text("ชื่อเรียก/รุ่นรถ (เช่น รถตู้ส่งด่วน EMS)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newLicensePlate,
                        onValueChange = { newLicensePlate = it },
                        label = { Text("เลขทะเบียนรถ (เช่น ผก-8899 ขก)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = newDriverName,
                        onValueChange = { newDriverName = it },
                        label = { Text("ชื่อพนักงานขับรถประจำคัน") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "🏢 สังกัดที่ทำการ: ${user.officeName} (${user.provinceGroup})",
                        color = Color(0xFF38BDF8),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newVehicleName.isBlank() || newLicensePlate.isBlank()) {
                            Toast.makeText(context, "กรุณากรอกชื่อและทะเบียนรถ", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        viewModel.addNewVehicle(
                            name = newVehicleName,
                            licensePlate = newLicensePlate,
                            modelYear = "2024",
                            driverName = newDriverName,
                            officeName = user.officeName,
                            provinceGroup = user.provinceGroup
                        )
                        showAddVehicleDialog = false
                        Toast.makeText(context, "เพิ่มรถใหม่เข้าที่ทำการสำเร็จ!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("บันทึกข้อมูล", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVehicleDialog = false }) {
                    Text("ยกเลิก", color = Color.Gray)
                }
            },
            containerColor = Color(0xFF182232)
        )
    }
}
