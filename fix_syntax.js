const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (fs.existsSync(filePath)) {
        let content = fs.readFileSync(filePath, 'utf8');
        // Replace second let placeNameCache = {};
        content = content.replace(
            `        // --- Reverse Geocoding Cache ---\n        let placeNameCache = {};`,
            `        // --- Reverse Geocoding Cache ---`
        );
        fs.writeFileSync(filePath, content);
        console.log(`Fixed ${filePath}`);
    }
});
