# Ramadhan Theme Mode

## TL;DR

> **Quick Summary**: Add a Ramadhan theme mode that automatically activates during Ramadhan month - changes the prayer timeline from teal to green and adds subtle lantern ornaments in the top corners. Uses existing `isRamadhanNow` detection and `RamadanColors`.
> 
> **Deliverables**:
> - New `TimelineColors` object in Color.kt with normal/ramadhan variants
> - New `ic_ramadhan_lantern.xml` vector drawable
> - Updated MainDashboard.kt with conditional colors and ornament composables
> 
> **Estimated Effort**: Short (2-3 hours)
> **Parallel Execution**: YES - 2 waves
> **Critical Path**: Task 1 (Color.kt) → Task 3 (MainDashboard colors) → Task 4 (Ornaments)

---

## Context

### Original Request
Add a Ramadhan theme mode that:
1. Keeps the main design unchanged
2. Adds subtle ornaments on the sides
3. Makes the "line thing" (prayer timeline) green during Ramadhan

### Interview Summary
**Key Discussions**:
- Ornament style: User chose lantern silhouettes (fanoos) - traditional Ramadhan lanterns
- Ornament placement: Top corners only (top-left and top-right)
- Code cleanup: Add TimelineColors tokens for cleaner, maintainable code

**Research Findings**:
- `isRamadhanNow` already computed in MainDashboard.kt line 50 using Hijri calendar
- `RamadanColors.accentPrimary = Color(0xFF11C76F)` exists in Color.kt but is UNUSED
- Hard-coded teal `Color(0xFF4ECDC4)` appears at lines 248, 272-275, 297
- Layout structure allows easy ornament insertion between overlay and Column
- Vector drawables loaded via `painterResource(R.drawable.xxx)`

### Metis Review
**Identified Gaps** (addressed):
- TV overscan: Ornaments offset 24.dp from corners for safety
- Ornament alpha: Set to 0.06-0.08 for subtlety on TV displays
- Mirroring: Use `graphicsLayer { scaleX = -1f }` for right lantern
- Focus: Ensure ornaments are not focusable (default Image behavior)
- Performance: Keep vector simple (few paths) for TV rendering

---

## Work Objectives

### Core Objective
Implement automatic Ramadhan theming that changes timeline colors to green and displays decorative lantern ornaments during Ramadhan month.

### Concrete Deliverables
- `Color.kt`: New `TimelineColors` object with `normal` and `ramadhan` color variants
- `ic_ramadhan_lantern.xml`: Vector drawable of lantern silhouette (white, ~120dp)
- `MainDashboard.kt`: Conditional timeline/dot colors + lantern Image composables

### Definition of Done
- [ ] Timeline dashed line is green during Ramadhan, teal otherwise
- [ ] All 6 timeline dots are green during Ramadhan, teal otherwise
- [ ] Current prayer icon tint is green during Ramadhan, teal otherwise
- [ ] Lantern ornaments visible in top-left and top-right during Ramadhan
- [ ] Lanterns NOT visible outside Ramadhan
- [ ] No visual changes to main layout structure
- [ ] App builds without errors: `./gradlew assembleDebug`

### Must Have
- Automatic activation based on existing `isRamadhanNow` boolean
- Green color matches `RamadanColors.accentPrimary` (0xFF11C76F)
- Lanterns are subtle (alpha ~0.06-0.08) and non-interactive
- No changes to other screens or components

### Must NOT Have (Guardrails)
- NO animations on lanterns (static only for TV performance)
- NO changes to global accentPrimary (gold) or app-wide theme
- NO settings screen or manual toggle - purely date-driven
- NO changes to PrayerInProgress.kt or other screens
- NO focusable ornaments (must not affect TV remote navigation)
- NO complex vector paths (keep simple for TV rendering)
- NO changes to prayer filtering logic or Imsak/Syuruq behavior

---

## Verification Strategy (MANDATORY)

### Test Decision
- **Infrastructure exists**: NO (no automated test framework)
- **User wants tests**: Manual verification
- **Framework**: Manual QA via ADB + visual inspection

### Manual Verification Approach

Each TODO includes executable verification steps that can be performed via ADB commands and visual inspection on emulator or device.

**By Deliverable Type:**

