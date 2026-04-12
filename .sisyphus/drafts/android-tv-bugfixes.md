# Draft: Android TV Mosque Display - 3 Bug Fixes

## Requirements (confirmed)
- Fix 3 bugs in Android TV mosque display app (Kotlin/Jetpack Compose)
- Use ULW loop: fix → build → install → verify on emulator → refix if still broken
- Verify each fix visually via emulator screenshot
- Commit and push to main when all 3 are fixed

## Bug Analysis

### Bug 1: Pengajian not showing
- **Files**: MainActivity.kt (lines 164-167), SupabaseRepository.kt (lines 108-121)
- **Data flow**: Supabase `pengajian` table → getPengajian() → filter for non-null `judul` AND `pembicara` → MainDashboard → MultiSourceRunningText
- **Root cause**: PengajianRemote has nullable fields (judul, pembicara). Filter at line 166 requires BOTH non-null. Empty if Supabase data has nulls.
- **Fix approach**: Debug logging + potentially relax filter or add fallback display

### Bug 2: Iqamah popup text half-visible/cut off
- **Files**: PrayerAlertOverlay.kt (lines 110-143 card, 200-207 subtitle)
- **Root cause**: 
  - Card: `fillMaxWidth(0.65f)` = 1248px on 1920px TV
  - Padding: `horizontal = 48.dp` removes 96dp → ~1152px content
  - Text: 44.sp "MOHON BERDIRI UNTUK SHALAT [PRAYER]" (~35 chars) tight fit
  - `.clip(RoundedCornerShape(40.dp))` clips corners
  - `graphicsLayer { translationY = 40f }` pushes card down
  - No height constraint - content can exceed clip
- **Fix approach**: Increase width to 0.75f, reduce padding, reduce font to 38sp, remove translationY

### Bug 3: TikTok handle cut off in running text
- **Files**: MainActivity.kt (lines 236-240), EnhancedRunningText.kt
- **Root cause**: socialMediaLinks is ONE item with ~175 char string. TikTok at END gets brief visibility during scroll.
- **Fix approach**: Split into separate items so each scrolls independently

## Technical Decisions
- **Parallel strategy**: Bug 2 is independent. Bug 1 + Bug 3 both touch MainActivity.kt (different line ranges, 70+ lines apart)
- **Execution**: Can parallelize Bug 2 with Bug 1+3 combination task
- **Verification**: Single build → install → verify all 3 at once via emulator

## Scope Boundaries
- INCLUDE: The 3 specific bugs only
- EXCLUDE: Any other features, refactoring, or "improvements"

## Open Questions
- None - all context gathered from code analysis

## Research Findings
- All files verified and read
- Test panel exists in app (MENU/INFO key triggers overlay testing)
- Build: `./gradlew assembleDebug --no-daemon`
- Install: `adb install -r app/build/outputs/apk/debug/app-debug.apk`
- Emulator: AndroidTV_API34
