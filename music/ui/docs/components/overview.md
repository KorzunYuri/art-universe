# Component Overview

This document categorizes all UI components in the Music Universe module by their purpose and reusability.

## 1. Small Single-Purpose Components

Reusable UI elements serving a specific, focused purpose.

**Shared** (`src/music/shared/components/`):
- [ConfirmDialog](../../src/music/shared/components/ConfirmDialog/) - Modal confirmation dialog
- [EditableText](../../src/music/shared/components/EditableText/) - Inline text editing widget
- [ExternalLink](../../src/music/shared/components/ExternalLink/) - Link that opens in new tab
- [LabelWithPopup](../../src/music/shared/components/LabelWithPopup/) - Label with tooltip/popup
- [NavigationCard](../../src/music/shared/components/NavigationCard/) - Card for navigation menu
- [NotificationContainer](../../src/music/shared/components/NotificationContainer/) - Toast notification display
- [Pagination](../../src/music/shared/components/Pagination/) - Pagination controls for tables
- [ReadonlyAttr](../../src/music/shared/components/ReadonlyAttr/) - Read-only attribute display
- [StaticAutocompleteInput](../../src/music/shared/components/StaticAutocompleteInput/) - Static autocomplete dropdown

**LastFM** (`src/music/data/raw/lastfm/components/`):
- [EntityTagItem](../../src/music/data/raw/lastfm/components/EntityTagItem/) - Single tag/category chip display
- [EntityTagPanel](../../src/music/data/raw/lastfm/components/EntityTagPanel/) - Panel showing multiple tags
- [LastfmArtistFilterButton](../../src/music/data/raw/lastfm/components/LastfmArtistFilterButton/) - Filter button for artist queries
- [LastfmArtistLink](../../src/music/data/raw/lastfm/components/LastfmArtistLink/) - Link to LastFM artist page
- [MaintenanceButton](../../src/music/data/raw/lastfm/components/MaintenanceButton/) - Maintenance operation trigger

**Master** (`src/music/data/master/components/`):
- [CategoryName](../../src/music/data/master/components/CategoryName/) - Display category name with styling
- [MasterEntityPanelItem](../../src/music/data/master/components/MasterEntityPanelItem/) - Single item in master entity panel

**Quiz** (`src/music/quiz/components/`):
- [CategoryWeightItem](../../src/music/quiz/components/CategoryWeightItem/) - Category weight configuration item
- [PipelineStepDetail](../../src/music/quiz/components/PipelineStepDetail/) - Display pipeline step details
- [StepStats](../../src/music/quiz/components/StepStats/) - Display step statistics
- [StepTypeSelector](../../src/music/quiz/components/StepTypeSelector/) - Select pipeline step type

**Quiz Step Configs** (`src/music/quiz/components/stepConfigs/`):
- [BlacklistFilterConfig](../../src/music/quiz/components/stepConfigs/BlacklistFilterConfig.tsx) - Configure blacklist filter step
- [FinalCategoriesBalancerConfig](../../src/music/quiz/components/stepConfigs/FinalCategoriesBalancerConfig.tsx) - Configure category balancer step
- [FinalLimiterConfig](../../src/music/quiz/components/stepConfigs/FinalLimiterConfig.tsx) - Configure limiter step
- [WhitelistFilterConfig](../../src/music/quiz/components/stepConfigs/WhitelistFilterConfig.tsx) - Configure whitelist filter step

## 2. Base Components for Extension/Reuse

Generic, configurable components designed to be extended or reused across domains.

**Shared** (`src/music/shared/components/`):
- [EntityTable](../../src/music/shared/components/EntityTable/) - Generic paginated table with search, sort, and filters
- [EntityPicker](../../src/music/shared/components/EntityPicker/) - Generic entity picker with autocomplete
- [EntityLookup](../../src/music/shared/components/EntityLookup/) - Generic entity lookup/search input

## 3. Components That Extend/Reuse Base Components

Domain-specific implementations that wrap or configure base components.

