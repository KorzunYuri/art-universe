# Spotify REST API

REST API service for seeding and managing Spotify entities. Provides endpoints for manually adding artists/albums/tracks as seed entities for the ETL pipeline to discover and expand.

## Key Features

- Seed artist by Spotify ID - creates the entity and triggers the ETL graph expansion
- Lookup entities by Spotify ID
- OAuth2 client credentials authentication for Spotify API calls during seeding

## Run Configuration

- `MURAW_SPOTIFY_REST_API_PORT` - Service port
- `SPOTIFY_CLIENT_ID` - Spotify application client ID
- `SPOTIFY_CLIENT_SECRET` - Spotify application client secret

## Related Documentation

- [Spotify Models](../spotify-models/README.md) - JPA entities and DTOs
- [Spotify Modules Overview](../README.md) - Overview of all Spotify modules
- [Project Modules Index](../../../../../docs/MODULES.md) - Return to main modules index
