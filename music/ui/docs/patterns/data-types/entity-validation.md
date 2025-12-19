# Entity Validation Pattern

## What It Is

Pre-operation validation pattern that ensures entity state meets requirements before executing operations, with automatic state corrections.

## Why It Exists

Prevents invalid operations, provides automatic approval before binding, and ensures data consistency.

## Location

[src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts](../../src/music/data/raw/lastfm/hooks/useLastfmEntityApproval.ts)

## Key Validation: ensureIsValidForBinding

**Purpose:** Ensure entity is approved before binding

**Signature:**
- Input: `hasMasterExisted` (boolean)
- Output: `Promise<boolean>` (validation passed)

**Logic:**
1. Check current approval status
2. If APPROVED: Return true (already valid)
3. If PENDING or AUTOAPPROVED: Auto-approve, return true
4. If REJECTED: Return false (cannot bind)

## Approval Status Rules

### Valid for Binding

**Statuses:**
- APPROVED: Already approved, proceed
- PENDING: Auto-approve, then proceed
- AUTOAPPROVED: Auto-approve as APPROVED, then proceed

**Action:** Automatic approval if not already approved

### Invalid for Binding

**Statuses:**
- REJECTED: Cannot bind rejected entity

**Action:** Return false, prevent binding

## Auto-Approval Flow

**Process:**
1. Validation check triggered
2. Status is PENDING or AUTOAPPROVED
3. Call approval update API
4. Change status to APPROVED
5. Return success
6. Binding proceeds

**Benefit:** User doesn't need manual approval step

## Usage in Binding Workflow

**Binding Component Pattern:**
1. User clicks "Bind" button
2. Component calls `ensureIsValidForBinding()`
3. Validation runs (may auto-approve)
4. If validation passes: Execute binding
5. If validation fails: Show error message

**Example:** [EntityBinding](../../src/music/data/raw/lastfm/components/EntityBinding/EntityBinding.tsx)

## Error Handling

**Validation Failure:**
- Return false from validation
- Component shows error message
- Binding not executed

**API Error During Auto-Approval:**
- Catch exception
- Return false
- User notified of failure

## Benefits

**User Experience:**
- Fewer manual steps
- Automatic approval when appropriate
- Clear error messages when invalid

**Data Consistency:**
- All bound entities guaranteed approved
- No invalid bindings in database

**Business Logic:**
- Approval rules enforced
- Consistent behavior across UI

## Validation Timing

**Pre-Operation:**
- Before binding executes
- Before server request
- Prevents wasted requests

**Just-In-Time:**
- At operation time, not earlier
- Catches status changes since page load

## Extensibility

**Additional Validations:**
- Could check for duplicate bindings
- Could validate entity data completeness
- Could check user permissions

**Pattern:**
- Async validation function
- Returns boolean or throws
- Used in mutation onMutate

## Related Patterns

- [Approval Workflow](../../flows/approval-workflow.md) - Approval status management
- [Binding Workflow](../../flows/binding-workflow.md) - Binding process
- [Entity Types](./entity-types.md) - Entity interfaces

## Related Documentation

- [useLastfmEntityApproval Hook](../react-query/hooks.md) - Hook containing validation
- [EntityBinding Component](../../components/lastfm/entity-binding.md) - Component using validation
