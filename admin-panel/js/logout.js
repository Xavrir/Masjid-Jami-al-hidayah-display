/**
 * Logout Module
 * 
 * Handles user logout with session clearing and redirect
 * Used across all protected pages
 * 
 * Dependencies:
 * - supabase.js (must be loaded first)
 * 
 * Usage:
 * - Simple logout: logoutUser()
 * - Logout with custom message: logoutUser('Custom message')
 * - Silent logout (no confirm): logoutUser('', true)
 */

/**
 * Main logout function
 * Shows confirmation dialog, clears session, and redirects to login
 * 
 * @param {string} customMessage - Custom message for confirm dialog (optional)
 * @param {boolean} silent - Skip confirm dialog if true (optional)
 * @returns {Promise<void>}
 */
async function logoutUser(customMessage = '', silent = false) {
    try {
        // Show confirmation dialog unless silent mode
        if (!silent) {
            const message = customMessage || 'Apakah Anda yakin ingin keluar dari akun ini?';
            const confirmed = confirm(message);
            
            if (!confirmed) {
                console.log('Logout cancelled by user');
                return;
            }
        }

        // Show loading indicator
        showLogoutLoading();

        // Get current session before logout
        const session = await getSession();
        if (session) {
            console.log('Current user:', session.user.email);
        }

        // Sign out from Supabase
        const { error } = await supabase.auth.signOut();

        if (error) {
            console.error('Supabase signOut error:', error);
            hideLogoutLoading();
            showLogoutError('Gagal keluar. Coba lagi.');
            return;
        }

        // Clear all local/session storage
        clearAllStorage();

        // Show success message
        showLogoutSuccess();

        // Redirect to login after brief delay
        setTimeout(() => {
            window.location.href = '/admin-panel/login.html';
        }, 800);

    } catch (error) {
        console.error('Logout error:', error);
        hideLogoutLoading();
        showLogoutError('Terjadi kesalahan saat keluar. Silakan refresh halaman.');
    }
}

/**
 * Alternative logout with custom redirect
 * Allows specifying a custom redirect URL
 * 
 * @param {string} redirectUrl - URL to redirect to after logout
 * @param {boolean} showConfirm - Show confirmation dialog (default: true)
 * @returns {Promise<void>}
 */
async function logoutUserWithRedirect(redirectUrl = '/admin-panel/login.html', showConfirm = true) {
    try {
        if (showConfirm) {
            const confirmed = confirm('Apakah Anda yakin ingin keluar dari akun ini?');
            if (!confirmed) return;
        }

        showLogoutLoading();

        // Sign out
        const { error } = await supabase.auth.signOut();
        if (error) throw error;

        // Clear storage
        clearAllStorage();

        // Show success and redirect
        showLogoutSuccess();
        setTimeout(() => {
            window.location.href = redirectUrl;
        }, 800);

    } catch (error) {
        console.error('Logout redirect error:', error);
        hideLogoutLoading();
        showLogoutError('Gagal keluar. Silakan refresh halaman.');
    }
}

/**
 * Silent logout for background operations
 * Does NOT show confirmation or redirect
 * Useful for auto-logout on token expiry
 * 
 * @returns {Promise<boolean>} - true if successful, false otherwise
 */
async function silentLogout() {
    try {
        console.log('Performing silent logout...');
        
        const { error } = await supabase.auth.signOut();
        
        if (error) {
            console.error('Silent logout error:', error);
            return false;
        }

        // Clear storage
        clearAllStorage();
        console.log('Silent logout completed');
        return true;

    } catch (error) {
        console.error('Silent logout exception:', error);
        return false;
    }
}

/**
 * Force logout (immediate, no confirm, no animation)
 * Used for security/timeout scenarios
 */
async function forceLogout() {
    try {
        await supabase.auth.signOut().catch(() => {});
        clearAllStorage();
        window.location.href = '/admin-panel/login.html';
    } catch (error) {
        console.error('Force logout error:', error);
        window.location.href = '/admin-panel/login.html';
    }
}

// ============================================
// HELPER FUNCTIONS
// ============================================

/**
 * Clear all browser storage (localStorage, sessionStorage)
 */
function clearAllStorage() {
    try {
        // Clear localStorage
        const keysToKeep = ['theme-preference']; // Keep non-auth items if needed
        Object.keys(localStorage).forEach(key => {
            if (!keysToKeep.includes(key)) {
                localStorage.removeItem(key);
            }
        });

        // Clear sessionStorage
        sessionStorage.clear();

        console.log('Storage cleared successfully');
    } catch (error) {
        console.error('Clear storage error:', error);
    }
}

