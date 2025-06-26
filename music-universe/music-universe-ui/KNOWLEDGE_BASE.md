# Music Universe UI - Knowledge Base

## Project Overview

This React application serves as the user interface for the Art Universe project, specifically focusing on the Music Universe domain. It provides interfaces for managing music data from various sources (LastFM, MusicBrainz, etc.) and allows users to approve, bind, and manage music entities for use in music quizzes.

## Architecture

### Core Components

1. **UI Component Layer**
   - Reusable UI components
   - Source-specific components
   - Layout components

2. **Data Access Layer**
   - API client functions
   - Data fetching hooks
   - Pagination utilities

3. **Routing Layer**
   - Route configuration
   - Navigation components
   - Page components

4. **State Management**
   - Local component state
   - Custom hooks for shared state
   - Data caching strategies

## Key Concepts

### Data Sources

The system works with multiple data sources:
- raw data sources:
  - **LastFM** (`lastfm` module)
  - **MusicBrainz** (planned)
  - **AlbumOfTheYear** (planned)
- **Music Data** (approved data central storage)
- **Music Quiz** (subset of approved data approved for quiz)

### Entity Types

The system manages several entity types:
- **Artists**
- **Albums**
- **Tracks**
- **Tags**
- **Categories and Dimensions** (planned)

### Approval System

Entities from external sources can have different approval statuses:
- `PENDING` (1): Default state, awaiting decision
- `APPROVED` (2): Manually approved for use
- `DECLINED` (3): Manually rejected
- `AUTOAPPROVED` (4): Created automatically and thus temporary approved

### Binding System

External entities can be bound to internal ('approved') entities in the Music Data system.
Example: artists from LastFM can be bound to artists in Music Data. 
Binding of an external entity with an unknown name automatically creates new internal entity in Music Data.
Additionally, internal entity can be approved for quiz. The logic of this approval is transitive, in example:
1. when user clicks on an element to 'approve entity for quiz' in Lastfm artists table...
2. make sure external entity is approved in Lastfm
3. make sure internal entity exists in Music Data
4. bind external entity to internal entity in Music Data
5. bind internal entity to quiz entity (normally, quiz will only store ids of internal entities approved for quiz)

## Design Patterns

### Component Pattern
Used for building reusable UI components:
- Shared components in `shared/components`
- Source-specific components in `sources/{source}/components`

### Custom Hooks Pattern
Used for encapsulating reusable logic:
- `PaginatedResource` for handling paginated API data
- State management hooks

### Container/Presentational Pattern
Used for separating data fetching from rendering:
- Container components handle data fetching and state
- Presentational components focus on rendering

### Module Pattern
Used for organizing code by feature:
- Source-specific modules (`lastfm`, `music-data`)
- Shared modules (`shared`)

### API Client Pattern
Used for centralizing API calls:
- Source-specific API clients (`lastfm/api`, `music-data/api`)
- Consistent error handling and response processing

## Project Structure

### Directory Organization

```
src/music-universe/
├── shared/                  # Shared components and utilities
│   ├── components/          # Reusable UI components
│   ├── hooks/               # Custom React hooks
│   ├── styles/              # Shared styles
│   └── types/               # TypeScript type definitions
├── sources/                 # Source-specific modules
│   ├── lastfm/              # LastFM-specific components and logic
│   │   ├── api/             # LastFM API client functions
│   │   ├── components/      # LastFM-specific components
│   │   ├── config/          # LastFM configuration
│   │   ├── constants/       # LastFM constants
│   │   ├── pages/           # LastFM page components
│   │   └── types/           # LastFM type definitions
│   └── music-data/          # Music Data (internal) components and logic
│       ├── api/             # Music Data API client functions
│       └── config/          # Music Data configuration
└── main.tsx                 # Application entry point
```

## Key Components and Their Purposes

### Shared Components

- `ExternalLink`: Component for rendering external links
- `ReadonlyAttr`: Component for displaying read-only attributes
- `NavigationCard`: Component for navigation links
- `LabelWithPopup`: Component for labels with tooltip popups
- `EntityBinding`: Component for binding external entities to internal entities
- `EntityTable`: Generic table component for displaying and managing entities

### LastFM Components

