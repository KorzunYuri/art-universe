# Production Secrets Setup

## Quick Start

1. Copy the template:
   ```powershell
   cp secrets.yaml.template secrets.yaml
   ```

2. Edit `secrets.yaml` and replace all `<CHANGE_ME>` values with your production credentials.

3. Create namespaces first (if not already created):
   ```bash
   kubectl apply -f ../../base/namespaces.yaml
   ```

4. Apply the secrets:
   ```bash
   kubectl apply -f secrets.yaml
   ```

## Notes

- `secrets.yaml` is gitignored and must never be committed
- Unlike the local overlay, prod secrets do not include `POSTGRES_PASSWORD` or
  `MURAW_LASTFM_DB_REPLICATION_PASSWORD` since databases run externally
- Use the same database user passwords as your external PostgreSQL instances
