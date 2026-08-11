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

    val INITIAL_VEHICLES = listOf(
        VehicleEntity(
            id = "V001",
            name = "รถบรรทุกตู้ใหญ่ 6 ล้อ (ขนส่งด่วนขอนแก่น-กรุงเทพฯ)",
            licensePlate = "83-4521 ขก",
            modelYear = "2024",
            status = "MOVING",
            currentLat = 16.4322,
            currentLng = 102.8236,
            speedKmh = 68,
            headingBearing = 180f,
            fuelPercent = 88,
            batteryVoltage = 24.2,
            activeRouteId = "R001",
            isEngineLocked = false,
            driverName = "นายสมชาย ใจดี",
            officeName = "ปณ.เมืองขอนแก่น",
            provinceGroup = "ขอนแก่น (ขก)"
        ),
        VehicleEntity(
            id = "V002",
            name = "รถตู้ส่งพัสดุด่วน EMS ปณ.ขอนแก่น #1",
            licensePlate = "ผก-9812 ขก",
            modelYear = "2023",
            status = "IDLE",
            currentLat = 16.4385,
            currentLng = 102.8310,
            speedKmh = 0,
            headingBearing = 90f,
            fuelPercent = 95,
            batteryVoltage = 12.8,
            activeRouteId = "R002",
            isEngineLocked = false,
            driverName = "นายวิชัย มั่นคง",
            officeName = "ปณ.เมืองขอนแก่น",
            provinceGroup = "ขอนแก่น (ขก)"
        ),
        VehicleEntity(
            id = "V003",
            name = "รถกระบะขนส่งตู้ทึบ ปณ.เมืองขอนแก่น #2",
            licensePlate = "1ตท-3341 ขก",
            modelYear = "2024",
            status = "MOVING",
            currentLat = 16.4410,
            currentLng = 102.8360,
            speedKmh = 42,
            headingBearing = 45f,
            fuelPercent = 74,
            batteryVoltage = 12.7,
            activeRouteId = "R003",
            isEngineLocked = false,
            driverName = "นายประสิทธิ์ คำมี",
            officeName = "ปณ.เมืองขอนแก่น",
            provinceGroup = "ขอนแก่น (ขก)"
        ),
        VehicleEntity(
            id = "V004",
            name = "รถบรรทุกเทรลเลอร์ 10 ล้อ ศูนย์ไปรษณีย์ขอนแก่น",
            licensePlate = "70-1122 ขก",
            modelYear = "2024",
            status = "MOVING",
            currentLat = 16.4812,
            currentLng = 102.8180,
            speedKmh = 72,
            headingBearing = 210f,
            fuelPercent = 82,
            batteryVoltage = 24.5,
            activeRouteId = "R004",
            isEngineLocked = false,
            driverName = "นายณรงค์ สายชล",
            officeName = "ศูนย์ไปรษณีย์ขอนแก่น",
            provinceGroup = "ขอนแก่น (ขก)"
        ),
        VehicleEntity(
            id = "V005",
            name = "รถกระบะส่งพัสดุ ปณ.น้ำพอง",
            licensePlate = "ผก-4411 ขก",
            modelYear = "2022",
            status = "STOPPED",
            currentLat = 16.8210,
            currentLng = 102.8020,
            speedKmh = 0,
            headingBearing = 0f,
            fuelPercent = 65,
            batteryVoltage = 12.5,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "นายอนุรักษ์ มีโชค",
            officeName = "ปณ.น้ำพอง",
            provinceGroup = "ขอนแก่น (ขก)"
        ),
        VehicleEntity(
            id = "V006",
            name = "รถตู้ส่งด่วน EMS ปณ.เมืองอุดรธานี",
            licensePlate = "ผก-1234 อด",
            modelYear = "2023",
            status = "MOVING",
            currentLat = 17.4138,
            currentLng = 102.7872,
            speedKmh = 55,
            headingBearing = 120f,
            fuelPercent = 90,
            batteryVoltage = 12.8,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "นายสมศักดิ์ นามดี",
            officeName = "ปณ.เมืองอุดรธานี",
            provinceGroup = "อุดรธานี (อด)"
        ),
        VehicleEntity(
            id = "V007",
            name = "รถบรรทุก 4 ล้อใหญ่ ปณ.เมืองนครราชสีมา",
            licensePlate = "81-9988 นม",
            modelYear = "2023",
            status = "MOVING",
            currentLat = 14.9799,
            currentLng = 102.0978,
            speedKmh = 62,
            headingBearing = 310f,
            fuelPercent = 78,
            batteryVoltage = 12.9,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "นายบุญมี มั่นใจ",
            officeName = "ปณ.เมืองนครราชสีมา",
            provinceGroup = "นครราชสีมา (นม)"
        ),
        VehicleEntity(
            id = "V008",
            name = "รถตู้ขนส่ง ปณ.เมืองอุบลราชธานี",
            licensePlate = "ผก-5566 อบ",
            modelYear = "2022",
            status = "IDLE",
            currentLat = 15.2287,
            currentLng = 104.8564,
            speedKmh = 0,
            headingBearing = 0f,
            fuelPercent = 85,
            batteryVoltage = 12.6,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "นายเกียรติศักดิ์ ชัยชนะ",
            officeName = "ปณ.เมืองอุบลราชธานี",
            provinceGroup = "อุบลราชธานี (อบ)"
        )
    )

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
