# Binding Workflow

## Overview

Process of linking raw external entities to canonical master entities, creating relationship for data consolidation.

Provides the UI part of [Entity Binding Feature](../../../../docs/kb/features/binding-raw-entities-to-master.md).

## Workflow Steps

### 1. Ensure Entity Approved

- Check entity approval status
- If PENDING or AUTOAPPROVED: Auto-approve to APPROVED
- If DECLINED: Show error, prevent binding
- If APPROVED: Proceed

### 2. Open Binding Modal

- User clicks "Bind" button in table row
- [EntityBinding](../components/lastfm/entity-binding.md) modal opens
- Modal shows entity name and search field

### 3. Determine Binding State

**Three States:**

**BOUND:**
- Entity already bound to master
- Show master entity name
- Offer "Unbind" action

**UNBOUND_WITH_MATCH:**
- Entity not bound
- User selected master entity from search
- Show selected master entity
- Offer "Link" action

**UNBOUND_NO_MATCH:**
- Entity not bound
- No master entity selected
- Show search field
- Offer "Create" action

### 4. Execute Binding Action

**Link to Existing:**
- User selected existing master entity
- Call bind-to-existing API
- Pass master entity ID + raw entity ID

**Create and Link:**
- User entered new master entity name
- Call bind-to-new API
- Backend creates master entity
- Backend creates binding

**Unbind:**
- User clicks unbind
- Show confirmation dialog
- Call unbind API
- Remove binding relationship

### 5. Update Cache

- Invalidate lookup queries (fresh search results)
- Update entity detail cache (show binding)
- Reload entity if needed

### 6. Close Modal

- Modal closes
- Table refreshes with updated binding status
- User sees "Bound to: {master name}"

## Binding States Detail

**State Determination:**
- Check `entity.getMasterEntity()`
- Check `selectedMasterEntity` from picker
- Combine to determine state

**State Machine:**
- Initial: UNBOUND_NO_MATCH
- User selects entity: UNBOUND_WITH_MATCH
- After binding: BOUND

## Components Involved

- [EntityBinding](../components/lastfm/entity-binding.md) - Main binding modal
- [EntityLookup](../components/shared/entity-lookup.md) - Master entity search
- [EntityPicker](../components/shared/entity-picker.md) - Entity selection
- [ConfirmDialog](../components/shared/confirm-dialog.md) - Unbind confirmation

## API Integration

- **Queries:** [useEntityLookup](../../src/shared/hooks/useEntityLookup.ts)
- **Mutations:** Binding functions in [music-data-common-binding.ts](../../src/music/data/master/api/music-data-common-binding.ts)
- **Cache:** [Cache invalidation strategies](../patterns/react-query/cache-invalidation.md)

## Validation Rules

- Entity must be APPROVED before binding
- Master entity must exist (for bind-to-existing)
- Master entity name required (for bind-to-new)
- Cannot bind already-bound entity to different master
- Unbinding requires confirmation

## Context-Based Scoping

For artist-related entities (albums, tracks):
- Lookup context derived from entity's artist
- Master entity search scoped to same artist
- Prevents binding album to wrong artist's album

## Related Flows

- [Approval Workflow](./approval-workflow.md) - Prerequisite for binding
- [Category Management](./category-management.md) - Category binding patterns

## Related Patterns

- [Lookup Context](../patterns/lookup/lookup-context.md) - Scoped lookups
- [Context Factory](../patterns/lookup/context-factory.md) - Context creation
- [Entity Validation](../patterns/data-types/entity-validation.md) - Pre-binding validation
