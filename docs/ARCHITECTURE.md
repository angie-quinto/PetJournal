# Architecture Documentation — PetJournal

## 1. Architecture Pattern
- **MVVM (Model-View-ViewModel)** with Jetpack Compose UI.
- **Unidirectional Data Flow (UDF)**: UI observes `StateFlow` exposed by ViewModels. ViewModels invoke repository methods and use cases.
- **Repository Pattern**: Repositories abstract local Room data sources and file storage, providing clean domain models to ViewModels.

## 2. Layer Structure
- `ui/` or `feature/`: Composable screens, ViewModels, UI state models.
- `domain/`: Domain models, repository interfaces, use cases (business logic).
- `data/`: Room entities, DAOs, Room database, repository implementations, mappers.
- `core/`: Database helpers, file storage utilities, notification managers, backup service, common extensions.

## 3. Data Flow
1. User interacts with Composable UI.
2. UI calls ViewModel function.
3. ViewModel executes repository suspend functions or observes Flow.
4. Repository queries Room DAO or local file storage.
5. Room DAO emits Flow of Entity objects; repository maps entities to domain models.
6. ViewModel updates StateFlow; Compose UI recomposes with new state.
