# Approval Workflow

## Overview

Manual approval process for raw entities from external sources before they can be bound to master entities or used in quizzes.

Provides raw entity approval interface for [Entity Binding Feature](../../../../docs/kb/features/binding-raw-entities-to-master.md).

## Workflow Steps

1. **Raw Entity Arrives**
   - Entity imported from external source (Last.fm)
   - Initial status: PENDING or AUTOAPPROVED

2. **User Reviews Entity**
   - View entity in table (artists, albums, tracks, tags)
   - Check data quality (name, metadata)
   - Identify duplicates or invalid entries

3. **User Sets Approval Status**
   - Click approval toggle/dropdown
   - Select new status: APPROVED, DECLINED, or PENDING
   - Status updated via API

4. **Optimistic UI Update**
   - UI updates immediately (optimistic)
   - Request sent to server
   - Rollback on error

5. **Enable Binding**
   - Only APPROVED entities can be bound
   - Binding button enabled/disabled based on status

## Approval States

- **PENDING** - Awaiting review
- **AUTOAPPROVED** - Automatically approved by system (meets criteria)
- **APPROVED** - Manually approved by user
- **DECLINED** - Rejected, not suitable for binding

## State Transitions

- PENDING → APPROVED (manual approval)
- PENDING → DECLINED (manual rejection)
- AUTOAPPROVED → APPROVED (confirm auto-approval)
- APPROVED → DECLINED (change decision)
- DECLINED → APPROVED (reverse decision)

## Components Involved

- [ApprovalToggle](../components/lastfm/approval-toggle.md) - UI for changing status
- [LastfmArtistsTable](../components/lastfm/artists-table.md) - Table with approval column
- [LastfmAlbumsTable](../components/lastfm/albums-table.md) - Albums with approval
- [LastfmTracksTable](../components/lastfm/tracks-table.md) - Tracks with approval
- [LastfmTagsTable](../components/lastfm/tags-table.md) - Tags with approval

## API Integration

- **Mutations:** [useLastfmEntityApproval](../../src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts)
- **Endpoints:** [Approval API](../../src/music/data/raw/shared/api/approval.tsx)
- **Error Handling:** [Error handling patterns](../api-integration/api-error-handling)

## Validation Rules

- Only APPROVED or AUTOAPPROVED entities can be bound
- DECLINED entities cannot be bound
- Status change requires network request (not local-only)

## Related Flows

- [Binding Workflow](./binding-workflow.md) - Requires approved entity
- [Category Management](./category-management.md) - Categories don't require approval
