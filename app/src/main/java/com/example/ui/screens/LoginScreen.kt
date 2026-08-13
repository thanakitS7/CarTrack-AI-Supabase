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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserEntity
import com.example.ui.TrackingViewModel

@Composable
fun LoginScreen(
    viewModel: TrackingViewModel,
    onLoginSuccess: (UserEntity) -> Unit
) {
    val context = LocalContext.current
    val allUsers by viewModel.allUsers.collectAsState()

    var activeTab by remember { mutableStateOf("LOGIN") } // "LOGIN", "REGISTER", "QUICK"

    // Login Fields
    var loginInput by remember { mutableStateOf("") } // Phone or Name
    var loginPassword by remember { mutableStateOf("") }
    var loginPasswordVisible by remember { mutableStateOf(false) }

    // Register Fields
    var regUsername by remember { mutableStateOf("") }
    var regFirstName by remember { mutableStateOf("") }
    var regLastName by remember { mutableStateOf("") }
    var regPhone by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regPasswordVisible by remember { mutableStateOf(false) }
    val regRole = "DRIVER" // Locked to DRIVER only for public self-registration
    var regOffice by remember { mutableStateOf("ปณ.เมืองขอนแก่น") }
    var regProvinceGroup by remember { mutableStateOf("ขอนแก่น (ขก)") }

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
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // App Logo & Header
            Surface(
                shape = CircleShape,
                color = Color(0xFF6750A4).copy(alpha = 0.25f),
                modifier = Modifier.size(68.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(38.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "AutoGuard Fleet Tracker",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "ระบบลงชื่อเข้าใช้ และสมัครสมาชิกพนักงานขับรถ/ผู้จัดการ",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Tab Buttons (Login / Register / Quick Select)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B), shape = RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Button(
                    onClick = { activeTab = "LOGIN" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "LOGIN") Color(0xFF6750A4) else Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text("เข้าสู่ระบบ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { activeTab = "REGISTER" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "REGISTER") Color(0xFF10B981) else Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text("สมัครสมาชิก", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { activeTab = "QUICK" },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (activeTab == "QUICK") Color(0xFF3B82F6) else Color.Transparent,
                        contentColor = Color.White
                    )
                ) {
                    Text("เลือกด่วน", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. LOGIN TAB
            if (activeTab == "LOGIN") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF182232)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "🔐 ลงชื่อเข้าใช้งานด้วยชื่อ หรือ เบอร์โทร",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = loginInput,
                            onValueChange = { loginInput = it },
                            label = { Text("เบอร์โทรศัพท์ หรือ ชื่อผู้ใช้") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8))
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { loginPassword = it },
                            label = { Text("รหัสผ่าน (Password)") },
                            leadingIcon = {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8))
                            },
                            trailingIcon = {
                                IconButton(onClick = { loginPasswordVisible = !loginPasswordVisible }) {
                                    Icon(
                                        imageVector = if (loginPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (loginPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF6750A4),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "💡 บัญชีทดสอบ: U001, U002, manager, admin (รหัสผ่าน: 1234)",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp
                        )

                        Button(
                            onClick = {
                                val input = loginInput.trim()
                                if (input.isBlank()) {
                                    Toast.makeText(context, "กรุณากรอกเบอร์โทรศัพท์ หรือ ชื่อผู้ใช้ (เช่น U001)", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // Match user by username, id, phone, name, or U00x alias
                                val matchedUser = allUsers.find { u ->
                                    (u.username.isNotBlank() && u.username.equals(input, ignoreCase = true)) ||
                                    (u.id.equals(input, ignoreCase = true)) ||
                                    (u.id.replace("USR-", "U").equals(input, ignoreCase = true)) ||
                                    (u.phone.isNotBlank() && u.phone == input) ||
                                    (u.name.contains(input, ignoreCase = true))
                                }

                                if (matchedUser != null) {
                                    val isPassValid = matchedUser.password.isBlank() ||
                                            matchedUser.password == loginPassword.trim() ||
                                            (matchedUser.password == "1234" && (loginPassword.trim() == "1234" || loginPassword.trim() == "123456")) ||
                                            (matchedUser.password == "123456" && (loginPassword.trim() == "1234" || loginPassword.trim() == "123456"))

                                    if (!isPassValid) {
                                        Toast.makeText(context, "❌ รหัสผ่านไม่ถูกต้อง กรุณาลองใหม่อีกครั้ง (ลอง 1234)", Toast.LENGTH_SHORT).show()
                                    } else {
                                        viewModel.loginUser(matchedUser)
                                        onLoginSuccess(matchedUser)
                                        Toast.makeText(context, "เข้าสู่ระบบสำเร็จ! ยินดีต้อนรับ ${matchedUser.name}", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "❌ ไม่พบผู้ใช้งาน '$input' (ลองใช้ U001, U002 หรือแถบ 'เลือกด่วน')", Toast.LENGTH_LONG).show()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
                        ) {
                            Text("เข้าสู่ระบบ (Login)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 2. REGISTER TAB
            if (activeTab == "REGISTER") {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF182232)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "📝 สมัครสมาชิกพนักงานขับรถ (DRIVER)",
                            color = Color(0xFF10B981),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Username Field
                        OutlinedTextField(
                            value = regUsername,
                            onValueChange = { regUsername = it },
                            label = { Text("ชื่อผู้ใช้ / Username (สำหรับลงชื่อเข้าใช้)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // First Name & Last Name Fields
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = regFirstName,
                                onValueChange = { regFirstName = it },
                                label = { Text("ชื่อ") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = regLastName,
                                onValueChange = { regLastName = it },
                                label = { Text("นามสกุล") },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF475569)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Phone Field
                        OutlinedTextField(
                            value = regPhone,
                            onValueChange = { regPhone = it },
                            label = { Text("เบอร์โทรศัพท์") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Password Field
                        OutlinedTextField(
                            value = regPassword,
                            onValueChange = { regPassword = it },
                            label = { Text("ตั้งรหัสผ่าน (Password)") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = Color(0xFF94A3B8)) },
                            trailingIcon = {
                                IconButton(onClick = { regPasswordVisible = !regPasswordVisible }) {
                                    Icon(
                                        imageVector = if (regPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (regPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Locked Role Notice (DRIVER Only)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "สิทธิ์การสมัคร: DRIVER (พนักงานขับรถ) 🔒",
                                        color = Color(0xFF10B981),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "สิทธิ์อื่นๆ (ADMIN, MANAGER ฯลฯ) จะถูกเพิ่มโดย Admin ใน Dashboard เท่านั้น",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        // Province Group Dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = regProvinceGroup,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("📍 กลุ่มจังหวัดที่สังกัด") },
                                trailingIcon = {
                                    IconButton(onClick = { provinceDropdownExpanded = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF10B981),
                                    unfocusedBorderColor = Color(0xFF475569)
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
                                modifier = Modifier.background(Color(0xFF1E293B))
                            ) {
                                provinceList.forEach { prov ->
                                    DropdownMenuItem(
                                        text = { Text(prov, color = Color.White, fontSize = 13.sp) },
                                        onClick = {
                                            regProvinceGroup = prov
                                            provinceDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = regOffice,
                            onValueChange = { regOffice = it },
                            label = { Text("🏢 ที่ทำการไปรษณีย์") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF475569)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (regUsername.isBlank()) {
                                    Toast.makeText(context, "กรุณากรอก Username (ชื่อผู้ใช้)", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (regFirstName.isBlank()) {
                                    Toast.makeText(context, "กรุณากรอกชื่อ", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (regPhone.isBlank()) {
                                    Toast.makeText(context, "กรุณากรอกเบอร์โทรศัพท์", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                if (regPassword.isBlank()) {
                                    Toast.makeText(context, "กรุณากำหนดรหัสผ่าน", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                val fullName = "${regFirstName.trim()} ${regLastName.trim()}".trim()
                                val newUserId = "USR-${System.currentTimeMillis() % 10000}"
                                val newUser = UserEntity(
                                    id = newUserId,
                                    name = fullName,
                                    username = regUsername.trim(),
                                    role = regRole, // "DRIVER"
                                    phone = regPhone.trim(),
                                    password = regPassword.trim(),
                                    officeName = regOffice.trim(),
                                    provinceGroup = regProvinceGroup,
                                    status = "ACTIVE"
                                )

                                viewModel.loginUser(newUser)
                                viewModel.addUserToSupabase(
                                    id = newUser.id,
                                    name = newUser.name,
                                    username = newUser.username,
                                    role = newUser.role,
                                    phone = newUser.phone,
                                    password = newUser.password,
                                    officeName = newUser.officeName,
                                    provinceGroup = newUser.provinceGroup
                                )
                                onLoginSuccess(newUser)
                                Toast.makeText(context, "สมัครสมาชิกสำเร็จ! ยินดีต้อนรับ ${newUser.name}", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                        ) {
                            Text("สมัครสมาชิก & เข้าสู่ระบบ", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. QUICK SELECT TAB
            if (activeTab == "QUICK") {
                Text(
                    text = "⚡ เลือกเปลี่ยนบัญชีผู้ใช้ทดสอบด่วน:",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allUsers.forEach { user ->
                        val roleColor = when (user.role.uppercase()) {
                            "MANAGER" -> Color(0xFFF59E0B)
                            "ADMIN" -> Color(0xFFA855F7)
                            "DISPATCHER" -> Color(0xFF06B6D4)
                            else -> Color(0xFF3B82F6)
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF182232)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, roleColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                                .clickable {
                                    viewModel.loginUser(user)
                                    onLoginSuccess(user)
                                    Toast.makeText(context, "ยินดีต้อนรับ ${user.name} (${user.role})", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = roleColor.copy(alpha = 0.2f),
                                    modifier = Modifier.size(44.dp)
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
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
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
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "📍 กลุ่มจังหวัด: ${user.provinceGroup}",
                                        color = Color(0xFF38BDF8),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "🔑 Pass: ${if (user.password.isNotBlank()) user.password else "123456"} • 🏢 ${user.officeName}",
                                        color = Color.LightGray,
                                        fontSize = 11.sp
                                    )
                                }

                                Text(
                                    text = "เข้าใช้งาน ➔",
                                    color = roleColor,
                                    fontSize = 12.sp,
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
