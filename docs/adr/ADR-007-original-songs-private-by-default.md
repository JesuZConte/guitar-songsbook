# ADR-007: Original compositions are private by default

## Status
Proposed (to be implemented in v2)

## Context
The app will support two types of user-created content:
1. **Chord arrangements** of existing songs (covers, transcriptions)
2. **Original compositions** — songs the user wrote themselves

These have fundamentally different privacy expectations and legal implications.

## Decision
Original compositions (songs not found in any external catalog) default to **Private** visibility. The user must explicitly choose to make them Public.

Visibility options:
- **Private** — only visible to the author, never appears in search
- **Public** — visible to all users, can be rated and added to others' playlists

This contrasts with community submissions of existing songs, which default to Public.

## Consequences
**Positive:**
- Respects the songwriter's ownership — their original work is not shared without explicit consent
- Legally safer — no risk of accidentally publishing someone else's composition under a different name
- Builds trust: users are more likely to use the app as a personal notebook if they know it's private by default
- Aligns with standard creative app patterns (Notion, Bear, Day One — all private by default)

**Negative:**
- Reduces the size of the public catalog (fewer community-contributed originals)
- Users who want to share must take an extra step

**UX implementation:** On the save screen for original songs, show a clear toggle:
```
Visibility
  ● Private — only you can see this
  ○ Public — share with the community
```
Not a hidden setting — a deliberate, visible choice at creation time.
