const fs = require('fs');

let html = fs.readFileSync('index.html', 'utf8');

// Replace timestamp parser, isVehicleOffline, getVehicleStatus, fetchHistoryPointsForVehicle, and fetchVehiclesData
const oldSectionStart = html.indexOf("function isVehicleOffline(v) {");
const oldSectionEnd = html.indexOf("function renderVehicleCardHTML(v) {");

if (oldSectionStart === -1 || oldSectionEnd === -1) {
    console.error("Could not find section boundaries", oldSectionStart, oldSectionEnd);
    process.exit(1);
}

const newSection = `function parseTimestampMs(timeVal) {
            if (!timeVal) return 0;
            if (typeof timeVal === 'number') return timeVal;
            let s = String(timeVal).trim();
            if (!s) return 0;
            if (/^\\d{10,13}$/.test(s)) {
                let n = Number(s);
                return n < 10000000000 ? n * 1000 : n;
            }
            if (s.includes(' ') && !s.includes('T')) {
                s = s.replace(' ', 'T');
            }
            if (s.includes('T') && !s.endsWith('Z') && !s.match(/[+-]\\d{2}(:\\d{2})?$/)) {
                s += 'Z';
            }
            const t = new Date(s).getTime();
            return isNaN(t) ? 0 : t;
        }

        function isVehicleInactive(v) {
            if (!v) return true;
            const timeVal = v.updated_at || v.created_at || v.timestamp || v.last_updated || v.lastUpdateMillis || v.last_update_millis;
            if (!timeVal) return false;
            const lastTimeMs = parseTimestampMs(timeVal);
            if (!lastTimeMs || lastTimeMs <= 0) return false;
            const diffMs = Date.now() - lastTimeMs;
            return diffMs > 15 * 60 * 1000; // > 15 minutes
        }

        function isVehicleOffline(v) {
            if (!v) return true;
            const timeVal = v.updated_at || v.created_at || v.timestamp || v.last_updated || v.lastUpdateMillis || v.last_update_millis;
            if (!timeVal) return false;
            const lastTimeMs = parseTimestampMs(timeVal);
            if (!lastTimeMs || lastTimeMs <= 0) return false;
            const diffMs = Date.now() - lastTimeMs;
            return diffMs > 25 * 60 * 1000; // > 25 minutes without GPS update
        }

        function getVehicleStatus(v) {
            if (!v) return "IDLE";
            const rawStatus = (v.status || '').toUpperCase();
            
            // If explicit arrived / completed status
            if (rawStatus.includes("COMPLETED") || rawStatus.includes("PARKED") || rawStatus.includes("ถึงเป้าหมาย") || rawStatus.includes("ถึงที่หมาย") || rawStatus.includes("ARRIVED") || rawStatus.includes("DESTINATION")) {
                return "PARKED";
            }

            // If active trip is in progress or moving in app (even walking/holding phone or starting trip)
            if (rawStatus.includes("MOVING") || rawStatus.includes("IN_PROGRESS") || rawStatus.includes("กำลังวิ่ง") || rawStatus.includes("เริ่มเดินทาง") || rawStatus.includes("GPS สด") || rawStatus.includes("จำลอง")) {
                return "MOVING";
            }

            // If moving by speed (> 0.5 km/h)
            if ((v.speed_kmh || 0) > 0.5) {
                return "MOVING";
            }

            // Check if GPS has not sent updates for a long time
            if (isVehicleOffline(v)) {
                return "OFFLINE";
            }

            if (isVehicleInactive(v)) {
                return "IDLE";
            }

            return "IDLE";
        }

        function updateMobileTrackingCard() {
            const card = document.getElementById("mobileVehicleTrackingCard");
            if (!card) return;

            if (!selectedVehicleId) {
                card.classList.add("hidden");
                return;
            }

            const v = vehiclesData.find(item => item.vehicle_id === selectedVehicleId);
            if (!v) {
                card.classList.add("hidden");
                return;
            }

            const st = getVehicleStatus(v);
            let statusBadge = "";
            if (st === "OFFLINE") {
                statusBadge = \`<span class="bg-rose-950/80 text-rose-300 border border-rose-700/80 px-2 py-0.5 rounded-full font-bold text-[10px]"><i class="fa-solid fa-wifi-slash mr-1 text-rose-400"></i>OFFLINE (ขาดเชื่อมต่อ)</span>\`;
            } else if (st === "PARKED") {
                statusBadge = \`<span class="bg-sky-950/90 text-sky-300 border border-sky-700/80 px-2 py-0.5 rounded-full font-bold text-[10px]"><i class="fa-solid fa-flag-checkered mr-1 text-sky-400"></i>🏁 ถึงเป้าหมายแล้ว</span>\`;
            } else if (st === "MOVING") {
                statusBadge = \`<span class="bg-emerald-500/20 text-emerald-400 border border-emerald-500/30 px-2 py-0.5 rounded-full font-bold text-[10px] animate-pulse"><i class="fa-solid fa-circle text-[6px] mr-1"></i>วิ่งอยู่ \${v.speed_kmh || 0} km/h</span>\`;
            } else {
                statusBadge = \`<span class="bg-amber-500/20 text-amber-400 border border-amber-500/30 px-2 py-0.5 rounded-full font-bold text-[10px]"><i class="fa-solid fa-parking mr-1"></i>จอดพัก / IDLE</span>\`;
            }

            const distanceKm = (vehicleDistancesKm[v.vehicle_id] || 0.0).toFixed(1);
            const placeName = vehicleLocationNames[v.vehicle_id] || \`พิกัด \${(v.latitude || 0).toFixed(4)}, \${(v.longitude || 0).toFixed(4)}\`;

            card.innerHTML = \`
                <div class="flex items-center justify-between pb-1.5 border-b border-slate-800">
                    <div class="flex items-center space-x-2 truncate">
                        <div class="w-7 h-7 rounded-lg bg-sky-500/20 text-sky-400 flex items-center justify-center font-bold shrink-0">
                            <i class="fa-solid fa-truck-fast text-xs"></i>
                        </div>
                        <div class="truncate">
                            <h4 class="font-bold text-white text-xs truncate">\${v.vehicle_name || v.vehicle_id} <span class="text-[10px] text-slate-400">(\${v.license_plate || '-'})</span></h4>
                            <p class="text-[10px] text-slate-400 truncate">คนขับ: \${v.driver_name || '-'} • \${v.office_name || v.province_group || ''}</p>
                        </div>
                    </div>
                    <div class="flex items-center space-x-1 shrink-0">
                        \${statusBadge}
                        <button onclick="deselectVehicle()" class="text-slate-400 hover:text-white p-1 ml-1 text-sm" title="ปิด"><i class="fa-solid fa-xmark"></i></button>
                    </div>
                </div>
                <div class="flex items-center justify-between text-[11px] text-slate-300">
                    <div class="truncate pr-2" title="\${placeName}">
                        <i class="fa-solid fa-location-dot text-rose-400 mr-1"></i>
                        <span class="text-slate-200 font-medium">\${placeName}</span>
                    </div>
                    <div class="shrink-0 text-sky-400 font-mono font-bold">
                        <i class="fa-solid fa-route mr-1"></i>\${distanceKm} กม.
                    </div>
                </div>
                <div class="grid grid-cols-3 gap-1.5 pt-1">
                    <button onclick="toggleAutoFollow(!isAutoFollowEnabled)" class="py-1.5 px-2 \${isAutoFollowEnabled ? 'bg-sky-600 text-white font-bold' : 'bg-slate-800 text-sky-400 border border-slate-700'} hover:bg-sky-500 hover:text-white rounded-lg text-[10px] transition flex items-center justify-center space-x-1">
                        <i class="fa-solid fa-crosshairs"></i>
                        <span>\${isAutoFollowEnabled ? 'กำลังติดตาม' : 'ติดตามรถ'}</span>
                    </button>
                    <button onclick="centerOnSelectedVehicle()" class="py-1.5 px-2 bg-slate-800 hover:bg-slate-700 text-slate-200 border border-slate-700 rounded-lg text-[10px] transition flex items-center justify-center space-x-1">
                        <i class="fa-solid fa-magnifying-glass-location"></i>
                        <span>ซูมใกล้</span>
                    </button>
                    <button onclick="openVehicleHistoryModal('\${v.vehicle_id}')" class="py-1.5 px-2 bg-indigo-900/80 hover:bg-indigo-800 text-indigo-200 border border-indigo-500/30 rounded-lg text-[10px] font-semibold transition flex items-center justify-center space-x-1">
                        <i class="fa-solid fa-clock-rotate-left"></i>
                        <span>ดูประวัติ</span>
                    </button>
                </div>
            \`;
            card.classList.remove("hidden");
        }

        function deselectVehicle() {
            selectedVehicleId = null;
            isAutoFollowEnabled = false;
            const autoFollowToggle = document.getElementById("autoFollowToggle");
            if (autoFollowToggle) autoFollowToggle.checked = false;
            updateMobileTrackingCard();
            updateUI(vehiclesData);
        }

        function centerOnSelectedVehicle() {
            if (!selectedVehicleId) return;
            const v = vehiclesData.find(item => item.vehicle_id === selectedVehicleId);
            if (v && v.latitude && v.longitude) {
                map.flyTo([v.latitude, v.longitude], 16, { duration: 1.0 });
            }
        }

        function toggleGroupByProvince(enabled) {
            isGroupedByProvince = enabled;
            const badge = document.getElementById("groupCountBadge");
            if (badge) {
                if (enabled) badge.classList.remove("hidden");
                else badge.classList.add("hidden");
            }
            updateUI(vehiclesData);
        }

        function toggleAccordionGroup(groupName) {
            userInteractedAccordion = true;
            if (expandedGroups.has(groupName)) {
                expandedGroups.delete(groupName);
            } else {
                expandedGroups.add(groupName);
            }
            updateUI(vehiclesData);
        }

        function toggleAutoFollow(enabled) {
            isAutoFollowEnabled = enabled;
            const icon = document.getElementById("autoFollowIcon");
            if (icon) {
                if (enabled) {
                    icon.className = "fa-solid fa-crosshairs text-sky-400 text-xs animate-pulse";
                } else {
                    icon.className = "fa-solid fa-crosshairs text-slate-400 text-xs";
                }
            }
            if (isAutoFollowEnabled && selectedVehicleId) {
                centerOnSelectedVehicle();
            }
        }

        // Adjust map bounds to encompass all visible vehicle markers
        function fitMapBoundsAllVehicles() {
            const searchInput = document.getElementById("searchInput");
            const searchVal = searchInput ? searchInput.value.toLowerCase() : "";
            let targetVehicles = vehiclesData;
            if (searchVal) {
                targetVehicles = targetVehicles.filter(v => 
                    (v.vehicle_name || '').toLowerCase().includes(searchVal) ||
                    (v.license_plate || '').toLowerCase().includes(searchVal) ||
                    (v.driver_name || '').toLowerCase().includes(searchVal) ||
                    (v.province_group || '').toLowerCase().includes(searchVal) ||
                    (v.office_name || '').toLowerCase().includes(searchVal)
                );
            }
            const validCoords = targetVehicles
                .filter(v => v.latitude && v.longitude)
                .map(v => [v.latitude, v.longitude]);

            if (validCoords.length === 0) {
                alert("ไม่พบตำแหน่งรถบนแผนที่ในขณะนี้");
                return;
            }

            if (validCoords.length === 1) {
                map.flyTo(validCoords[0], 14, { duration: 1 });
            } else {
                const bounds = L.latLngBounds(validCoords);
                map.fitBounds(bounds, { padding: [50, 50], maxZoom: 15, animate: true });
            }
        }

        // Playback state variables
        let currentHistoryPoints = [];
        let currentHistoryIndex = 0;
        let isPlaybackPlaying = false;
        let playbackInterval = null;
        let playbackMarker = null;
        let mainPolyline = null;

        // --- Distance Calculation Function (Haversine Formula) ---
        function getDistanceFromLatLonInKm(lat1, lon1, lat2, lon2) {
            const R = 6371; // Radius of the earth in km
            const dLat = (lat2 - lat1) * Math.PI / 180;
            const dLon = (lon2 - lon1) * Math.PI / 180;
            const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                      Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                      Math.sin(dLon / 2) * Math.sin(dLon / 2);
            const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            return R * c;
        }

        function calculateTodayDistanceKm(points) {
            if (!points || points.length < 2) return 0.0;
            let total = 0;
            for (let i = 0; i < points.length - 1; i++) {
                const p1 = points[i];
                const p2 = points[i + 1];
                if (p1.latitude && p1.longitude && p2.latitude && p2.longitude) {
                    const dist = getDistanceFromLatLonInKm(p1.latitude, p1.longitude, p2.latitude, p2.longitude);
                    if (!isNaN(dist) && dist > 0.005 && dist < 5.0) { // filter noise & jumps
                        total += dist;
                    }
                }
            }
            return total;
        }

        // --- Reverse Geocoding Cache ---
        let placeNameCache = {};
        async function getPlaceName(lat, lng) {
            if (!lat || !lng) return "-";
            const key = \`\${lat.toFixed(3)},\${lng.toFixed(3)}\`;
            if (placeNameCache[key]) return placeNameCache[key];

            try {
                const res = await fetch(\`https://nominatim.openstreetmap.org/reverse?format=json&lat=\${lat}&lon=\${lng}&zoom=16&addressdetails=1\`, {
                    headers: { 'Accept-Language': 'th,en' }
                });
                if (res.ok) {
                    const data = await res.json();
                    const addr = data.address || {};
                    const name = addr.village || addr.subdistrict || addr.district || addr.city || addr.state || data.display_name.split(',')[0] || \`\${lat.toFixed(4)}, \${lng.toFixed(4)}\`;
                    placeNameCache[key] = name;
                    return name;
                }
            } catch (e) {}
            return \`\${lat.toFixed(4)}, \${lng.toFixed(4)}\`;
        }

        function clearSystemCache() {
            if (confirm("คุณต้องการล้างแคชข้อมูลเก่าทั้งหมดในเบราว์เซอร์ เพื่อโหลดข้อมูลสดใหม่จากระบบเรียลไทม์หรือไม่?")) {
                try {
                    localStorage.clear();
                    sessionStorage.clear();
                } catch (e) {}
                vehiclesData = [];
                vehicleDistancesKm = {};
                vehicleLocationNames = {};
                placeNameCache = {};
                alert("ล้างแคชเรียบร้อยแล้ว กำลังรีโหลดระบบ...");
                location.reload();
            }
        }

        // --- Data Normalization Helpers ---
        function normalizeVehicle(v) {
            if (!v) return v;
            const vid = String(v.vehicle_id || v.id || v.vehicleid || v.v_id || '');
            const vname = v.vehicle_name || v.name || v.vehiclename || v.v_name || vid || 'รถไม่มีชื่อ';
            const plate = v.license_plate || v.licenseplate || v.plate || '-';
            const driver = v.driver_name || v.drivername || v.driver || '-';
            const office = v.office_name || v.officename || v.office || '-';
            const postal = v.postal_code || v.postalcode || v.zip_code || v.zipcode || v.postal || '-';
            const province = v.province_group || v.provincegroup || v.province || v.province_name || '-';

            let lat = 0.0;
            if (v.latitude !== undefined && v.latitude !== null && !isNaN(v.latitude) && Number(v.latitude) !== 0) lat = Number(v.latitude);
            else if (v.currentlat !== undefined && v.currentlat !== null && !isNaN(v.currentlat) && Number(v.currentlat) !== 0) lat = Number(v.currentlat);
            else if (v.current_lat !== undefined && v.current_lat !== null && !isNaN(v.current_lat) && Number(v.current_lat) !== 0) lat = Number(v.current_lat);
            else if (v.lat !== undefined && v.lat !== null && !isNaN(v.lat) && Number(v.lat) !== 0) lat = Number(v.lat);

            let lng = 0.0;
            if (v.longitude !== undefined && v.longitude !== null && !isNaN(v.longitude) && Number(v.longitude) !== 0) lng = Number(v.longitude);
            else if (v.currentlng !== undefined && v.currentlng !== null && !isNaN(v.currentlng) && Number(v.currentlng) !== 0) lng = Number(v.currentlng);
            else if (v.current_lng !== undefined && v.current_lng !== null && !isNaN(v.current_lng) && Number(v.current_lng) !== 0) lng = Number(v.current_lng);
            else if (v.lng !== undefined && v.lng !== null && !isNaN(v.lng) && Number(v.lng) !== 0) lng = Number(v.lng);

            let speed = 0;
            if (v.speed_kmh !== undefined && v.speed_kmh !== null && !isNaN(v.speed_kmh)) speed = Number(v.speed_kmh);
            else if (v.speedkmh !== undefined && v.speedkmh !== null && !isNaN(v.speedkmh)) speed = Number(v.speedkmh);
            else if (v.speed !== undefined && v.speed !== null && !isNaN(v.speed)) speed = Number(v.speed);

            v.vehicle_id = vid;
            v.id = vid;
            v.vehicle_name = vname;
            v.name = vname;
            v.license_plate = plate;
            v.licenseplate = plate;
            v.driver_name = driver;
            v.drivername = driver;
            v.office_name = office;
            v.officename = office;
            v.postal_code = postal;
            v.postalcode = postal;
            v.province_group = province;
            v.provincegroup = province;
            v.latitude = lat;
            v.currentlat = lat;
            v.longitude = lng;
            v.currentlng = lng;
            v.speed_kmh = speed;
            v.speedkmh = speed;
            v.is_active = v.is_active !== undefined ? v.is_active : true;
            return v;
        }

        function normalizeHistoryPoint(p) {
            if (!p) return p;
            let lat = Number(p.latitude || p.currentlat || p.lat || 0.0);
            let lng = Number(p.longitude || p.currentlng || p.lng || 0.0);
            let spd = Number(p.speed_kmh || p.speedkmh || p.speed || 0);
            p.latitude = lat;
            p.longitude = lng;
            p.speed_kmh = spd;
            return p;
        }

        async function fetchHistoryPointsForVehicle(vehicleId) {
            const cleanedUrl = supabaseUrl.trim().replace(/\\/$/, "");
            const headers = { 'apikey': supabaseKey, 'Authorization': \`Bearer \${supabaseKey}\` };
            const cacheBuster = \`&_t=\${Date.now()}\`;
            try {
                // Try matching by vehicle_id or license_plate
                let res = await fetch(\`\${cleanedUrl}/rest/v1/location_history?vehicle_id=eq.\${vehicleId}&order=created_at.asc&limit=300\${cacheBuster}\`, { headers });
                if (res.ok) {
                    const data = await res.json();
                    if (Array.isArray(data) && data.length > 0) {
                        return data.map(p => normalizeHistoryPoint(p));
                    }
                }
                // Try fallback with telemetry_history
                let res2 = await fetch(\`\${cleanedUrl}/rest/v1/telemetry_history?vehicle_id=eq.\${vehicleId}&order=created_at.asc&limit=300\${cacheBuster}\`, { headers });
                if (res2.ok) {
                    const data2 = await res2.json();
                    if (Array.isArray(data2) && data2.length > 0) {
                        return data2.map(p => normalizeHistoryPoint(p));
                    }
                }
            } catch (e) {
                console.warn("Fetch location history error:", e);
            }
            return [];
        }

        // --- Fast Bulk Fetch Vehicles & Telemetry from Supabase ---
        let isFetchingVehicles = false;
        async function fetchVehiclesData() {
            if (isFetchingVehicles) return;
            isFetchingVehicles = true;
            try {
                const cleanedUrl = supabaseUrl.trim().replace(/\\/$/, "");
                const headers = {
                    'apikey': supabaseKey,
                    'Authorization': \`Bearer \${supabaseKey}\`
                };
                const cacheBuster = \`&_t=\${Date.now()}\`;

                // Parallel fetch vehicles, latest telemetry, and active usage logs
                const [vehRes, locRes, usageRes] = await Promise.allSettled([
                    fetch(\`\${cleanedUrl}/rest/v1/vehicles?select=*\${cacheBuster}\`, { headers }),
                    fetch(\`\${cleanedUrl}/rest/v1/location_history?select=*&order=created_at.desc&limit=600\${cacheBuster}\`, { headers }),
                    fetch(\`\${cleanedUrl}/rest/v1/vehicle_usage_logs?select=*&order=created_at.desc&limit=100\${cacheBuster}\`, { headers })
                ]);

                let rawVehicles = [];
                if (vehRes.status === 'fulfilled' && vehRes.value.ok) {
                    const data = await vehRes.value.json();
                    if (Array.isArray(data) && data.length > 0) {
                        rawVehicles = data;
                    }
                }

                if (rawVehicles.length === 0) {
                    if (!vehiclesData || vehiclesData.length === 0) {
                        vehiclesData = DEFAULT_SAMPLE_VEHICLES.map(v => normalizeVehicle(v));
                    }
                } else {
                    vehiclesData = rawVehicles.map(v => normalizeVehicle(v));
                }

                // Process latest telemetry points into a lookup map by vehicle_id and plate
                const latestTelemetryMap = {};
                const vehicleHistoryPointsMap = {};

                if (locRes.status === 'fulfilled' && locRes.value.ok) {
                    const locData = await locRes.value.json();
                    if (Array.isArray(locData)) {
                        for (const p of locData) {
                            const vKey = String(p.vehicle_id || p.vehicleid || p.license_plate || p.licenseplate || '').trim();
                            if (vKey) {
                                if (!latestTelemetryMap[vKey]) {
                                    latestTelemetryMap[vKey] = p;
                                }
                                if (!vehicleHistoryPointsMap[vKey]) {
                                    vehicleHistoryPointsMap[vKey] = [];
                                }
                                vehicleHistoryPointsMap[vKey].push(normalizeHistoryPoint(p));
                            }
                        }
                    }
                }

                // Process active usage logs into a lookup map
                const activeUsageMap = {};
                if (usageRes.status === 'fulfilled' && usageRes.value.ok) {
                    const usageData = await usageRes.value.json();
                    if (Array.isArray(usageData)) {
                        for (const u of usageData) {
                            const vKey = String(u.vehicle_id || u.vehicleid || u.license_plate || '').trim();
                            if (vKey && !activeUsageMap[vKey]) {
                                activeUsageMap[vKey] = u;
                            }
                        }
                    }
                }

                // Merge live telemetry and usage status into vehiclesData
                for (const v of vehiclesData) {
                    const vid = String(v.vehicle_id || '').trim();
                    const vPlate = String(v.license_plate || '').trim();
                    const vPlateClean = vPlate.replace(/\\s+/g, '');

                    // Find matching telemetry point
                    const telem = latestTelemetryMap[vid] || 
                                  latestTelemetryMap[vPlate] || 
                                  latestTelemetryMap[vPlateClean];

                    if (telem) {
                        const lat = Number(telem.latitude || telem.lat || 0);
                        const lng = Number(telem.longitude || telem.lng || 0);
                        if (lat !== 0 && lng !== 0) {
                            v.latitude = lat;
                            v.currentlat = lat;
                            v.longitude = lng;
                            v.currentlng = lng;
                        }
                        if (telem.speed_kmh !== undefined && telem.speed_kmh !== null) {
                            v.speed_kmh = Number(telem.speed_kmh);
                        }
                        if (telem.status) {
                            v.status = telem.status;
                        }
                        if (telem.created_at || telem.timestamp) {
                            v.updated_at = telem.created_at || telem.timestamp;
                        }
                        if (telem.driver_name && (!v.driver_name || v.driver_name === '-')) {
                            v.driver_name = telem.driver_name;
                        }
                    }

                    // Find matching usage log
                    const usage = activeUsageMap[vid] || activeUsageMap[vPlate] || activeUsageMap[vPlateClean];
                    if (usage) {
                        if (usage.driver_name && (!v.driver_name || v.driver_name === '-')) {
                            v.driver_name = usage.driver_name;
                        }
                        if (usage.status && (!v.status || v.status === 'IDLE' || v.status === 'STOPPED')) {
                            v.status = usage.status;
                        }
                        if (usage.office_name && (!v.office_name || v.office_name === '-')) {
                            v.office_name = usage.office_name;
                        }
                    }

                    // Calculate distance from history points if available
                    const hist = vehicleHistoryPointsMap[vid] || vehicleHistoryPointsMap[vPlate] || [];
                    if (hist.length > 1) {
                        // Reverse because history was sorted desc
                        const sortedHist = [...hist].reverse();
                        vehicleDistancesKm[v.vehicle_id] = calculateTodayDistanceKm(sortedHist);
                    }
                }

                // Render UI immediately without waiting for place names
                updateUI(vehiclesData);

                const lastUpdatedEl = document.getElementById("lastUpdated");
                if (lastUpdatedEl) lastUpdatedEl.innerText = new Date().toLocaleTimeString('th-TH');

                // Resolve place names asynchronously in background
                for (const v of vehiclesData) {
                    if (v.latitude && v.longitude) {
                        getPlaceName(v.latitude, v.longitude).then(name => {
                            vehicleLocationNames[v.vehicle_id] = name;
                            const el = document.getElementById(\`loc-name-\${v.vehicle_id}\`);
                            if (el) el.innerText = name;
                        });
                    }
                }
            } catch (err) {
                console.error("Fetch vehicles error:", err);
                if (!vehiclesData || vehiclesData.length === 0) {
                    vehiclesData = DEFAULT_SAMPLE_VEHICLES.map(v => normalizeVehicle(v));
                    updateUI(vehiclesData);
                }
            } finally {
                isFetchingVehicles = false;
            }
        }

        `;

html = html.substring(0, oldSectionStart) + newSection + html.substring(oldSectionEnd);

fs.writeFileSync('index.html', html);
fs.writeFileSync('web/index.html', html);
fs.writeFileSync('docs/index.html', html);

console.log("Successfully replaced fetch & status logic in index.html, web/index.html, and docs/index.html");
