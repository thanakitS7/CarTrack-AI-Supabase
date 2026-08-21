const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (!fs.existsSync(filePath)) return;
    let content = fs.readFileSync(filePath, 'utf8');

    const oldFetchBlock = `let usageLogs = [];
                try {
                    const uRes = await fetch(\`\${cleanedUrl}/rest/v1/vehicle_usage_logs?created_at=gte.\${startIso}&created_at=lte.\${endIso}&order=created_at.asc&limit=2000\`, { headers, cache: 'no-store' });
                    if (uRes.ok) {
                        const uData = await uRes.json();
                        if (Array.isArray(uData)) usageLogs = uData;
                    }
                } catch(e) {}`;

    const newFetchBlock = `let usageLogs = [];
                try {
                    let uOffset = 0;
                    while (true) {
                        const uRes = await fetch(\`\${cleanedUrl}/rest/v1/vehicle_usage_logs?created_at=gte.\${startIso}&created_at=lte.\${endIso}&order=created_at.asc&limit=\${pageSize}&offset=\${uOffset}\`, { headers, cache: 'no-store' });
                        if (!uRes.ok) break;
                        const uData = await uRes.json();
                        if (!Array.isArray(uData) || uData.length === 0) break;
                        usageLogs.push(...uData);
                        if (uData.length < pageSize) break;
                        uOffset += pageSize;
                    }
                } catch(e) {}`;

    if (content.includes(oldFetchBlock)) {
        content = content.replace(oldFetchBlock, newFetchBlock);
        fs.writeFileSync(filePath, content);
        console.log("Fixed usage pagination in " + filePath);
    } else {
        console.log("Block not found in " + filePath);
    }
});
