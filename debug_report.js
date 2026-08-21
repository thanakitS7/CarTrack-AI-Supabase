const url = "https://rhzglphlfzhautvwpnae.supabase.co/rest/v1/location_history?created_at=gte.2026-08-20T17:00:00.000Z&created_at=lte.2026-08-21T16:59:59.999Z&order=created_at.asc&limit=10000";
const key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJoemdscGhsZnpoYXV0dndwbmFlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU4OTQzNDUsImV4cCI6MjEwMTQ3MDM0NX0.JMx8DvXgES0x9N7YmjW2n_7mgMYm_bgAkv39xb_Q2Jo";

fetch(url, { headers: { 'apikey': key, 'Authorization': `Bearer ${key}` } })
  .then(res => res.json())
  .then(data => {
      console.log("Total records fetched:", data.length);
      const byVehicle = {};
      data.forEach(r => {
          byVehicle[r.vehicle_id] = (byVehicle[r.vehicle_id] || 0) + 1;
      });
      console.log("Records by vehicle:", byVehicle);
  })
  .catch(err => console.error(err));
