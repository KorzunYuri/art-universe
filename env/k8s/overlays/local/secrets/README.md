# Kubernetes Secrets for Local Environment

This directory contains secret configurations for the local Kubernetes deployment.

## Setup Instructions

1. Copy the template file to create your secrets:
   ```powershell
   cp secrets.yaml.template secrets.yaml
   ```

2. Edit `secrets.yaml` and replace all `<CHANGE_ME>` placeholders with actual values.

3. Apply the secrets to your cluster:
   ```powershell
   kubectl apply -f secrets.yaml
   ```

## Required Secrets

### mu-data namespace

| Secret Name | Keys | Description |
|-------------|------|-------------|
| `lastfm-db-credentials` | POSTGRES_PASSWORD, MURAW_LASTFM_DB_WRITER_PASSWORD, MURAW_LASTFM_DB_READER_PASSWORD, MURAW_LASTFM_DB_REPLICATION_PASSWORD | LastFM database credentials |
| `music-data-db-credentials` | POSTGRES_PASSWORD, MU_DATA_DB_PASSWORD_DM, MU_QUIZ_DB_PASSWORD_DM | Music Data database credentials |

### mu-lastfm namespace

| Secret Name | Keys | Description |
|-------------|------|-------------|
| `lastfm-api-key` | MURAW_LASTFM_API_KEY | LastFM API key for data collection |
| `lastfm-db-credentials` | (same as mu-data) | Database credentials for LastFM services |

## Security Notes

- Never commit actual secret values to version control
- The `.gitignore` file ensures only templates are tracked
- For production, use Kubernetes Sealed Secrets or an external secrets manager
