# Binding Raw Entities To Master

Binding raw entities to master is the core process of master data management. It includes:
- creating new master entities from the raw entities (with possible adjustments of name or other parameters)
- binding raw entities to the existing master entities

The concept is explained in [Music Master Data Service](../../../music/data/master/docs/entity-relations.md) aspect doc.

At the moment of writing this Lastfm was the only source of raw entities, with Spotify planned.


## Management Flows

### Creating New Master Entity Flow

- Raw entity is collected from an external source
- Raw entity is reviewed and approved
- **Master entity is created based on the raw entity**

### Binding Raw Entity To a Master Entity

- Raw entity is collected from an external source
- Raw entity is reviewed and approved
- **Raw entity is bound to a master entity**


## Modules Involved

- [Music Master Data Service](../../../music/data/master/README.md) - Manages master entities
- [Lastfm Read API](../../../music/data/raw/lastfm/lastfm-rest-api/README.md) - Provides read access to Lastfm entities
- [Lastfm Write API](../../../music/data/master/README.md) - Provides write access to Lastfm entities (approval status management)
- [Music UI](../../../music/ui/README.md) - Provides user interface for all the required operations
