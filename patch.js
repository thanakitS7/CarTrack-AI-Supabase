const fs = require('fs');
let code = fs.readFileSync('web/index.html', 'utf8');

const targetStr = `
            // If active trip is in progress or moving in app (even walking/holding phone or starting trip)
            if (rawStatus.includes("MOVING") || rawStatus.includes("IN_PROGRESS") || rawStatus.includes("กำลังวิ่ง") || rawStatus.includes("เริ่มเดินทาง") || rawStatus.includes("GPS สด") || rawStatus.includes("จำลอง")) {
                return "MOVING";
            }

            // If moving by speed (> 0.5 km/h)
            if ((v.speed_kmh || 0) > 0.5) {
                return "MOVING";
            }

            // Check if GPS has not sent updates for a very long time
            if (isVehicleOffline(v)) {
                return "OFFLINE";
            }
`;

const replaceStr = `
            // Check if GPS has not sent updates for a very long time
            if (isVehicleOffline(v)) {
                return "OFFLINE";
            }

            // If active trip is in progress or moving in app (even walking/holding phone or starting trip)
            if (rawStatus.includes("MOVING") || rawStatus.includes("IN_PROGRESS") || rawStatus.includes("กำลังวิ่ง") || rawStatus.includes("เริ่มเดินทาง") || rawStatus.includes("GPS สด") || rawStatus.includes("จำลอง")) {
                return "MOVING";
            }

            // If moving by speed (> 0.5 km/h)
            if ((v.speed_kmh || 0) > 0.5) {
                return "MOVING";
            }
`;

if (code.includes(targetStr.trim())) {
    code = code.replace(targetStr.trim(), replaceStr.trim());
    fs.writeFileSync('web/index.html', code);
    console.log("Patched successfully");
} else {
    console.log("Could not find target string");
}
