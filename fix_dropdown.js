const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (fs.existsSync(filePath)) {
        let content = fs.readFileSync(filePath, 'utf8');

        // Target exactly the span and button end
        const target = `                        <span>ล้างค่า</span>\n                    </button>\n                </div>`;
        const replacement = `                        <span>ล้างค่า</span>\n                    </button>\n                </div>\n                <!-- Status Filter -->\n                <div class="flex items-center space-x-1.5">\n                    <select id="statusFilter" onchange="filterVehicles()" class="w-full py-1.5 px-2 bg-slate-900 border border-slate-700 rounded-lg text-xs text-slate-100 focus:outline-none focus:border-sky-500">\n                        <option value="ALL">รวมทุกสถานะ</option>\n                        <option value="MOVING">🚗 กำลังวิ่ง (Moving)</option>\n                        <option value="PARKED">🏁 ถึงเป้าหมาย (Parked)</option>\n                        <option value="IDLE">☕ จอดพัก (Idle)</option>\n                        <option value="OFFLINE">🔌 ออฟไลน์ (Offline)</option>\n                    </select>\n                </div>`;

        if (!content.includes('id="statusFilter"')) {
            content = content.replace(target, replacement);
        }

        fs.writeFileSync(filePath, content);
        console.log("Updated dropdown in " + filePath);
    }
});