- `LastfmArtistsTable`: Container component for displaying and managing LastFM artists
- `LastfmArtistsTableRow`: Component for rendering a single artist row
- `LastfmArtistsTableHeader`: Component for rendering the table header with sorting
- `LastfmTracksTable`: Container component for displaying and managing LastFM tracks
- `LastfmTracksTableRow`: Component for rendering a single track row with complex binding logic
- `LastfmTracksTableHeader`: Component for rendering the tracks table header with sorting
- `ApprovalToggle`: Component for toggling approval status

### Page Components

- `LastfmHome`: Home page for LastFM module
- `LastfmArtists`: Page for managing LastFM artists
- `LastfmAlbums`: Page for managing LastFM albums
- `LastfmTracks`: Page for managing LastFM tracks
- `LastfmTags`: Page for managing LastFM tags

### Custom Hooks

- `PaginatedResource`: Hook for handling paginated API resources with search, sort, and pagination

## API Integration

### LastFM API

- `fetchArtists`: Fetches paginated artists from LastFM API
- `updateArtistApprovalStatus`: Updates artist approval status
- `fetchTracks`: Fetches paginated tracks from LastFM API
- `updateTrackApprovalStatus`: Updates track approval status

### Music Data API

- `fetchBoundArtists`: Fetches bound artists from Music Data API
- `bindArtist`: Binds a LastFM artist to a Music Data artist
- `unbindArtist`: Unbinds a LastFM artist from Music Data
- `fetchBoundTracks`: Fetches bound tracks from Music Data API
- `bindTrack`: Binds a LastFM track to a Music Data track (requires artist external ID)
- `unbindTrack`: Unbinds a LastFM track from Music Data

## Component Interaction Patterns

### Artist Management Flow

1. `LastfmArtistsTable` fetches paginated artists from LastFM API
2. `LastfmArtistsTable` fetches bound artists from Music Data API
3. `LastfmArtistsTableRow` displays artist information
4. `ApprovalToggle` allows changing approval status
5. Binding controls allow binding/unbinding artists to Music Data
6. Changes are persisted via API calls and local state updates

### Track Management Flow

1. `LastfmTracksTable` fetches paginated tracks from LastFM API
2. `LastfmTracksTable` fetches bound tracks from Music Data API
3. `LastfmTracksTableRow` displays track information with complex binding logic:
   - First ensures artist is approved in LastFM
   - Then ensures artist is bound to Music Data
   - Then approves track in LastFM if needed
   - Finally binds track to Music Data
4. `ApprovalToggle` allows changing track approval status
5. `EntityBinding` component handles the complex binding workflow

### Data Binding Flow

1. User selects an entity to bind
2. If entity is not approved, it's automatically approved
3. For tracks: Artist is also approved and bound if needed
4. Entity is bound to Music Data via API call
5. UI is updated to show binding status
6. User can unbind entity if needed

### Binding and Approval Interaction

The binding process and approval status are closely related:
1. When binding an entity, the system ensures it's approved in LastFM
2. The row components handle this relationship through the `handleEntityChange` function
3. When an entity is bound, the component ensures the approval status is updated to reflect this
4. This separation of concerns keeps the `EntityBinding` component focused on binding functionality while the parent component manages the relationship between binding and approval

## Styling Approach

The project uses a combination of:
- CSS Modules for component-specific styles
- SCSS for more complex styling needs
- Global styles for application-wide styling

Naming conventions:
- Component styles: `ComponentName.module.css` or `ComponentName.module.scss`
- Shared styles: `common.module.scss`

## State Management

The application uses:
- React's built-in state management (useState, useEffect)
- Custom hooks for encapsulating complex state logic
- Props for passing data between components
- Callbacks for child-to-parent communication

## Pagination Pattern

The `PaginatedResource` hook provides:
- Data fetching with pagination
- Search functionality
- Sorting
- Loading state management
- Error handling

## Type System and API Standards

### Shared Types

The application uses a standardized type system:

- `ApiResponse<T>`: Standard response wrapper used by all backend modules
- `BoundEntity`: Interface for bound entity references (referenceId, referenceName)
- `BoundEntityResponse`: Interface for bound entity API responses (includes externalId, dataSource)
- `Bindable`: Interface for entities that can be bound to internal entities
- `Approvable`: Interface for entities that can be approved or declined

### API Request/Response Patterns

All API calls follow consistent patterns:
- Request DTOs: `{Entity}BindingRequest` interfaces for binding operations
- Response wrapper: All responses use `ApiResponse<T>` structure
- Error handling: Consistent error logging and user feedback
- Loading states: Proper loading indicators during API calls

