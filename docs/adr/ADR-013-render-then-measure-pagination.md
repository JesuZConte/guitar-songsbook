# ADR-013: Render-then-measure pagination for the Song Reader

## Status
Accepted (v1 — Day 14)

## Context
The Song Reader needs to split song content across swipeable pages. The original implementation used `SECTIONS_PER_PAGE = 2` (pack 2 sections per page regardless of size), which was later replaced with an estimation-based approach using `TEXT_HEIGHT_FACTOR = 1.8` to predict how many lines would fit in the available screen height.

The estimation approach worked at small font sizes (10–14sp) but produced persistent overflow at 16sp and above:
- Content exceeded the estimated page height
- The last line(s) were clipped or hidden behind the page indicator
- Users needed to scroll within a page to see the full content — defeating the purpose of pagination

The root cause is fundamental: estimating Compose Text height from `fontSize` alone is imprecise. Actual rendered height depends on font metrics (which vary by font family — Monospace vs Merriweather vs system), font weight (Bold takes more vertical space), device density, the user's system font scale setting, and Android version. No single multiplier is correct for all combinations.

Three approaches were considered:

1. **Keep tuning the multiplier** — chase the correct factor per font size/family. Fragile; breaks when any rendering variable changes. Requires constant re-calibration.

2. **Fixed page height with vertical scroll** — cap content at a fixed height and allow overflow via scroll. Used as a safety net, but means users sometimes need to scroll within a page, which is exactly the UX problem we were trying to solve.

3. **Render-then-measure (Kindle approach)** — render the entire song as one continuous column, measure the actual pixel height after Compose lays it out, then compute page breaks as scroll offsets. Eliminates all estimation.

## Decision
**Adopt the render-then-measure approach.** The entire song content is rendered as a single tall `FullSongColumn`. A `SubcomposeLayout`-based `VirtualPagedSong` composable:

1. Measures the full content height in a single layout pass (no estimation)
2. Computes `pageCount = ceil(totalContentHeightPx / viewportHeightPx)`
3. Reports `pageCount` back to `ReaderViewModel` via `onMeasuredPageCount()`
4. Renders a `HorizontalPager` where each page clips and offsets the same content by `-pageIndex × viewportHeight`

Font size changes trigger re-render → re-measurement → new page count automatically, with no manual recalculation.

### What is removed
- `paginateContent()` — the estimation function
- `estimateSongHeaderHeight()` — estimation helper
- `TEXT_HEIGHT_FACTOR`, `PAGE_INDICATOR_HEIGHT`, `DEFAULT_PAGE_HEIGHT` — estimation constants
- `setAvailableHeight()`, `repaginate()` — estimation-driven ViewModel methods
- `SongPager`, `PageContent` — old composables that rendered split `List<SongSection>` pages
- `BoxWithConstraints` height measurement in the screen

### What replaces them
- `FullSongColumn` — renders the complete song (header + all sections) in one Column
- `PageSlice` — a `Layout` composable that clips content to the viewport and applies a Y offset
- `VirtualPagedSong` — wraps the full content in a `SubcomposeLayout` to measure height, then renders a `HorizontalPager` using `PageSlice` for each virtual page
- `ReaderViewModel.onMeasuredPageCount()` — receives the measured page count from the Compose layer

## Consequences

**Positive:**
- Pixel-perfect pagination at every font size, font family, device density, and system font scale — no edge cases
- Simpler ViewModel — pagination logic is gone; the ViewModel only owns font size, current page, and song data
- Font size changes, screen rotation, and fullscreen toggle all automatically produce correct page counts
- Eliminates the entire category of "estimation is wrong" bugs
- Closer to industry standard (Kindle, Apple Books, Pocket all use layout-based pagination)

**Negative:**
- Each page in the `HorizontalPager` re-composes the full `FullSongColumn` (the same content is placed N times). For typical guitar songs (< 150 lines) this is negligible. For very long songs it could cause recomposition overhead — mitigatable with `movableContentOf` if needed.
- Page breaks always occur at line boundaries, not between sections. A section header may appear at the very bottom of a page with no lines below it. Mitigation: acceptable for v1; could add "orphan control" (minimum lines after a header) in a future pass.
- The reading position may shift slightly when toggling fullscreen (viewport height changes → page count changes → current page changes). Mitigation: preserve the scroll offset in dp and recompute the correct new page index after viewport change.
- `PaginationTest.kt` unit tests (which tested the old `paginateContent()` pure function) are deleted. Layout-based pagination cannot be unit tested on the JVM — it requires the Android runtime. Instrumented Compose UI tests (`androidTest`) can replace these if desired.