**LastFM Tables** (`src/music/data/raw/lastfm/components/`):
- [LastfmAlbumsTable](../../src/music/data/raw/lastfm/components/LastfmAlbumsTable/) - Uses EntityTable for LastFM albums
- [LastfmAlbumsTableRow](../../src/music/data/raw/lastfm/components/LastfmAlbumsTableRow/) - Row component for LastfmAlbumsTable
- [LastfmArtistsTable](../../src/music/data/raw/lastfm/components/LastfmArtistsTable/) - Uses EntityTable for LastFM artists
- [LastfmArtistsTableRow](../../src/music/data/raw/lastfm/components/LastfmArtistsTableRow/) - Row component for LastfmArtistsTable
- [LastfmTagsTable](../../src/music/data/raw/lastfm/components/LastfmTagsTable/) - Uses EntityTable for LastFM tags
- [LastfmTagsTableRow](../../src/music/data/raw/lastfm/components/LastfmTagsTableRow/) - Row component for LastfmTagsTable
- [LastfmTracksTable](../../src/music/data/raw/lastfm/components/LastfmTracksTable/) - Uses EntityTable for LastFM tracks
- [LastfmTracksTableRow](../../src/music/data/raw/lastfm/components/LastfmTracksTableRow/) - Row component for LastfmTracksTable

**Master Tables** (`src/music/data/master/components/`):
- [AlbumsTable](../../src/music/data/master/components/AlbumsTable/) - Uses EntityTable for master albums
- [ArtistsTable](../../src/music/data/master/components/ArtistsTable/) - Uses EntityTable for master artists
- [ArtistsTableRow](../../src/music/data/master/components/ArtistsTableRow/) - Row component for ArtistsTable
- [CategoriesTable](../../src/music/data/master/components/CategoriesTable/) - Uses EntityTable for master categories
- [CategoriesTableRow](../../src/music/data/master/components/CategoriesTableRow/) - Row component for CategoriesTable
- [TracksTable](../../src/music/data/master/components/TracksTable/) - Uses EntityTable for master tracks
- [MasterEntityPicker](../../src/music/data/master/components/MasterEntityPicker/) - Wraps EntityPicker with master data source preset

**Quiz Tables** (`src/music/quiz/components/`):
- [GameTable](../../src/music/quiz/components/GameTable/) - Uses EntityTable for quiz games
- [GenerationsList](../../src/music/quiz/components/GenerationsList/) - Uses EntityTable for quiz generations
- [ResultStatsTable](../../src/music/quiz/components/ResultStatsTable/) - Uses EntityTable for quiz result statistics

## 4. Domain-Specific Components

Complex components implementing specific business logic or workflows.

**LastFM** (`src/music/data/raw/lastfm/components/`):
- [ApprovalToggle](../../src/music/data/raw/lastfm/components/ApprovalToggle/) - Raw entity approval status management
- [EntityBinding](../../src/music/data/raw/lastfm/components/EntityBinding/) - Raw entity to master entity binding workflow modal

**Master** (`src/music/data/master/components/`):
- [CategoryDag](../../src/music/data/master/components/CategoryDag/) - Category hierarchy DAG visualization (using @xyflow/react)
- [MasterEntityPanel](../../src/music/data/master/components/MasterEntityPanel/) - Panel displaying master entity information

**Quiz** (`src/music/quiz/components/`):
- [GameDetails](../../src/music/quiz/components/GameDetails/) - Quiz game details and configuration
- [PipelineEditor](../../src/music/quiz/components/PipelineEditor/) - Visual pipeline builder and editor
- [PipelineStepper](../../src/music/quiz/components/PipelineStepper/) - Step-by-step pipeline wizard navigation
- [PipelineTabs](../../src/music/quiz/components/PipelineTabs/) - Tab navigation for pipeline views
- [QuizBinding](../../src/music/quiz/components/QuizBinding/) - Quiz track binding workflow
- [StepBuilder](../../src/music/quiz/components/StepBuilder/) - Pipeline step construction interface
- [StepPreview](../../src/music/quiz/components/StepPreview/) - Preview pipeline step configuration