/**
 * Get current session from Supabase
 * @returns {Promise<object|null>}
 */
async function getSession() {
    try {
        const { data: { session } } = await supabase.auth.getSession();
        return session;
    } catch (error) {
        console.error('Get session error:', error);
        return null;
    }
}

// ============================================
// UI FEEDBACK FUNCTIONS
// ============================================

/**
 * Show loading state during logout
 */
function showLogoutLoading() {
    // Check if there's an existing loading element
    let loadingDiv = document.getElementById('logoutLoading');
    
    if (!loadingDiv) {
        loadingDiv = document.createElement('div');
        loadingDiv.id = 'logoutLoading';
        loadingDiv.innerHTML = `
            <div style="
                position: fixed;
                top: 0;
                left: 0;
                width: 100%;
                height: 100%;
                background: rgba(255, 255, 255, 0.95);
                display: flex;
                align-items: center;
                justify-content: center;
                z-index: 99999;
                flex-direction: column;
                gap: 20px;
            ">
                <div style="
                    width: 50px;
                    height: 50px;
                    border: 4px solid #e0e0e0;
                    border-top-color: #667eea;
                    border-radius: 50%;
                    animation: logout-spin 0.8s linear infinite;
                "></div>
                <p style="color: #333; font-size: 16px; margin: 0;">Sedang keluar dari akun...</p>
            </div>
            <style>
                @keyframes logout-spin {
                    to { transform: rotate(360deg); }
                }
            </style>
        `;
        document.body.appendChild(loadingDiv);
    } else {
        loadingDiv.style.display = 'flex';
    }
}

/**
 * Hide loading state
 */
function hideLogoutLoading() {
    const loadingDiv = document.getElementById('logoutLoading');
    if (loadingDiv) {
        loadingDiv.style.display = 'none';
    }
}

/**
 * Show success message
 */
function showLogoutSuccess() {
    let successDiv = document.getElementById('logoutSuccess');
    
    if (!successDiv) {
        successDiv = document.createElement('div');
        successDiv.id = 'logoutSuccess';
        document.body.appendChild(successDiv);
    }

    successDiv.innerHTML = `
        <div style="
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, 0.95);
            display: flex;
            align-items: center;
            justify-content: center;
            z-index: 99999;
            flex-direction: column;
            gap: 15px;
        ">
            <div style="
                font-size: 60px;
                color: #27ae60;
            ">✓</div>
            <p style="color: #333; font-size: 18px; margin: 0; font-weight: 600;">
                Logout Berhasil
            </p>
            <p style="color: #666; font-size: 14px; margin: 0;">
                Sampai jumpa kembali...
            </p>
        </div>
    `;
    successDiv.style.display = 'flex';
}

/**
 * Show error message
 */
function showLogoutError(message = 'Gagal keluar dari akun') {
    let errorDiv = document.getElementById('logoutError');
    
    if (!errorDiv) {
        errorDiv = document.createElement('div');
        errorDiv.id = 'logoutError';
        document.body.appendChild(errorDiv);
    }

    errorDiv.innerHTML = `
        <div style="
            position: fixed;
            top: 20px;
            right: 20px;
            background: #fee;
            border: 2px solid #fcc;
            color: #c33;
            padding: 15px 20px;
            border-radius: 8px;
            z-index: 99999;
            display: flex;
            align-items: center;
            gap: 10px;
            max-width: 400px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
        ">
            <i class="bi bi-exclamation-circle-fill" style="font-size: 20px;"></i>
            <span>${message}</span>
        </div>
    `;

    // Auto-hide after 5 seconds
    setTimeout(() => {
        errorDiv.style.display = 'none';
    }, 5000);
}

// ============================================
// EVENT LISTENERS
// ============================================

/**
 * Auto-logout on page unload if needed
 */
window.addEventListener('beforeunload', () => {
    // Optional: perform any cleanup before page unload
    // Currently just for logging
    console.log('Page unloading - logout module cleanup');
});

/**
 * Handle session change events
 */
if (typeof supabase !== 'undefined') {
    supabase.auth.onAuthStateChange((event, session) => {
        console.log('Auth state changed:', event);
        
        // Handle token expiry
        if (event === 'TOKEN_REFRESHED') {
            console.log('Token refreshed');
        }
        
        if (event === 'SIGNED_OUT') {
            console.log('User signed out');
            clearAllStorage();
        }
    });
}

// ============================================
// EXPORTS (for ES6 modules if needed)
// ============================================

// Uncomment if using as ES6 module:
// export { logoutUser, logoutUserWithRedirect, silentLogout, forceLogout, clearAllStorage };
