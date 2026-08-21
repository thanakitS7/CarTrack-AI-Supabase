const fs = require('fs');

['index.html', 'web/index.html', 'docs/index.html'].forEach(filePath => {
    if (!fs.existsSync(filePath)) return;
    let content = fs.readFileSync(filePath, 'utf8');

    const oldFetchBlock = `let historyRecords = [];
                const res = await fetch(\`\${cleanedUrl}/rest/v1/location_history?created_at=gte.\${startIso}&created_at=lte.\${endIso}&order=created_at.asc&limit=10000\`, { headers, cache: 'no-store' });
                if (res.ok) {
                    const data = await res.json();
                    if (Array.isArray(data)) {
                        historyRecords = data.map(p => normalizeHistoryPoint(p));
                    }
                }`;

    const newFetchBlock = `let historyRecords = [];
                let offset = 0;
                const pageSize = 1000;
                while (true) {
                    const res = await fetch(\`\${cleanedUrl}/rest/v1/location_history?created_at=gte.\${startIso}&created_at=lte.\${endIso}&order=created_at.asc&limit=\${pageSize}&offset=\${offset}\`, { headers, cache: 'no-store' });
                    if (!res.ok) break;
                    const data = await res.json();
                    if (!Array.isArray(data) || data.length === 0) break;
                    historyRecords.push(...data.map(p => normalizeHistoryPoint(p)));
                    if (data.length < pageSize) break;
                    offset += pageSize;
                }`;

    if (content.includes(oldFetchBlock)) {
        content = content.replace(oldFetchBlock, newFetchBlock);
        fs.writeFileSync(filePath, content);
        console.log("Fixed pagination in " + filePath);
    } else {
        console.log("Block not found in " + filePath);
    }
});
