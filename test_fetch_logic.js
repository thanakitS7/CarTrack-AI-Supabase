const fs = require('fs');
const code = fs.readFileSync('index.html', 'utf8');

console.log("File length:", code.length);
