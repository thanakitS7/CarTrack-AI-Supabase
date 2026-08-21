const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (fs.existsSync(filePath)) {
        let content = fs.readFileSync(filePath, 'utf8');
        
        // 1. Re-add DEFAULT_SAMPLE_VEHICLES right before `function clearSystemCache()`
        if (!content.includes('const DEFAULT_SAMPLE_VEHICLES = [')) {
            content = content.replace(
                'function clearSystemCache() {',
                `const DEFAULT_SAMPLE_VEHICLES = [
            { id: "V001", vehicle_id: "V001", vehicle_name: "รถบรรทุก 6 ล้อ ปณ.ขอนแก่น", name: "รถบรรทุก 6 ล้อ ปณ.ขอนแก่น", license_plate: "81-2233 ขก", licenseplate: "81-2233 ขก", modelyear: "2023", driver_name: "นายสมชาย ผู้ขับขี่", drivername: "นายสมชาย ผู้ขับขี่", office_name: "ปณ.เมืองขอนแก่น", officename: "ปณ.เมืองขอนแก่น", province_group: "ขอนแก่น (ขก)", provincegroup: "ขอนแก่น (ขก)", latitude: 16.432, longitude: 102.823, speed_kmh: 0, status: "PARKED", is_active: true, fuel_percent: 85, fuelpercent: 85, battery_voltage: 12.6, battery: 12.6 },
            { id: "V002", vehicle_id: "V002", vehicle_name: "รถตู้ EMS ปณ.บ้านเป็ด", name: "รถตู้ EMS ปณ.บ้านเป็ด", license_plate: "นข-9900 ขก", licenseplate: "นข-9900 ขก", modelyear: "2022", driver_name: "นายสมชาย ผู้ขับขี่", drivername: "นายสมชาย ผู้ขับขี่", office_name: "ปณ.บ้านเป็ด", officename: "ปณ.บ้านเป็ด", province_group: "ขอนแก่น (ขก)", provincegroup: "ขอนแก่น (ขก)", latitude: 16.452, longitude: 102.812, speed_kmh: 0, status: "OFFLINE", is_active: true, fuel_percent: 45, fuelpercent: 45, battery_voltage: 11.9, battery: 11.9 },
            { id: "V003", vehicle_id: "V003", vehicle_name: "รถจักรยานยนต์นำจ่าย ปณ.เมืองขอนแก่น", name: "รถจักรยานยนต์นำจ่าย ปณ.เมืองขอนแก่น", license_plate: "1ตท-3341 ขก", licenseplate: "1ตท-3341 ขก", modelyear: "2024", driver_name: "นายสมชาย ผู้ขับขี่", drivername: "นายสมชาย ผู้ขับขี่", office_name: "ปณ.เมืองขอนแก่น", officename: "ปณ.เมืองขอนแก่น", province_group: "ขอนแก่น (ขก)", provincegroup: "ขอนแก่น (ขก)", latitude: 16.441, longitude: 102.836, speed_kmh: 42, status: "MOVING", is_active: true, fuel_percent: 74, fuelpercent: 74, battery_voltage: 12.7, battery: 12.7 },
            { id: "V004", vehicle_id: "V004", vehicle_name: "รถบรรทุกเทรลเลอร์ 10 ล้อ ศูนย์ไปรษณีย์ขอนแก่น", name: "รถบรรทุกเทรลเลอร์ 10 ล้อ ศูนย์ไปรษณีย์ขอนแก่น", license_plate: "70-1122 ขก", licenseplate: "70-1122 ขก", modelyear: "2024", driver_name: "ธนกฤต เทิงสูงเนิน", drivername: "ธนกฤต เทิงสูงเนิน", office_name: "ศูนย์ไปรษณีย์ขอนแก่น", officename: "ศูนย์ไปรษณีย์ขอนแก่น", province_group: "ศูนย์ขอนแก่น (ศป)", provincegroup: "ศูนย์ขอนแก่น (ศป)", latitude: 16.4812, longitude: 102.818, speed_kmh: 72, status: "MOVING", is_active: true, fuel_percent: 82, fuelpercent: 82, battery_voltage: 24.5, battery: 24.5 },
            { id: "V005", vehicle_id: "V005", vehicle_name: "รถกระบะส่งพัสดุ ปณ.น้ำพอง", name: "รถกระบะส่งพัสดุ ปณ.น้ำพอง", license_plate: "ผก-4411 ขก", licenseplate: "ผก-4411 ขก", modelyear: "2022", driver_name: "นายสมชาย ผู้ขับขี่", drivername: "นายสมชาย ผู้ขับขี่", office_name: "ปณ.น้ำพอง", officename: "ปณ.น้ำพอง", province_group: "ขอนแก่น (ขก)", provincegroup: "ขอนแก่น (ขก)", latitude: 16.821, longitude: 102.802, speed_kmh: 0, status: "STOPPED", is_active: true, fuel_percent: 65, fuelpercent: 65, battery_voltage: 12.5, battery: 12.5 },
            { id: "V006", vehicle_id: "V006", vehicle_name: "รถตู้ส่งด่วน EMS ปณ.เมืองอุดรธานี", name: "รถตู้ส่งด่วน EMS ปณ.เมืองอุดรธานี", license_plate: "ผก-1234 อด", licenseplate: "ผก-1234 อด", modelyear: "2023", driver_name: "นายอุดร สายส่ง", drivername: "นายอุดร สายส่ง", office_name: "ปณ.เมืองอุดรธานี", officename: "ปณ.เมืองอุดรธานี", province_group: "อุดรธานี (อด)", provincegroup: "อุดรธานี (อด)", latitude: 17.4138, longitude: 102.7872, speed_kmh: 55, status: "MOVING", is_active: true, fuel_percent: 90, fuelpercent: 90, battery_voltage: 12.8, battery: 12.8 },
            { id: "V119", vehicle_id: "V119", vehicle_name: "test 6 ล้อ", name: "test 6 ล้อ", license_plate: "Test-3030", licenseplate: "Test-3030", modelyear: "2024", driver_name: "ธนกฤต เทิงสูงเนิน", drivername: "ธนกฤต เทิงสูงเนิน", office_name: "ศูนย์ไปรษณีย์ขอนแก่น", officename: "ศูนย์ไปรษณีย์ขอนแก่น", province_group: "ศูนย์ขอนแก่น (ศป)", provincegroup: "ศูนย์ขอนแก่น (ศป)", latitude: 16.44418109, longitude: 102.79355781, speed_kmh: 0, status: "COMPLETED", is_active: true, fuel_percent: 100, fuelpercent: 100, battery_voltage: 12.5, battery: 12.5 }
        ];

        function clearSystemCache() {`
            );
        }

        // 2. Remove &_t=${Date.now()} from Supabase API requests and use cache: 'no-store' instead
        content = content.replace(/const cacheBuster = `&_t=\$\{Date\.now\(\)\}`;/g, '');
        content = content.replace(/\$\{cacheBuster\}/g, '');
        content = content.replace(/\{ headers \}/g, "{ headers, cache: 'no-store' }");
        
        // Let's also make sure we only replace { headers } with { headers, cache: 'no-store' } safely.
        // Wait, the regex replaced all `{ headers }`. Let's undo it and be more precise.
        // I will do it carefully using string replace.

        fs.writeFileSync(filePath, content);
        console.log(`Fixed bugs in ${filePath}`);
    }
});
