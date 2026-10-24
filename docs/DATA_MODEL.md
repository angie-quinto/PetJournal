# Data Model Documentation — PetJournal

## 1. Overview
Room SQLite database storing relational data for pets, health events, specialized event details, weight records, journal entries, care reminders, and attachments. Non-relational user preferences are stored in DataStore.

## 2. Core Entities
- **PetEntity**: `id` (PK, String/UUID), `name`, `species`, `customSpecies`, `breed`, `sex`, `birthDate`, `birthDatePrecision`, `adoptionDate`, `weightUnitPreference`, `photoPath`, `microchipNumber`, `medicalNotes`, `createdAt`, `updatedAt`, `archivedAt`.
- **HealthEventEntity**: `id` (PK), `petId` (FK), `type`, `title`, `eventDate`, `eventTime`, `notes`, `providerName`, `diagnosisText`, `nextDueDate`, `createdAt`, `updatedAt`, `deletedAt`.
- **VaccinationDetailEntity**, **DewormingDetailEntity**, **MedicationDetailEntity**, **VetVisitDetailEntity**, **SymptomDetailEntity**: Specialized detail tables keyed by `healthEventId`.
- **WeightRecordEntity**: `id` (PK), `petId` (FK), `weightGrams` (Positive Int), `measuredAt` (Epoch ms), `notes`, `createdAt`, `updatedAt`, `deletedAt`.
- **JournalEntryEntity**: `id` (PK), `petId` (FK), `title`, `content`, `category`, `eventDate`, `createdAt`, `updatedAt`, `deletedAt`.
- **CareReminderEntity**: `id` (PK), `petId` (FK), `healthEventId` (FK, nullable), `title`, `notes`, `dueAt` (Epoch ms), `recurrenceType`, `recurrenceRule`, `status`, `completedAt`, `notificationEnabled`, `createdAt`, `updatedAt`.
- **AttachmentEntity**: `id` (PK), `petId` (FK), `journalEntryId` (FK, nullable), `healthEventId` (FK, nullable), `relativeFilePath`, `mimeType`, `caption`, `createdAt`, `deletedAt`.
- **ReminderOccurrenceEntity**: Tracks history of recurring reminder completions.

## 3. Database Rules
- Foreign keys enabled with cascading or restrictive deletion policies.
- Indexes on `petId`, event dates, `measuredAt`, `dueAt`, and status.
- Soft deletion (`deletedAt`) for recoverable records.
- Schema versioning and migration testing.
