const SUPABASE_URL = "https://wqupptqjbkuldglnpvor.supabase.co"
const SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndxdXBwdHFqYmt1bGRnbG5wdm9yIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjcwMDA5MzYsImV4cCI6MjA4MjU3NjkzNn0.-MJ1IYVHeQTHMOhXftTW8l_-bb0sA5yPco2T_sRq5M4"

// ========================================================
// SUPABASE CONFIGURATION CONSTANTS
// ============================================

const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);