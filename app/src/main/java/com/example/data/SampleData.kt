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
            status = "IDLE",
            currentLat = 16.44424086,
            currentLng = 102.79357396,
            speedKmh = 68,
            headingBearing = 180f,
            fuelPercent = 88,
            batteryVoltage = 24.2,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "นายสมชาย ผู้ขับขี่",
            officeName = "ปณ.เมืองขอนแก่น",
            postalCode = "40000",
            provinceGroup = "ขอนแก่น (ขก)"
        ),
        VehicleEntity(
            id = "V002",
            name = "รถตู้ส่งพัสดุด่วน EMS ปณ.ขอนแก่น #1",
            licensePlate = "ผก-9812 ขก",
            modelYear = "2024",
            status = "MOVING",
            currentLat = 16.4442308,
            currentLng = 102.8300232,
            speedKmh = 0,
            headingBearing = 90f,
            fuelPercent = 95,
            batteryVoltage = 12.8,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "อามเอง",
            officeName = "ปณ.ขอนแก่น",
            postalCode = "40000",
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
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "นายสมชาย ผู้ขับขี่",
            officeName = "ปณ.เมืองขอนแก่น",
            postalCode = "40000",
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
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "ธนกฤต เทิงสูงเนิน",
            officeName = "ศูนย์ไปรษณีย์ขอนแก่น",
            postalCode = "40010",
            provinceGroup = "ศูนย์ขอนแก่น (ศป)"
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
            driverName = "นายสมชาย ผู้ขับขี่",
            officeName = "ปณ.น้ำพอง",
            postalCode = "40310",
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
            driverName = "นายอุดร สายส่ง",
            officeName = "ปณ.เมืองอุดรธานี",
            postalCode = "41000",
            provinceGroup = "อุดรธานี (อด)"
        ),
        VehicleEntity(
            id = "V119",
            name = "test 6 ล้อ",
            licensePlate = "Test-3030",
            modelYear = "2024",
            status = "COMPLETED",
            currentLat = 16.44418109,
            currentLng = 102.79355781,
            speedKmh = 0,
            headingBearing = 0f,
            fuelPercent = 100,
            batteryVoltage = 12.5,
            activeRouteId = null,
            isEngineLocked = false,
            driverName = "ธนกฤต เทิงสูงเนิน",
            officeName = "ศูนย์ไปรษณีย์ขอนแก่น",
            postalCode = "40010",
            provinceGroup = "ศูนย์ขอนแก่น (ศป)"
        )
    )

    val INITIAL_ROUTES = emptyList<RouteGeofenceEntity>()

    val INITIAL_ALERTS = emptyList<AlertEntity>()

    val INITIAL_USERS = listOf(
        UserEntity(
            id = "U001",
            username = "U001",
            name = "นายสมชาย ผู้ขับขี่",
            role = "DRIVER",
            phone = "081-234-5678",
            password = "123456",
            officeName = "ปณ.เมืองขอนแก่น",
            postalCode = "40000",
            provinceGroup = "ขอนแก่น (ขก)",
            assignedVehicleId = "V001",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "U002",
            username = "U002",
            name = "นางสาววิภา หัวหน้าศูนย์",
            role = "MANAGER",
            phone = "089-876-5432",
            password = "123456",
            officeName = "ปณ.เมืองขอนแก่น",
            postalCode = "40000",
            provinceGroup = "ขอนแก่น (ขก)",
            assignedVehicleId = "V002",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "U003",
            username = "admin",
            name = "ผู้ดูแลระบบ ขอนแก่น",
            role = "ADMIN",
            phone = "086-555-4321",
            password = "123456",
            officeName = "ศูนย์ไปรษณีย์ขอนแก่น",
            postalCode = "40010",
            provinceGroup = "ศูนย์ขอนแก่น (ศป)",
            assignedVehicleId = "V004",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "U004",
            username = "U004",
            name = "นายอุดร สายส่ง",
            role = "DRIVER",
            phone = "082-111-2233",
            password = "123456",
            officeName = "ปณ.เมืองอุดรธานี",
            postalCode = "41000",
            provinceGroup = "อุดรธานี (อด)",
            assignedVehicleId = "V006",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "U005",
            username = "thanakit",
            name = "ธนกฤต เทิงสูงเนิน",
            role = "DRIVER",
            phone = "083-444-5566",
            password = "123456",
            officeName = "ศูนย์ไปรษณีย์ขอนแก่น",
            postalCode = "40010",
            provinceGroup = "ศูนย์ขอนแก่น (ศป)",
            assignedVehicleId = "V119",
            status = "ACTIVE"
        ),
        UserEntity(
            id = "U007",
            username = "arm.th",
            name = "อามเอง",
            role = "DRIVER",
            phone = "084-777-8899",
            password = "123456",
            officeName = "ปณ.ขอนแก่น",
            postalCode = "40000",
            provinceGroup = "ขอนแก่น (ขก)",
            assignedVehicleId = "V002",
            status = "ACTIVE"
        )
    )
}
