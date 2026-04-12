# Ramadhan Ketupat Corner Decorations (Android TV Compose)

## TL;DR

> **Quick Summary**: Replace the existing Ramadan lantern corner images with new ketupat vector drawables, shown only when Hijri month is Ramadan, and animate them with a calm horizontal sway.
>
> **Deliverables**:
> - New vector asset: `android/app/src/main/res/drawable/ic_ramadhan_ketupat.xml`
> - Updated dashboard corners: `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt`
>
> **Estimated Effort**: Short
> **Parallel Execution**: NO (light dependency: drawable must exist before wiring)
> **Critical Path**: Create ketupat drawable -> Replace lanterns + add sway animation -> Build/lint verification

---

## Context

### Original Request
"Plan out a vector ketupat for Ramadhan time only, it will show in the dashboard like on the top right side and top left side, moving side to side."

### Key Repo Facts (verified)
- App is **Android TV** using **Kotlin + Jetpack Compose**.
- Ramadan gating already exists:
  - `android/app/src/main/java/com/masjiddisplay/utils/DateTimeUtils.kt:117` `isRamadan(date)` -> `hijriMonth == 9`.
- Dashboard currently renders **Ramadan lantern** corner images:
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:162`-`182` shows `R.drawable.ic_ramadhan_lantern` at top-left and top-right.

### Confirmed Decisions
- Show ketupat **only during Ramadan** (Hijri month check).
- **Replace** existing lantern decorations (do not show both).
- Create a new **Android VectorDrawable** asset (green + gold accents).
- Motion is **subtle side-to-side sway**.
- No new test infrastructure; verification is **build/lint smoke**.

---

## Work Objectives

### Core Objective
During Ramadan, display two ketupat corner decorations (top-left and top-right) on the dashboard and animate them horizontally without distracting from the main prayer/time content.

### Definition of Done
- Ketupat decorations render only when `isRamadhanNow` is true.
- Existing lantern decorations are no longer displayed.
- Decorations sway horizontally (subtle) and do not cause layout shifts.
- `./gradlew assembleDebug` and `./gradlew lintDebug` succeed.

### Guardrails (Must / Must Not)
- MUST keep Ramadan gating logic as-is (reuse `isRamadan(currentTime)`; do not modify `DateTimeUtils.kt`).
- MUST keep the decoration z-order consistent with the current lanterns (rendered inside the `Box` before the main `Column` content).
- MUST use Compose infinite animation via `rememberInfiniteTransition` + `animateFloat` with `RepeatMode.Reverse`.
- SHOULD apply sway via `graphicsLayer { translationX = ... }` (GPU path; avoids layout remeasure).
- MUST NOT add new dependencies for animation.
- MUST NOT delete `ic_ramadhan_lantern` (leave it for rollback / future use).

---

## Verification Strategy

### Automated / Agent-Executable Checks (Primary)
These do not require a person to "eyeball" the UI.

```bash
cd android

# Build
./gradlew assembleDebug

# Lint
./gradlew lintDebug

# Sanity: confirm APK exists
test -f app/build/outputs/apk/debug/app-debug.apk
```

If `xmllint` is available on the executor machine:

```bash
cd android
xmllint --noout app/src/main/res/drawable/ic_ramadhan_ketupat.xml
```

### Repeatable Ramadan Gating Verification (Outside the Real Calendar Window)
Because `MainDashboard` derives `isRamadhanNow` from `Date()` at runtime, the repeatable way to validate Ramadan-only rendering is to set the device/emulator date into Ramadan.

Option A (preferred if supported): set date via ADB (may require elevated permissions on some devices)

```bash
# Disable automatic time if allowed
adb shell settings put global auto_time 0 || true
adb shell settings put global auto_time_zone 0 || true

# Attempt to set a known Ramadan date/time (example)
adb shell date -s 20260220.120000 || adb shell su -c "date -s 20260220.120000" || true
```

Option B: set date manually in Android TV Settings
- Settings -> Date & time -> disable network-provided time -> set date to a Ramadan day.

Verification signal (no extra tooling required)
- The dashboard already shows the Hijri date text (`getHijriDate(currentTime)`) in the top-left. When it contains "RAMADHAN", the ketupat decorations must be visible.

### Optional Visual Evidence Capture (Non-blocking, dev-only)
If running locally on an emulator/device with ADB (run from repo root):

```bash
mkdir -p .sisyphus/evidence
adb exec-out screencap -p > .sisyphus/evidence/ramadhan-ketupat-corners.png
test -s .sisyphus/evidence/ramadhan-ketupat-corners.png
```

---

## Execution Strategy

### Wave Plan
- Wave 1: Create the ketupat vector drawable asset.
- Wave 2: Replace lantern usage in `MainDashboard.kt` and add sway animation.
- Wave 3: Run build/lint verification.

---

## TODOs

### 1) Create ketupat VectorDrawable asset

**What to do**:
- Add `android/app/src/main/res/drawable/ic_ramadhan_ketupat.xml`.
- Design targets:
  - Clear ketupat silhouette at `140.dp` size.
  - Primary fill: Ramadan green (from `android/app/src/main/java/com/masjiddisplay/ui/theme/Color.kt` `RamadanColors.accentPrimary` -> `#11C76F`).
  - Accent/highlights: gold (`#D4AF37`) to suggest woven texture.
  - Match the lantern vector baseline params:
    - `android:width="140dp"`, `android:height="140dp"`
    - `android:viewportWidth="100"`, `android:viewportHeight="100"` (see `ic_ramadhan_lantern.xml`).
  - Minimal visual spec (so "ketupat" is not subjective):
    - A diamond body (rotated square) as the main shape.
    - At least 2 weave bands/lines (diagonal or horizontal) to read as woven.
    - Optional but recommended: 2 small ribbon/tail shapes at the bottom.
  - Avoid gradients; keep it lightweight (hard cap: <= 10 `<path>` elements).

