# ADR-021: Leather Journal UI refinement — design token alignment and component decisions

## Status
Accepted

## Context
ADR-008 established the "vintage craft" visual identity and its guiding principle: *brand in the frame, clarity in the content*. By v1.3.0 the palette and most chrome components had been built (cognac→navy gradient header, brass primitives, stitched frame, FlameGuitar emblem, ReaderToolbar). However, several screens still diverged from the canonical reference design ("Cancionero — Leather Journal Final") in specific, measurable ways:

- FAB was a circle; the reference specifies a rounded square (more physical, less generic)
- The "Mis colecciones" section header was plain text with no horizontal rule or item count
- The "Todas las canciones" icon had no cream tile container — just a bare icon
- Filter chips (HomeScreen, SettingsScreen) had no brass active state — selections looked like stock Material 3
- The "Coming soon" badge used `secondaryContainer` instead of brass fill
- The settings toggle was a plain Material 3 `Switch`
- The help dialog used a plain `AlertDialog` with no custom styling
- Bottom navigation used a flat `NavigationBar` with no gradient or brass active pill

A detailed gap analysis was performed against the reference, and a set of implementation decisions were made about how to close each gap.

## Decision

### 1. Typography: Merriweather retained for v1; Fraunces planned for a future pass

The reference specifies Fraunces (Google Fonts) as the primary typeface. The v1.3.0 commit (`64cd960`) deliberately introduced Merriweather. Merriweather is a proven editorial serif that reads well at small sizes and in all lighting conditions the app targets (dark venues, outdoor glare). Fraunces is a more display-focused, quirky serif — appropriate for the wordmark and card titles, but requires validation at 11–13sp before a full commitment.

**Decision:** Keep Merriweather for the entire v1 type system. Schedule Fraunces evaluation as a standalone typography pass in a future release. At that point, test both at all sizes and lighting conditions before adopting.

### 2. `font-variant: small-caps` approximated, not implemented natively

The reference uses `font-variant: small-caps` on section labels, the app title, and navigation labels. On Android, true small-caps requires a font file with an `smcp` OpenType feature. Merriweather does not ship a small-caps variant. Fraunces variable font does, but is not yet in the project.

**Decision:** Approximate the small-caps look using `letterSpacing` + `fontWeight` where the visual effect is needed. Mark this as a known gap. Revisit when the Fraunces typography pass is done.

### 3. AdMob banner retained on the All Songs screen

The reference design has no ad slot. However, ADR-016 commits to AdMob banner as the v1 monetization strategy. Removing the banner is not a design decision that can be made unilaterally.

**Decision:** Retain `BannerAd` on `HomeScreen`. As a future quality improvement, consider wrapping the ad slot in a thin `c.rule` border and `bgSoft` background to give it a "framed insert" appearance that reduces visual clash with the leather aesthetic.

### 4. FAB shape changed to rounded square

The circle FAB reads as a generic Android pattern. A 52dp rounded square with `cornerRadius = 14dp` feels more like a physical brass clasp or button — consistent with the "physical object" metaphor of the leather journal.

**Decision:** Change `BrassFAB` in `Brass.kt` from `CircleShape` / 56dp to `RoundedCornerShape(14.dp)` / 52dp.

### 5. Bottom navigation: wrapped Material 3 first, full custom if needed

A fully custom bottom nav gives complete control over layout, gradient, and the brass active pill. However, it requires reimplementing insets handling, accessibility semantics, and touch ripple from scratch. A wrapped `NavigationBar` using `indicatorColor = Color.Transparent` and a custom icon slot can achieve ~90% of the visual spec.

**Decision:** Wrap the existing `NavigationBar` first. Use `NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)` to suppress the M3 indicator, and render a `BrassPill` around the active icon inside the icon slot. If the result looks off (indicator suppression fighting the brass pill, label placement awkward), replace with a fully custom composable.

### 6. Filter chips: custom `LeatherChip` composable

Material 3 `FilterChip` selected state uses the M3 primary color with a checkmark — neither brass fill nor the right shape. The reference specifies brass radial fill for active, `1.5dp c.rule` border for inactive. This diverges enough from M3 that theming alone cannot bridge the gap.

**Decision:** Build a `LeatherChip` composable in `ui/components/` that wraps `BrassSurface` (active) or a plain outlined box (inactive). Use it everywhere chips appear: HomeScreen filters, SettingsScreen theme/language selectors.

### 7. Settings toggle: custom brass implementation

Material 3 `Switch` cannot be styled to the brass-fill-on-track spec without fighting the component API. The toggle appears once (chord notation) and the states are simple.

**Decision:** Replace `Switch` with a custom `BrassToggle` composable. Implement as a `Box` with `animateDpAsState` for knob position. Low priority — implement last in the refinement pass.

### 8. Help dialog: styled AlertDialog without double-frame

The reference specifies a `cardSoft` background, section labels in `c.section`, and a brass "Entendido" CTA. It also specifies a `box-shadow: inset 0 0 0 3px` double-frame effect. Native Android `AlertDialog`/`Dialog` cannot produce inset box-shadows without a custom Canvas backdrop. The double-frame is a visual detail with negligible UX value at this surface frequency.

**Decision:** Style the `AlertDialog` content with correct colors and typography. Skip the inset double-frame. If a fully custom dialog is ever needed, replace with a `Dialog` composable using a custom card.

## Implementation order (by visual impact)

1. FAB shape — 2 lines, immediately visible
2. "Mis colecciones" section header — adds polish to the primary home surface
3. AllSongsCard icon tile — cream container around the icon
4. `LeatherChip` + wire up HomeScreen and SettingsScreen filter chips
5. "Coming soon" badge → `BrassSurface`
6. Bottom nav gradient + brass pill
7. Help dialog styling
8. `BrassToggle` replacing settings `Switch`

## Consequences

**Positive:**
- All primary navigation surfaces (home, nav bar, reader header) will match the reference design
- `LeatherChip` becomes a reusable primitive — filter chips, version selectors, and any future segmented controls share one component
- Decisions are documented: future contributors know *why* Merriweather was kept and what Fraunces evaluation requires

**Negative:**
- The wrapped bottom nav approach may require a follow-up replacement if indicator suppression is visually unsatisfying
- True `font-variant: small-caps` remains unimplemented until the Fraunces typography pass
- The ad banner will always create visual tension with the leather aesthetic — a permanent known tradeoff
