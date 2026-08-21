const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (fs.existsSync(filePath)) {
        let content = fs.readFileSync(filePath, 'utf8');

        // 1. Update normalizeVehicle to use workplace
        content = content.replace(
            /const office = v\.office_name \|\| v\.officename \|\| v\.office \|\| '-';/g,
            `const office = v.workplace || v.office_name || v.officename || v.office || '-';`
        );

        // 2. Remove "กลุ่ม ปจ." from renderVehicleCardHTML
        const cardTarget = /<div( class="col-span-2")?><i class="fa-solid fa-building text-emerald-400 mr-1"><\/i>ที่ทำการ: <span class="text-slate-200">\$\{v\.office_name \|\| '-'\}<\/span><\/div>\s*<div><i class="fa-solid fa-map-location-dot text-indigo-400 mr-1"><\/i>กลุ่ม ปจ\.: <span class="text-slate-200">\$\{v\.province_group \|\| '-'\}<\/span><\/div>/g;
        const cardReplacement = `<div class="col-span-2"><i class="fa-solid fa-building text-emerald-400 mr-1"></i>ที่ทำการ: <span class="text-slate-200">\$\{v.office_name || '-'\}<\/span></div>`;
        
        // Let's do a more robust regex or simple split for renderVehicleCardHTML
        content = content.replace(
            `<div><i class="fa-solid fa-building text-emerald-400 mr-1"></i>ที่ทำการ: <span class="text-slate-200">\${v.office_name || '-'}</span></div>
                        <div><i class="fa-solid fa-map-location-dot text-indigo-400 mr-1"></i>กลุ่ม ปจ.: <span class="text-slate-200">\${v.province_group || '-'}</span></div>`,
            `<div class="col-span-2"><i class="fa-solid fa-building text-emerald-400 mr-1"></i>ที่ทำการ: <span class="text-slate-200">\${v.office_name || '-'}</span></div>`
        );

        // 3. Update the grouping toggle label
        content = content.replace(
            `<span class="text-[11px] font-medium"><i class="fa-solid fa-folder-tree text-sky-400 mr-1"></i>จัดกลุ่มตามกลุ่มจังหวัด</span>`,
            `<span class="text-[11px] font-medium"><i class="fa-solid fa-folder-tree text-sky-400 mr-1"></i>จัดกลุ่มตามที่ทำการ</span>`
        );

        // 4. Update grouping logic in updateUI
        content = content.replace(
            `// Group vehicles by province_group\n                const groups = {};\n                filtered.forEach(v => {\n                    const gName = (v.province_group || 'อื่นๆ / ไม่ระบุกลุ่ม').trim();`,
            `// Group vehicles by office_name (workplace)\n                const groups = {};\n                filtered.forEach(v => {\n                    const gName = (v.office_name && v.office_name !== '-' ? v.office_name : 'อื่นๆ / ไม่ระบุที่ทำการ').trim();`
        );
        content = content.replace(
            `// Group vehicles by province_group
                const groups = {};
                filtered.forEach(v => {
                    const gName = (v.province_group || 'อื่นๆ / ไม่ระบุกลุ่ม').trim();`,
            `// Group vehicles by office_name (workplace)
                const groups = {};
                filtered.forEach(v => {
                    const gName = (v.office_name && v.office_name !== '-' ? v.office_name : 'อื่นๆ / ไม่ระบุที่ทำการ').trim();`
        );

        // 5. Update search logic to also search in workplace/office_name but province_group is still fine, 
        // no need to change that as office_name is already in the filter condition: `(v.office_name || '').toLowerCase().includes(searchVal)`

        fs.writeFileSync(filePath, content);
        console.log("Updated " + filePath);
    }
});
