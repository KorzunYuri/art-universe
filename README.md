The aim of the project is to collect data about music/literature and other artworks into a single db and later use it as a source for quiz questions.

# Modules

- **art-universe-commons** - common interfaces used across the domains
- **music-universe** - parent module for music domain
- **music-data** - data source containing 'approved' entities and attributes for music domain
  - **music-data-raw** - unified raw data collected from various sources
  - **music-data-raw-lastfm** - data collection and storage for [last.fm](https://www.last.fm/)
  - **music-data-raw-albumoftheyear** - data collection and storage for [albumoftheyear.org](https://www.albumoftheyear.org/)
  - **music-data-raw-musicbrainz** - data collection and storage for [musicbrainz.org](https://musicbrainz.org/)
 
Modules to be added in the future:
- **literature-universe** and submoduler similar to music domain
- **quiz** - data collection and storage for cross-domain quiz questions
