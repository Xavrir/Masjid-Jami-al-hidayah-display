/**
 * Authentication Guard Module
 * 
 * Protects pages from unauthenticated access.
 * Call requireAuth() at the start of each protected page.
 * 
 * Usage:
 * <script src="js/config/supabase.js"></script>
 * <script src="js/auth.js"></script>
 * <script>
 *   // At the top of your page script:
 *   await requireAuth();
 *   // Now safe to load page content
 * </script>
 */

// ============================================
// LOADING STATE MANAGEMENT
// ============================================

/**
 * Create and show a loading indicator
 * Used while checking authentication status
 */
function createLoadingScreen() {
    const screen = document.createElement('div');
    screen.id = 'auth-loading-screen';
    screen.style.cssText = `
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background: rgba(255, 255, 255, 0.95);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 9999;
        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    `;
    
    screen.innerHTML = `
        <div style="text-align: center;">
            <div style="
                width: 50px;
                height: 50px;
                border: 4px solid #e0e0e0;
                border-top-color: #667eea;
                border-radius: 50%;
                margin: 0 auto 20px;
                animation: spin 0.8s linear infinite;
            "></div>
            <p style="
                font-size: 16px;
                color: #333;
                margin: 0;
            ">Memeriksa akses...</p>
        </div>
        <style>
            @keyframes spin {
                to { transform: rotate(360deg); }
            }
        </style>
    `;
    
    document.body.appendChild(screen);
    return screen;
}

/**
 * Remove loading indicator
 */
function removeLoadingScreen() {
    const screen = document.getElementById('auth-loading-screen');
    if (screen) {
        screen.remove();
    }
}

// ============================================
// AUTH GUARD MAIN FUNCTION
// ============================================

/**
 * Require Authentication - Main guard function
 * 
 * Call this at the top of protected pages.
 * It will:
 * 1. Show loading indicator
 * 2. Check if user is authenticated
 * 3. Redirect to login if not
 * 4. Remove loading indicator if authenticated
 * 5. Resolve promise when ready
 * 
 * @param {Object} options - Configuration options
 * @param {string} options.loginUrl - URL to redirect if not authenticated (default: /admin-panel/login.html)
 * @param {boolean} options.hideContent - Hide page content while checking (default: true)
 * @returns {Promise<Object>} User object if authenticated
 * 
 * @example
 * // Simple usage
 * const user = await requireAuth();
 * console.log('Logged in as:', user.email);
 * 
 * @example
 * // With custom login URL
 * const user = await requireAuth({ loginUrl: '/login.html' });
 */
async function requireAuth(options = {}) {
    const {
        loginUrl = '/admin-panel/login.html',
        hideContent = true
    } = options;

    // Show loading indicator
    const loadingScreen = createLoadingScreen();

    // Hide page body while checking
    if (hideContent) {
        document.body.style.opacity = '0';
        document.body.style.transition = 'opacity 0.3s ease';
    }

    try {
        // Get current user from Supabase
        const { data: { user }, error } = await supabase.auth.getUser();

        // Handle errors
        if (error) {
            console.warn('Auth check error:', error);
            redirectToLogin(loginUrl);
            return null;
        }

        // User not authenticated
        if (!user) {
            console.info('No authenticated user found. Redirecting to login.');
            redirectToLogin(loginUrl);
            return null;
        }

        // User is authenticated - show content
        console.log('✓ User authenticated:', user.email);
        removeLoadingScreen();
        
        if (hideContent) {
            document.body.style.opacity = '1';
        }

        return user;

    } catch (error) {
        console.error('Auth guard error:', error);
        redirectToLogin(loginUrl);
        return null;
    }
}

// ============================================
// SESSION CHECKING (LIGHTWEIGHT)
// ============================================

/**
 * Check if user is authenticated without showing loading screen
 * 
 * Use this for checking auth status in utility functions,
 * not on page load (use requireAuth() instead).
 * 
 * @returns {Promise<Object|null>} User object or null if not authenticated
 * 
 * @example
 * const user = await checkAuth();
 * if (user) {
 *   console.log('User is logged in');
 * } else {
 *   console.log('User is not logged in');
 * }
 */
async function checkAuth() {
    try {
        const { data: { user }, error } = await supabase.auth.getUser();
        
        if (error || !user) {
            return null;
        }
        
        return user;
    } catch (error) {
        console.error('Check auth error:', error);
        return null;
    }
}

/**
 * Get current session details
 * 
 * Returns full session object with tokens and user info.
 * Useful for debugging or advanced auth needs.
 * 
 * @returns {Promise<Object|null>} Session object or null
 * 
 * @example
 * const session = await getSession();
 * if (session) {
 *   console.log('Access token expires:', session.expires_at);
 * }
 */
