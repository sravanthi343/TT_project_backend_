This folder stores the H2 file-based database files generated at runtime.

Files created here automatically when the backend starts:
  cmsdb.mv.db   — the main H2 database file (all tables and data live here)
  cmsdb.trace.db — H2 query trace log (only appears when tracing is enabled)

Because data is stored in this folder, it SURVIVES application restarts.
Do NOT delete these files unless you want to wipe all stored data.

To connect via H2 Console:
  URL:      http://localhost:8080/h2-console
  JDBC URL: jdbc:h2:file:./data/cmsdb
  Username: sa
  Password: (leave blank)
