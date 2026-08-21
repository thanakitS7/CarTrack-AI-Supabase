const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (fs.existsSync(filePath)) {
        let content = fs.readFileSync(filePath, 'utf8');

        // 1. CSS
        if (!content.includes('.leaflet-marker-icon { transition: transform 1.5s ease-out !important; }')) {
            content = content.replace('</style>', `    .leaflet-marker-icon { transition: transform 1.5s ease-out !important; }\n    </style>`);
        }

        // 2. Dropdown
        const searchBtnHTML = `                    <button onclick="clearSearch()" title="ล้างค่าการค้นหา" class="bg-slate-700 hover:bg-slate-600 active:bg-slate-800 text-slate-200 px-3 py-2 rounded-lg text-xs font-medium transition flex items-center space-x-1 shrink-0 border border-slate-600">\n                        <i class="fa-solid fa-rotate-left text-[11px]"></i>\n                        <span>ล้างค่า</span>\n                    </button>\n                </div>\n                <div class="flex items-center justify-between text-xs text-slate-400 px-1">`;
        const newDropdownHTML = `                    <button onclick="clearSearch()" title="ล้างค่าการค้นหา" class="bg-slate-700 hover:bg-slate-600 active:bg-slate-800 text-slate-200 px-3 py-2 rounded-lg text-xs font-medium transition flex items-center space-x-1 shrink-0 border border-slate-600">\n                        <i class="fa-solid fa-rotate-left text-[11px]"></i>\n                        <span>ล้างค่า</span>\n                    </button>\n                </div>\n                <!-- Status Filter -->\n                <div class="flex items-center space-x-1.5">\n                    <select id="statusFilter" onchange="filterVehicles()" class="w-full py-1.5 px-2 bg-slate-900 border border-slate-700 rounded-lg text-xs text-slate-100 focus:outline-none focus:border-sky-500">\n                        <option value="ALL">รวมทุกสถานะ</option>\n                        <option value="MOVING">🚗 กำลังวิ่ง (Moving)</option>\n                        <option value="PARKED">🏁 ถึงเป้าหมาย (Parked)</option>\n                        <option value="IDLE">☕ จอดพัก (Idle)</option>\n                        <option value="OFFLINE">🔌 ออฟไลน์ (Offline)</option>\n                    </select>\n                </div>\n                <div class="flex items-center justify-between text-xs text-slate-400 px-1">`;
        
        if (!content.includes('id="statusFilter"')) {
            content = content.replace(searchBtnHTML, newDropdownHTML);
        }

        // 3. updateUI filter and sort
        const oldFilterLogicRegex = /const searchVal = document\.getElementById\("searchInput"\)\.value\.toLowerCase\(\);\s+const filtered = vehicles\.filter\(v =>\s+\(v\.vehicle_name \|\| ''\)\.toLowerCase\(\)\.includes\(searchVal\) \|\|\s+\(v\.license_plate \|\| ''\)\.toLowerCase\(\)\.includes\(searchVal\) \|\|\s+\(v\.driver_name \|\| ''\)\.toLowerCase\(\)\.includes\(searchVal\) \|\|\s+\(v\.province_group \|\| ''\)\.toLowerCase\(\)\.includes\(searchVal\) \|\|\s+\(v\.office_name \|\| ''\)\.toLowerCase\(\)\.includes\(searchVal\)\s+\);/g;

        const newFilterLogic = `const searchVal = document.getElementById("searchInput").value.toLowerCase();
            const statusFilter = document.getElementById("statusFilter") ? document.getElementById("statusFilter").value : "ALL";

            let filtered = vehicles.filter(v => 
                (v.vehicle_name || '').toLowerCase().includes(searchVal) ||
                (v.license_plate || '').toLowerCase().includes(searchVal) ||
                (v.driver_name || '').toLowerCase().includes(searchVal) ||
                (v.province_group || '').toLowerCase().includes(searchVal) ||
                (v.office_name || '').toLowerCase().includes(searchVal)
            );

            if (statusFilter !== "ALL") {
                filtered = filtered.filter(v => getVehicleStatus(v) === statusFilter);
            }

            // Sort vehicles: MOVING first, then IDLE, PARKED, OFFLINE
            const statusPriority = { "MOVING": 1, "IDLE": 2, "PARKED": 3, "OFFLINE": 4 };
            filtered.sort((a, b) => {
                const sA = statusPriority[getVehicleStatus(a)] || 99;
                const sB = statusPriority[getVehicleStatus(b)] || 99;
                return sA - sB;
            });`;

        if (!content.includes('statusPriority = { "MOVING": 1')) {
            content = content.replace(oldFilterLogicRegex, newFilterLogic);
        }

        // 4. update clearSearch
        const oldClearSearchRegex = /function clearSearch\(\) {\s+const input = document\.getElementById\("searchInput"\);\s+if \(input\) {\s+input\.value = "";\s+}\s+filterVehicles\(\);\s+}/g;
        const newClearSearch = `function clearSearch() {\n            const input = document.getElementById("searchInput");\n            if (input) input.value = "";\n            const statusSelect = document.getElementById("statusFilter");\n            if (statusSelect) statusSelect.value = "ALL";\n            filterVehicles();\n        }`;
        
        if (!content.includes('statusSelect.value = "ALL"')) {
            content = content.replace(oldClearSearchRegex, newClearSearch);
        }

        fs.writeFileSync(filePath, content);
        console.log("Updated " + filePath);
    }
});