## Naming Conventions

1. **Component Files**
   - PascalCase for component names
   - Component name matches file name
   - Example: `LastfmTracksTable.tsx`

2. **Style Files**
   - Component name followed by `.module.css` or `.module.scss`
   - Example: `LastfmTracksTable.module.css`

3. **API Functions**
   - camelCase verb + noun
   - Example: `fetchTracks`, `bindTrack`

4. **Type Definitions**
   - PascalCase for interface and type names
   - Example: `LastfmTrack`, `PaginatedResourceOptions`

5. **Constants**
   - UPPER_CASE for constant values
   - PascalCase for constant objects
   - Example: `ApprovalStatus.APPROVED`

## Common Patterns

### Component Export Pattern

Components are typically exported from an index.ts file in their directory:

```typescript
export { ComponentName } from './ComponentName';
```

This allows for cleaner imports:

```typescript
import { ComponentName } from '@/path/to/components';
```

### Component Props Pattern

Component props are defined using TypeScript interfaces:

```typescript
interface ComponentProps {
  prop1: string;
  prop2?: number;
  onEvent: (value: string) => void;
}

export const Component = ({ prop1, prop2, onEvent }: ComponentProps) => {
  // Component implementation
};
```

### API Response Handling Pattern

API responses are wrapped in a consistent structure:

```typescript
interface ApiResponse<T> {
  success: boolean;
  message?: string;
  data: T;
}
```

### Conditional Rendering Pattern

Components use conditional rendering for different states:

```typescript
{loading ? (
  <div>Loading...</div>
) : data ? (
  <DataComponent data={data} />
) : (
  <div>No data available</div>
)}
```

## Build and Development

- Vite for fast development and building
- TypeScript for type safety
- ESLint for code quality
- SASS for advanced styling
- React Router for navigation

## Configuration

- Environment-specific configuration
- API endpoints configuration in source-specific config files
- Path aliases for cleaner imports (`@/` for src directory)

## Recent Improvements

### Enhanced Entity Binding System

The binding system has been improved to better handle the relationship between binding and approval:

1. **Separation of Concerns**:
   - `EntityBinding` component now focuses solely on binding functionality
   - Parent components (like row components) handle the relationship between binding and approval status

2. **Improved Data Flow**:
   - When an entity is bound, the parent component ensures the approval status is updated
   - This is handled through the `handleEntityChange` function in row components

3. **Optimized Loading of Bound Entities**:
   - Table components now load bound entities immediately after loading main entities
   - This ensures that binding information is available as soon as possible
   - Explicit handling of bound/unbound state for all entities

4. **Enhanced UI Feedback**:
   - Clear visual indication of binding status
   - Automatic update of approval status when binding changes

### Track Binding Implementation

Implemented comprehensive track binding functionality:

1. **Complex Binding Logic**: Track binding requires:
   - Artist approval in LastFM (if not already approved)
   - Artist binding to Music Data (if not already bound)
   - Track approval in LastFM (if not already approved)
   - Track binding to Music Data with artist reference

2. **API Integration**: 
   - `TrackBindingRequest` includes track name and artist external ID
   - Follows same patterns as artist binding
   - Proper error handling and state management

3. **UI Components**:
   - Added binding column to tracks table
   - Complex binding workflow in `LastfmTracksTableRow`
   - Consistent styling and user experience

### Type System Standardization

Eliminated code duplication by creating shared type definitions:

1. **Shared API Types**:
   - `ApiResponse<T>` moved to shared types
   - `BoundEntityResponse` replaces duplicate `BoundArtist`/`BoundTrack` interfaces
   - Consistent type usage across all API clients

2. **Improved Maintainability**:
   - Single source of truth for common interfaces
   - Easier to update API contracts
   - Better TypeScript intellisense and error checking

### Improved Search and Filtering

The tables now support advanced search and filtering options:
- Search by name
- Filter by approval status
- Filter by minimum play count
- Filter by minimum listeners count
- Artist-specific track filtering

## Future Enhancements

Based on the project files and knowledge base:

1. Implement album management for LastFM
2. Add support for other data sources (MusicBrainz, AlbumOfTheYear)
3. Implement tag management and category binding
4. Add dimension and category management
5. Implement quiz-specific approval workflow
6. Add bulk operations for entity management
7. Implement advanced filtering and search capabilities
8. Add data export/import functionality
