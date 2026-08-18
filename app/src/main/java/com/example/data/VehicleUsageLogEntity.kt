package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicle_usage_logs")
data class VehicleUsageLogEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val vehicleId: String,
    val licensePlate: String = "",
    val driverName: String = "",
    val officeName: String = "",
    val postalCode: String = "",
    val provinceGroup: String = "",
    val status: String = "Rest Stop",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val landmarkName: String = "",
    val durationMinutes: Long = 30L,
    val parkStartTime: Long = System.currentTimeMillis() - (30 * 60 * 1000L),
    val parkEndTime: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isSyncedToCloud: Boolean = false
)
