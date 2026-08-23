# Planning

Living doc — update this whenever scope, stack, or decisions change.

## What this is

`transportmanage` — a fleet/vehicle management app. Starting point is vehicle
tracking (trucks + live location), expanding to trips, drivers, and beyond.

## Tech stack

| Layer | Choice | Status |
|---|---|---|
| Backend | Java, Spring Boot | Planned — scaffolding not started yet |
| Database | PostgreSQL via Neon | In progress (this branch) |
| Frontend | TBD | Not decided |
| Migrations | Flyway-style versioned SQL (`db/migrations/V<n>__desc.sql`) | In progress |

Open decisions (revisit when the Spring Boot branch starts):
- Base Java package name (e.g. `com.transportmanage`)
- Java version, Spring Boot version
- Frontend framework, or API-only for now

## Roadmap

1. **Neon DB + DDLs setup** (current branch: `task/3-neon-db---linking-with-claude-and-ddls-setup`)
   - Link repo to Neon project, branch-first dev workflow
   - Extend existing tracking schema (`trucks`, `location_history`) with `drivers` and `trips`
2. **Spring Boot scaffold** (future branch)
   - Standard Maven layout, connect to Neon via JDBC, wire up Flyway
3. **Core fleet management features** (future)
   - Trip assignment, driver management, reporting

## Key decisions log

- **Kept existing `trucks` table instead of adding a new `vehicles` table.**
  Production already had `trucks` (+ `location_history` for GPS pings) in
  active use. `trips.truck_id` references `trucks.id` (VARCHAR) rather than
  introducing a duplicate vehicle entity.
- **Dropped the `clients` table for now.** Not needed yet — add back later
  once trip-to-client billing/assignment is actually needed.
- **Neon branch-first workflow.** Schema changes are made on a Neon dev
  branch (`dev-ddls-setup`) before touching `production`. See
  `PROJECT_STRUCTURE.md` for the files this relies on.
