const url = "https://rhzglphlfzhautvwpnae.supabase.co/rest/v1/location_history?created_at=gte.2026-08-20T17:00:00.000Z&created_at=lte.2026-08-21T16:59:59.999Z&order=created_at.asc";
const key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJoemdscGhsZnpoYXV0dndwbmFlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU4OTQzNDUsImV4cCI6MjEwMTQ3MDM0NX0.JMx8DvXgES0x9N7YmjW2n_7mgMYm_bgAkv39xb_Q2Jo";

async function fetchAll() {
    let allData = [];
    let offset = 0;
    const pageSize = 1000;
    while (true) {
        const res = await fetch(`${url}&limit=${pageSize}&offset=${offset}`, { headers: { 'apikey': key, 'Authorization': `Bearer ${key}` } });
        if (!res.ok) break;
        const data = await res.json();
        if (!Array.isArray(data) || data.length === 0) break;
        allData.push(...data);
        if (data.length < pageSize) break;
        offset += pageSize;
    }
    
    console.log("Total records fetched with pagination:", allData.length);
    const byVehicle = {};
    allData.forEach(r => {
        byVehicle[r.vehicle_id] = (byVehicle[r.vehicle_id] || 0) + 1;
    });
    console.log("Records by vehicle:", byVehicle);
}
fetchAll();