| Type | Verification Method |
|------|---------------------|
| Color.kt changes | Build success + visual inspection |
| Vector drawable | Build success + visual rendering |
| MainDashboard.kt | Visual inspection on 1080p/4K display |

**Evidence Requirements:**
- Screenshot of dashboard during simulated Ramadhan
- Screenshot of dashboard during non-Ramadhan
- Build logs showing successful compilation

---

## Execution Strategy

### Parallel Execution Waves

```
Wave 1 (Start Immediately):
├── Task 1: Add TimelineColors to Color.kt [no dependencies]
└── Task 2: Create ic_ramadhan_lantern.xml [no dependencies]

Wave 2 (After Wave 1):
├── Task 3: Update MainDashboard timeline colors [depends: 1]
└── Task 4: Add lantern ornaments to MainDashboard [depends: 2]

Wave 3 (After Wave 2):
└── Task 5: Integration testing & verification [depends: 3, 4]

Critical Path: Task 1 → Task 3 → Task 5
Parallel Speedup: ~30% faster than sequential
```

### Dependency Matrix

| Task | Depends On | Blocks | Can Parallelize With |
|------|------------|--------|---------------------|
| 1 | None | 3 | 2 |
| 2 | None | 4 | 1 |
| 3 | 1 | 5 | 4 |
| 4 | 2 | 5 | 3 |
| 5 | 3, 4 | None | None (final) |

### Agent Dispatch Summary

| Wave | Tasks | Recommended Dispatch |
|------|-------|---------------------|
| 1 | 1, 2 | Parallel: both can start immediately |
| 2 | 3, 4 | Parallel: can run together after Wave 1 |
| 3 | 5 | Sequential: final verification |

---

## TODOs

- [ ] 1. Add TimelineColors to Color.kt

  **What to do**:
  - Add a new `TimelineColors` object after `RamadanColors` in Color.kt
  - Define `normal` as the current teal color (0xFF4ECDC4)
  - Define `normalSoft` as teal with alpha for passed states
  - Define `ramadhan` referencing `RamadanColors.accentPrimary`
  - Define `ramadhanSoft` for passed states during Ramadhan

  **Must NOT do**:
  - Do not modify existing `AppColors` or `RamadanColors` objects
  - Do not change the MaterialTheme in Theme.kt

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Simple file addition, single location, clear pattern to follow
  - **Skills**: None required
    - This is a straightforward Kotlin object addition
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not needed - this is pure data/constants, no visual design

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 2)
  - **Blocks**: Task 3
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/theme/Color.kt:50-53` - RamadanColors object pattern to follow

  **API/Type References**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/theme/Color.kt:8-45` - AppColors object structure as reference

  **Implementation Details**:
  ```kotlin
  // Add after RamadanColors object (line 53)
  object TimelineColors {
      val normal = Color(0xFF4ECDC4)           // Current teal
      val normalSoft = Color(0x804ECDC4)       // 50% alpha for passed
      val ramadhan = RamadanColors.accentPrimary  // Green
      val ramadhanSoft = RamadanColors.accentPrimarySoft
  }
  ```

  **Acceptance Criteria**:

  **Build Verification:**
  ```bash
  # Agent runs from android/ directory:
  ./gradlew compileDebugKotlin 2>&1 | tail -20
  # Assert: BUILD SUCCESSFUL
  # Assert: No compilation errors mentioning Color.kt
  ```

  **Code Verification:**
  ```bash
  # Agent runs:
  grep -A 6 "object TimelineColors" android/app/src/main/java/com/masjiddisplay/ui/theme/Color.kt
  # Assert: Output shows TimelineColors object with normal, normalSoft, ramadhan, ramadhanSoft
  ```

  **Commit**: YES
  - Message: `feat(theme): add TimelineColors for Ramadhan theme support`
  - Files: `android/app/src/main/java/com/masjiddisplay/ui/theme/Color.kt`
  - Pre-commit: `./gradlew compileDebugKotlin`

---

