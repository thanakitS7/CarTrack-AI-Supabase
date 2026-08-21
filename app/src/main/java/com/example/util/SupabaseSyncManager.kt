package com.example.util

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object SupabaseSyncManager {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    const val DEFAULT_SUPABASE_URL = "https://rhzglphlfzhautvwpnae.supabase.co"
    const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJoemdscGhsZnpoYXV0dndwbmFlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU4OTQzNDUsImV4cCI6MjEwMTQ3MDM0NX0.JMx8DvXgES0x9N7YmjW2n_7mgMYm_bgAkv39xb_Q2Jo"

    /**
     * Send telemetry data to Supabase table (telemetry / vehicles)
     */
    suspend fun sendTelemetryToSupabase(
        baseUrl: String = DEFAULT_SUPABASE_URL,
        anonKey: String = DEFAULT_ANON_KEY,
        vehicleId: String,
        vehicleName: String,
        licensePlate: String,
        status: String,
        latitude: Double,
        longitude: Double,
        speedKmh: Int,
        fuelPercent: Int,
        batteryVoltage: Double,
        driverName: String = "",
        officeName: String = "",
        postalCode: String = "",
        provinceGroup: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")
        val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date())

        val resolvedPostal = if (postalCode.isNotBlank()) postalCode else {
            if (officeName.contains("ศป.") || officeName.contains("ศูนย์ไปรษณีย์") || licensePlate.contains("70-1122")) "40010"
            else if (officeName.contains("น้ำพอง")) "40310"
            else if (officeName.contains("อุดรธานี")) "41000"
            else if (officeName.contains("นครราชสีมา")) "30000"
            else if (officeName.contains("อุบลราชธานี")) "34000"
            else "40000"
        }

        val cleanStatus = when {
            status.uppercase().contains("COMPLETED") || status.contains("สิ้นสุด") -> "COMPLETED"
            status.uppercase().contains("PAUSED") || status.contains("หยุดพัก") -> "PAUSED"
            status.uppercase().contains("MOVING") || status.contains("กำลังวิ่ง") || status.contains("เริ่มเดินทาง") -> "MOVING"
            status.uppercase().contains("IDLE") || status.contains("จอด") -> "IDLE"
            status.length > 15 -> status.take(15)
            else -> status
        }

        try {
            val numId = vehicleId.replace("[^0-9]".toRegex(), "").toLongOrNull()
            // 1. Try to ensure vehicle exists in vehicles table (id (bigint), license_plate, workplace, post_id)
            val vPayload = JSONObject().apply {
                if (numId != null && numId > 0) put("id", numId)
                put("license_plate", licensePlate)
                if (officeName.isNotBlank()) put("workplace", officeName)
                if (resolvedPostal.isNotBlank()) put("post_id", resolvedPostal)
            }
            try {
                val vBody = vPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                val vReq = Request.Builder()
                    .url("$cleanedUrl/rest/v1/vehicles")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(vBody)
                    .build()
                client.newCall(vReq).execute().close()
            } catch (e: Exception) {
                // Non-critical
            }

            // 2. Log location point to location_history
            logTelemetryHistory(cleanedUrl, anonKey, vehicleId, licensePlate, driverName, officeName, resolvedPostal, provinceGroup, latitude, longitude, speedKmh, cleanStatus, timeStr)
            
            // 3. Log usage summary to vehicle_usage_logs
            logVehicleUsage(cleanedUrl, anonKey, vehicleId, licensePlate, driverName, officeName = officeName, postalCode = resolvedPostal, provinceGroup = provinceGroup, status = cleanStatus, startLat = latitude, startLng = longitude, timestamp = timeStr)
            
            Result.success("ส่งข้อมูลไปยัง Supabase สำเร็จ ($timeStr)")
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Error sending to Supabase", e)
            Result.failure(e)
        }
    }

    private fun logTelemetryHistory(
        baseUrl: String,
        anonKey: String,
        vehicleId: String,
        licensePlate: String,
        driverName: String,
        officeName: String,
        postalCode: String,
        provinceGroup: String,
        lat: Double,
        lng: Double,
        speed: Int,
        status: String,
        timestamp: String
    ) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")
        val cleanStatus = when {
            status.uppercase().contains("COMPLETED") || status.contains("สิ้นสุด") -> "COMPLETED"
            status.uppercase().contains("PAUSED") || status.contains("หยุดพัก") -> "PAUSED"
            status.uppercase().contains("MOVING") || status.contains("กำลังวิ่ง") || status.contains("เริ่มเดินทาง") -> "MOVING"
            status.uppercase().contains("IDLE") || status.contains("จอด") -> "IDLE"
            status.length > 15 -> status.take(15)
            else -> status
        }

        try {
            val locId = "L${System.currentTimeMillis()}"
            val nowMs = System.currentTimeMillis()

            val payloadObj = JSONObject().apply {
                put("id", locId)
                val parsedId = vehicleId.replace("[^0-9]".toRegex(), "").toLongOrNull(); if (parsedId != null && vehicleId.matches("^[0-9]+$".toRegex())) put("vehicle_id", parsedId) else put("vehicle_id", vehicleId)
                put("license_plate", licensePlate)
                put("driver_name", driverName)
                put("office_name", officeName)
                put("postal_code", postalCode)
                put("province_group", provinceGroup)
                put("latitude", lat)
                put("longitude", lng)
                put("speed_kmh", speed)
                put("heading_bearing", 0.0)
                put("timestamp", nowMs)
                put("created_at", timestamp)
                put("status", cleanStatus)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val columnErrorRegex = "Could not find the '([^']+)' column".toRegex()

            val tablesToTry = listOf("location_history", "telemetry_history")

            for (table in tablesToTry) {
                val currentObj = JSONObject(payloadObj.toString())
                var activeTruncateLen = 20
                for (attempt in 0..6) {
                    val body = currentObj.toString().toRequestBody(mediaType)
                    val request = Request.Builder()
                        .url("$cleanedUrl/rest/v1/$table")
                        .addHeader("apikey", anonKey)
                        .addHeader("Authorization", "Bearer $anonKey")
                        .addHeader("Content-Type", "application/json")
                        .post(body)
                        .build()

                    var shouldBreakLoop = false
                    client.newCall(request).execute().use { resp ->
                        val respStr = resp.body?.string() ?: ""
                        if (resp.isSuccessful) {
                            Log.d("SupabaseSync", "บันทึกประวัติพิกัดลง $table สำเร็จ")
                            return
                        } else {
                            val match = columnErrorRegex.find(respStr)
                            if (match != null) {
                                val missingCol = match.groupValues[1]
                                currentObj.remove(missingCol)
                            } else if (respStr.contains("22001") || respStr.contains("value too long")) {
                                val limitMatch = "varying\\((\\d+)\\)".toRegex().find(respStr)
                                val detectedLimit = limitMatch?.groupValues?.get(1)?.toIntOrNull()
                                activeTruncateLen = if (detectedLimit != null) {
                                    (detectedLimit - 1).coerceAtLeast(3)
                                } else {
                                    (activeTruncateLen - 5).coerceAtLeast(3)
                                }
                                Log.w("SupabaseSync", "Truncating string fields for table $table to $activeTruncateLen chars...")
                                truncateStringFields(currentObj, activeTruncateLen)
                            } else if (currentObj.has("id")) {
                                // If UUID / ID constraint error, remove 'id' so Supabase auto-generates key
                                currentObj.remove("id")
                            } else if (currentObj.has("created_at")) {
                                currentObj.remove("created_at")
                            } else {
                                Log.w("SupabaseSync", "ไม่สามารถเพิ่มข้อมูลลง $table: HTTP ${resp.code} $respStr")
                                shouldBreakLoop = true
                            }
                        }
                    }
                    if (shouldBreakLoop) break
                }
            }
        } catch (e: Exception) {
            Log.w("SupabaseSync", "Error logging telemetry history: ${e.message}")
        }
    }

    fun logVehicleUsage(
        baseUrl: String,
        anonKey: String,
        vehicleId: String,
        licensePlate: String,
        driverName: String,
        userId: String? = null,
        officeName: String = "",
        postalCode: String = "",
        provinceGroup: String = "",
        status: String = "IN_PROGRESS",
        startLat: Double? = null,
        startLng: Double? = null,
        endLat: Double? = null,
        endLng: Double? = null,
        totalDistanceKm: Double? = null,
        maxSpeedKmh: Int? = null,
        timestamp: String = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date())
    ) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")
        val cleanStatus = when {
            status.uppercase().contains("COMPLETED") || status.contains("สิ้นสุด") -> "COMPLETED"
            status.uppercase().contains("PAUSED") || status.contains("หยุดพัก") -> "PAUSED"
            status.uppercase().contains("MOVING") || status.contains("กำลังวิ่ง") || status.contains("เริ่มเดินทาง") -> "MOVING"
            status.uppercase().contains("IDLE") || status.contains("จอด") -> "IDLE"
            status.length > 15 -> status.take(15)
            else -> status
        }

        try {
            val usageId = "U${System.currentTimeMillis()}"

            val payloadObj = JSONObject().apply {
                put("id", usageId)
                val parsedId = vehicleId.replace("[^0-9]".toRegex(), "").toLongOrNull(); if (parsedId != null && vehicleId.matches("^[0-9]+$".toRegex())) put("vehicle_id", parsedId) else put("vehicle_id", vehicleId)
                if (!userId.isNullOrBlank()) {
                    put("user_id", userId)
                }
                put("license_plate", licensePlate)
                put("driver_name", driverName)
                put("office_name", officeName)
                put("postal_code", postalCode)
                put("province_group", provinceGroup)
                put("start_time", timestamp)
                if (cleanStatus == "COMPLETED") {
                    put("end_time", timestamp)
                }
                if (startLat != null) put("start_lat", startLat)
                if (startLng != null) put("start_lng", startLng)
                if (endLat != null) put("end_lat", endLat)
                if (endLng != null) put("end_lng", endLng)
                if (totalDistanceKm != null) put("total_distance_km", totalDistanceKm)
                if (maxSpeedKmh != null) put("max_speed_kmh", maxSpeedKmh)
                put("status", cleanStatus)
                put("created_at", timestamp)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val columnErrorRegex = "Could not find the '([^']+)' column".toRegex()

            val currentObj = JSONObject(payloadObj.toString())
            var activeTruncateLen = 20
            for (attempt in 0..6) {
                val body = currentObj.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("$cleanedUrl/rest/v1/vehicle_usage_logs")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(body)
                    .build()

                var shouldBreakLoop = false
                client.newCall(request).execute().use { resp ->
                    val respStr = resp.body?.string() ?: ""
                    if (resp.isSuccessful) {
                        Log.d("SupabaseSync", "บันทึกประวัติการใช้งานรถลง vehicle_usage_logs สำเร็จ")
                        return
                    } else {
                        val match = columnErrorRegex.find(respStr)
                        if (match != null) {
                            val missingCol = match.groupValues[1]
                            currentObj.remove(missingCol)
                        } else if (respStr.contains("22001") || respStr.contains("value too long")) {
                            val limitMatch = "varying\\((\\d+)\\)".toRegex().find(respStr)
                            val detectedLimit = limitMatch?.groupValues?.get(1)?.toIntOrNull()
                            activeTruncateLen = if (detectedLimit != null) {
                                (detectedLimit - 1).coerceAtLeast(3)
                            } else {
                                (activeTruncateLen - 5).coerceAtLeast(3)
                            }
                            Log.w("SupabaseSync", "Truncating string fields for vehicle_usage_logs to $activeTruncateLen chars...")
                            truncateStringFields(currentObj, activeTruncateLen)
                        } else if (currentObj.has("id")) {
                            currentObj.remove("id")
                        } else {
                            Log.w("SupabaseSync", "ไม่สามารถเพิ่มข้อมูลลง vehicle_usage_logs: HTTP ${resp.code} $respStr")
                            shouldBreakLoop = true
                        }
                    }
                }
                if (shouldBreakLoop) break
            }
        } catch (e: Exception) {
            Log.w("SupabaseSync", "Error logging vehicle usage: ${e.message}")
        }
    }

    private fun truncateStringFields(jsonObj: JSONObject, maxLength: Int = 20) {
        val keys = jsonObj.keys()
        val keysList = mutableListOf<String>()
        while (keys.hasNext()) {
            keysList.add(keys.next())
        }
        for (k in keysList) {
            val value = jsonObj.opt(k)
            if (value is String && value.length > maxLength) {
                jsonObj.put(k, value.take(maxLength))
            }
        }
    }

    /**
     * Fetch all vehicles from Supabase
     */
    suspend fun fetchVehiclesFromSupabase(
        baseUrl: String = DEFAULT_SUPABASE_URL,
        anonKey: String = DEFAULT_ANON_KEY
    ): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")

        try {
            val request = Request.Builder()
                .url("$cleanedUrl/rest/v1/vehicles?select=*")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch all users from Supabase 'users' table
     */
    suspend fun fetchUsersFromSupabase(
        baseUrl: String = DEFAULT_SUPABASE_URL,
        anonKey: String = DEFAULT_ANON_KEY
    ): Result<List<JSONObject>> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")

        try {
            val request = Request.Builder()
                .url("$cleanedUrl/rest/v1/users?select=*")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(body)
                    val list = mutableListOf<JSONObject>()
                    for (i in 0 until jsonArray.length()) {
                        list.add(jsonArray.getJSONObject(i))
                    }
                    Result.success(list)
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Delete user from Supabase 'users' table by ID
     */
    suspend fun deleteUserFromSupabase(
        baseUrl: String = DEFAULT_SUPABASE_URL,
        anonKey: String = DEFAULT_ANON_KEY,
        userId: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")

        try {
            val request = Request.Builder()
                .url("$cleanedUrl/rest/v1/users?id=eq.$userId")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .delete()
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("ลบข้อมูลผู้ใช้จาก Supabase เรียบร้อย")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Update user/driver data in Supabase 'users' table
     */
    suspend fun updateUserInSupabase(
        baseUrl: String = DEFAULT_SUPABASE_URL,
        anonKey: String = DEFAULT_ANON_KEY,
        userId: String,
        name: String,
        username: String = "",
        role: String = "DRIVER",
        email: String = "",
        phone: String = "",
        password: String = "123456",
        officeName: String = "ปณ.เมืองขอนแก่น",
        postalCode: String = "40000",
        provinceGroup: String = "ขอนแก่น (ขก)",
        assignedVehicleId: String = "",
        status: String = "ACTIVE"
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")

        try {
            val payloadObj = JSONObject().apply {
                put("id", userId)
                put("name", name)
                put("username", username)
                put("role", role)
                put("password", password)
                put("pincode", password) // schema-compatible fallback for not null
                put("officename", officeName)
                put("office_name", officeName)
                put("provincegroup", provinceGroup)
                put("province_group", provinceGroup)
                put("postal_code", postalCode)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val columnErrorRegex = "Could not find the '([^']+)' column".toRegex()
            var isSuccess = false
            var lastResponseBody = ""
            var lastCode = 0
            var activeTruncateLen = 20

            for (attempt in 0..5) {
                val body = payloadObj.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("$cleanedUrl/rest/v1/users")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    lastCode = response.code
                    lastResponseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        isSuccess = true
                    }
                }

                if (isSuccess) break

                val match = columnErrorRegex.find(lastResponseBody)
                if (match != null) {
                    val missingColumn = match.groupValues[1]
                    Log.w("SupabaseSync", "Removing unsupported column '$missingColumn' from users payload and retrying...")
                    payloadObj.remove(missingColumn)
                } else if (lastResponseBody.contains("22001") || lastResponseBody.contains("value too long")) {
                    val limitMatch = "varying\\((\\d+)\\)".toRegex().find(lastResponseBody)
                    val detectedLimit = limitMatch?.groupValues?.get(1)?.toIntOrNull()
                    activeTruncateLen = if (detectedLimit != null) {
                        (detectedLimit - 1).coerceAtLeast(3)
                    } else {
                        (activeTruncateLen - 5).coerceAtLeast(3)
                    }
                    Log.w("SupabaseSync", "Truncating string fields for users payload to $activeTruncateLen chars...")
                    truncateStringFields(payloadObj, activeTruncateLen)
                } else {
                    break
                }
            }

            if (isSuccess) {
                Result.success("อัปเดตผู้ใช้ใน Supabase สำเร็จ")
            } else {
                Result.failure(Exception("HTTP $lastCode: $lastResponseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send vehicle usage log (e.g. Rest Stop) to Supabase 'vehicle_usage_logs' table
     * Matches exact schema:
     * id, vehicle_id, user_id, license_plate, driver_name, office_name, postal_code,
     * province_group, start_time, end_time, start_lat, start_lng, end_lat, end_lng,
     * total_distance_km, max_speed_kmh, status, created_at
     */
    suspend fun sendVehicleUsageLogToSupabase(
        baseUrl: String = DEFAULT_SUPABASE_URL,
        anonKey: String = DEFAULT_ANON_KEY,
        logId: String,
        vehicleId: String,
        userId: String = "",
        licensePlate: String,
        driverName: String,
        officeName: String,
        postalCode: String,
        provinceGroup: String,
        status: String = "Rest Stop",
        latitude: Double,
        longitude: Double,
        durationMinutes: Long,
        parkStartTimeMs: Long,
        parkEndTimeMs: Long
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")
        val startIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date(parkStartTimeMs))
        val endIso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }.format(Date(parkEndTimeMs))

        try {
            val payloadObj = JSONObject().apply {
                put("id", logId)
                val parsedId = vehicleId.replace("[^0-9]".toRegex(), "").toLongOrNull(); if (parsedId != null && vehicleId.matches("^[0-9]+$".toRegex())) put("vehicle_id", parsedId) else put("vehicle_id", vehicleId)
                if (userId.isNotBlank()) put("user_id", userId)
                put("license_plate", licensePlate)
                put("driver_name", driverName)
                put("office_name", officeName)
                put("postal_code", postalCode)
                put("province_group", provinceGroup)
                put("status", status)
                put("start_time", startIso)
                put("end_time", endIso)
                // Specific coordinates for schema matching
                put("start_lat", latitude)
                put("start_lng", longitude)
                put("end_lat", latitude)
                put("end_lng", longitude)
                put("total_distance_km", 0.0)
                put("max_speed_kmh", 0)
                put("created_at", endIso)
                // Fallbacks if schema has latitude/longitude/duration_minutes
                put("latitude", latitude)
                put("longitude", longitude)
                put("duration_minutes", durationMinutes)
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val columnErrorRegex = "Could not find the '([^']+)' column".toRegex()
            var isSuccess = false
            var lastResponseBody = ""
            var lastCode = 0

            for (attempt in 0..5) {
                val body = payloadObj.toString().toRequestBody(mediaType)
                val request = Request.Builder()
                    .url("$cleanedUrl/rest/v1/vehicle_usage_logs")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    lastCode = response.code
                    lastResponseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        isSuccess = true
                    }
                }

                if (isSuccess) break

                val match = columnErrorRegex.find(lastResponseBody)
                if (match != null) {
                    val missingColumn = match.groupValues[1]
                    Log.w("SupabaseSync", "Removing unsupported column '$missingColumn' from vehicle_usage_logs and retrying...")
                    payloadObj.remove(missingColumn)
                } else {
                    break
                }
            }

            if (isSuccess) {
                Result.success("บันทึก Rest Stop ลง vehicle_usage_logs ใน Supabase สำเร็จ")
            } else {
                Result.failure(Exception("HTTP $lastCode: $lastResponseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    const val SUPABASE_SQL_SETUP_SCRIPT = """-- ===================================================
-- 🐘 SUPABASE DATABASE SETUP SCRIPT FOR POSTAL TRACKING APP
-- คัดลอกคำสั่ง SQL ทั้งหมดนี้ไปวางที่ Supabase > SQL Editor แล้วกด RUN
-- ===================================================

-- 1. ตาราง 'users' (ผู้ใช้งาน, คนขับ, ผู้จัดการ)
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    username TEXT DEFAULT '',
    role TEXT DEFAULT 'DRIVER', -- 'DRIVER', 'MANAGER', 'ADMIN', 'DISPATCHER', 'STAFF'
    email TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    password TEXT DEFAULT '123456',
    office_name TEXT DEFAULT 'ปณ.เมืองขอนแก่น',
    postal_code TEXT DEFAULT '40000',
    province_group TEXT DEFAULT 'ขอนแก่น (ขก)',
    assigned_vehicle_id TEXT DEFAULT '',
    status TEXT DEFAULT 'ACTIVE',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.users ADD COLUMN IF NOT EXISTS password TEXT DEFAULT '123456';
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS username TEXT DEFAULT '';
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS postal_code TEXT DEFAULT '40000';

ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write users" ON public.users;
CREATE POLICY "Allow public read/write users" ON public.users FOR ALL USING (true) WITH CHECK (true);

-- 2. ตาราง 'vehicles' (ข้อมูลตำแหน่งรถ GPS และสถานะ)
CREATE TABLE IF NOT EXISTS public.vehicles (
    id TEXT PRIMARY KEY,
    vehicle_id TEXT,
    vehicle_name TEXT,
    license_plate TEXT,
    driver_name TEXT,
    office_name TEXT DEFAULT 'ปณ.เมืองขอนแก่น',
    postal_code TEXT DEFAULT '40000',
    province_group TEXT DEFAULT 'ขอนแก่น (ขก)',
    status TEXT DEFAULT 'STOPPED',
    speed_kmh DOUBLE PRECISION DEFAULT 0.0,
    latitude DOUBLE PRECISION DEFAULT 16.4322,
    longitude DOUBLE PRECISION DEFAULT 102.8236,
    fuel_percent DOUBLE PRECISION DEFAULT 100.0,
    battery_voltage DOUBLE PRECISION DEFAULT 12.6,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.vehicles ADD COLUMN IF NOT EXISTS office_name TEXT DEFAULT 'ปณ.เมืองขอนแก่น';
ALTER TABLE public.vehicles ADD COLUMN IF NOT EXISTS postal_code TEXT DEFAULT '40000';
ALTER TABLE public.vehicles ADD COLUMN IF NOT EXISTS province_group TEXT DEFAULT 'ขอนแก่น (ขก)';

ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write vehicles" ON public.vehicles;
CREATE POLICY "Allow public read/write vehicles" ON public.vehicles FOR ALL USING (true) WITH CHECK (true);

-- 3. ตาราง 'location_history' (เก็บบันทึกประวัติพิกัด GPS)
CREATE TABLE IF NOT EXISTS public.location_history (
    id TEXT PRIMARY KEY,
    vehicle_id TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    speed_kmh DOUBLE PRECISION,
    heading_bearing DOUBLE PRECISION DEFAULT 0.0,
    status TEXT,
    timestamp BIGINT DEFAULT 0,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.location_history ADD COLUMN IF NOT EXISTS heading_bearing DOUBLE PRECISION DEFAULT 0.0;
ALTER TABLE public.location_history ADD COLUMN IF NOT EXISTS timestamp BIGINT DEFAULT 0;

ALTER TABLE public.location_history ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write location_history" ON public.location_history;
CREATE POLICY "Allow public read/write location_history" ON public.location_history FOR ALL USING (true) WITH CHECK (true);

-- 4. ตาราง 'vehicle_usage_logs' (บันทึกการเข้า/ออกงาน)
CREATE TABLE IF NOT EXISTS public.vehicle_usage_logs (
    id TEXT PRIMARY KEY,
    vehicle_id TEXT,
    license_plate TEXT,
    driver_name TEXT,
    office_name TEXT,
    postal_code TEXT,
    province_group TEXT,
    start_time TIMESTAMPTZ DEFAULT NOW(),
    end_time TIMESTAMPTZ,
    status TEXT DEFAULT 'IN_PROGRESS',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.vehicle_usage_logs ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write vehicle_usage_logs" ON public.vehicle_usage_logs;
CREATE POLICY "Allow public read/write vehicle_usage_logs" ON public.vehicle_usage_logs FOR ALL USING (true) WITH CHECK (true);

-- 5. ตาราง 'alerts' (แจ้งเตือนความผิดปกติ)
CREATE TABLE IF NOT EXISTS public.alerts (
    id TEXT PRIMARY KEY,
    vehicle_id TEXT,
    license_plate TEXT,
    alert_type TEXT,
    message TEXT,
    severity TEXT DEFAULT 'WARNING',
    created_at TIMESTAMPTZ DEFAULT NOW()
);

ALTER TABLE public.alerts ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write alerts" ON public.alerts;
CREATE POLICY "Allow public read/write alerts" ON public.alerts FOR ALL USING (true) WITH CHECK (true);
"""
}