**Must NOT do**:
- Do not remove or rename existing lantern assets.
- Do not introduce raster assets (PNG/JPG) for this feature.

**References**:
- Existing Ramadan lantern drawable name used by UI:
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:164` (`R.drawable.ic_ramadhan_lantern`)
- Existing vector drawable baseline params:
  - `android/app/src/main/res/drawable/ic_ramadhan_lantern.xml:2-6`
- Ramadan accent color definition:
  - `android/app/src/main/java/com/masjiddisplay/ui/theme/Color.kt:50`

**Acceptance Criteria**:
- File exists: `android/app/src/main/res/drawable/ic_ramadhan_ketupat.xml`.
- XML is well-formed (passes `xmllint --noout ...` if available).

---

### 2) Replace lantern decorations with ketupat + sway animation

**What to do**:
- In `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt`, locate the existing Ramadan lantern block:
  - `if (isRamadhanNow) { ... R.drawable.ic_ramadhan_lantern ... }` around `:162-182`.
- Replace both `Image` calls to use `R.drawable.ic_ramadhan_ketupat`.
- Add a subtle horizontal sway animation:
  - Create a `val swayDp` from `rememberInfiniteTransition().animateFloat(...)`.
  - Apply it via `graphicsLayer { translationX = ... }` using dp->px conversion (e.g., `with(LocalDensity.current) { swayDp.dp.toPx() }`).
  - Keep the right ketupat mirrored with `scaleX = -1f` (same as current lantern) and invert sway direction for symmetry.
- Keep placement consistent with current lanterns:
  - `.size(140.dp)`
  - `.offset(x = 24.dp, y = 24.dp)` and right side `.offset(x = (-24).dp, y = 24.dp)`
  - If TV overscan clips corners, increase offsets (e.g., 48.dp) while preserving alignment.
- Alpha default:
  - Allowed range: `0.06f`-`0.10f`.
  - Default target: `0.08f`.

**Suggested animation defaults** (fits “subtle sway”):
- Amplitude: +/- `12.dp` (implemented as `animateFloat(-12f, 12f, ...)`)
- Duration: `5000ms`
- Easing: `EaseInOutSine`
- Repeat: `RepeatMode.Reverse`

**Implementation note**:
- `MainDashboard.kt` will need animation imports (`androidx.compose.animation.core.*`) and `LocalDensity` (`androidx.compose.ui.platform.LocalDensity`) for dp->px conversion.

**Must NOT do**:
- Do not change `isRamadhanNow` computation or `isRamadan()`.
- Do not convert this into a new feature toggle.
- Do not change the dashboard header layout/padding.

**References**:
- Existing lantern placement + z-order pattern (rendered in the `Box` before the `Column`):
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:148-189`
- Existing animation pattern in repo (infinite transition):
  - `android/app/src/main/java/com/masjiddisplay/ui/components/PrayerAlertOverlay.kt` (search for `rememberInfiniteTransition`)

**Acceptance Criteria**:
- Lantern references removed from `MainDashboard.kt` Ramadan decoration block.
- `R.drawable.ic_ramadhan_ketupat` referenced in both corner `Image` calls.
- Sway is implemented via `graphicsLayer.translationX` only (no animated `offset(x = ...)` -> avoids layout shift).
- Right ketupat behavior is fixed (no ambiguity): mirrored via `scaleX = -1f` AND sway direction inverted for symmetry.

---

### 3) Build/lint smoke verification

**What to do**:
- Run build and lint commands.

**Acceptance Criteria**:
```bash
cd android
./gradlew assembleDebug
./gradlew lintDebug
test -f app/build/outputs/apk/debug/app-debug.apk
```

---

## Commit Strategy
- Single commit recommended.
- Suggested message: `feat(ui): add ramadhan ketupat corner sway`.

---

## Success Criteria
- Ramadan-only ketupat decorations appear in top-left/top-right and sway subtly.
- No regressions to dashboard layout.
- Build + lint pass.
