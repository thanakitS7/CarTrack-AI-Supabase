const url = "https://rhzglphlfzhautvwpnae.supabase.co/rest/v1/vehicles?select=*&limit=5";
const key = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InJoemdscGhsZnpoYXV0dndwbmFlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODU4OTQzNDUsImV4cCI6MjEwMTQ3MDM0NX0.JMx8DvXgES0x9N7YmjW2n_7mgMYm_bgAkv39xb_Q2Jo";

fetch(url, { headers: { 'apikey': key, 'Authorization': `Bearer ${key}` } })
  .then(res => res.json())
  .then(data => console.log(JSON.stringify(data, null, 2)))
  .catch(err => console.error(err));
