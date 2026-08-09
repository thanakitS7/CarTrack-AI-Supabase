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
        provinceGroup: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")
        val timeStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date())

        try {
            val jsonPayload = JSONObject().apply {
                put("vehicle_id", vehicleId)
                put("vehicle_name", vehicleName)
                put("license_plate", licensePlate)
                put("driver_name", driverName)
                put("office_name", officeName)
                put("province_group", provinceGroup)
                put("status", status)
                put("speed_kmh", speedKmh)
                put("latitude", latitude)
                put("longitude", longitude)
                put("fuel_percent", fuelPercent)
                put("battery_voltage", batteryVoltage)
                put("updated_at", timeStr)
            }.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val body = jsonPayload.toRequestBody(mediaType)

            // Try 1: UPSERT (POST with merge-duplicates)
            val requestUpsert = Request.Builder()
                .url("$cleanedUrl/rest/v1/vehicles")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(body)
                .build()

            var isSuccess = false
            var lastResponseBody = ""
            var lastCode = 0

            client.newCall(requestUpsert).execute().use { response ->
                lastCode = response.code
                lastResponseBody = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    isSuccess = true
                }
            }

            // Try 2: If Upsert failed (e.g. no primary key constraint), try PATCH update
            if (!isSuccess) {
                val patchUrl = "$cleanedUrl/rest/v1/vehicles?vehicle_id=eq.$vehicleId"
                val requestPatch = Request.Builder()
                    .url(patchUrl)
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .patch(body)
                    .build()

                client.newCall(requestPatch).execute().use { response ->
                    lastCode = response.code
                    val patchBody = response.body?.string() ?: ""
                    if (response.isSuccessful && patchBody != "[]" && patchBody.isNotEmpty()) {
                        isSuccess = true
                        lastResponseBody = patchBody
                    }
                }
            }

            // Try 3: If PATCH didn't update anything, try standard INSERT (POST without resolution header)
            if (!isSuccess) {
                val requestInsert = Request.Builder()
                    .url("$cleanedUrl/rest/v1/vehicles")
                    .addHeader("apikey", anonKey)
                    .addHeader("Authorization", "Bearer $anonKey")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Prefer", "return=representation")
                    .post(body)
                    .build()

                client.newCall(requestInsert).execute().use { response ->
                    lastCode = response.code
                    lastResponseBody = response.body?.string() ?: ""
                    if (response.isSuccessful) {
                        isSuccess = true
                    }
                }
            }

            if (isSuccess) {
                Log.d("SupabaseSync", "Successfully synced to Supabase: $lastResponseBody")
                logTelemetryHistory(cleanedUrl, anonKey, vehicleId, latitude, longitude, speedKmh, status, timeStr)
                Result.success("ส่งข้อมูลไปยัง Supabase สำเร็จ ($timeStr)")
            } else {
                Log.e("SupabaseSync", "Failed Supabase request ($lastCode): $lastResponseBody")
                Result.failure(Exception("Supabase Error ($lastCode): $lastResponseBody"))
            }
        } catch (e: Exception) {
            Log.e("SupabaseSync", "Error sending to Supabase", e)
            Result.failure(e)
        }
    }

    private fun logTelemetryHistory(
        baseUrl: String,
        anonKey: String,
        vehicleId: String,
        lat: Double,
        lng: Double,
        speed: Int,
        status: String,
        timestamp: String
    ) {
        try {
            val jsonPayload = JSONObject().apply {
                put("vehicle_id", vehicleId)
                put("latitude", lat)
                put("longitude", lng)
                put("speed_kmh", speed)
                put("status", status)
                put("created_at", timestamp)
            }.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("$baseUrl/rest/v1/telemetry_history")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .post(jsonPayload.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().close()
        } catch (e: Exception) {
            Log.w("SupabaseSync", "Optional telemetry_history log omitted or table not present")
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
        provinceGroup: String = "ขอนแก่น (ขก)",
        assignedVehicleId: String = "",
        status: String = "ACTIVE"
    ): Result<String> = withContext(Dispatchers.IO) {
        val cleanedUrl = baseUrl.trim().removeSuffix("/").removeSuffix("/rest/v1")

        try {
            val payload = JSONObject().apply {
                put("id", userId)
                put("name", name)
                put("username", username)
                put("role", role)
                put("email", email)
                put("phone", phone)
                put("password", password)
                put("office_name", officeName)
                put("province_group", provinceGroup)
                put("assigned_vehicle_id", assignedVehicleId)
                put("status", status)
                put("updated_at", SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).format(Date()))
            }.toString()

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val request = Request.Builder()
                .url("$cleanedUrl/rest/v1/users")
                .addHeader("apikey", anonKey)
                .addHeader("Authorization", "Bearer $anonKey")
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "resolution=merge-duplicates,return=representation")
                .post(payload.toRequestBody(mediaType))
                .build()

            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: ""
                if (response.isSuccessful) {
                    Result.success("อัปเดตผู้ใช้ใน Supabase สำเร็จ")
                } else {
                    Result.failure(Exception("HTTP ${response.code}: $body"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    const val SUPABASE_SQL_SETUP_SCRIPT = """-- ===================================================
-- 🐘 SUPABASE DATABASE CREATION SCRIPT FOR USERS & VEHICLES
-- คัดลอกคำสั่ง SQL นี้ไปวางใน Supabase SQL Editor แล้วกด Run ได้เลย
-- ===================================================

-- 1. สร้างตาราง 'users' สำหรับเก็บข้อมูลผู้ใช้งาน, คนขับรถ และ ผู้จัดการ (MANAGER)
CREATE TABLE IF NOT EXISTS public.users (
    id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    username TEXT DEFAULT '',
    role TEXT DEFAULT 'DRIVER', -- 'DRIVER', 'MANAGER', 'ADMIN', 'DISPATCHER', 'STAFF'
    email TEXT DEFAULT '',
    phone TEXT DEFAULT '',
    password TEXT DEFAULT '123456', -- รหัสผ่านเข้าใช้งาน
    office_name TEXT DEFAULT 'ปณ.เมืองขอนแก่น',
    province_group TEXT DEFAULT 'ขอนแก่น (ขก)', -- ควบคุมเฉพาะกลุ่มจังหวัดของตัวเองสำหรับ role MANAGER
    assigned_vehicle_id TEXT DEFAULT '',
    status TEXT DEFAULT 'ACTIVE',
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- เพิ่มคอลัมน์ password และ username ในกรณีที่มีตาราง users อยู่ก่อนแล้ว
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS password TEXT DEFAULT '123456';
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS username TEXT DEFAULT '';

-- ปลดล็อกสิทธิ์ (RLS Policy) ให้แอปอ่าน/เขียนตาราง users ได้
ALTER TABLE public.users ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write users" ON public.users;
CREATE POLICY "Allow public read/write users" ON public.users FOR ALL USING (true) WITH CHECK (true);

-- 2. สร้างตาราง 'vehicles' สำหรับเก็บตำแหน่ง GPS และสถานะรถ
CREATE TABLE IF NOT EXISTS public.vehicles (
    vehicle_id TEXT PRIMARY KEY,
    vehicle_name TEXT,
    license_plate TEXT,
    driver_name TEXT,
    office_name TEXT,
    province_group TEXT,
    status TEXT,
    speed_kmh INT DEFAULT 0,
    latitude DOUBLE PRECISION DEFAULT 13.7563,
    longitude DOUBLE PRECISION DEFAULT 100.5018,
    fuel_percent INT DEFAULT 100,
    battery_voltage DOUBLE PRECISION DEFAULT 12.6,
    updated_at TIMESTAMPTZ DEFAULT NOW()
);

-- ปลดล็อกสิทธิ์ (RLS Policy) ให้แอปอ่าน/เขียนตาราง vehicles ได้
ALTER TABLE public.vehicles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write vehicles" ON public.vehicles;
CREATE POLICY "Allow public read/write vehicles" ON public.vehicles FOR ALL USING (true) WITH CHECK (true);

-- 3. สร้างตาราง 'telemetry_history' สำหรับเก็บบันทึกประวัติพิกัด GPS ย้อนหลัง
CREATE TABLE IF NOT EXISTS public.telemetry_history (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    speed_kmh INT,
    status TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- ปลดล็อกสิทธิ์ (RLS Policy) ให้แอปอ่าน/เขียนตาราง telemetry_history ได้
ALTER TABLE public.telemetry_history ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "Allow public read/write history" ON public.telemetry_history;
CREATE POLICY "Allow public read/write history" ON public.telemetry_history FOR ALL USING (true) WITH CHECK (true);
"""
}
