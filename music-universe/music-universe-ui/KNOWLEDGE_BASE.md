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

### Music Data API

- `fetchBoundArtists`: Fetches bound artists from Music Data API
- `bindArtist`: Binds a LastFM artist to a Music Data artist
- `unbindArtist`: Unbinds a LastFM artist from Music Data

## Component Interaction Patterns

### Artist Management Flow

1. `LastfmArtistsTable` fetches paginated artists from LastFM API
2. `LastfmArtistsTable` fetches bound artists from Music Data API
3. `LastfmArtistsTableRow` displays artist information
4. `ApprovalToggle` allows changing approval status
5. Binding controls allow binding/unbinding artists to Music Data
6. Changes are persisted via API calls and local state updates

### Data Binding Flow

1. User selects an artist to bind
2. If artist is not approved, it's automatically approved
3. Artist is bound to Music Data via API call
4. UI is updated to show binding status
5. User can unbind artist if needed

### Binding and Approval Interaction

The binding process and approval status are closely related:
1. When binding an artist, the system ensures it's approved in LastFM
2. The `LastfmArtistsTableRow` component handles this relationship through the `handleEntityChange` function
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

## Naming Conventions

1. **Component Files**
   - PascalCase for component names
   - Component name matches file name
   - Example: `LastfmArtistsTable.tsx`

2. **Style Files**
   - Component name followed by `.module.css` or `.module.scss`
   - Example: `LastfmArtistsTable.module.css`

3. **API Functions**
   - camelCase verb + noun
   - Example: `fetchArtists`, `bindArtist`

4. **Type Definitions**
   - PascalCase for interface and type names
   - Example: `LastfmArtist`, `PaginatedResourceOptions`

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
   - Parent components (like `LastfmArtistsTableRow`) handle the relationship between binding and approval status

2. **Improved Data Flow**:
   - When an entity is bound, the parent component ensures the approval status is updated
   - This is handled through the `handleEntityChange` function in `LastfmArtistsTableRow`

3. **Optimized Loading of Bound Entities**:
   - `LastfmArtistsTable` now loads bound entities immediately after loading artists
   - This ensures that binding information is available as soon as possible
   - Explicit handling of bound/unbound state for all entities

4. **Enhanced UI Feedback**:
   - Clear visual indication of binding status
   - Automatic update of approval status when binding changes

### Improved Search and Filtering

The artist table now supports more advanced search and filtering options:
- Search by name
- Filter by approval status
- Filter by minimum play count
- Filter by minimum listeners count

## Future Enhancements

Based on the project files and knowledge base:

1. Implement track management for LastFM
2. Add album management for LastFM
3. Implement tag management and category binding
4. Add support for other data sources (MusicBrainz, AlbumOfTheYear)
5. Implement dimension and category management
6. Add track binding functionality
