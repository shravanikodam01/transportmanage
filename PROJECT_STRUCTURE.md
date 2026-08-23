# Project Structure

Living doc — update this whenever files/directories are added, removed, or
repurposed. See `PLANNING.md` for the "why" behind the project.

```
transportmanage/
├── .neon                    # Neon org/project/branch link (safe to commit, no secrets)
├── .env.local               # Neon connection strings, pulled via `neon env pull` (gitignored)
├── .gitignore
├── db/
│   └── migrations/
│       └── V1__init_schema.sql   # Flyway-style versioned SQL migrations
├── .agents/skills/          # Installed agent skills (neon, neon-postgres)
├── .claude/                 # Claude Code project config
├── skills-lock.json         # Lockfile for installed agent skills
├── PLANNING.md              # Roadmap, stack decisions, decision log
├── PROJECT_STRUCTURE.md     # This file
└── README.md
```

## Database (`db/migrations/`)

- Naming: `V<version>__<description>.sql` (Flyway convention), so it plugs
  straight into Flyway once Spring Boot is scaffolded — no renaming needed.
- Applied in order, tracked by version number.
- Current schema: `trucks`, `location_history` (pre-existing, live tracking
  data) + `drivers`, `trips` (added in `V1`).

## Neon setup

- `.neon` pins this workspace to org `Shravani` / project `transport`.
- Schema changes happen on the `dev-ddls-setup` branch first, verified, then
  merged into `production` via Neon's branch-first workflow (see the `neon`
  skill for the CLI commands: `neon checkout`, `neon diff`, etc.).
- `.env.local` holds the active branch's `DATABASE_URL` — never commit it.

## Not yet added

- No application code yet (Spring Boot scaffold is a separate future branch;
  see `PLANNING.md` roadmap).
- No frontend.