- [ ] 2. Create ic_ramadhan_lantern.xml vector drawable

  **What to do**:
  - Create a new vector drawable file for the lantern silhouette
  - Use simple path(s) for performance on TV
  - Set fill color to white (#FFFFFF) for runtime tinting flexibility
  - Viewport and size: 24x24 or 48x48 is fine (will scale via Modifier.size)
  - Design: Traditional fanoos (Ramadhan lantern) silhouette - simple recognizable shape

  **Must NOT do**:
  - Do not use complex gradients or multiple colors
  - Do not exceed ~10 paths (keep simple for TV rendering)
  - Do not add animations or animated vector attributes

  **Recommended Agent Profile**:
  - **Category**: `artistry`
    - Reason: Creating a visual asset (vector drawable) with design considerations
  - **Skills**: [`frontend-design`]
    - `frontend-design`: Creating a polished, recognizable lantern icon design
  - **Skills Evaluated but Omitted**:
    - `playwright`: Not needed - no browser interaction required

  **Parallelization**:
  - **Can Run In Parallel**: YES
  - **Parallel Group**: Wave 1 (with Task 1)
  - **Blocks**: Task 4
  - **Blocked By**: None (can start immediately)

  **References**:

  **Pattern References**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/res/drawable/ic_subuh.xml` - Existing vector drawable structure (simple paths, white fill)
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/res/drawable/ic_imsak.xml` - Another example of vector pattern

  **Target Location**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/res/drawable/ic_ramadhan_lantern.xml`

  **Design Guidance**:
  - Lantern shape: Dome top, tapered body, decorative bottom, small handle/hook at top
  - Style: Silhouette only (solid fill, no internal details)
  - Keep path count low (1-3 paths max)
  - Recognizable at small sizes but will display at ~120-160dp on TV

  **Acceptance Criteria**:

  **Build Verification:**
  ```bash
  # Agent runs from android/ directory:
  ./gradlew compileDebugKotlin 2>&1 | tail -20
  # Assert: BUILD SUCCESSFUL
  # Assert: No resource errors
  ```

  **File Verification:**
  ```bash
  # Agent runs:
  ls -la android/app/src/main/res/drawable/ic_ramadhan_lantern.xml
  # Assert: File exists
  
  head -20 android/app/src/main/res/drawable/ic_ramadhan_lantern.xml
  # Assert: Valid XML with <vector> root element
  # Assert: Contains android:fillColor="#FFFFFF" or similar
  ```

  **Commit**: YES
  - Message: `feat(assets): add Ramadhan lantern ornament vector drawable`
  - Files: `android/app/src/main/res/drawable/ic_ramadhan_lantern.xml`
  - Pre-commit: `./gradlew compileDebugKotlin`

---

- [ ] 3. Update MainDashboard.kt timeline colors to use TimelineColors

  **What to do**:
  - Import `TimelineColors` from ui/theme/Color.kt
  - Create a computed `timelineColor` variable based on `isRamadhanNow`
  - Replace hard-coded `Color(0xFF4ECDC4)` in Canvas drawLine (line ~248)
  - Replace hard-coded colors in dot Box backgrounds (lines ~272-275)
  - Replace hard-coded color in Icon tint for current prayer (line ~297)
  - Use `.copy(alpha = ...)` for passed/default states

  **Must NOT do**:
  - Do not change the timeline layout structure or positioning
  - Do not modify prayer filtering logic
  - Do not change any other colors (corner labels, text, etc.)
  - Do not touch `isRamadhanNow` computation

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Straightforward find-and-replace with conditional logic
  - **Skills**: None required
    - Simple Kotlin conditional color logic
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Not needed - no design decisions, just color token swap

  **Parallelization**:
  - **Can Run In Parallel**: YES (with Task 4, after Task 1)
  - **Parallel Group**: Wave 2 (with Task 4)
  - **Blocks**: Task 5
  - **Blocked By**: Task 1 (needs TimelineColors)

  **References**:

  **Pattern References**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:50` - `isRamadhanNow` variable location
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:137-140` - Existing conditional pattern (cornerColor uses if/else)

  **Locations to Modify**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:248` - Canvas drawLine color
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:272-275` - Dot background colors (when block)
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:297` - Icon tint for current prayer

  **Implementation Approach**:
  ```kotlin
  // Add import at top
  import com.masjiddisplay.ui.theme.TimelineColors
  
  // Add computed color after isRamadhanNow (around line 51)
  val timelineColor = if (isRamadhanNow) TimelineColors.ramadhan else TimelineColors.normal
  
  // Replace in Canvas drawLine (line ~248):
  color = timelineColor,  // was: Color(0xFF4ECDC4)
  
  // Replace in dot backgrounds (lines ~272-275):
  when {
      isCurrent -> timelineColor
      isNext -> timelineColor
      isPassed -> timelineColor.copy(alpha = 0.5f)
      else -> timelineColor.copy(alpha = 0.3f)
  }
  
  // Replace in Icon tint (line ~297):
  tint = if (isCurrent) timelineColor else Color.White.copy(alpha = 0.8f)
  ```

  **Acceptance Criteria**:

  **Build Verification:**
  ```bash
  # Agent runs from android/ directory:
  ./gradlew compileDebugKotlin 2>&1 | tail -20
  # Assert: BUILD SUCCESSFUL
  ```

  **Code Verification:**
  ```bash
  # Agent runs:
  grep -c "Color(0xFF4ECDC4)" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Output is "0" (no hard-coded teal remaining)
  
  grep -c "timelineColor" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Output is >= 5 (used in multiple places)
  
  grep "import.*TimelineColors" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Import statement exists
  ```

  **Commit**: NO (groups with Task 4)

---

- [ ] 4. Add lantern ornaments to MainDashboard.kt

  **What to do**:
  - Import `R.drawable.ic_ramadhan_lantern` resource
  - Add two `Image` composables inside the main `Box`, between overlay and Column
  - Position top-left lantern with `Alignment.TopStart` and offset (24.dp, 24.dp)
  - Position top-right lantern with `Alignment.TopEnd` and offset ((-24).dp, 24.dp)
  - Mirror right lantern horizontally using `graphicsLayer { scaleX = -1f }`
  - Set alpha to 0.06f for subtle appearance
  - Wrap in `if (isRamadhanNow) { ... }` conditional
  - Set size to 140.dp for TV visibility

  **Must NOT do**:
  - Do not make ornaments focusable or clickable
  - Do not add animations
  - Do not modify the Column or existing content layout
  - Do not use alpha higher than 0.12f (too distracting)

  **Recommended Agent Profile**:
  - **Category**: `quick`
    - Reason: Adding composables with known modifier patterns
  - **Skills**: None required
    - Standard Compose Image with modifiers
  - **Skills Evaluated but Omitted**:
    - `frontend-ui-ux`: Could help but overkill for simple Image placement

  **Parallelization**:
  - **Can Run In Parallel**: YES (with Task 3, after Task 2)
  - **Parallel Group**: Wave 2 (with Task 3)
  - **Blocks**: Task 5
  - **Blocked By**: Task 2 (needs drawable)

  **References**:

  **Pattern References**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:147-152` - Background Image loading pattern
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt:154-158` - Overlay Box placement (ornaments go after this)

  **Insertion Point**:
  - After line 158 (overlay Box), before line 160 (Column)
  
  **Implementation**:
  ```kotlin
  // Insert between overlay Box and Column (after line 158)
  if (isRamadhanNow) {
      // Top-left lantern
      Image(
          painter = painterResource(id = R.drawable.ic_ramadhan_lantern),
          contentDescription = null,
          modifier = Modifier
              .size(140.dp)
              .align(Alignment.TopStart)
              .offset(x = 24.dp, y = 24.dp)
              .alpha(0.06f)
      )
      // Top-right lantern (mirrored)
      Image(
          painter = painterResource(id = R.drawable.ic_ramadhan_lantern),
          contentDescription = null,
          modifier = Modifier
              .size(140.dp)
              .align(Alignment.TopEnd)
              .offset(x = (-24).dp, y = 24.dp)
              .alpha(0.06f)
              .graphicsLayer { scaleX = -1f }
      )
  }
  ```

  **Acceptance Criteria**:

  **Build Verification:**
  ```bash
  # Agent runs from android/ directory:
  ./gradlew compileDebugKotlin 2>&1 | tail -20
  # Assert: BUILD SUCCESSFUL
  ```

  **Code Verification:**
  ```bash
  # Agent runs:
  grep -c "ic_ramadhan_lantern" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Output is "2" (two Image references)
  
  grep "Alignment.TopStart\|Alignment.TopEnd" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Both alignments present
  
  grep "alpha(0.06f)" android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
  # Assert: Alpha modifier applied
  ```

  **Commit**: YES (groups with Task 3)
  - Message: `feat(ramadhan): add green timeline colors and lantern ornaments for Ramadhan theme`
  - Files: `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt`
  - Pre-commit: `./gradlew compileDebugKotlin`

---

- [ ] 5. Integration testing and visual verification

  **What to do**:
  - Build the debug APK
  - Install on emulator or device
  - Test during simulated Ramadhan (modify system date or use debug flag if available)
  - Verify all visual changes work correctly
  - Test on 1080p resolution
  - Capture screenshots for evidence

  **Must NOT do**:
  - Do not leave debug/test date modifications in code
  - Do not modify any files (testing only)

  **Recommended Agent Profile**:
  - **Category**: `visual-engineering`
    - Reason: Visual verification of UI changes on emulator/device
  - **Skills**: [`playwright`]
    - `playwright`: For automated screenshot capture and visual verification
  - **Skills Evaluated but Omitted**:
    - `frontend-design`: Not needed - verification only, no design work

  **Parallelization**:
  - **Can Run In Parallel**: NO
  - **Parallel Group**: Wave 3 (sequential, final)
  - **Blocks**: None (final task)
  - **Blocked By**: Tasks 3, 4

  **References**:

  **Build Commands**:
  - `/home/xavrir/Masjid-Jami-al-hidayah-display/android/` - Run `./gradlew assembleDebug`

  **Test Scenarios**:
  1. Normal mode (non-Ramadhan): Timeline should be teal, no lanterns
  2. Ramadhan mode: Timeline should be green, lanterns visible in corners
  3. Remote navigation: Focus should not get stuck on ornaments

  **Acceptance Criteria**:

  **Build APK:**
  ```bash
  # Agent runs from android/ directory:
  ./gradlew assembleDebug
  # Assert: BUILD SUCCESSFUL
  # Assert: APK exists at app/build/outputs/apk/debug/app-debug.apk
  
  ls -la app/build/outputs/apk/debug/app-debug.apk
  # Assert: File exists and size > 1MB
  ```

  **Visual Verification (via ADB + screenshots):**
  ```bash
  # Install and launch
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  adb shell am start -n com.masjiddisplay/.MainActivity
  
  # Wait for app to load
  sleep 5
  
  # Capture screenshot
  adb exec-out screencap -p > /tmp/ramadhan-test.png
  # Assert: Screenshot saved successfully
  ```

  **Evidence to Capture:**
  - [ ] Screenshot showing green timeline during Ramadhan period
  - [ ] Screenshot showing lantern ornaments in top corners
  - [ ] Screenshot showing normal teal timeline outside Ramadhan (if testable)

  **Commit**: NO (verification only, no code changes)

---

## Commit Strategy

| After Task | Message | Files | Verification |
|------------|---------|-------|--------------|
| 1 | `feat(theme): add TimelineColors for Ramadhan theme support` | Color.kt | `./gradlew compileDebugKotlin` |
| 2 | `feat(assets): add Ramadhan lantern ornament vector drawable` | ic_ramadhan_lantern.xml | `./gradlew compileDebugKotlin` |
| 3+4 | `feat(ramadhan): add green timeline colors and lantern ornaments for Ramadhan theme` | MainDashboard.kt | `./gradlew compileDebugKotlin` |

---

## Success Criteria

### Verification Commands
```bash
# Full build verification
cd android && ./gradlew assembleDebug
# Expected: BUILD SUCCESSFUL

# Check no hard-coded teal remains
grep -c "Color(0xFF4ECDC4)" app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt
# Expected: 0

# Check TimelineColors exists
grep -c "object TimelineColors" app/src/main/java/com/masjiddisplay/ui/theme/Color.kt
# Expected: 1

# Check lantern drawable exists
ls app/src/main/res/drawable/ic_ramadhan_lantern.xml
# Expected: file exists
```

### Final Checklist
- [ ] All "Must Have" present:
  - [ ] TimelineColors object with normal/ramadhan variants
  - [ ] Lantern vector drawable created
  - [ ] Timeline color conditional on isRamadhanNow
  - [ ] Lantern ornaments conditional on isRamadhanNow
- [ ] All "Must NOT Have" absent:
  - [ ] No animations on ornaments
  - [ ] No changes to Theme.kt or global colors
  - [ ] No focusable ornaments
  - [ ] No changes to other screens
- [ ] Build passes: `./gradlew assembleDebug`
