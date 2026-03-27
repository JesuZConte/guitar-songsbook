# ADR-001: MVVM + Clean Architecture as the base pattern

## Status
Accepted

## Context
We needed a scalable architecture for an Android app that would grow from a local JSON reader to a platform with remote data, user authentication, and community features. The team (solo developer, learning-focused) needed a pattern that is industry-standard, well-documented, and testable.

## Decision
Adopt MVVM (Model-View-ViewModel) with Clean Architecture layers:
- `domain/model` — pure Kotlin data classes, no Android dependencies
- `data/` — Room database, repositories, mappers
- `presentation/` — ViewModels with StateFlow, Compose screens

Manual dependency injection (no Hilt/Dagger) for simplicity during the learning phase.

## Consequences
**Positive:**
- ViewModels are fully testable without Android instrumentation
- Repository pattern allows swapping data sources (local JSON → Room → Firestore) without touching UI
- Industry-standard pattern — easy to onboard new contributors

**Negative:**
- Manual DI in `MainActivity` will grow as the app scales — will need Hilt by v2
- More boilerplate than a simpler MVC approach

**Future migration path:** Introduce Hilt when the number of ViewModels makes manual wiring in `MainActivity` unmaintainable (threshold: ~6 ViewModels)
