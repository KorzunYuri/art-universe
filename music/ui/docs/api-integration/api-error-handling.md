# Error Handling

## What It Is

Centralized error handling strategy using Axios interceptors to transform backend errors into application-specific error types, with React Query integration for UI error states.

## Why It Exists

Provides consistent error handling across all API calls, enables type-safe error handling in components, and separates error transformation from business logic.

## Error Flow

**Request → Backend → Response Interceptor → ApiError → React Query → Component**

1. API function makes request
2. Backend returns error response
3. Axios interceptor transforms to `ApiError`
4. React Query captures error
5. Component handles via error state

## ApiError Type

**Fields:**
- `message`: Human-readable error description
- `statusCode`: HTTP status code (0 for network errors)
- `errors`: Optional validation errors (field name → error messages)

## Error Transformation

### Axios Interceptors

Location: Each API config file ([musicdataconfig.ts](../../src/music/data/master/config/musicdataconfig.ts), [lastfmconfig.ts](../../src/music/data/raw/lastfm/config/lastfmconfig.ts), etc.)

**Transformation Logic:**

**Server Response Error (`error.response` exists):**
- Status 400 with validation errors → `ApiError` with `errors` field populated
- Status 404 → `ApiError` with "Resource not found"
- Status 409 → `ApiError` with conflict message
- Other status → `ApiError` with response message or generic message

**Network Error (`error.request` exists):**
- No response from server → `ApiError` with statusCode 0 and network error message

**Request Setup Error:**
- Configuration error → `ApiError` with error message and statusCode 0

## Error Handling in Components

### Query Errors

**Pattern:** Use `error` from React Query hook

**Implementation:**
- Check `error` state from `useQuery`
- Display error message to user
- Optionally show error details (status code, validation errors)

### Mutation Errors

**Pattern:** Use `onError` callback in mutation hook

**Implementation:**
- Define `onError` in `useMutation` call
- Show notification with error message
- Optionally handle specific error types (validation, conflict, etc.)

## Error Status Codes

### Common Status Codes

- **0:** Network error (no response from server)
- **400:** Validation error (check `errors` field for details)
- **404:** Resource not found
- **409:** Conflict (e.g., duplicate entity, constraint violation)
- **500:** Server error

### Status Code Handling

**In Components:**
- Display specific message based on status code
- Show validation errors for 400
- Suggest actions for specific errors (e.g., retry for network errors)

## Validation Errors

### Structure

**Field:** `errors?: Record<string, string[]>`

**Format:** Map of field name to array of error messages

**Example:**
- `{ name: ["Name is required", "Name must be at least 2 characters"] }`

### Display Pattern

**In Forms:**
- Show field-level errors next to input fields
- Highlight fields with errors
- Display general error message for non-field errors

## Notification Integration

### Notification Context

Components use notification context to display toast messages for errors.

**Pattern:**
- Import `useNotification` hook
- Call `showNotification` with error details
- Notification type: `error`
- Optional duration (default 3000ms)

## Error Handling Patterns

### Pattern 1: Display Error in Component

**Use Case:** Query errors that prevent rendering

**Implementation:**
- Check `isError` and `error` from `useQuery`
- Render error state with message
- Provide retry or navigation option

### Pattern 2: Toast Notification

**Use Case:** Mutation errors that don't block UI

**Implementation:**
- Use `onError` callback in `useMutation`
- Call `showNotification` with error message
- Continue showing UI (operation failed but app functional)

### Pattern 3: Validation Error Display

**Use Case:** Form validation errors

**Implementation:**
- Extract `errors` field from `ApiError`
- Map field errors to form fields
- Display errors next to inputs
- Highlight invalid fields

### Pattern 4: Optimistic Update Rollback

**Use Case:** Optimistic updates that fail

**Implementation:**
- Save previous state in `onMutate`
- Restore previous state in `onError`
- Show error notification
- Re-enable user interaction

## Global Error Handling

### Axios Interceptor Level

All errors transformed at interceptor level before reaching components.

**Benefit:** Consistent error structure across entire application

### React Query Level

React Query manages error state for all queries and mutations.

**Benefit:** Automatic error handling with loading/error states

## Error Logging

**Console Logging:**
- Validation errors logged to console for debugging
- Network errors logged with request details

**User-Facing Messages:**
- Generic messages for users (avoid exposing technical details)
- Specific messages for known error cases

## Related Documentation

- [React Query Hooks](../react-query/hooks.md) - Error handling in hooks
- [API Functions](./api-functions-patterns) - Functions that throw errors
