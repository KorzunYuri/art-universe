# UI refactoring plan

I would like to rework current UI using TanStack Query and TanStack Table to make code and user experience cleaner.

We can either update current implementation or build a new cleaner implementation from scratch while using existing implementation as a visual reference and reusing some existing hooks if needed.

Apart of refactoring I would like to introduce pages for individual entities view and management.

Also on backend we are currently implementing 'typed relations' - relations between entities other than ownership (artist-track, artist-album). 
Ownership is represented by primary_artist_id field on backend while typed relations will be stored in separate tables, but ownership will be merged with typed relations.
Below I will mention 'entity relations sections' - those are tables that list entity relations for an entity and allow to modify them.

Each action on the UI must pass an authorization check: user's rights limit what user can see and do. However, we don't have users at the moment so we just need to mock this check for now.

For both table pages and individual entity pages we must have ability to configure the data we display (explained below).

Individual pages much designed in a flexible way so that the can serve as a separate page and a side panel showing up when a row in a table is clicked.

Common principle applying to tables: 
1. along with set-in-stone columns like entity names there must be a possibility to configure additional displayed columns and remember this configuration. 
Additional columns can be of the following types: 
   - master entity attributes. These are not implemented yet but there will be a lot of them and only minority of them are to be displayed;
   - columns relevant to raw entities' binding to master: if entity displayed in the table can be bound to master entity then the table must have opportunity to display columns for binding management (see  EntityBinding component);
   - columns relevant to quiz: if the table displays master entities or entities bound to master entities then it must be capable of displaying columns for quiz management (see QuizBinding component).
2. for individual records the set of attributes can be different from the one displayed in the table
3. each table must use pagination with page of size 20 by default. Page size can be configurable if it's an OOTB feature.
3. each table must pre-load the next page of data (this is implemented)
4. when data for page is loaded, each individual record must be put to cache; this cache must be reused by the row component to not call backend N times.
   5. we must solve the following problem: if for entity E user configured attributes set A for the table and set B for the personal page and A doesn't contain all attributes from B it means that if we only load set A for the table then there will be only attributes from set A in cache, while some attributes from set B will be missing on a personal page.
We must address this issue: we should either load all attributes of A and B for the table or load attributes for entities separately (in batches for table and for individual entities).

I would like to keep values gathered in TanQuery when navigating between pages. 
If going to a new page within the same browser tab clears the cache then we should prefer a solution that doesn't clear the cache. 
Introduce a cache layer between UI and backend can be considered as well but I'd like to keep to TanStack Query.

Here is how I see the pages:
- master
  - master artists:
    - table with master artists. Columns:
      - fixed
        - name
        - categories panel (same as is implemented now, editing categories in table is easier for user than navigating to an individual page and editing there)
      - configured
        - attributes
        - quiz-related (QuizBinding)
    - search panel to search artist by name
    - filter artists by displayed columns (fixed columns, attributes)
    - create artist button
  - master artist (individual page):
    - artist name
    - configured attributes
    - categories panel (same as in the table)
    - entity relations sections (must be foldable):
      - related artists table
      - related albums table
      - related tracks table
  - master albums
    - table with master albums. Columns:
      - fixed
        - artist name
        - album name
        - categories panel
      - configured
        - attributes
        - quiz-related (if any, currently album is not used in quiz but we must comply with the interface)
    - search panel to search album by name
    - filter albums by displayed columns (fixed columns, attributes)
    - create album button
  - master album (individual page):
    - artist name
    - album name
    - configured attributes
    - categories panel
    - entity relations sections (must be foldable):
      - related artists table
      - related albums table
      - related tracks table
  - master tracks
    - table with master tracks. Columns:
      - fixed
        - artist name
        - track name
        - categories panel
      - configured
        - attributes
        - quiz-related (QuizBinding)
    - search panel to search track by name
    - filter tracks by displayed columns (fixed columns, attributes)
    - create track button
  - master track (individual page):
    - artist name
    - track name
    - configured attributes
    - categories panel
    - entity relations sections (must be foldable):
      - related artists table
      - related albums table
      - related tracks table
  - master categories table remains untouched in terms of functionality, we only should modify it to comply with new approach (TanQuery Table)
  - master category individual page is not required
- Lastfm
  - artists, albums, tracks, tags
    - table with Lastfm entities
      - all existing columns, with the following nuances
        - ApprovalToggle: view mode when user has no rights to edit Lastfm entities
        - master binding (EntityBinding)
          - configurable
          - for albums and tracks the component should only be displayed if album's|track's artist is bound to master entity as master album/track require primary_artist_id
          - should be in view mode when user has no rights to bind Lastfm entities to master
        - quiz (QuizBinding) (if relevant for this entity) 
          - configurable
          - component is visible only if entity is bound to master entity
          - should be in view mode when user has no rights to bind master entities to quiz
      - artists and albums tables must use EntityTagPanel in the same way track table does
  - individual artist page
    - name
    - attrs (fixed)
    - ApprovalToggle
    - master binding
    - tags panel (or table, but for start we could reuse EntityTagPanel if it is compatible with the new approach)
  - individual album page
    - artist name
    - album name
    - attrs (fixed)
    - ApprovalToggle
    - master binding
    - tracks table
    - tags panel
  - individual track page
    - artist name
    - track name
    - ApprovalToggle
    - master binding
    - tags panel (or table, but for start we could reuse EntityTagPanel if it is compatible with the new approach)

Some new or existing operations we must support (the list is not exhaustive, consider existing operations included in the list):
- create master artist from master artists page. Required fields: name
- create master album from master albums page. Required fields: primary_artist_id, name
- create master track from master tracks page. Required fields: primary_artist_id, name
- update master artist/album/track from its individual page (must have Save button)
- delete master artist/album/track (with confirmation via popup)
- add relation between two entities in the corresponding section on individual entity page. Relation consists of: source_entity_type, source_entity_id, target_entity_type, target_entity_id, relation_type_id
  - relation types must be loaded from master-data REST API and cached
  - each relation type is only applicable to one or more entity types combinations
- binding-related operations (now are covered by EntityBinding but I assume this component can be refactored)
  - creation of new master entities from raw entities
    - create master artist from raw artist. Breaks down to:
      - creating master artist from raw artist's name
      - binding raw artist to the new master artist
    - create master album from raw album or a raw artist bound to master artist. Breaks down to:
      - creating master album from raw album's name and its artist's master artist
      - binding raw album to the new master album
    - create master album with master tracks for that album from raw album and its tracks
      - is the same as previous operation plus new master tracks are created from raw album's tracks and those raw tracks are bound to them.
    - create master track from raw track or a raw artist bound to master artist. Breaks down to:
      - creating master track from raw track's name and its artist's master artist
      - binding raw track to the new master track
  - binding raw artist/album/track to existing master artist/album/track
  - 'approve' Lastfm artist/album/track to tag relation if both are bound to master, which basically would just create a relation between master entities. Currently we use an operation of binding external (raw) relation to master relation - this operation will be deprecated as relation between master entities is enough.

Explore the existing endpoints to understand which operations are supported and which are missing.

EntityBinding component must receive a pre-binding hook. Purpose: I want to create/bind raw entity in one click, but before binding raw entity to master I must approve this entity (this would take another click). EntityBinding might already work as needed - must be checked.

EntityBinding must allow to proceed to master entity individual page when raw entity is bound. Use case: I've created master album with tracks from raw album in one click but some tracks have typos in their names which I want to fix.
