# Debounced Lookup Pattern

## What It Is

Delay execution of lookup queries while user types, preventing excessive API requests and improving performance.

## Why It Exists

Reduces server load by limiting requests, improves user experience by waiting for typing to finish, and optimizes autocomplete performance.

## Location

[src/music/shared/components/UniversalEntityLookup/EntityLookup.tsx](../../src/music/shared/components/EntityLookup/EntityLookup.tsx)

## How It Works

### Debounce Delay

**Duration:** 300ms

**Effect:**
- User types character → timer starts
- User types another character within 300ms → timer resets
- User stops typing for 300ms → lookup executes

### Implementation

**State:**
- `timeoutRef`: Stores timeout ID
- `searchString`: Current input value

**Process:**
1. User types in input field
2. `onInputChange` handler called
3. Clear existing timeout (if any)
4. Set new timeout for 300ms
5. After timeout: Call `onChange` with search string
6. `onChange` triggers lookup query

## Timeout Management

**Cleanup:**
- Clear timeout on unmount
- Clear timeout on new input
- Prevents memory leaks and stale lookups

**Pattern:**
- Store timeout ID in ref (persists across renders)
- Clear timeout before setting new one
- Cleanup in `useEffect` return function

## Integration with useEntityLookup

**Flow:**
1. Component debounces input
2. After debounce: Update search param
3. `useEntityLookup` hook triggered
4. React Query executes lookup
5. Results returned to component

## Benefits

### Performance

**Without Debounce:**
- Search "Beatles" = 7 requests (B, Be, Bea, Beat, Beatl, Beatle, Beatles)

**With Debounce:**
- Search "Beatles" = 1 request (Beatles, after user stops typing)

### User Experience

**Reduced Visual Noise:**
- Fewer loading states
- Fewer result updates
- Smoother typing experience

**Server Load:**
- Dramatically fewer requests
- Less backend processing
- Better scalability

## Alternative: Throttle

**Debounce vs Throttle:**
- **Debounce:** Wait for pause in input
- **Throttle:** Limit frequency of execution

**Why Debounce:**
- Better for autocomplete (user finishes thought)
- Fewer requests than throttle
- More predictable behavior

## Configuration

**Debounce Duration:** 300ms

**Tuning Considerations:**
- Too short: More requests, less benefit
- Too long: Feels unresponsive
- 300ms: Good balance

## Related Patterns

- [Lookup Registry](./lookup-registry.md) - Lookup execution
- [Entity Types](../data-types/entity-types.md) - Entity lookup

## Related Documentation

- [useEntityLookup Hook](../react-query/hooks.md) - Lookup hook
- [EntityLookup Component](../../components/shared/entity-lookup.md) - Component using debounce
