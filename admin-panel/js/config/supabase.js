/**
 * Supabase Client Configuration
 * 
 * This file initializes a single, reusable Supabase client instance.
 * All pages and modules import and use this instance.
 * 
 * SECURITY NOTES:
 * - Uses SUPABASE_ANON_KEY (public, safe for frontend)
 * - NEVER uses SERVICE_ROLE_KEY in frontend code
 * - Row-Level Security (RLS) on database enforces access control
 * - Each user can only access their own data via RLS policies
 */

// ============================================
// SUPABASE CONFIGURATION CONSTANTS
// ============================================

const SUPABASE_URL = 'https://your-project.supabase.co';
const SUPABASE_ANON_KEY = 'your-anon-key-here';

/**
 * IMPORTANT: How to get your keys:
 * 1. Go to https://app.supabase.com
 * 2. Select your project
 * 3. Settings → API
 * 4. Copy Project URL → SUPABASE_URL
 * 5. Copy anon/public key → SUPABASE_ANON_KEY
 * 
 * NEVER share your SERVICE_ROLE_KEY in frontend code!
 * SERVICE_ROLE_KEY should only be used in backend/server code.
 */

// ============================================
// SUPABASE CLIENT INSTANCE
// ============================================

/**
 * Create a single Supabase client instance.
 * This uses the supabase-js library loaded from CDN.
 * 
 * Why single instance?
 * - Prevents multiple connections
 * - Shares auth state across pages
 * - Memory efficient
 * - Easy to manage session
 */
const supabase = window.supabase.createClient(SUPABASE_URL, SUPABASE_ANON_KEY);

// ============================================
// EXPORT FOR USE IN OTHER MODULES
// ============================================

/**
 * Usage in other files:
 * 
 * // Import in your page/module
 * // (no import statement needed, supabase is global)
 * 
 * // In your JavaScript module:
 * async function getUser() {
 *   const { data: { user } } = await supabase.auth.getUser();
 *   return user;
 * }
 * 
 * // Fetch data from database
 * async function getKasMasjidData() {
 *   const { data, error } = await supabase
 *     .from('kas_masjid')
 *     .select('*')
 *     .order('created_at', { ascending: false });
 *   
 *   if (error) {
 *     console.error('Database error:', error);
 *     return null;
 *   }
 *   return data;
 * }
 * 
 * // Sign out
 * async function logout() {
 *   const { error } = await supabase.auth.signOut();
 *   if (!error) {
 *     window.location.href = '/login.html';
 *   }
 * }
 */

// Make supabase accessible globally (already set via CDN)
// window.supabase is available because we load supabase-js from CDN in HTML

console.log('✓ Supabase client initialized');
