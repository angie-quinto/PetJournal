# PetJournal implementation backlog

This backlog is ordered by implementation dependencies. PetJournal remains offline-first: user records stay on-device, with no account, analytics, or network dependency.

## Phase 0 — Backlog and status reconciliation

- [x] **B0.1 Reconcile implementation status**
  - Acceptance: the status document reflects the existing pet/profile, dashboard, and journal CRUD UI; clearly distinguishes existing data/ViewModel foundations from missing UI or behavior.
- [x] **B0.2 Maintain this backlog**
  - Acceptance: each remaining feature has an ordered scope and observable acceptance criteria, updated as work lands.

## Phase 1 — Branded UI foundation

- [x] **B1.1 Establish a modern branded Material 3 system**
  - Acceptance: branded light and dark color schemes are used consistently on every supported Android version; typography, component shapes, contrast, and spacing are coherent.
- [x] **B1.2 Refresh app shell and common UX**
  - Acceptance: navigation and existing dashboard, pet, and journal screens share the design system; key controls have accessible labels and touch targets; empty, validation, loading, and failure states are clear.

## Phase 2 — Health and weight

- [x] **B2.1 Add pet-specific health event UI**
  - Acceptance: users can create, view, edit, and remove health records with fields for the supported event types and their specialized details.
- [x] **B2.2 Add weight history and chart**
  - Acceptance: users can record and review dated weights, see a useful history visualization, and view/enter consistent kg or lb values while storage remains normalized in grams.

## Phase 3 — Care reminders

- [x] **B3.1 Complete reminder management**
  - Acceptance: the Care destination lists reminders and supports create, edit, completion, and supported recurrence types for a selected pet.
- [x] **B3.2 Schedule local notifications**
  - Acceptance: enabled reminders are scheduled using on-device WorkManager, permission is requested only when appropriate, and notification delivery follows edits, cancellation, completion, and recurrence.
  - Decision: notification delivery is off unless enabled by the user; late completion preserves the original recurrence cadence.

## Phase 4 — Journal attachments

- [x] **B4.1 Attach and display journal photos**
  - Acceptance: users can pick images, retain them in app-managed storage, associate them with journal entries, and see them in journal details.
- [x] **B4.2 Clean up attachment files safely**
  - Acceptance: deleting records or attachments does not leave unintended app-managed orphan files; unrelated files are never removed.

## Phase 5 — Unified timeline and search

- [x] **B5.1 Build the pet timeline**
  - Acceptance: health events, weights, and journal entries appear together in chronological order with clear type/date context and navigation to useful record details.
- [x] **B5.2 Add search and filters**
  - Acceptance: users can search records and filter by pet, event type, and date without losing the selected criteria while navigating the results.

## Phase 6 — Backup and veterinary report

- [x] **B6.1 Export and restore versioned backups**
  - Acceptance: export produces a versioned JSON/ZIP archive with records and referenced attachments; import validates format and paths before changes; restore is atomic and reports errors without partial success.
  - Decision: restoring replaces on-device app data only after validation and explicit confirmation; backup restore does not merge records.
- [x] **B6.2 Export a veterinary summary**
  - Acceptance: a selected pet's useful health history and current profile details can be exported in a shareable, readable report, without network transmission by the app.

## Phase 7 — Release hardening

- [x] **B7.1 Preserve local data across schema upgrades**
  - Acceptance: remove destructive migration fallback, add explicit migrations for supported prior schemas, and verify migration paths against exported Room schemas.
- [ ] **B7.2 Verify core user journeys**
  - Acceptance: focused tests cover recurrence/date logic, conversions, persistence and migrations, backup validation/restore safety, and Compose flows for pet, health, reminder, journal, and timeline use.
- [ ] **B7.3 Review privacy and accessibility**
  - Acceptance: app remains offline-only; labels, contrast, scalable text, focus, and destructive-action confirmations are reviewed on key screens.