async function getSession() {
    try {
        const { data: { session }, error } = await supabase.auth.getSession();
        
        if (error || !session) {
            return null;
        }
        
        return session;
    } catch (error) {
        console.error('Get session error:', error);
        return null;
    }
}

// ============================================
// LOGOUT HANDLER
// ============================================

/**
 * Log out current user and redirect to login
 * 
 * Clears Supabase session and redirects to login page.
 * 
 * @param {Object} options - Configuration options
 * @param {string} options.loginUrl - URL to redirect after logout (default: /admin-panel/login.html)
 * @param {Function} options.onSuccess - Callback after successful logout
 * @returns {Promise<boolean>} True if logout successful
 * 
 * @example
 * // Simple logout
 * await logout();
 * 
 * @example
 * // With callback
 * await logout({
 *   onSuccess: () => console.log('Logged out!')
 * });
 */
async function logout(options = {}) {
    const {
        loginUrl = '/admin-panel/login.html',
        onSuccess = null
    } = options;

    try {
        // Show loading indicator
        const loadingScreen = createLoadingScreen();
        
        // Sign out from Supabase
        const { error } = await supabase.auth.signOut();

        if (error) {
            console.error('Logout error:', error);
            removeLoadingScreen();
            return false;
        }

        console.log('✓ User logged out');

        // Call success callback if provided
        if (onSuccess && typeof onSuccess === 'function') {
            onSuccess();
        }

        // Redirect to login after a short delay
        setTimeout(() => {
            window.location.href = loginUrl;
        }, 500);

        return true;

    } catch (error) {
        console.error('Logout error:', error);
        removeLoadingScreen();
        return false;
    }
}

// ============================================
// UTILITY FUNCTIONS
// ============================================

/**
 * Redirect to login page
 * Stores current page for potential redirect after login
 * 
 * @param {string} loginUrl - URL of login page
 */
function redirectToLogin(loginUrl) {
    // Store the page user tried to access (for future enhancement)
    const currentPage = window.location.pathname;
    sessionStorage.setItem('redirectAfterLogin', currentPage);

    // Redirect immediately
    window.location.href = loginUrl;
}

/**
 * Get user email (if authenticated)
 * 
 * @returns {Promise<string|null>} User email or null
 * 
 * @example
 * const email = await getUserEmail();
 * console.log('Admin email:', email);
 */
async function getUserEmail() {
    const user = await checkAuth();
    return user ? user.email : null;
}

/**
 * Get user ID (if authenticated)
 * 
 * @returns {Promise<string|null>} User ID or null
 * 
 * @example
 * const userId = await getUserId();
 * console.log('Admin ID:', userId);
 */
async function getUserId() {
    const user = await checkAuth();
    return user ? user.id : null;
}

// ============================================
// WATCH AUTH STATE CHANGES
// ============================================

/**
 * Subscribe to auth state changes
 * Useful for updating UI when user logs in/out
 * 
 * @param {Function} callback - Function to call on auth state change
 *                              Receives (user, session) as parameters
 * @returns {Function} Unsubscribe function
 * 
 * @example
 * const unsubscribe = onAuthStateChange((user, session) => {
 *   if (user) {
 *     console.log('User logged in:', user.email);
 *   } else {
 *     console.log('User logged out');
 *   }
 * });
 * 
 * // Later, unsubscribe if needed:
 * unsubscribe();
 */
function onAuthStateChange(callback) {
    const { data: { subscription } } = supabase.auth.onAuthStateChange(
        (event, session) => {
            const user = session?.user || null;
            callback(user, session);
        }
    );

    // Return unsubscribe function
    return () => {
        if (subscription) {
            subscription.unsubscribe();
        }
    };
}

// ============================================
// AUTO LOGOUT ON TAB CLOSE (OPTIONAL)
// ============================================

/**
 * Enable automatic logout when page is closed
 * 
 * Use with caution - logs out user when tab closes.
 * Useful for high-security applications.
 * 
 * @example
 * // In your login page:
 * enableLogoutOnTabClose();
 */
function enableLogoutOnTabClose() {
    window.addEventListener('beforeunload', async (e) => {
        // Only logout if not navigating to another page in same domain
        if (e.currentTarget.performance.navigation.type === 1) {
            await logout();
        }
    });
}

// ============================================
// CONSOLE LOG FOR DEBUGGING
// ============================================

console.log('✓ Auth guard loaded. Available functions:');
console.log('  - requireAuth(options) - Main guard function for pages');
console.log('  - checkAuth() - Lightweight user check');
console.log('  - logout(options) - Sign out user');
console.log('  - getUserEmail() - Get current user email');
console.log('  - getUserId() - Get current user ID');
console.log('  - onAuthStateChange(callback) - Watch auth changes');
