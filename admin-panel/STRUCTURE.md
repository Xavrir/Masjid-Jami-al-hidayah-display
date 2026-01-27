# Admin Panel - Folder Structure

## 📁 Directory Tree

```
admin-panel/
├── index.html                 # Entry point (auth check & redirect)
├── login.html                 # Login page
├── dashboard.html             # Main dashboard (protected)
├── STRUCTURE.md               # This file
│
├── pages/                      # Feature pages (all protected)
│   ├── kas_masjid.html        # Treasury management
│   ├── ayat_quran.html        # Quranic verses management
│   ├── hadist.html            # Hadith management
│   └── pengajian.html         # Islamic study session management
│
├── js/
│   ├── config/
│   │   └── supabase.js        # Supabase client (reusable)
│   │
│   ├── modules/               # Feature-specific logic
│   │   ├── auth.js            # Authentication methods
│   │   ├── kas_masjid.js      # Treasury data operations
│   │   ├── ayat_quran.js      # Quran data operations
│   │   ├── hadist.js          # Hadith data operations
│   │   └── pengajian.js       # Study session operations
│   │
│   ├── utils/                 # Shared utilities
│   │   ├── dom.js             # DOM manipulation helpers
│   │   ├── notify.js          # Toast/notification system
│   │   ├── formatting.js      # Text & number formatting
│   │   └── validation.js      # Form validation helpers
│   │
│   └── auth-guard.js          # Page protection middleware
│
├── css/
│   ├── variables.css          # CSS custom properties (colors, spacing)
│   ├── layout.css             # Grid, flexbox layouts
│   ├── components.css         # Reusable component styles
│   ├── dark-mode.css          # Dark mode overrides
│   └── responsive.css         # Mobile/TV breakpoints
│
└── assets/
    ├── images/                # Logo, backgrounds, photos
    └── icons/                 # SVG icons or icon fonts
```

---

## 📋 File Descriptions

### Root Pages

| File | Purpose |
|------|---------|
| `index.html` | Entry point. Checks auth → redirects to login or dashboard |
| `login.html` | Login form with Supabase Auth |
| `dashboard.html` | Main dashboard after login (shows stats, quick actions) |

### Pages/ (Protected Feature Pages)

| File | Purpose |
|------|---------|
| `kas_masjid.html` | Manage mosque treasury (add, edit, delete, view reports) |
| `ayat_quran.html` | Manage Quranic verses (display on app) |
| `hadist.html` | Manage hadith collection |
| `pengajian.html` | Manage study sessions/schedules |

### JS Config/

| File | Purpose |
|------|---------|
| `supabase.js` | Initialize Supabase client. Export `supabase` instance. Used across all pages. |

### JS Modules/

| File | Purpose |
|------|---------|
| `auth.js` | Login, logout, session check, password reset |
| `kas_masjid.js` | Fetch, create, update, delete treasury records |
| `ayat_quran.js` | Fetch, create, update, delete Quranic verses |
| `hadist.js` | Fetch, create, update, delete hadith |
| `pengajian.js` | Fetch, create, update, delete study sessions |

### JS Utils/

| File | Purpose |
|------|---------|
| `dom.js` | Helper functions like `$()`, `show()`, `hide()`, `addClass()` |
| `notify.js` | Toast notifications (success, error, warning, info) |
| `formatting.js` | Format dates, currency, numbers, Indonesian text |
| `validation.js` | Email, phone, required field checks |

### JS Root

| File | Purpose |
|------|---------|
| `auth-guard.js` | Runs on page load. Blocks access if not logged in. |

### CSS/

| File | Purpose |
|------|---------|
| `variables.css` | CSS custom properties (--color-primary, --spacing-lg, etc.) |
| `layout.css` | AdminLTE grid, sidebar, header layouts |
| `components.css` | Buttons, cards, modals, forms |
| `dark-mode.css` | Dark theme overrides |
| `responsive.css` | Mobile (< 768px), Tablet, TV (> 1920px) breakpoints |

### Assets/

| Folder | Contains |
|--------|----------|
| `images/` | Logo, backgrounds, user avatars |
| `icons/` | SVG icons (edit, delete, add, settings) |

---

## 🔐 Security Architecture

1. **Auth Guard**: Every protected page loads `auth-guard.js` first
   - Checks if user is logged in
   - Redirects to login if not authenticated

2. **Supabase Client**: Single instance in `config/supabase.js`
   - Never exposes service role key
   - Uses public anon key in frontend
   - Row-level security (RLS) enforces data access

3. **Protected Pages**: All feature pages require login
   - No direct access without authentication

---

## 🎨 Design System (AdminLTE v4)

- **Sidebar**: Navigation menu
- **Topbar**: User info, logout button
- **Main Content**: Responsive cards and tables
- **Mobile-friendly**: Hamburger menu, stacked layout
- **TV-friendly**: Large text, big buttons (48px minimum)

---

## 📌 How to Use This Structure

1. **Login**: User goes to `index.html` → redirected to `login.html`
2. **Authenticated**: After login, redirects to `dashboard.html`
3. **Navigation**: Sidebar links to `/pages/*.html`
4. **Data Operations**: Each page imports its module (e.g., `kas_masjid.js`)
5. **Supabase**: All modules import from `config/supabase.js`
6. **UI Helpers**: Import from `utils/` for notifications, DOM manipulation
7. **Auth Logout**: Click logout → clears session → redirects to login

---

## 🚀 Next Steps

Create these files with production-ready code:
1. Core pages (index, login, dashboard)
2. Supabase config
3. Auth system
4. Feature pages (one by one)
5. CSS & utilities
