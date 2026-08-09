package com.example.data

import com.example.util.LatLng

object SampleData {

    // Bangkok to Chonburi Motorway Waypoints
    val MOTORWAY_WAYPOINTS = listOf(
        LatLng(13.7381, 100.6283), // Rama IX Expressway Junction
        LatLng(13.7292, 100.6782), // Lat Krabang Toll Gate
        LatLng(13.7125, 100.7421), // Suvarnabhumi Curve
        LatLng(13.6821, 100.8251), // Bang Bo Service Area
        LatLng(13.6120, 100.9320), // Bang Pakong River Bridge
        LatLng(13.5220, 100.9950), // Chonburi Bypass Interchange
        LatLng(13.3611, 100.9847)  // Chonburi City Center
    )

    // Inner Bangkok Safe Zone Waypoints (Vibhavadi - Din Daeng Corridor)
    val VIBHAVADI_WAYPOINTS = listOf(
        LatLng(13.7628, 100.5511), // Victory Monument / Din Daeng
        LatLng(13.7885, 100.5601), // Sutthisan Intersection
        LatLng(13.8122, 100.5609), // Lat Phrao Intersection
        LatLng(13.8540, 100.5732), // Kasetsart Intersection
        LatLng(13.9130, 100.6010), // Don Mueang Airport
        LatLng(13.9650, 100.6170)  // Rangsit Tollway
    )

    val INITIAL_VEHICLES = emptyList<VehicleEntity>()

    val INITIAL_ROUTES = emptyList<RouteGeofenceEntity>()

    val INITIAL_ALERTS = emptyList<AlertEntity>()

    val INITIAL_USERS = listOf(
        UserEntity(
            id = "USR-001",
            name = "นายสมชาย ใจดี",
            role = "DRIVER",
            phone = "081-234-5678",
            officeName = "ปณ.เมืองขอนแก่น",
            provinceGroup = "ขอนแก่น (ขก)",
            assignedVehicleId = "V001",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "USR-002",
            name = "นายวิชัย มั่นคง",
            role = "DRIVER",
            phone = "089-876-5432",
            officeName = "ปณ.เมืองขอนแก่น",
            provinceGroup = "ขอนแก่น (ขก)",
            assignedVehicleId = "V002",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "USR-003",
            name = "นายประเสริฐ คุมพื้นที่ (Manager)",
            role = "MANAGER",
            phone = "086-555-4321",
            officeName = "ที่ทำการไปรษณีย์เขต 4",
            provinceGroup = "ขอนแก่น (ขก)",
            assignedVehicleId = "",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "USR-004",
            name = "ผู้ดูแลระบบศูนย์ควบคุม (Admin)",
            role = "ADMIN",
            phone = "02-999-8888",
            officeName = "ศูนย์ไปรษณีย์ขอนแก่น",
            provinceGroup = "ทุกกลุ่มจังหวัด",
            assignedVehicleId = "",
            status = "ACTIVE"
        )
    )
}
