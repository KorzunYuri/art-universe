# Category DAG (Directed Acyclic Graph)

Categories form a hierarchical taxonomy using a DAG structure where categories can have multiple parents, enabling flexible classification without redundancy or cycles.


## Design Concept

The system models category relationships as a **Directed Acyclic Graph (DAG)** rather than a tree, allowing:
- Multiple parent categories per child (e.g., "Jazz Piano" = both "Jazz" and "Piano")
- Efficient traversal from general to specific categories
- Cycle prevention to maintain DAG integrity
- Smart cascade deletion preserving hierarchy integrity

## Key Components

### Entities

- [Category.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/Category.java) - Category entity with bidirectional navigation to parent/child relations
- [CategoryCategory.java](../../src/main/java/yurykorzun/art/universe/music/data/master/entity/CategoryCategory.java) - Hierarchical relation entity linking source (parent) to target (child) categories

### DTOs

- [CategoryDagDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDagDTO.java) - Complete DAG representation with nodes and edges for visualization
- [CategoryDagNodeDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDagNodeDTO.java) - Node with category metadata (isRoot, childrenCount, artistsCount, tracksCount)
- [CategoryDagEdgeDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryDagEdgeDTO.java) - Directed edge from source (parent) to target (child)
- [CategoryRelationDTO.java](../../src/main/java/yurykorzun/art/universe/music/data/master/dto/CategoryRelationDTO.java) - Request DTO for creating/deleting relations

### Service & Repository

- [CategoryService.java](../../src/main/java/yurykorzun/art/universe/music/data/master/service/CategoryService.java) - Service interface defining DAG operations
- [CategoryServiceImpl.java](../../src/main/java/yurykorzun/art/universe/music/data/master/service/CategoryServiceImpl.java) - Implementation with cycle detection and smart deletion logic
- [CategoryCategoryRepository.java](../../src/main/java/yurykorzun/art/universe/music/data/master/repository/CategoryCategoryRepository.java) - Repository for parent/child traversal queries

## Key Operations

- getCategoryDag() - Returns complete DAG with all categories as nodes (with metadata) and all relations as directed edges for UI visualization
- createCategoryRelation() - Creates parent->child relation with cycle prevention using depth-first search to verify no path exists from child to parent
- deleteCategoryRelation() - Removes a specific parent->child relation without affecting the categories themselves
- deleteCategory() - Deletes a category while preserving DAG integrity by connecting its children to its parents (smart cascade: Rock -> Alternative Rock -> Indie Rock becomes Rock -> Indie Rock after deleting Alternative Rock)

## Validation & Constraints

**Cycle Prevention:** Before creating any relation, the service checks if a path exists from target to source. If yes, creating source->target would create a cycle.

**Algorithm:** Depth-first search with visited set for cycle detection in `hasPath(sourceId, targetId)` and `hasPathRecursive()`.

**Diamond Relations:** Currently allowed (commented-out validation exists in code for future restriction if needed).

## Design Patterns

- **Graph Traversal**: Recursive DFS for cycle detection and path finding
- **Smart Cascade**: Deletion preserves graph connectivity by bridging removed nodes
- **Separation of Concerns**: Relation management isolated from category CRUD operations

## See Also

- [Master Data API Reference](../api.md) - Category and relation endpoints
- [Entity Relations Reference](../entity-relations.md) - Overview of entity relationships
