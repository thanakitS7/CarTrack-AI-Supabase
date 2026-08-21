const fs = require('fs');

let html = fs.readFileSync('index.html', 'utf8');

// Update MOVING status label
html = html.replace(
    `            } else if (st === "MOVING") {\n                statusColor = "bg-emerald-500/20 text-emerald-400 border-emerald-500/30";\n                statusLabel = "MOVING";\n            }`,
    `            } else if (st === "MOVING") {\n                statusColor = "bg-emerald-500/20 text-emerald-400 border-emerald-500/30";\n                statusLabel = \`🚗 กำลังวิ่ง (\${v.speed_kmh || 0} km/h)\`;\n            }`
);

// Update map marker icon for MOVING to fa-truck-fast or fa-car-side
html = html.replace(
    `                } else if (st === "MOVING") {\n                    markerClass = "vehicle-moving pulse-moving";\n                    markerIcon = "fa-location-arrow";\n                }`,
    `                } else if (st === "MOVING") {\n                    markerClass = "vehicle-moving pulse-moving";\n                    markerIcon = "fa-truck-fast";\n                }`
);

fs.writeFileSync('index.html', html);
fs.writeFileSync('web/index.html', html);
fs.writeFileSync('docs/index.html', html);

console.log("Updated icons and labels across all web files.");
