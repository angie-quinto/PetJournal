# PetJournal backup format

PetJournal exports a ZIP archive with format version `1`. It is an offline, unencrypted data archive; users should store and share it accordingly.

## Archive entries

- `manifest.json` records `formatVersion`, Room `schemaVersion`, `appVersion`, `exportedAt` (Unix epoch milliseconds), and per-table `recordCounts`.
- `database.json` contains `schemaVersion` and a `tables` object. Each table maps to an array of rows using the current Room column names. BLOB values are represented as an object with a `__base64_blob` property.
- `files/<relative-path>` contains each app-managed file referenced by the exported database, such as pet or journal photos. The database retains its relative file paths.

## Restore behavior and limits

Restore replaces existing PetJournal rows and referenced app-managed files; it does not merge. The app validates the archive before asking for confirmation, then validates again while staging attachments and applies database changes in a transaction. Staged files are promoted only after validation. Unsafe paths, duplicate ZIP entries, missing referenced files, unexpected tables/columns, unsupported format/schema versions, and corrupt archives are rejected.

Current defensive limits are 10,000 ZIP entries, 64 MiB per JSON entry, 100 MiB per attachment, and 1 GiB total attachment data. The archive is not encrypted or signed, so only restore backups from trusted sources.
