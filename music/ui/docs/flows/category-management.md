# Category Management Flow

## Overview

Management of category hierarchy using Directed Acyclic Graph (DAG) structure, enabling multiple parent relationships.

## Workflow Steps

### 1. View Category Hierarchy

- Navigate to Categories page
- View table of categories
- Click "View DAG" to open graph visualization
- See category relationships as graph

### 2. Graph Visualization

- Categories shown as nodes
- Parent-child relationships shown as edges
- Auto-layout using dagre algorithm
- Zoom and pan controls

### 3. Add Parent-Child Relationship

**Process:**
1. Select source category (future child)
2. Click target category (future parent)
3. System validates for cycles
4. If valid: Edge created
5. If cycle: Error shown, edge not created

**Cycle Detection:**
- Check if target is descendant of source
- Prevent: A → B → C → A (invalid cycle)
- Allow: A → B, A → C, B → D, C → D (diamond pattern)

### 4. Remove Relationship

**Process:**
1. Click edge between categories
2. Press Delete key or use context menu
3. Show confirmation dialog
4. If confirmed: Call API to remove relationship
5. Edge removed from graph

### 5. Highlight Ancestors

**Process:**
1. Right-click category node
2. Select "Show Ancestors" from menu
3. API fetches all ancestors
4. Ancestor nodes highlighted in graph

### 6. Highlight Descendants

**Process:**
1. Right-click category node
2. Select "Show Descendants" from menu
3. API fetches all descendants
4. Descendant nodes highlighted in graph

### 7. Edit Category Name

**Process:**
1. Click category name in table
2. Name becomes editable (inline)
3. User modifies name
4. Press Enter or click outside
5. Name updated via API

## Graph Operations

**Supported:**
- Add parent (with cycle prevention)
- Remove parent
- View ancestors
- View descendants
- Drag nodes to reposition

**Prevented:**
- Creating cycles
- Self-referencing (category as own parent)

## Validation Rules

- **No Cycles:** Adding edge must not create cycle
- **No Self-Reference:** Category cannot be its own parent
- **Multiple Parents Allowed:** Category can have many parents (DAG)
- **Diamond Pattern Allowed:** A → B, A → C, B → D, C → D (valid)

## Components Involved

- [CategoriesTable](../components/master/categories-table.md) - Category list with parent counts
- [CategoryDag](../components/master/category-dag.md) - Graph visualization
- [EditableText](../components/shared/editable-text.md) - Inline name editing
- [ConfirmDialog](../components/shared/confirm-dialog.md) - Delete confirmation

## API Integration

- **Queries:** [useCategory](../../src/music/data/master/hooks/useCategory.ts)
- **Mutations:** Category functions in [music-data-categories.ts](../../src/music/data/master/api/music-data-categories.ts)
- **Graph Data:** [fetchMasterEntities](../../src/music/data/master/api/music-data-common-fetching.ts) with relations

## DAG Structure

**Properties:**
- Directed: Edges have direction (parent → child)
- Acyclic: No cycles allowed
- Graph: Multiple paths allowed

**Benefits:**
- Category can belong to multiple parent categories
- Rich taxonomies (e.g., "Jazz Fusion" → "Jazz" AND "Fusion")
- Flexible organization

## Graph Algorithms

**Auto-Layout:**
- Dagre algorithm for positioning
- Hierarchical layout (top to bottom)
- Minimizes edge crossings

**Cycle Detection:**
- Depth-first search from potential parent
- Check if potential child is reachable
- If reachable: Would create cycle

**Ancestor/Descendant Query:**
- Breadth-first search from node
- Follow parent edges (ancestors) or child edges (descendants)
- Return all reachable nodes

## Related Flows

- [Binding Workflow](./binding-workflow.md) - Bind raw tags to categories
- [Approval Workflow](./approval-workflow.md) - Tags require approval before binding

## Related Patterns

- [Entity Types](../patterns/data-types/entity-types.md) - Category entity
- [Pagination](../patterns/data-fetching/table-pagination) - Category table pagination
