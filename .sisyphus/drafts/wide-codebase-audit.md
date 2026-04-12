# Draft: Wide Codebase Logic Audit & Verification Test Plan

## Requirements (confirmed)
- **Scope**: Full codebase audit covering Android app + Admin Panel
- **Platform**: Android TV (Kotlin/Jetpack Compose) + Web Admin (HTML/JS) + Supabase backend
- **Feature Areas**: 
  1. Full-screen banner overlay
  2. Banner timing window (15-25 min post-iqamah)
  3. Slideshow interval derivation
  4. Pengajian schema fallback mapping
  5. Social-media ticker chunking
  6. Iqamah popup layout/clipping risk
  7. Kas arithmetic rules
  8. Supabase readiness race handling

## Technical Decisions
- **No existing test infrastructure** - Tests must be created from scratch
- **Android testing**: JUnit + Compose Testing + Espresso for UI
- **Admin panel testing**: Manual verification (no Jest/Vitest setup)
- **Verification approach**: Agent-executable commands + screenshots

## Research Findings

### 1. Banner Overlay & Timing
- **Files**: MainDashboard.kt (lines 149-165), BannerSlideshow.kt
- **Logic**: `shouldShowBanners` calculated as 15-25 minutes post-iqamah
- **Risks**: 
  - Midnight boundary crossing (handled by Calendar)
  - Multiple prayers with overlapping windows
  - No clock skew validation

### 2. Slideshow Interval Derivation
- **Files**: MainDashboard.kt (lines 162-165), BannerSlideshow.kt (lines 38-39)
- **Formula**: `(10 * 60 * 1000) / banners.size.coerceAtLeast(1)`
- **Default**: 8000ms if no banners
- **Edge cases**: Empty list (early return), single banner (no auto-advance)

### 3. Pengajian Schema Fallback
- **Files**: SupabaseApiService.kt (lines 70-83), SupabaseRepository.kt (lines 247-256)
- **Fallback mapping**:
  - judul → tema
  - pembicara → ustadz
  - hari → tanggal
  - jam → defaults to "-"
- **Filter**: Removes entries where BOTH title AND speaker are missing

### 4. Social-Media Ticker Chunking
- **Files**: EnhancedRunningText.kt (lines 121-175)
- **Strategy**: Source-based chunking with emoji prefixes
- **Duration**: `(content.length * 300).coerceIn(20000, 60000)` milliseconds
- **Edge cases**: Empty list (default message), single item (no cycling)

### 5. Iqamah Popup Layout
- **File**: PrayerAlertOverlay.kt (lines 40-314)
- **Layout**: 65% screen width, 36sp fonts, 90dp countdown ring
- **Clipping risks**:
  - Text overflow on small screens (maxLines=2, ellipsis)
  - Fixed padding (48.dp) on variable screens
  - No responsive font sizing

### 6. Kas Arithmetic Rules
- **Files**: SupabaseRepository.kt (lines 128-203), CurrencyUtils.kt
- **Data type**: Long (64-bit integer) - no floating point issues
- **Operations**: Addition/subtraction only - no division precision issues
- **Trend**: `incomeMonth >= expenseMonth` → UP, else DOWN
- **Edge cases**: Negative balance (supported), zero balance (displays correctly)

### 7. Supabase Readiness Race Handling
- **Critical race**: Module script (async) vs regular script (sync)
- **Mitigation**: Custom `supabaseReady` event + polling (100ms interval)
- **Issues**:
  - Login timeout callback executes even on failure
  - No retry logic anywhere
  - announcements.js has no error handling

## Open Questions
- None - all requirements clear from user request

## Scope Boundaries
- INCLUDE: Logic audit, risk assessment, verification tests
- INCLUDE: All 8 feature areas specified
- INCLUDE: Both Android app and Admin Panel
- EXCLUDE: Actual implementation of fixes
- EXCLUDE: File modifications
