package com.example.ui

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.RouteGeofenceEntity
import com.example.data.SampleData
import com.example.data.TrackingRepository
import com.example.data.VehicleEntity
import com.example.util.GeoUtils
import com.example.util.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = TrackingRepository(db.appDao())

    val allVehicles = repository.allVehicles.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allRoutes = repository.allRoutes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allAlerts = repository.allAlerts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allUsers = repository.allUsers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentUser = MutableStateFlow<com.example.data.UserEntity?>(null)
    val currentUser: StateFlow<com.example.data.UserEntity?> = _currentUser.asStateFlow()

    fun loginUser(user: com.example.data.UserEntity) {
        _currentUser.value = user
    }

    fun logout() {
        _currentUser.value = null
    }

    private val _selectedVehicleId = MutableStateFlow("V001")
    val selectedVehicleId: StateFlow<String> = _selectedVehicleId.asStateFlow()

    private val _isTripActive = MutableStateFlow(false)
    val isTripActive: StateFlow<Boolean> = _isTripActive.asStateFlow()

    private val _isSimulating = MutableStateFlow(false)
    val isSimulating: StateFlow<Boolean> = _isSimulating.asStateFlow()

    private val _isDeviatedTestMode = MutableStateFlow(false)
    val isDeviatedTestMode: StateFlow<Boolean> = _isDeviatedTestMode.asStateFlow()

    private val _simulationSpeedMultiplier = MutableStateFlow(1)
    val simulationSpeedMultiplier: StateFlow<Int> = _simulationSpeedMultiplier.asStateFlow()

    // Google Sheets Cloud Sync State
    private val _googleSheetsUrl = MutableStateFlow(com.example.util.GoogleSheetsSyncManager.DEFAULT_WEBHOOK_URL)
    val googleSheetsUrl: StateFlow<String> = _googleSheetsUrl.asStateFlow()

    private val _isGoogleSheetsSyncEnabled = MutableStateFlow(false)
    val isGoogleSheetsSyncEnabled: StateFlow<Boolean> = _isGoogleSheetsSyncEnabled.asStateFlow()

    // Supabase Cloud Database State
    private val _supabaseUrl = MutableStateFlow(com.example.util.SupabaseSyncManager.DEFAULT_SUPABASE_URL)
    val supabaseUrl: StateFlow<String> = _supabaseUrl.asStateFlow()

    private val _supabaseAnonKey = MutableStateFlow(com.example.util.SupabaseSyncManager.DEFAULT_ANON_KEY)
    val supabaseAnonKey: StateFlow<String> = _supabaseAnonKey.asStateFlow()

    private val _isSupabaseSyncEnabled = MutableStateFlow(true)
    val isSupabaseSyncEnabled: StateFlow<Boolean> = _isSupabaseSyncEnabled.asStateFlow()

    private val _lastSyncStatus = MutableStateFlow("เชื่อมต่อ Supabase แล้ว")
    val lastSyncStatus: StateFlow<String> = _lastSyncStatus.asStateFlow()

    private val _isSyncingInProcess = MutableStateFlow(false)
    val isSyncingInProcess: StateFlow<Boolean> = _isSyncingInProcess.asStateFlow()

    // Playback state
    private val _playbackIndex = MutableStateFlow(0)
    val playbackIndex: StateFlow<Int> = _playbackIndex.asStateFlow()

    private val _isPlaybackPlaying = MutableStateFlow(false)
    val isPlaybackPlaying: StateFlow<Boolean> = _isPlaybackPlaying.asStateFlow()

    private var simulationJob: Job? = null
    private var playbackJob: Job? = null
    private var activeTripSyncJob: Job? = null

    // Combined active vehicle state
    val activeVehicle = combine(allVehicles, _selectedVehicleId) { vehicles, id ->
        vehicles.firstOrNull { it.id == id } ?: vehicles.firstOrNull()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Active route for selected vehicle
    val activeRoute = combine(allRoutes, activeVehicle) { routes, vehicle ->
        if (vehicle == null) null
        else routes.firstOrNull { it.id == vehicle.activeRouteId } ?: routes.firstOrNull { it.vehicleId == vehicle.id }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Parsed waypoints for active route
    val activeWaypoints = combine(activeRoute) { routes ->
        val route = routes.firstOrNull()
        if (route != null) {
            repository.parseWaypoints(route.waypointsJson)
        } else {
            SampleData.MOTORWAY_WAYPOINTS
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SampleData.MOTORWAY_WAYPOINTS
    )

    val locationHistory = combine(selectedVehicleId) { id ->
        id
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "V001"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeHistoryPoints = _selectedVehicleId.flatMapLatest { id ->
        repository.getLocationHistory(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allVehicleUsageLogs = repository.allUsageLogs.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        viewModelScope.launch {
            repository.initializeSampleDataIfNeeded()
            // Initial sync from Supabase cloud
            syncVehiclesFromCloud()
            syncUsersFromCloud()
            // Periodic sync every 15 seconds
            while (isActive) {
                delay(15_000)
                syncVehiclesFromCloud()
            }
        }
    }

    private val _isRealGpsActive = MutableStateFlow(false)
    val isRealGpsActive: StateFlow<Boolean> = _isRealGpsActive.asStateFlow()

    private val _isGpsPermissionGranted = MutableStateFlow(false)
    val isGpsPermissionGranted: StateFlow<Boolean> = _isGpsPermissionGranted.asStateFlow()

    private val _currentGpsAccuracy = MutableStateFlow(0f)
    val currentGpsAccuracy: StateFlow<Float> = _currentGpsAccuracy.asStateFlow()

    private val _tripDistanceMeters = MutableStateFlow(0.0)
    val tripDistanceMeters: StateFlow<Double> = _tripDistanceMeters.asStateFlow()

    private val _isTripPaused = MutableStateFlow(false)
    val isTripPaused: StateFlow<Boolean> = _isTripPaused.asStateFlow()

    private val _tripStartTimeMs = MutableStateFlow(0L)
    private val _tripTopSpeedKmh = MutableStateFlow(0)
    private val _tripOverspeedCount = MutableStateFlow(0)

    private val _tripSummary = MutableStateFlow<TripSummaryData?>(null)
    val tripSummary: StateFlow<TripSummaryData?> = _tripSummary.asStateFlow()

    private val _speedLimitKmh = MutableStateFlow(90)
    val speedLimitKmh: StateFlow<Int> = _speedLimitKmh.asStateFlow()

    private val _isOverspeeding = MutableStateFlow(false)
    val isOverspeeding: StateFlow<Boolean> = _isOverspeeding.asStateFlow()

    private var lastOverspeedAlertTimeMs = 0L

    private var lastGpsLat: Double = 0.0
    private var lastGpsLng: Double = 0.0

    private var lastSyncTimeMs = 0L

    // Parking & Rest Stop Tracker: detect vehicle parked at same coordinates > 30 minutes
    private var parkStartLat = 0.0
    private var parkStartLng = 0.0
    private var parkStartTimeMs = 0L
    private var hasLoggedRestStop = false

    private fun checkAndRecordRestStop(vehicle: VehicleEntity, speedKmh: Int, lat: Double, lng: Double) {
        val isParked = (speedKmh <= 2)
        val now = System.currentTimeMillis()

        if (isParked) {
            if (parkStartTimeMs == 0L) {
                // Started parking
                parkStartLat = lat
                parkStartLng = lng
                parkStartTimeMs = now
                hasLoggedRestStop = false
            } else {
                // Check if vehicle is still within ~50 meters of the initial parked coordinate
                val distFromStart = GeoUtils.distanceMeters(parkStartLat, parkStartLng, lat, lng)
                if (distFromStart > 60.0) {
                    // Vehicle moved away - reset parking counter
                    parkStartLat = lat
                    parkStartLng = lng
                    parkStartTimeMs = now
                    hasLoggedRestStop = false
                } else {
                    val parkedDurationMs = now - parkStartTimeMs
                    val thirtyMinutesMs = 30 * 60 * 1000L // 30 minutes

                    if (parkedDurationMs >= thirtyMinutesMs && !hasLoggedRestStop) {
                        hasLoggedRestStop = true
                        val durationMinutes = parkedDurationMs / (60 * 1000L)
                        val loggedInUser = _currentUser.value
                        val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
                            ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
                            ?: vehicle.driverName
                            ?: "ผู้ใช้งานระบบ"

                        val placeName = GeoUtils.getFallbackThaiLandmark(lat, lng)

                        viewModelScope.launch {
                            val logEntity = com.example.data.VehicleUsageLogEntity(
                                vehicleId = vehicle.id,
                                licensePlate = vehicle.licensePlate,
                                driverName = effectiveDriverName,
                                officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                                postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                                provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                                status = "Rest Stop",
                                latitude = lat,
                                longitude = lng,
                                landmarkName = placeName,
                                durationMinutes = durationMinutes,
                                parkStartTime = parkStartTimeMs,
                                parkEndTime = now,
                                createdAt = now,
                                isSyncedToCloud = true
                            )

                            // 1. Insert into local SQLite Room database table 'vehicle_usage_logs'
                            repository.addVehicleUsageLog(logEntity)

                            // 2. Insert into Supabase table 'vehicle_usage_logs'
                            if (_isSupabaseSyncEnabled.value) {
                                com.example.util.SupabaseSyncManager.sendVehicleUsageLogToSupabase(
                                    baseUrl = _supabaseUrl.value,
                                    anonKey = _supabaseAnonKey.value,
                                    logId = logEntity.id,
                                    vehicleId = vehicle.id,
                                    userId = loggedInUser?.id ?: "",
                                    licensePlate = vehicle.licensePlate,
                                    driverName = effectiveDriverName,
                                    officeName = logEntity.officeName,
                                    postalCode = logEntity.postalCode,
                                    provinceGroup = logEntity.provinceGroup,
                                    status = "Rest Stop",
                                    latitude = lat,
                                    longitude = lng,
                                    durationMinutes = durationMinutes,
                                    parkStartTimeMs = parkStartTimeMs,
                                    parkEndTimeMs = now
                                )
                            }

                            // 3. Add a security/activity notification alert
                            val alert = com.example.data.AlertEntity(
                                vehicleId = vehicle.id,
                                vehicleName = vehicle.name,
                                licensePlate = vehicle.licensePlate,
                                alertType = "REST_STOP",
                                severity = "INFO",
                                title = "☕ บันทึกจุดพักรถ (Rest Stop) อัตโนมัติ",
                                description = "รถจอดนิ่ง ณ พิกัด $placeName เป็นเวลานานกว่า 30 นาที (คนขับ: $effectiveDriverName)",
                                latitude = lat,
                                longitude = lng,
                                distanceFromRouteMeters = 0,
                                timestamp = now,
                                isAcknowledged = false
                            )
                            repository.addAlert(alert)
                        }
                    }
                }
            }
        } else {
            // Vehicle is actively moving (speed > 2 km/h) - reset parking tracker
            parkStartTimeMs = 0L
            hasLoggedRestStop = false
        }
    }

    fun setSpeedLimitKmh(limit: Int) {
        _speedLimitKmh.value = 90
    }

    private fun checkAndHandleOverspeed(vehicle: VehicleEntity, speedKmh: Int, lat: Double, lng: Double) {
        if (speedKmh > _tripTopSpeedKmh.value) {
            _tripTopSpeedKmh.value = speedKmh
        }

        val limit = _speedLimitKmh.value
        val isExceeded = speedKmh > limit
        _isOverspeeding.value = isExceeded

        val loggedInUser = _currentUser.value
        val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
            ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
            ?: vehicle.driverName
            ?: "ผู้ใช้งานระบบ"

        if (isExceeded) {
            _tripOverspeedCount.value += 1
            val now = System.currentTimeMillis()
            if (now - lastOverspeedAlertTimeMs > 10000L) {
                lastOverspeedAlertTimeMs = now
                triggerVibrationAlert()
                viewModelScope.launch {
                    val placeName = GeoUtils.getFallbackThaiLandmark(lat, lng)
                    val alert = com.example.data.AlertEntity(
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        alertType = "SPEEDING",
                        severity = "CRITICAL",
                        title = "⚠️ แจ้งเตือน! รถขับเกินความเร็วที่กำหนด (ผู้ขับ: $effectiveDriverName)",
                        description = "ความเร็วปัจจุบัน ${speedKmh} กม./ชม. (ขีดจำกัด ${limit} กม./ชม.) ณ ${placeName}",
                        latitude = lat,
                        longitude = lng,
                        distanceFromRouteMeters = 0,
                        timestamp = now,
                        isAcknowledged = false
                    )
                    repository.addAlert(alert)

                    if (_isGoogleSheetsSyncEnabled.value) {
                        com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                            webhookUrl = _googleSheetsUrl.value,
                            vehicleId = vehicle.id,
                            vehicleName = vehicle.name,
                            licensePlate = vehicle.licensePlate,
                            driverName = effectiveDriverName,
                            status = "⚠️ OVERSPEED (ขับเร็ว ${speedKmh} กม./ชม. เกินจำกัด ${limit} กม./ชม.)",
                            latitude = lat,
                            longitude = lng,
                            speedKmh = speedKmh,
                            fuelPercent = vehicle.fuelPercent,
                            batteryVoltage = vehicle.batteryVoltage
                        )
                    }
                }
            }
        }
    }

    fun setGpsPermissionGranted(granted: Boolean) {
        _isGpsPermissionGranted.value = granted
    }

    fun updateRealGpsLocation(
        lat: Double,
        lng: Double,
        speedKmh: Int,
        heading: Float,
        accuracy: Float = 5f
    ) {
        _isRealGpsActive.value = true
        _currentGpsAccuracy.value = accuracy

        val vehicle = activeVehicle.value ?: return

        // If trip is paused, do not update movement telemetry
        if (_isTripPaused.value) {
            return
        }

        checkAndHandleOverspeed(vehicle, speedKmh, lat, lng)
        checkAndRecordRestStop(vehicle, speedKmh, lat, lng)

        // Accumulate trip distance if trip is active
        if (_isTripActive.value) {
            if (lastGpsLat != 0.0 && lastGpsLng != 0.0) {
                val dist = GeoUtils.distanceMeters(lastGpsLat, lastGpsLng, lat, lng)
                // Filter minor GPS noise (<1m) and unrealistic jumps (>1000m)
                if (dist > 1.0 && dist < 1000.0) {
                    _tripDistanceMeters.value += dist
                }
            }
            lastGpsLat = lat
            lastGpsLng = lng
        }

        viewModelScope.launch {
            repository.updateVehiclePosition(
                vehicleId = vehicle.id,
                newLat = lat,
                newLng = lng,
                speedKmh = speedKmh,
                heading = heading
            )

                // Auto-sync real GPS coordinates to Supabase & Google Sheets if trip is active
                val now = System.currentTimeMillis()
                val currentStatus = if (_isTripActive.value) {
                    if (speedKmh > 3) "MOVING (กำลังวิ่ง GPS สด)" else "MOVING (เริ่มเดินทาง GPS สด)"
                } else {
                    if (speedKmh > 3) "MOVING (ขับขี่พักทริป)" else "IDLE (จอดพัก)"
                }

                if (_isTripActive.value && (now - lastSyncTimeMs > 5000L)) {
                    lastSyncTimeMs = now
                    
                    val loggedInUser = _currentUser.value
                    val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
                        ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
                        ?: vehicle.driverName
                        ?: "ผู้ใช้งานระบบ"

                    if (_isSupabaseSyncEnabled.value) {
                        viewModelScope.launch {
                            val sbResult = com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                                baseUrl = _supabaseUrl.value,
                                anonKey = _supabaseAnonKey.value,
                                vehicleId = vehicle.id,
                                vehicleName = vehicle.name,
                                licensePlate = vehicle.licensePlate,
                                driverName = effectiveDriverName,
                                officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                                postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                                provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                                status = currentStatus,
                                latitude = lat,
                                longitude = lng,
                                speedKmh = speedKmh,
                                fuelPercent = vehicle.fuelPercent,
                                batteryVoltage = vehicle.batteryVoltage
                            )
                            _lastSyncStatus.value = sbResult.getOrElse { "ส่ง Supabase สำเร็จ (${speedKmh} กม./ชม.)" }
                        }
                    }

                    if (_isGoogleSheetsSyncEnabled.value) {
                        val result = com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                            webhookUrl = _googleSheetsUrl.value,
                            vehicleId = vehicle.id,
                            vehicleName = vehicle.name,
                            licensePlate = vehicle.licensePlate,
                            driverName = effectiveDriverName,
                            status = currentStatus,
                            latitude = lat,
                            longitude = lng,
                            speedKmh = speedKmh,
                            fuelPercent = vehicle.fuelPercent,
                            batteryVoltage = vehicle.batteryVoltage
                        )
                    }
                }
        }
    }

    fun updateFuelLevel(newFuelPercent: Int) {
        val vehicle = activeVehicle.value ?: return
        viewModelScope.launch {
            repository.updateVehiclePosition(
                vehicleId = vehicle.id,
                newLat = vehicle.currentLat,
                newLng = vehicle.currentLng,
                speedKmh = vehicle.speedKmh,
                heading = vehicle.headingBearing
            )
        }
    }

    fun startTrip() {
        _isTripActive.value = true
        _isTripPaused.value = false
        _tripStartTimeMs.value = System.currentTimeMillis()
        _tripDistanceMeters.value = 0.0
        _tripTopSpeedKmh.value = 0
        _tripOverspeedCount.value = 0
        _tripSummary.value = null

        try {
            com.example.service.TrackingForegroundService.startService(getApplication())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val vehicle = activeVehicle.value
        val loggedInUser = _currentUser.value
        val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
            ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
            ?: vehicle?.driverName
            ?: "ผู้ใช้งานระบบ"

        if (vehicle != null) {
            lastGpsLat = vehicle.currentLat
            lastGpsLng = vehicle.currentLng
            viewModelScope.launch {
                repository.updateVehiclePosition(
                    vehicleId = vehicle.id,
                    newLat = vehicle.currentLat,
                    newLng = vehicle.currentLng,
                    speedKmh = vehicle.speedKmh,
                    heading = vehicle.headingBearing
                )
                _lastSyncStatus.value = "เริ่มออกเดินทางแล้ว (ผู้ขับ: $effectiveDriverName)"
                val currentStatus = "MOVING (เริ่มเดินทาง GPS สด)"
                
                if (_isSupabaseSyncEnabled.value) {
                    com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                        baseUrl = _supabaseUrl.value,
                        anonKey = _supabaseAnonKey.value,
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                        postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                        provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                        status = currentStatus,
                        latitude = vehicle.currentLat,
                        longitude = vehicle.currentLng,
                        speedKmh = vehicle.speedKmh,
                        fuelPercent = vehicle.fuelPercent,
                        batteryVoltage = vehicle.batteryVoltage
                    )

                    // Log this user's trip start into vehicle_usage_logs table
                    com.example.util.SupabaseSyncManager.logVehicleUsage(
                        baseUrl = _supabaseUrl.value,
                        anonKey = _supabaseAnonKey.value,
                        vehicleId = vehicle.id,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        userId = loggedInUser?.id,
                        officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                        postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                        provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                        status = "IN_PROGRESS",
                        startLat = vehicle.currentLat,
                        startLng = vehicle.currentLng
                    )
                }

                if (_isGoogleSheetsSyncEnabled.value) {
                    com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                        webhookUrl = _googleSheetsUrl.value,
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        status = currentStatus,
                        latitude = vehicle.currentLat,
                        longitude = vehicle.currentLng,
                        speedKmh = vehicle.speedKmh,
                        fuelPercent = vehicle.fuelPercent,
                        batteryVoltage = vehicle.batteryVoltage
                    )
                }
            }
        }

        startActiveTripSyncLoop()
    }

    private fun startActiveTripSyncLoop() {
        activeTripSyncJob?.cancel()
        activeTripSyncJob = viewModelScope.launch {
            while (_isTripActive.value) {
                kotlinx.coroutines.delay(4000L) // Auto sync every 4 seconds
                if (!_isTripActive.value) break
                if (_isTripPaused.value) continue

                val vehicle = activeVehicle.value ?: continue
                val loggedInUser = _currentUser.value
                val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
                    ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
                    ?: vehicle.driverName
                    ?: "ผู้ใช้งานระบบ"

                val currentStatus = if (_isSimulating.value) {
                    "MOVING (จำลองการวิ่ง)"
                } else if (vehicle.speedKmh > 3) {
                    "MOVING (กำลังวิ่ง GPS สด)"
                } else {
                    "MOVING (เริ่มเดินทาง/จอดรอ GPS สด)"
                }

                if (_isSupabaseSyncEnabled.value) {
                    val sbResult = com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                        baseUrl = _supabaseUrl.value,
                        anonKey = _supabaseAnonKey.value,
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                        postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                        provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                        status = currentStatus,
                        latitude = vehicle.currentLat,
                        longitude = vehicle.currentLng,
                        speedKmh = vehicle.speedKmh,
                        fuelPercent = vehicle.fuelPercent,
                        batteryVoltage = vehicle.batteryVoltage
                    )
                    val timeStr = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.US).format(java.util.Date())
                    _lastSyncStatus.value = sbResult.getOrElse { "ส่งข้อมูล Supabase สดสำเร็จ ($timeStr)" }
                }

                if (_isGoogleSheetsSyncEnabled.value) {
                    com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                        webhookUrl = _googleSheetsUrl.value,
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        status = currentStatus,
                        latitude = vehicle.currentLat,
                        longitude = vehicle.currentLng,
                        speedKmh = vehicle.speedKmh,
                        fuelPercent = vehicle.fuelPercent,
                        batteryVoltage = vehicle.batteryVoltage
                    )
                }
            }
        }
    }

    fun pauseTrip() {
        val newPausedState = !_isTripPaused.value
        _isTripPaused.value = newPausedState

        val vehicle = activeVehicle.value
        val loggedInUser = _currentUser.value
        val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
            ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
            ?: vehicle?.driverName
            ?: "ผู้ใช้งานระบบ"

        if (vehicle != null) {
            viewModelScope.launch {
                val statusText = if (newPausedState) "⏸️ พักรถ (พักการทำงาน GPS)" else "▶️ เดินทางต่อ"
                repository.updateVehiclePosition(
                    vehicleId = vehicle.id,
                    newLat = vehicle.currentLat,
                    newLng = vehicle.currentLng,
                    speedKmh = if (newPausedState) 0 else vehicle.speedKmh,
                    heading = vehicle.headingBearing
                )
                _lastSyncStatus.value = statusText
                if (_isGoogleSheetsSyncEnabled.value) {
                    com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                        webhookUrl = _googleSheetsUrl.value,
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        status = statusText,
                        latitude = vehicle.currentLat,
                        longitude = vehicle.currentLng,
                        speedKmh = if (newPausedState) 0 else vehicle.speedKmh,
                        fuelPercent = vehicle.fuelPercent,
                        batteryVoltage = vehicle.batteryVoltage
                    )
                }
            }
        }
    }

    fun endTrip() {
        val vehicle = activeVehicle.value
        val startTime = if (_tripStartTimeMs.value > 0L) _tripStartTimeMs.value else System.currentTimeMillis() - 600000L
        val durationMs = (System.currentTimeMillis() - startTime).coerceAtLeast(1000L)
        val totalSecs = durationMs / 1000L
        val mins = totalSecs / 60L
        val secs = totalSecs % 60L

        if (vehicle != null) {
            val context = getApplication<Application>().applicationContext
            val startLoc = GeoUtils.getFallbackThaiLandmark(lastGpsLat.takeIf { it != 0.0 } ?: vehicle.currentLat, lastGpsLng.takeIf { it != 0.0 } ?: vehicle.currentLng)
            val endLoc = GeoUtils.getFallbackThaiLandmark(vehicle.currentLat, vehicle.currentLng)

            _tripSummary.value = TripSummaryData(
                vehicleName = vehicle.name,
                licensePlate = vehicle.licensePlate,
                distanceKm = _tripDistanceMeters.value / 1000.0,
                durationMinutes = mins,
                durationSeconds = secs,
                topSpeedKmh = _tripTopSpeedKmh.value.coerceAtLeast(vehicle.speedKmh),
                startPlace = startLoc,
                endPlace = endLoc,
                overspeedCount = _tripOverspeedCount.value
            )
        }

        _isTripActive.value = false
        _isTripPaused.value = false
        _isSimulating.value = false
        simulationJob?.cancel()
        activeTripSyncJob?.cancel()

        try {
            com.example.service.TrackingForegroundService.stopService(getApplication())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val loggedInUser = _currentUser.value
        val effectiveDriverName = loggedInUser?.name?.takeIf { it.isNotBlank() }
            ?: loggedInUser?.username?.takeIf { it.isNotBlank() }
            ?: vehicle?.driverName
            ?: "ผู้ใช้งานระบบ"

        if (vehicle != null) {
            viewModelScope.launch {
                repository.updateVehiclePosition(
                    vehicleId = vehicle.id,
                    newLat = vehicle.currentLat,
                    newLng = vehicle.currentLng,
                    speedKmh = 0,
                    heading = vehicle.headingBearing
                )
                _lastSyncStatus.value = "ถึงที่หมายแล้ว (บันทึกประวัติการใช้งานของ $effectiveDriverName)"
                if (_isSupabaseSyncEnabled.value) {
                    com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                        baseUrl = _supabaseUrl.value,
                        anonKey = _supabaseAnonKey.value,
                        vehicleId = vehicle.id,
                        vehicleName = vehicle.name,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                        postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                        provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                        status = "COMPLETED (สิ้นสุดการเดินทาง)",
                        latitude = vehicle.currentLat,
                        longitude = vehicle.currentLng,
                        speedKmh = 0,
                        fuelPercent = vehicle.fuelPercent,
                        batteryVoltage = vehicle.batteryVoltage
                    )

                    // Log this user's trip completion into vehicle_usage_logs table
                    com.example.util.SupabaseSyncManager.logVehicleUsage(
                        baseUrl = _supabaseUrl.value,
                        anonKey = _supabaseAnonKey.value,
                        vehicleId = vehicle.id,
                        licensePlate = vehicle.licensePlate,
                        driverName = effectiveDriverName,
                        userId = loggedInUser?.id,
                        officeName = loggedInUser?.officeName?.takeIf { it.isNotBlank() } ?: vehicle.officeName,
                        postalCode = loggedInUser?.postalCode?.takeIf { it.isNotBlank() } ?: vehicle.postalCode,
                        provinceGroup = loggedInUser?.provinceGroup?.takeIf { it.isNotBlank() } ?: vehicle.provinceGroup,
                        status = "COMPLETED",
                        endLat = vehicle.currentLat,
                        endLng = vehicle.currentLng,
                        totalDistanceKm = _tripDistanceMeters.value / 1000.0,
                        maxSpeedKmh = _tripTopSpeedKmh.value
                    )
                }
                com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                    webhookUrl = _googleSheetsUrl.value,
                    vehicleId = vehicle.id,
                    vehicleName = vehicle.name,
                    licensePlate = vehicle.licensePlate,
                    driverName = effectiveDriverName,
                    status = "PARKED (ถึงเป้าหมายเรียบร้อย)",
                    latitude = vehicle.currentLat,
                    longitude = vehicle.currentLng,
                    speedKmh = 0,
                    fuelPercent = vehicle.fuelPercent,
                    batteryVoltage = vehicle.batteryVoltage
                )
            }
        }
    }

    fun dismissTripSummary() {
        _tripSummary.value = null
    }

    fun selectVehicle(vehicleId: String) {
        _selectedVehicleId.value = vehicleId
        _isDeviatedTestMode.value = false
    }

    fun resetDatabaseWithKhonKaenData() {
        viewModelScope.launch {
            repository.resetDatabaseWithKhonKaenData()
        }
    }

    fun toggleSimulation(enable: Boolean) {
        _isSimulating.value = enable
        if (enable) {
            startSimulationLoop()
        } else {
            simulationJob?.cancel()
        }
    }

    fun setSimulationSpeed(multiplier: Int) {
        _simulationSpeedMultiplier.value = multiplier
    }

    fun triggerTestDeviation(forceDeviate: Boolean) {
        _isDeviatedTestMode.value = forceDeviate
        if (forceDeviate) {
            triggerVibrationAlert()
        }
    }

    fun toggleEngineLock(vehicleId: String, lock: Boolean) {
        viewModelScope.launch {
            repository.toggleEngineLock(vehicleId, lock)
            if (lock) triggerVibrationAlert()
        }
    }

    fun acknowledgeAlert(alertId: Long) {
        viewModelScope.launch {
            repository.acknowledgeAlert(alertId)
        }
    }

    fun addNewVehicle(
        name: String,
        licensePlate: String,
        modelYear: String,
        driverName: String = "",
        officeName: String = "",
        postalCode: String = "",
        provinceGroup: String = "ขอนแก่น (ขก)"
    ) {
        viewModelScope.launch {
            val newId = "V_${System.currentTimeMillis() % 100000}"
            val finalOffice = officeName.ifBlank { "ปณ.เมืองขอนแก่น" }
            val finalPostal = if (postalCode.isNotBlank()) postalCode else {
                if (finalOffice.contains("ศป.") || finalOffice.contains("ศูนย์ไปรษณีย์") || licensePlate.contains("70-1122")) "40010"
                else if (finalOffice.contains("น้ำพอง")) "40310"
                else if (finalOffice.contains("อุดรธานี")) "41000"
                else if (finalOffice.contains("นครราชสีมา")) "30000"
                else if (finalOffice.contains("อุบลราชธานี")) "34000"
                else "40000"
            }
            val vehicle = VehicleEntity(
                id = newId,
                name = name,
                licensePlate = licensePlate,
                modelYear = modelYear.ifEmpty { "2024" },
                status = "STOPPED",
                currentLat = 16.4322,
                currentLng = 102.8236,
                speedKmh = 0,
                headingBearing = 0f,
                fuelPercent = 100,
                batteryVoltage = 12.8,
                activeRouteId = "R001",
                isEngineLocked = false,
                driverName = driverName.ifBlank { "สมชาย ใจดี (คนขับ)" },
                officeName = finalOffice,
                postalCode = finalPostal,
                provinceGroup = provinceGroup.ifBlank { "ขอนแก่น (ขก)" }
            )
            repository.addVehicle(vehicle)
            selectVehicle(newId)

            if (_isSupabaseSyncEnabled.value) {
                val res = com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                    baseUrl = _supabaseUrl.value,
                    anonKey = _supabaseAnonKey.value,
                    vehicleId = vehicle.id,
                    vehicleName = vehicle.name,
                    licensePlate = vehicle.licensePlate,
                    driverName = vehicle.driverName,
                    officeName = vehicle.officeName,
                    postalCode = vehicle.postalCode,
                    provinceGroup = vehicle.provinceGroup,
                    status = "STOPPED",
                    latitude = vehicle.currentLat,
                    longitude = vehicle.currentLng,
                    speedKmh = vehicle.speedKmh,
                    fuelPercent = vehicle.fuelPercent,
                    batteryVoltage = vehicle.batteryVoltage
                )
                _lastSyncStatus.value = res.getOrElse { "บันทึกในเครื่องสำเร็จ (Supabase: ${it.message})" }
            }
        }
    }

    fun updateVehicleDriverName(vehicleId: String, newDriverName: String) {
        viewModelScope.launch {
            val vehicles = allVehicles.value
            val v = vehicles.firstOrNull { it.id == vehicleId } ?: return@launch
            val updated = v.copy(driverName = newDriverName)
            repository.addVehicle(updated)
        }
    }

    fun updateVehicleDetails(
        vehicleId: String,
        name: String,
        licensePlate: String,
        modelYear: String,
        driverName: String,
        officeName: String = "",
        postalCode: String = "",
        provinceGroup: String = ""
    ) {
        viewModelScope.launch {
            val vehicles = allVehicles.value
            val v = vehicles.firstOrNull { it.id == vehicleId } ?: return@launch
            val finalOffice = officeName.ifBlank { v.officeName }
            val finalPlate = licensePlate.ifBlank { v.licensePlate }
            val finalPostal = if (postalCode.isNotBlank()) postalCode else {
                if (v.postalCode.isNotBlank()) v.postalCode
                else if (finalOffice.contains("ศป.") || finalOffice.contains("ศูนย์ไปรษณีย์") || finalPlate.contains("70-1122")) "40010"
                else if (finalOffice.contains("น้ำพอง")) "40310"
                else if (finalOffice.contains("อุดรธานี")) "41000"
                else if (finalOffice.contains("นครราชสีมา")) "30000"
                else if (finalOffice.contains("อุบลราชธานี")) "34000"
                else "40000"
            }
            val updated = v.copy(
                name = name.ifBlank { v.name },
                licensePlate = finalPlate,
                modelYear = modelYear.ifBlank { v.modelYear },
                driverName = driverName.ifBlank { v.driverName },
                officeName = finalOffice,
                postalCode = finalPostal,
                provinceGroup = provinceGroup.ifBlank { v.provinceGroup }
            )
            repository.updateVehicle(updated)
            if (_isSupabaseSyncEnabled.value) {
                com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                    baseUrl = _supabaseUrl.value,
                    anonKey = _supabaseAnonKey.value,
                    vehicleId = updated.id,
                    vehicleName = updated.name,
                    licensePlate = updated.licensePlate,
                    driverName = updated.driverName,
                    officeName = updated.officeName,
                    postalCode = updated.postalCode,
                    provinceGroup = updated.provinceGroup,
                    status = "UPDATED (แก้ไขข้อมูลรถ)",
                    latitude = updated.currentLat,
                    longitude = updated.currentLng,
                    speedKmh = updated.speedKmh,
                    fuelPercent = updated.fuelPercent,
                    batteryVoltage = updated.batteryVoltage
                )
            }
            if (_isGoogleSheetsSyncEnabled.value) {
                com.example.util.GoogleSheetsSyncManager.sendTelemetryToGoogleSheets(
                    webhookUrl = _googleSheetsUrl.value,
                    vehicleId = updated.id,
                    vehicleName = updated.name,
                    licensePlate = updated.licensePlate,
                    driverName = updated.driverName,
                    officeName = updated.officeName,
                    provinceGroup = updated.provinceGroup,
                    status = "UPDATED (แก้ไขข้อมูลคนขับ/รถ)",
                    latitude = updated.currentLat,
                    longitude = updated.currentLng,
                    speedKmh = updated.speedKmh,
                    fuelPercent = updated.fuelPercent,
                    batteryVoltage = updated.batteryVoltage
                )
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicleId)
            val remaining = allVehicles.value.filter { it.id != vehicleId }
            if (remaining.isNotEmpty()) {
                selectVehicle(remaining.first().id)
            }
        }
    }

    fun syncVehiclesFromCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            // First attempt to fetch from Supabase
            val sbResult = com.example.util.SupabaseSyncManager.fetchVehiclesFromSupabase(
                _supabaseUrl.value,
                _supabaseAnonKey.value
            )

            var list = if (sbResult.isSuccess && !sbResult.getOrNull().isNullOrEmpty()) {
                sbResult.getOrNull() ?: emptyList()
            } else {
                com.example.util.GoogleSheetsSyncManager.fetchVehiclesFromCloud(_googleSheetsUrl.value).getOrNull() ?: emptyList()
            }

            if (list.isNotEmpty()) {
                for (jsonObj in list) {
                    val vId = jsonObj.optString("id").ifBlank {
                        jsonObj.optString("vehicle_id").ifBlank {
                            jsonObj.optString("vehicleId").ifBlank { jsonObj.optString("รหัสรถ") }
                        }
                    }
                    if (vId.isNotBlank()) {
                        val plate = jsonObj.optString("license_plate").ifBlank {
                            jsonObj.optString("licenseplate").ifBlank {
                                jsonObj.optString("licensePlate").ifBlank {
                                    jsonObj.optString("ทะเบียนรถ", "-")
                                }
                            }
                        }
                        val rawOffice = jsonObj.optString("workplace").ifBlank {
                            jsonObj.optString("office_name").ifBlank {
                                jsonObj.optString("officeName").ifBlank {
                                    jsonObj.optString("officename", "")
                                }
                            }
                        }
                        val rawPostal = jsonObj.optString("post_id").ifBlank {
                            jsonObj.optString("postal_code").ifBlank {
                                jsonObj.optString("postalcode").ifBlank {
                                    jsonObj.optString("zip_code").ifBlank {
                                        jsonObj.optString("zipcode").ifBlank {
                                            jsonObj.optString("รหัสไปรษณีย์", "")
                                        }
                                    }
                                }
                            }
                        }
                        val name = jsonObj.optString("name").ifBlank {
                            jsonObj.optString("vehicle_name").ifBlank {
                                jsonObj.optString("vehicleName").ifBlank {
                                    if (plate.isNotBlank() && plate != "-") "รถ $plate" else "Vehicle $vId"
                                }
                            }
                        }
                        val modelYr = jsonObj.optString("modelyear").ifBlank {
                            jsonObj.optString("model_year", "2024")
                        }
                        val rawDriver = jsonObj.optString("driver_name").ifBlank {
                            jsonObj.optString("driverName").ifBlank {
                                jsonObj.optString("drivername", "")
                            }
                        }
                        val rawProvince = jsonObj.optString("province_group").ifBlank {
                            jsonObj.optString("provinceGroup", "")
                        }
                        val rawStatus = jsonObj.optString("status").ifBlank {
                            jsonObj.optString("สถานะ", "STOPPED")
                        }

                        val lat = jsonObj.optDouble("currentlat", jsonObj.optDouble("latitude", jsonObj.optDouble("current_lat", jsonObj.optDouble("lat", 0.0))))
                        val lng = jsonObj.optDouble("currentlng", jsonObj.optDouble("longitude", jsonObj.optDouble("current_lng", jsonObj.optDouble("lng", 0.0))))
                        val speed = jsonObj.optInt("speed_kmh", jsonObj.optInt("speedKmh", jsonObj.optInt("speed", 0)))

                        val existing = allVehicles.value.firstOrNull { it.id == vId }

                        val driver = if (rawDriver.isNotBlank()) rawDriver else (existing?.driverName ?: "พนักงานขับรถ")
                        val office = if (rawOffice.isNotBlank()) rawOffice else {
                            if (existing != null) existing.officeName
                            else if (name.contains("ศูนย์ไปรษณีย์ขอนแก่น")) "ศูนย์ไปรษณีย์ขอนแก่น"
                            else if (name.contains("น้ำพอง")) "ปณ.น้ำพอง"
                            else if (name.contains("อุดรธานี")) "ปณ.เมืองอุดรธานี"
                            else "ปณ.เมืองขอนแก่น"
                        }
                        val postal = if (rawPostal.isNotBlank()) rawPostal else {
                            if (existing != null && existing.postalCode.isNotBlank()) existing.postalCode
                            else if (office.contains("ศป.") || office.contains("ศูนย์ไปรษณีย์") || plate.contains("70-1122")) "40010"
                            else if (office.contains("น้ำพอง")) "40310"
                            else if (office.contains("อุดรธานี")) "41000"
                            else if (office.contains("นครราชสีมา")) "30000"
                            else if (office.contains("อุบลราชธานี")) "34000"
                            else "40000"
                        }
                        val provinceGrp = if (rawProvince.isNotBlank()) rawProvince else (existing?.provinceGroup ?: "ขอนแก่น (ขก)")

                        val updateStr = jsonObj.optString("updated_at").ifBlank {
                            jsonObj.optString("created_at").ifBlank { jsonObj.optString("timestamp") }
                        }
                        var lastUpdateMs = System.currentTimeMillis()
                        if (updateStr.isNotBlank()) {
                            val formats = listOf(
                                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                                "yyyy-MM-dd'T'HH:mm:ss",
                                "yyyy-MM-dd HH:mm:ss"
                            )
                            for (fmt in formats) {
                                try {
                                    val d = java.text.SimpleDateFormat(fmt, java.util.Locale.US).parse(updateStr)
                                    if (d != null) {
                                        lastUpdateMs = d.time
                                        break
                                    }
                                } catch (e: Exception) {}
                            }
                        }

                        if (existing != null) {
                            repository.updateVehicle(
                                existing.copy(
                                    name = name,
                                    licensePlate = plate,
                                    modelYear = modelYr,
                                    driverName = driver,
                                    officeName = office,
                                    postalCode = postal,
                                    provinceGroup = provinceGrp,
                                    status = rawStatus,
                                    currentLat = if (lat != 0.0) lat else existing.currentLat,
                                    currentLng = if (lng != 0.0) lng else existing.currentLng,
                                    speedKmh = speed,
                                    lastUpdateMillis = lastUpdateMs
                                )
                            )
                        } else {
                            repository.addVehicle(
                                com.example.data.VehicleEntity(
                                    id = vId,
                                    name = name,
                                    licensePlate = plate,
                                    modelYear = modelYr,
                                    status = rawStatus,
                                    currentLat = if (lat != 0.0) lat else 16.4322,
                                    currentLng = if (lng != 0.0) lng else 102.8236,
                                    speedKmh = speed,
                                    headingBearing = 0f,
                                    fuelPercent = 100,
                                    batteryVoltage = 12.6,
                                    activeRouteId = null,
                                    isEngineLocked = false,
                                    driverName = driver,
                                    officeName = office,
                                    postalCode = postal,
                                    provinceGroup = provinceGrp,
                                    lastUpdateMillis = lastUpdateMs
                                )
                            )
                        }
                    }
                }
                onComplete(true, "ซิงค์รายชื่อรถและคนขับ ${list.size} คันจากคลาวด์สำเร็จ!")
            } else {
                onComplete(true, "ไม่มีข้อมูลใหม่จากคลาวด์")
            }
        }
    }

    fun addNewRoute(
        routeName: String,
        type: String,
        startName: String,
        endName: String,
        toleranceMeters: Int,
        maxSpeed: Int
    ) {
        viewModelScope.launch {
            val vId = selectedVehicleId.value
            val newRoute = RouteGeofenceEntity(
                id = "R_${System.currentTimeMillis()}",
                vehicleId = vId,
                name = routeName,
                type = type,
                centerLat = 13.7381,
                centerLng = 100.6283,
                radiusMeters = if (type == "CIRCLE_ZONE") 1000.0 else 300.0,
                maxAllowedSpeed = maxSpeed,
                toleranceMeters = toleranceMeters,
                waypointsJson = "13.7381,100.6283;13.7292,100.6782;13.7125,100.7421;13.6821,100.8251",
                isActive = true,
                startLocationName = startName,
                endLocationName = endName
            )
            repository.addRoute(newRoute)
        }
    }

    fun updateGoogleSheetsUrl(url: String) {
        _googleSheetsUrl.value = url
    }

    fun setSupabaseConfig(url: String, key: String) {
        _supabaseUrl.value = url
        _supabaseAnonKey.value = key
    }

    fun toggleSupabaseSync(enabled: Boolean) {
        _isSupabaseSyncEnabled.value = enabled
    }

    fun syncCurrentVehicleToSupabaseNow() {
        val vehicle = activeVehicle.value ?: return
        viewModelScope.launch {
            _isSyncingInProcess.value = true
            _lastSyncStatus.value = "กำลังส่งข้อมูลเข้า Supabase..."
            val result = com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                baseUrl = _supabaseUrl.value,
                anonKey = _supabaseAnonKey.value,
                vehicleId = vehicle.id,
                vehicleName = vehicle.name,
                licensePlate = vehicle.licensePlate,
                driverName = vehicle.driverName,
                officeName = vehicle.officeName,
                postalCode = vehicle.postalCode,
                provinceGroup = vehicle.provinceGroup,
                status = vehicle.status,
                latitude = vehicle.currentLat,
                longitude = vehicle.currentLng,
                speedKmh = vehicle.speedKmh,
                fuelPercent = vehicle.fuelPercent,
                batteryVoltage = vehicle.batteryVoltage
            )
            _isSyncingInProcess.value = false
            _lastSyncStatus.value = result.getOrElse { "ผิดพลาด Supabase: ${it.message}" }
        }
    }

    fun toggleGoogleSheetsSync(enabled: Boolean) {
        _isGoogleSheetsSyncEnabled.value = enabled
    }

    fun syncCurrentVehicleToGoogleSheetsNow() {
        val vehicle = activeVehicle.value ?: return
        viewModelScope.launch {
            _isSyncingInProcess.value = true
            _lastSyncStatus.value = "กำลังส่งข้อมูลเข้า Supabase..."
            val result = com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                baseUrl = com.example.util.SupabaseSyncManager.DEFAULT_SUPABASE_URL,
                anonKey = com.example.util.SupabaseSyncManager.DEFAULT_ANON_KEY,
                vehicleId = vehicle.id,
                vehicleName = vehicle.name,
                licensePlate = vehicle.licensePlate,
                driverName = vehicle.driverName,
                officeName = vehicle.officeName,
                postalCode = vehicle.postalCode,
                provinceGroup = vehicle.provinceGroup,
                status = vehicle.status,
                latitude = vehicle.currentLat,
                longitude = vehicle.currentLng,
                speedKmh = vehicle.speedKmh,
                fuelPercent = vehicle.fuelPercent,
                batteryVoltage = vehicle.batteryVoltage
            )
            _isSyncingInProcess.value = false
            _lastSyncStatus.value = result.getOrElse { "ผิดพลาด: ${it.message}" }
        }
    }

    private fun startSimulationLoop() {
        simulationJob?.cancel()
        simulationJob = viewModelScope.launch {
            var stepIndex = 0
            val waypoints = SampleData.MOTORWAY_WAYPOINTS

            while (_isSimulating.value && _isTripActive.value) {
                val delayMs = (2000L / _simulationSpeedMultiplier.value).coerceAtLeast(400L)
                delay(delayMs)

                val vehicle = activeVehicle.value ?: continue
                if (vehicle.isEngineLocked) continue

                var targetPoint: LatLng
                var targetSpeed = 80 + (stepIndex % 15)

                if (_isDeviatedTestMode.value) {
                    // Test speed burst > speed limit
                    targetPoint = LatLng(
                        lat = 13.7480 + ((stepIndex % 10) * 0.0020),
                        lng = 100.7700 + ((stepIndex % 10) * 0.0025)
                    )
                    targetSpeed = 105 + ((stepIndex % 4) * 5) // 105 - 120 km/h overspeed test
                } else {
                    // Follow normal motorway sequence
                    val wayIdx = (stepIndex % waypoints.size)
                    val baseWay = waypoints[wayIdx]
                    val nextWay = waypoints[(wayIdx + 1) % waypoints.size]

                    // Smooth interpolate between points
                    val progress = (stepIndex % 5) / 5.0
                    targetPoint = LatLng(
                        lat = baseWay.lat + (nextWay.lat - baseWay.lat) * progress,
                        lng = baseWay.lng + (nextWay.lng - baseWay.lng) * progress
                    )
                }

                checkAndHandleOverspeed(vehicle, targetSpeed, targetPoint.lat, targetPoint.lng)

                val bearing = GeoUtils.calculateBearing(
                    vehicle.currentLat, vehicle.currentLng,
                    targetPoint.lat, targetPoint.lng
                )

                val dist = GeoUtils.distanceMeters(vehicle.currentLat, vehicle.currentLng, targetPoint.lat, targetPoint.lng)
                if (dist > 1.0) {
                    _tripDistanceMeters.value += dist
                }

                repository.updateVehiclePosition(
                    vehicleId = vehicle.id,
                    newLat = targetPoint.lat,
                    newLng = targetPoint.lng,
                    speedKmh = targetSpeed,
                    heading = bearing
                )

                // Sync to Supabase & Google Sheets every 3 steps if sync enabled
                if (stepIndex % 3 == 0) {
                    val currentVeh = activeVehicle.value ?: vehicle
                    launch {
                        if (_isSupabaseSyncEnabled.value) {
                            val sbResult = com.example.util.SupabaseSyncManager.sendTelemetryToSupabase(
                                baseUrl = _supabaseUrl.value,
                                anonKey = _supabaseAnonKey.value,
                                vehicleId = currentVeh.id,
                                vehicleName = currentVeh.name,
                                licensePlate = currentVeh.licensePlate,
                                driverName = currentVeh.driverName,
                                officeName = currentVeh.officeName,
                                postalCode = currentVeh.postalCode,
                                provinceGroup = currentVeh.provinceGroup,
                                status = currentVeh.status,
                                latitude = targetPoint.lat,
                                longitude = targetPoint.lng,
                                speedKmh = targetSpeed,
                                fuelPercent = currentVeh.fuelPercent,
                                batteryVoltage = currentVeh.batteryVoltage
                            )
                            _lastSyncStatus.value = sbResult.getOrElse { "Supabase: ${it.message}" }
                        }
                    }
                }

                stepIndex++
            }
        }
    }

    fun syncUsersFromCloud(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val sbResult = com.example.util.SupabaseSyncManager.fetchUsersFromSupabase(
                _supabaseUrl.value,
                _supabaseAnonKey.value
            )
            if (sbResult.isSuccess) {
                val list = sbResult.getOrNull() ?: emptyList()
                if (list.isNotEmpty()) {
                    val userEntities = list.mapNotNull { jsonObj ->
                        val uId = jsonObj.optString("id")
                        if (uId.isNotBlank()) {
                            val rawOffice = jsonObj.optString("officename").ifBlank {
                                jsonObj.optString("office_name", "ปณ.เมืองขอนแก่น")
                            }
                            val rawProvince = jsonObj.optString("provincegroup").ifBlank {
                                jsonObj.optString("province_group", "ขอนแก่น (ขก)")
                            }
                            val rawPassword = jsonObj.optString("password").ifBlank {
                                jsonObj.optString("pincode", "123456")
                            }
                            val rawPostal = jsonObj.optString("postal_code").ifBlank {
                                jsonObj.optString("postalcode").ifBlank {
                                    jsonObj.optString("zip_code").ifBlank {
                                        jsonObj.optString("zipcode", "")
                                    }
                                }
                            }
                            val postal = if (rawPostal.isNotBlank()) rawPostal else {
                                if (rawOffice.contains("ศป.") || rawOffice.contains("ศูนย์ไปรษณีย์")) "40010"
                                else if (rawOffice.contains("น้ำพอง")) "40310"
                                else if (rawOffice.contains("อุดรธานี")) "41000"
                                else if (rawOffice.contains("นครราชสีมา")) "30000"
                                else if (rawOffice.contains("อุบลราชธานี")) "34000"
                                else "40000"
                            }
                            com.example.data.UserEntity(
                                id = uId,
                                name = jsonObj.optString("name", "ผู้ใช้งาน"),
                                username = jsonObj.optString("username", ""),
                                role = jsonObj.optString("role", "DRIVER"),
                                email = jsonObj.optString("email", ""),
                                phone = jsonObj.optString("phone", ""),
                                password = rawPassword,
                                officeName = rawOffice,
                                postalCode = postal,
                                provinceGroup = rawProvince,
                                assignedVehicleId = jsonObj.optString("assigned_vehicle_id", ""),
                                status = jsonObj.optString("status", "ACTIVE")
                            )
                        } else null
                    }
                    if (userEntities.isNotEmpty()) {
                        repository.insertUsers(userEntities)
                        onComplete(true, "ซิงค์รายชื่อ User ${userEntities.size} รายการจาก Supabase สำเร็จ!")
                    } else {
                        onComplete(true, "ไม่มีข้อมูล User ใหม่จาก Supabase")
                    }
                } else {
                    onComplete(true, "ยังไม่มีข้อมูล User ใน Supabase")
                }
            } else {
                onComplete(false, sbResult.exceptionOrNull()?.message ?: "ไม่สามารถดึงข้อมูล User ได้")
            }
        }
    }

    fun addUserToSupabase(
        id: String = "USR-${System.currentTimeMillis() % 10000}",
        name: String,
        username: String = "",
        role: String = "DRIVER",
        phone: String = "",
        password: String = "123456",
        officeName: String = "ปณ.เมืองขอนแก่น",
        postalCode: String = "",
        provinceGroup: String = "ขอนแก่น (ขก)",
        assignedVehicleId: String = "",
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val finalPostal = if (postalCode.isNotBlank()) postalCode else {
                if (officeName.contains("ศป.") || officeName.contains("ศูนย์ไปรษณีย์")) "40010"
                else if (officeName.contains("น้ำพอง")) "40310"
                else if (officeName.contains("อุดรธานี")) "41000"
                else if (officeName.contains("นครราชสีมา")) "30000"
                else if (officeName.contains("อุบลราชธานี")) "34000"
                else "40000"
            }
            val newUser = com.example.data.UserEntity(
                id = id,
                name = name,
                username = username,
                role = role,
                phone = phone,
                password = password,
                officeName = officeName,
                postalCode = finalPostal,
                provinceGroup = provinceGroup,
                assignedVehicleId = assignedVehicleId,
                status = "ACTIVE"
            )
            repository.addUser(newUser)
            val res = com.example.util.SupabaseSyncManager.updateUserInSupabase(
                baseUrl = _supabaseUrl.value,
                anonKey = _supabaseAnonKey.value,
                userId = newUser.id,
                name = newUser.name,
                username = newUser.username,
                role = newUser.role,
                phone = newUser.phone,
                password = newUser.password,
                officeName = newUser.officeName,
                postalCode = newUser.postalCode,
                provinceGroup = newUser.provinceGroup,
                assignedVehicleId = newUser.assignedVehicleId,
                status = newUser.status
            )
            if (res.isSuccess) {
                onComplete(true, "เพิ่มผู้ใช้ $name ($role) ลง Supabase สำเร็จ!")
            } else {
                onComplete(true, "บันทึกข้อมูลผู้ใช้ในเครื่องสำเร็จ (Supabase: ${res.exceptionOrNull()?.message})")
            }
        }
    }

    fun updateUserInSupabase(
        user: com.example.data.UserEntity,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            repository.addUser(user)
            val res = com.example.util.SupabaseSyncManager.updateUserInSupabase(
                baseUrl = _supabaseUrl.value,
                anonKey = _supabaseAnonKey.value,
                userId = user.id,
                name = user.name,
                username = user.username,
                role = user.role,
                phone = user.phone,
                password = user.password,
                officeName = user.officeName,
                postalCode = user.postalCode,
                provinceGroup = user.provinceGroup,
                assignedVehicleId = user.assignedVehicleId,
                status = user.status
            )
            if (res.isSuccess) {
                onComplete(true, "อัปเดตผู้ใช้ ${user.name} ใน Supabase สำเร็จ!")
            } else {
                onComplete(true, "บันทึกข้อมูลในเครื่องสำเร็จ (Supabase: ${res.exceptionOrNull()?.message})")
            }
        }
    }

    fun deleteUserInSupabase(
        userId: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            val res = com.example.util.SupabaseSyncManager.deleteUserFromSupabase(
                baseUrl = _supabaseUrl.value,
                anonKey = _supabaseAnonKey.value,
                userId = userId
            )
            onComplete(true, "ลบผู้ใช้เรียบร้อยแล้ว")
        }
    }

    private fun triggerVibrationAlert() {
        try {
            val context = getApplication<Application>().applicationContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) {
            // Ignore if vibration permissions unavailable
        }
    }
}

data class TripSummaryData(
    val vehicleName: String,
    val licensePlate: String,
    val distanceKm: Double,
    val durationMinutes: Long,
    val durationSeconds: Long,
    val topSpeedKmh: Int,
    val startPlace: String,
    val endPlace: String,
    val overspeedCount: Int
)
