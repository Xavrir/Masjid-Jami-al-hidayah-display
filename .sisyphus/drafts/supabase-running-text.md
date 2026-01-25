# Draft: Supabase DB + Running Text Loop

## Requirements (stated)
- Configure project to use Supabase instance at `https://wqupptqjbkuldglnpvor.supabase.co`.
- Change data sources for these datasets/tables: `kas_masjid`, `ayat_quran`, `hadits`, `pengajian`.
- Display the data on the running text (ticker) in a loop.

## Credentials (provided)
- User provided a Supabase `anon` API key (JWT). (Not recorded in this draft for security.)
- Plan will assume it will be injected via environment/Gradle BuildConfig, not hardcoded.

## Assumptions (not yet confirmed)
- The app currently reads these datasets from local/static files or hardcoded arrays.
- The running text already exists; we will rewire it to read from Supabase.
- Display is read-only in the app; data entry happens elsewhere (Supabase dashboard/admin tool).

## Technical Decisions (pending)
- Where Supabase queries run: client-side (anon key) vs server-side (service role).
- Row Level Security (RLS) posture: public read policies vs server-only.
- Caching strategy: in-memory, localStorage/IndexedDB, or server cache.
- Offline behavior: fallback to last-known data vs show placeholder.

## Test Strategy Decision (initial)
- **Infrastructure exists**: PARTIAL (Android/Gradle default test tasks exist, but no explicit unit/UI tests found in repo yet)
- **User wants tests**: (pending)
- **QA approach**: Manual verification unless you want us to add JUnit/Compose UI tests

## Runtime/Deployment Constraints (found)
- Android manifest already includes `INTERNET` and `ACCESS_NETWORK_STATE` permissions.
- App currently achieves "offline support" by using bundled `MockData` / `IslamicContent` (no caching layer found yet).

## Research Findings
- App is an Android TV app written in Kotlin + Jetpack Compose.
- Current data is local/static:
  - `android/app/src/main/java/com/masjiddisplay/data/MockData.kt` provides `kasData` + `announcements`.
  - `android/app/src/main/java/com/masjiddisplay/data/IslamicContent.kt` provides `quranVerses` + `hadiths` and random selection helpers.
- Running text component exists:
  - `android/app/src/main/java/com/masjiddisplay/ui/components/AnnouncementTicker.kt` takes `List<String>` and scrolls the joined string in an infinite loop.
  - `android/app/src/main/java/com/masjiddisplay/ui/screens/MainDashboard.kt` currently appends a kas summary string into the announcements list for the ticker.
- `MainActivity.kt` currently wires UI directly to `MockData.*` (no repository/network layer yet).

### Supabase Kotlin Client (external)
- Recommended library: `supabase-community/supabase-kt`.
- Typical Gradle setup uses Supabase BOM + modules:
  - `io.github.jan-tennert.supabase:postgrest-kt` (DB)
  - `io.github.jan-tennert.supabase:realtime-kt` (optional)
  - Ktor engine for Android (often `io.ktor:ktor-client-okhttp`)
  - `kotlinx-serialization-json` for decoding rows.
- Security: Android app should only use `anon` key; never embed `service_role`.

## Open Questions
- Confirm preferred secret injection method: `local.properties`/Gradle BuildConfig vs other.
- What are the exact table schemas (columns) for `kas_masjid`, `ayat_quran`, `hadits`, `pengajian`?
- Do you want Quran/Hadith content to stay in their cards, and ALSO appear in the ticker? Or move them entirely into the ticker?
- How should the ticker format each item (ordering, separators, language, truncation)?
- Should the ticker rotate across all 4 sources in one combined loop, or have separate segments?
- Update frequency: real-time via Supabase Realtime, or poll every N seconds/minutes?

## Scope Boundaries (initial)
- INCLUDE: wiring Supabase client, reading data, formatting, ticker loop, basic error/loading states.
- EXCLUDE (unless requested): building an admin UI for editing data; complex auth flows.
