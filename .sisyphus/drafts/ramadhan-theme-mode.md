# Draft: Ramadhan Theme Mode

## Requirements (confirmed)

- **Core Objective**: Add a Ramadhan theme mode that activates automatically during Ramadhan
- **Design Philosophy**: Keep main design unchanged - only ADD elements, don't restructure
- **Timeline Color Change**: Make the "line thing" (prayer timeline + dots) GREEN during Ramadhan
- **Side Ornaments**: Add subtle, non-distracting ornaments on left and right sides
- **Display Target**: Must work on TV display (1080p/4K, viewed from 5-10 meters)

## Technical Decisions

### Ramadhan Detection
- **Decision**: Use existing `isRamadhanNow` computed in MainDashboard.kt (line 50)
- **Rationale**: Already implemented, uses accurate Hijri calendar conversion via `isRamadan()` function in DateTimeUtils.kt

### Color Strategy
- **Decision**: Use existing `RamadanColors.accentPrimary` (Color(0xFF11C76F) - green)
- **Rationale**: Already defined in Color.kt but NOT currently used - perfect for timeline/dots

### Current Hard-coded Colors to Replace
| Location | Current Color | Description |
|----------|---------------|-------------|
| MainDashboard.kt:248 | `Color(0xFF4ECDC4)` | Timeline dashed line |
| MainDashboard.kt:272-275 | `Color(0xFF4ECDC4)` | Dots (current/next/passed/default) |
| MainDashboard.kt:297 | `Color(0xFF4ECDC4)` | Icon tint for current prayer |

### Ornament Approach
- **Decision**: Add vector drawable ornaments positioned on left/right sides
- **Placement**: Inside top-level Box, AFTER the dark overlay but BEFORE the Column
- **Positioning**: Use `Modifier.align()` with `Alignment.CenterStart` and `Alignment.CenterEnd`
- **Style**: Subtle Islamic geometric patterns or lantern silhouettes
- **Opacity**: Use alpha ~0.15-0.25 for subtle, non-distracting effect

## Research Findings

### Layout Structure (MainDashboard.kt)
```
Box(fillMaxSize) {
    Image(background wallpaper)          // Line 147-152
    Box(dark overlay 50% black)          // Line 154-158
    // <-- ORNAMENTS GO HERE (between overlay and content)
    Column(padding) {                    // Line 160-328
        Row(header)
        Column(weight 1f - prayer times)
        MultiSourceRunningText(ticker)
    }
}
```

### Existing RamadanColors (Color.kt lines 50-53)
```kotlin
object RamadanColors {
    val accentPrimary = Color(0xFF11C76F)      // Green - USE THIS
    val accentPrimarySoft = Color(0x5211C76F)  // Soft green variant
}
```

### isRamadhanNow Usage (Current)
Currently only used for:
1. Filtering mainPrayers list (swaps Imsak/Syuruq)
2. Setting cornerLabel/cornerTime/cornerEmoji/cornerColor

**NOT currently used for**:
- Timeline color
- Dot colors
- Icon tint colors
- Any visual theming

### Drawable Loading Pattern
- Uses `painterResource(id = R.drawable.XXX)` for loading drawables
- Vector drawables are white shapes, tinted at runtime via `Icon(tint = ...)`
- Can add new vector XML files to `res/drawable/`

## Open Questions

- [x] Ornament style: Islamic geometric vs lantern silhouettes? → **DECIDED: Lantern silhouettes (fanoos)**
- [x] Ornament placement: Corners vs vertical strips on sides? → **DECIDED: Top corners only (top-left and top-right)**
- [x] Code cleanup: Add TimelineColors tokens? → **DECIDED: Yes, add TimelineColors for cleaner code**

## Scope Boundaries

### INCLUDE
- Change timeline/dots/icon tint to green during Ramadhan
- Add subtle side ornaments (left and right) during Ramadhan
- Create new vector drawable files for ornaments
- Use existing RamadanColors

### EXCLUDE
- Changing the main layout structure
- Modifying any other screens (PrayerInProgress, etc.)
- Adding animation to ornaments
- Changing background image during Ramadhan
- Adding sound effects or other media

## Files to Modify

1. **MainDashboard.kt** - Add conditional coloring and ornament composables
2. **Color.kt** - Add timeline-specific Ramadhan color (optional, could reuse accentPrimary)
3. **New: res/drawable/ramadhan_ornament_left.xml** - Left side ornament
4. **New: res/drawable/ramadhan_ornament_right.xml** - Right side ornament (mirrored)

## Implementation Approach

### Phase 1: Timeline Green During Ramadhan
1. Create a computed color variable based on `isRamadhanNow`
2. Replace hard-coded `Color(0xFF4ECDC4)` with conditional color
3. Apply to: Canvas line, dots background, icon tint

### Phase 2: Side Ornaments
1. Create vector drawable ornaments (Islamic pattern or lantern)
2. Add conditional composables inside top-level Box
3. Position with alignment and alpha for subtlety
