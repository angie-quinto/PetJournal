# AGENTS.md — PetJournal Development Guidelines for AI Agents

## Overview
PetJournal is an offline-first Android application built with Kotlin, Jetpack Compose, Material 3, Room, and Coroutines.

## Core Rules for Agents
1. **Offline-first & Privacy-focused**: No cloud sync, no analytics, no network usage in MVP. All data stored locally in Room and app-managed storage.
2. **Follow the Backlog**: Implement tickets sequentially as defined in the build specification and `task.artifact.md`. Update `task.artifact.md` as tasks are completed.
3. **Testability & Architecture**: Maintain clean separation of concerns (UI -> ViewModel -> Repository -> Room DAO). Room is the local source of truth. Composables never call DAOs directly.
4. **Verification**: Always run builds and unit tests (`./gradlew testDebugUnitTest`) after meaningful changes. Never claim a test or build passed unless actually executed and observed.
5. **No Destructive Migrations**: Room migrations must be explicitly tested and never use destructive fallback.
