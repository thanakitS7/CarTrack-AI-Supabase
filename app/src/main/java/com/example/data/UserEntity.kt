package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val username: String = "",
    val role: String = "DRIVER", // "DRIVER", "MANAGER", "ADMIN", "DISPATCHER", "STAFF"
    val email: String = "",
    val phone: String = "",
    val password: String = "123456",
    val officeName: String = "ปณ.เมืองขอนแก่น",
    val postalCode: String = "40000",
    val provinceGroup: String = "ขอนแก่น (ขก)",
    val assignedVehicleId: String = "",
    val status: String = "ACTIVE",
    val lastUpdateMillis: Long = System.currentTimeMillis()
)
